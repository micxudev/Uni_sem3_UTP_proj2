import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

private static Server server;
private static Thread serverThread;
private static final Logger logger = Logger.getInstance();
private static final Object lock = new Object();

public static void main() {
    Thread.currentThread().setName("Main thread");
    createConsoleThread();
    startServer(true);
}

/**
 * Creates and starts Console Thread
 */
private static void createConsoleThread() {
    Thread consoleThread = new Thread(() -> {
        logger.info(Thread.currentThread().getName() + " started running.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while ((input = in.readLine()) != null) {
                synchronized (lock) {
                    input = input.trim().toLowerCase();
                    if (input.isEmpty()) {
                        continue;
                    }
                    logger.console("Received command: " + input);
                    if (!processCommand(input)) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error reading from the console.", e);
        } catch (Exception e) {
            logger.error("Unexpected error in console.", e);
        } finally {
            logger.info(Thread.currentThread().getName() + " stopped running.");
            logger.shutDown();
        }
    }, "Console thread");
    consoleThread.start();
}

/**
 * Processes the input command and takes action.
 *
 * @param command the input command
 * @return true to continue running, false to terminate the loop
 */
private static boolean processCommand(String command) {
    switch (command) {
        case "stop":
            stopServer(true);
            break;
        case "start":
            startServer(true);
            break;
        case "restart":
            stopServer(false);
            startServer(false);
            break;
        case "end":
            stopServer(false);
            return false;
        default:
            logger.warn("Unknown command: " + command);
            logger.info("""
                    Available commands:
                    server: 'stop, start, restart'
                    terminate network: 'end'""");
    }
    return true;
}

/**
 * Starts the server if not already running.
 * Initializes a new {@link Server} instance and creates a dedicated thread to run the server.
 *
 * @param showMessage if true, logs a warning when the server is already running.
 */
private static void startServer(boolean showMessage) {
    synchronized (lock) {
        if (serverThread == null || !serverThread.isAlive()) {
            try {
                server = new Server("server/server.properties");
                serverThread = new Thread(server, "Server thread");
                serverThread.start();
            } catch (IllegalArgumentException | IOException e) {
                logger.error("Error starting server.", e);
            }
        } else {
            if (showMessage) logger.warn("Server is already running.");
        }
    }
}

/**
 * Stops the server if currently running.
 * Shuts down the {@link Server} instance and terminates its thread.
 * Waits for the thread to terminate to ensure a clean shutdown.
 *
 * @param showMessage if true, logs a warning when the server is not running.
 */
private static void stopServer(boolean showMessage) {
    synchronized (lock) {
        if (server != null) {
            server.stop();
            try {
                serverThread.join();
                serverThread = null;
                server = null;
            } catch (InterruptedException e) {
                logger.error(serverThread.getName() + " interrupted during termination.", e);
                Thread.currentThread().interrupt();
            }
        } else {
            if (showMessage) logger.warn("Server is not running.");
        }
    }
}