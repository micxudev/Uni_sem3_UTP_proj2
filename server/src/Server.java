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

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final ConcurrentHashMap<ConnectionHandler, Boolean> activeConnections;
    private final ConcurrentHashMap<String, ConnectionHandler> activeUsers;
    private final String name;
    private final HashSet<String> bannedPhrases;
    private final String bannedPhrasesStr;
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
        this.bannedPhrases = parseBannedPhrases(props.getProperty("bannedPhrases", ""));
        this.bannedPhrasesStr = String.join(", ", bannedPhrases);
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
            handler.sendMessage(SEND_TYPE.SHUTDOWN.toString());
            handler.cleanUpResources();
        });
    }

    public boolean isRunning() {
        return isRunning;
    }

    private HashSet<String> parseBannedPhrases(String str) {
        HashSet<String> bannedPhrases = new HashSet<>();
        if (str == null || str.isEmpty()) {
            return bannedPhrases;
        }
        Pattern p = Pattern.compile("\"([^\"]*)\"|\\b(\\w+)\\b");
        Matcher m = p.matcher(str);
        while (m.find()) {
            if (m.group(1) != null) {
                bannedPhrases.add(m.group(1));
            } else if (m.group(2) != null) {
                bannedPhrases.add(m.group(2));
            }
        }
        return bannedPhrases;
    }

    public void shareMessageWith(String senderUsername, ArrayList<String> recipients, String message) {
        if (recipients == null || recipients.isEmpty()) {
            logger.warn("No recipients provided by " + senderUsername);
            return;
        }
        if (message == null || message.isEmpty()) {
            logger.warn("Empty message provided by " + senderUsername);
            return;
        }
        String protocolFormattedMessage = SEND_TYPE.MESSAGE.toString() + MESSAGE_SENDER.SENDER + senderUsername + "\n" + message + "\n\0\0";
        List<String> missingUsers = recipients.stream()
            .filter(username -> {
                ConnectionHandler user = activeUsers.get(username);
                if (user != null) {
                    user.sendMessage(protocolFormattedMessage);
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
        String protocolFormattedMessage = SEND_TYPE.CONNECTION + action.toString() + CONNECTION_USERNAME.USERNAME + username;
        activeUsers.values().stream()
            .filter(handler -> handler != excluded)
            .forEach(handler -> handler.sendMessage(protocolFormattedMessage));
    }

    public boolean isValidUsername(String username) {
        return username.matches("^[a-z0-9_]{5,32}$");
    }

    public boolean isTakenUsername(String username) {
        return activeUsers.containsKey(username);
    }

    public HashSet<String> getBannedPhrases() {
        return bannedPhrases;
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