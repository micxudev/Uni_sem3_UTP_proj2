import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final ConcurrentHashMap<ConnectionHandler, Boolean> activeConnections;
    private final ConcurrentHashMap<String, ConnectionHandler> activeUsers;
    private final String name;
    private final List<Pattern> bannedPatterns;
    private String bannedPhrasesStr;
    private final Logger logger;
    private volatile boolean isRunning;

    public Server(String configPath) throws IllegalArgumentException, IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configPath)) {
            props.load(in);
        }
        int port = Integer.parseInt(props.getProperty("port", "80"));
        this.serverSocket = new ServerSocket(port);
        this.activeConnections = new ConcurrentHashMap<>();
        this.activeUsers = new ConcurrentHashMap<>();
        this.name = props.getProperty("name", "Server");
        this.bannedPatterns = parseBannedPhrases(props.getProperty("bannedPhrases", ""));
        this.logger = Logger.getInstance();
    }

    @Override
    public void run() {
        logger.info(Thread.currentThread().getName() + " started running.");
        logger.info(name + " is listening on port: " + serverSocket.getLocalPort());
        isRunning = true;
        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                Thread.ofVirtual().start(new ConnectionHandler(socket, this)).setName("CH");
            } catch (IOException e) {
                if (isRunning) {
                    logger.error("Error accepting client connection.", e);
                }
            }
        }
        logger.info(Thread.currentThread().getName() + " stopped running.");
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            } else {
                logger.warn("Server Socket was already closed.");
            }
        } catch (IOException e) {
            logger.error("Error during closing server socket.", e);
        }
        logger.info("Notifying all active connections about server shutdown...");
        activeConnections.keySet().forEach(handler -> {
            handler.sendMessage(Formatter.getShutdownFormatted());
            handler.cleanUpResources();
        });
    }

    public boolean isRunning() {
        return isRunning;
    }

    private List<Pattern> parseBannedPhrases(String str) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<String> bannedPhrases = new ArrayList<>();
        Pattern p = Pattern.compile("\"([^\"]*)\"|\\b(\\w+)\\b");
        Matcher m = p.matcher(str);
        while (m.find()) {
            if (m.group(1) != null) {
                bannedPhrases.add(m.group(1));
            } else if (m.group(2) != null) {
                bannedPhrases.add(m.group(2));
            }
        }

        this.bannedPhrasesStr = String.join(", ", bannedPhrases);

        return bannedPhrases.stream()
            .map(phrase -> Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b", Pattern.CASE_INSENSITIVE))
            .toList();
    }

    public String[] parseReceivedMessage(String senderUsername, String input) throws IllegalArgumentException {
        String[] result = new String[2]; // [message, recipients]

        // default behaviour (only message):
        // "message" - send to every other connected client (except sender)
        if (!input.startsWith("/msg ")) {
            result[0] = input.trim();
            result[1] = activeUsers.keySet().stream()
                .filter(username -> !username.equals(senderUsername))
                .collect(Collectors.joining(" "));
            return result;
        }

        // message to specific users:
        input = input.substring(5).trim(); // removes '/msg ' part (and possibly spaces)

        if (input.startsWith("NOT ")) {
            // message to everyone with exception to some people:
            // "/msg NOT <username1> <username2> <username3> : message"
            input = input.substring(4).trim(); // removes 'NOT ' part (and possibly spaces)

            String[] messageAndUsers = splitInputIntoMessageAndUsers(input);
            HashSet<String> excludedUsers = new HashSet<>(List.of(messageAndUsers[1].split(" ")));

            // filter out sender and excluded recipients
            result[0] = senderUsername + " whispers to you: " + messageAndUsers[0];
            result[1] = activeUsers.keySet().stream()
                .filter(username -> !username.equals(senderUsername) && !excludedUsers.contains(username))
                .collect(Collectors.joining(" "));
        } else {
            // "/msg <username> : message"                          - send a message to a specific person
            // "/msg <username1> <username2> <username3> : message" - send a message to multiple specific people
            String[] messageAndUsers = splitInputIntoMessageAndUsers(input);
            result[0] = senderUsername + " whispers to you: " + messageAndUsers[0];
            result[1] = messageAndUsers[1];
        }
        return result;
    }

    private String[] splitInputIntoMessageAndUsers(String input) throws IllegalArgumentException {
        String[] result = new String[2];
        int colonInd = input.indexOf(':');
        if (colonInd == -1) {
            throw new IllegalArgumentException("SERVER: Invalid message format");
        }
        result[0] = input.substring(colonInd + 1).trim();
        result[1] = input.substring(0, colonInd).trim();
        return result;
    }

    public void attemptToShareMessageWith(ConnectionHandler sender, String message, ArrayList<String> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            logger.warn("No recipients provided by " + sender.getLogUsername());
            return;
        }
        if (message == null || message.isEmpty()) {
            logger.warn("Empty message provided by " + sender.getLogUsername());
            return;
        }

        // check for banned phrase
        if (containsBannedPhrase(message)) {
            sender.sendMessage(Formatter.getBannedPhraseCommandFormatted());
            logger.info(sender.getLogUsername() + "sent a message with a banned phrase");
            return;
        }

        // attempt to send the message to existing users
        String formattedMessage = Formatter.getMessageFormatted(sender.getUsername(), message);
        List<String> missingUsers = recipients.stream()
            .filter(username -> {
                ConnectionHandler user = activeUsers.get(username);
                if (user != null) {
                    user.sendMessage(formattedMessage);
                    return false;
                }
                return true;
            }).toList();
        if (!missingUsers.isEmpty()) {
            logger.warn("No connection handlers found for recipients: " + String.join(", ", missingUsers));
        }
    }

    public void addUserToActive(String username, ConnectionHandler handler) {
        notifyAllActiveUsers(CONNECTION_ACTION.ADDED, username, handler);
        activeUsers.put(username, handler);
    }

    public void removeUserFromActive(String username) {
        ConnectionHandler removed = activeUsers.remove(username);
        notifyAllActiveUsers(CONNECTION_ACTION.REMOVED, username, removed);
    }

    private void notifyAllActiveUsers(CONNECTION_ACTION action, String username, ConnectionHandler excluded) {
        String formattedMessage = Formatter.getConnectionFormatted(action, username);
        activeUsers.values().stream()
            .filter(handler -> handler != excluded)
            .forEach(handler -> handler.sendMessage(formattedMessage));
    }

    public boolean isValidUsername(String username) {
        return username.matches("^[a-z0-9_]{5,32}$");
    }

    public boolean isTakenUsername(String username) {
        return activeUsers.containsKey(username);
    }

    public boolean containsBannedPhrase(String message) {
        for (Pattern pattern : bannedPatterns) {
            if (pattern.matcher(message).find()) {
                return true; // found banned phrase
            }
        }
        return false;
    }

    public String getBannedPhrasesStr() {
        return bannedPhrasesStr;
    }

    public String getActiveUsersStr() {
        return String.join(" ", activeUsers.keySet());
    }

    public void addConnectionToActive(ConnectionHandler handler) {
        activeConnections.put(handler, true);
    }

    public void removeConnectionFromActive(ConnectionHandler handler) {
        activeConnections.remove(handler);
    }
}