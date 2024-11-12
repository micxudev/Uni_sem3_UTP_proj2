import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private final ConcurrentHashMap<ConnectionHandler, Boolean> activeConnections;
    private final String name;
    private final ArrayList<String> bannedPhrases;
    private final Logger logger;
    private volatile boolean isRunning;

    public Server(String configPath) throws IllegalArgumentException, IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(configPath)) {
            props.load(in);
        }
        this.serverSocket = new ServerSocket(Integer.parseInt(props.getProperty("port")));
        this.pool = Executors.newFixedThreadPool(Integer.parseInt(props.getProperty("nThreads")));
        this.activeConnections = new ConcurrentHashMap<>();
        this.name = props.getProperty("name");
        this.bannedPhrases = parseBannedPhrases(props.getProperty("bannedPhrases", ""));
        this.logger = Logger.getInstance();
        this.isRunning = false;
    }

    private ArrayList<String> parseBannedPhrases(String str) {
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
        return bannedPhrases;
    }

    @Override
    public void run() {
        logger.info(Thread.currentThread().getName() + " started running.");
        logger.info(name + " is listening on port: " + serverSocket.getLocalPort());
        isRunning = true;
        while (isRunning) {
            try {
                Socket connectionSocket = serverSocket.accept();

                ConnectionHandler handler = new ConnectionHandler(connectionSocket, this);
                activeConnections.put(handler, true);
                pool.execute(handler);

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

        if (!activeConnections.isEmpty()) {
            logger.info("Sending " + activeConnections.size() + " shutdown notifications.");
            for (ConnectionHandler handler : activeConnections.keySet()) {
                handler.sendMessage("closed", true);
            }
        }

        logger.info("Shutting down execution pool...");
        pool.shutdownNow();
    }

    public void removeConnectionHandler(ConnectionHandler handler) {
        activeConnections.remove(handler);
    }

    public void sendMessageIfActive(String ip, String message) {
        for (ConnectionHandler handler : activeConnections.keySet()) {
            if (handler.getConnectionSocket().getInetAddress().getHostAddress().equals(ip)) {
                handler.sendMessage(message, false);
                return;
            }
        }
        logger.warn("No connection handler found for " + ip);
    }
}