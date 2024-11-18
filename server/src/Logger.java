import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger {
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String RESET = "\u001B[0m";
    private static final String GRAY = "\u001B[37m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";

    private final PrintWriter out = new PrintWriter(System.out, true);
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private PrintWriter fileWriter;
    private volatile boolean isRunning;

    private Logger() {
        initLogFile();
        createLogThread();
    }

    private static class LoggerHolder {
        private static final Logger INSTANCE = new Logger();
    }

    public static Logger getInstance() {
        return LoggerHolder.INSTANCE;
    }

    private void initLogFile() {
        try {
            Path logsPath = Paths.get("server","logs");
            if (!Files.exists(logsPath)) {
                Files.createDirectories(logsPath);
            }

            String logFileName = LocalDateTime.now().format(FILE_TIME_FORMATTER) + ".log";
            Path logFilePath = logsPath.resolve(logFileName);
            fileWriter = new PrintWriter(new FileWriter(logFilePath.toFile(), true), true);

        } catch (IOException e) {
            error("Failed to initialise log file", e);
        }
    }

    private void createLogThread() {
        new Thread(() -> {
            info(Thread.currentThread().getName() + " started running.");
            isRunning = true;
            while (isRunning || !logQueue.isEmpty()) {
                try {
                    String logMessage = logQueue.take();
                    fileWriter.println(logMessage);
                } catch (InterruptedException e) {
                    error(Thread.currentThread().getName() + " interrupted during take().", e);
                }
            }
            fileWriter.close();
            info(Thread.currentThread().getName() + " stopped running. (not included in log file)");
        }, "Log thread").start();
    }

    public void console(String message) {
        log("CONSOLE", message, GREEN);
    }

    public void info(String message) {
        log("INFO", message, GRAY);
    }

    public void warn(String message) {
        log("WARN", message, YELLOW);
    }

    public synchronized void error(String message, Exception e) {
        log("ERROR", message, RED);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        out.println(RED + sw + RESET);
        logQueue.offer(sw.toString());
    }

    private synchronized void log(String level, String message, String color) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        String formattedMessage = color + "[" + time + "]" + " [" + Thread.currentThread().getName() +  "/" + level + "]: " + message + RESET;
        out.println(formattedMessage);
        logQueue.offer(formattedMessage.replaceAll("\u001B\\[[;\\d]*m",""));
    }

    public void shutDown() {
        isRunning = false;
    }
}