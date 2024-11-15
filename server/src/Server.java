import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService connectionPool;
    private final ConcurrentHashMap<String, ConnectionHandler> activeConnections;
    private final String name;
    private final HashSet<String> bannedPhrases;
    private final String bannedPhrasesStr;
    private final Logger logger;
    private volatile boolean isRunning;
    enum CONNECTION_ACTION { ADDED, REMOVED }

    public Server(String configPath) throws IllegalArgumentException, IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configPath)) {
            props.load(in);
        }
        int port = Integer.parseInt(props.getProperty("port", "80"));
        int poolSize = Integer.parseInt(props.getProperty("connectionPoolSize", "100"));
        this.serverSocket = new ServerSocket(port);
        this.connectionPool = Executors.newFixedThreadPool(poolSize);
        this.activeConnections = new ConcurrentHashMap<>();
        this.name = props.getProperty("name", "Server");
        this.bannedPhrases = parseBannedPhrases(props.getProperty("bannedPhrases", ""));
        this.bannedPhrasesStr = String.join(",", bannedPhrases) + '\n';
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
                connectionPool.execute(new ConnectionHandler(socket, this));
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
                logger.warn("Socket was already closed.");
            }
        } catch (IOException e) {
            logger.error("Error closing server socket.", e);
        }
        logger.info("Notifying all active users about server shutdown...");
        activeConnections.values().forEach(handler -> {
            handler.sendMessage("server closed");
            handler.cleanUpResources();
        });
        logger.info("Shutting down connection pool...");
        connectionPool.shutdownNow();
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
            logger.warn("No recipients provided to share the message with");
            return;
        }
        if (message == null || message.isEmpty()) {
            logger.warn("Empty message provided by " + senderUsername);
            return;
        }
        String protocolFormattedMessage = String.format("type: message\nsender: %s\n%s\0\0", senderUsername, message);
        List<String> missingUsers = recipients.stream()
            .filter(username -> {
                ConnectionHandler user = activeConnections.get(username);
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
        activeConnections.put(username, handler);
        notifyAllActiveUsers(CONNECTION_ACTION.ADDED, username);
    }

    public void removeUserFromActive(String username) {
        activeConnections.remove(username);
        notifyAllActiveUsers(CONNECTION_ACTION.REMOVED, username);
    }

    private void notifyAllActiveUsers(CONNECTION_ACTION action, String username) {
        String actionStr = switch (action) {
            case ADDED -> "added";
            case REMOVED -> "removed";
        };
        String protocolFormattedMessage = String.format("type: connection\naction: %s\nusername: %s", actionStr, username);
        activeConnections.values().forEach(handler -> handler.sendMessage(protocolFormattedMessage));
    }

    public boolean isValidUsername(String username) {
        return username.matches("^[a-z0-9_]{5,32}$");
    }

    public boolean isTakenUsername(String username) {
        return activeConnections.containsKey(username);
    }

    public HashSet<String> getBannedPhrases() {
        return bannedPhrases;
    }

    public String getBannedPhrasesStr() {
        return bannedPhrasesStr;
    }
}