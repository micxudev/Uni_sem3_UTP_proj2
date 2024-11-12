import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Server server;
    private static Thread serverThread;
    private static final Logger logger = Logger.getInstance();
    private static final Object lock = new Object();

    public static void main(String[] args) {
        Thread.currentThread().setName("Main thread");
        createConsoleThread();
        startServer(true);
    }

    private static void createConsoleThread() {
        Thread consoleThread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
                logger.info(Thread.currentThread().getName() + " started running.");
                while ((input = in.readLine()) != null){
                    synchronized (lock){
                        input = input.trim().toLowerCase();

                        if (input.isEmpty()) {
                            continue;
                        }

                        logger.console(input);

                        if (input.startsWith("tell ")) {
                            validateTell(input);
                        } else if (input.equals("stop")) {
                            stopServer(true);
                        } else if (input.equals("start")) {
                            startServer(true);
                        } else if (input.equals("restart")) {
                            stopServer(false);
                            startServer(false);
                        } else if (input.equals("end")) {
                            stopServer(false);
                            break;
                        } else {
                            unknownCommand();
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading from the console.", e);
            } finally {
                logger.info(Thread.currentThread().getName() + " stopped running."); // actually not, only after next line
                logger.shutDown(); // ideally should be called by Main thread, after which Main thread should terminate
            }
        }, "Console thread");
        consoleThread.start();
    }

    private static void validateTell(String input) {
        String ipRegex= "tell\\s+((?:[0-9]{1,3}\\.){3}[0-9]{1,3})\\s+\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(ipRegex).matcher(input);

        if (matcher.matches()) {
            String ip = matcher.group(1);
            String message = matcher.group(2);
            server.sendMessageIfActive(ip, message);
        } else {
            logger.warn("Invalid tell input. Usage: tell <IP> \"message\"");
        }
    }

    private static void startServer(boolean showMessage) {
        synchronized (lock) {
            if (serverThread == null || !serverThread.isAlive()){
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

    private static void unknownCommand() {
        logger.warn("This command does not exist.");
        logger.info("""
                Available commands:
                user: 'tell <IP> "message"'
                server: 'stop, start, restart'
                terminate network: 'end'""");
    }
}