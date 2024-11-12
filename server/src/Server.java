import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final ExecutorService pool;
    private final ServerSocket serverSocket;
    private final Logger logger;
    private volatile boolean isRunning;
    private final ConcurrentHashMap<ConnectionHandler, Boolean> activeConnections;

    public Server(int port, int nThreads) throws IllegalArgumentException, IOException {
        this.pool = Executors.newFixedThreadPool(nThreads);
        this.serverSocket = new ServerSocket(port);
        this.logger = Logger.getInstance();
        this.isRunning = false;
        this.activeConnections = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        logger.info(Thread.currentThread().getName() + " started running.");
        logger.info("Listening on port: " + serverSocket.getLocalPort());
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