package connectionImplementation;

import chatImplementation.ChatComponentOpen;
import chatImplementation.ChatsController;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import static connectionImplementation.ConnectionStatus.*;

public class Connection {
    private static final ConnectionController connectionController = ConnectionPanel.getConnectionController();
    private static final ChatComponentOpen globalChatOpen = ChatsController.getChatsStorage().get("Global Chat").getValue();
    private static Socket socket;
    private static String username;
    private static BlockingQueue<String> messagesQueue;
    private static boolean isClosedManually;
    private static boolean passedUsernameValidation;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static Thread workerThread;

    private Connection() {}

    public static ConnectionStatus setNewConnection(Socket newSocket, String newUsername) {
        socket = newSocket;
        username = newUsername;
        messagesQueue = new LinkedBlockingQueue<>();
        passedUsernameValidation = false;
        isClosedManually = false;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            startReadThread();
            startWorkerThread();
        } catch (IOException _) {
            return IO_EXCEPTION;
        }
        return getConnectionStatusAfterUsernameValidation();
    }

    // threads
    private static void startReadThread() {
        new Thread(() -> {
            try {
                while (true) {
                    int size = readMessageSize();
                    String message = readMessageOfSize(size);
                    messagesQueue.offer(message);
                }
            } catch (IllegalArgumentException | IOException e) {
                if (!isClosedManually) {
                    connectionController.serverClosedConnection("disconnected with: " + e.getMessage());
                }
            } finally {
                workerThread.interrupt();
                closeConnection();
            }
        }).start();
    }

    private static void startWorkerThread() {
        workerThread = new Thread(() -> {
            while (true) {
                try {
                    if (passedUsernameValidation) {
                        String message = messagesQueue.take();
                        System.out.println(message);
                        SwingUtilities.invokeLater(() -> globalChatOpen.addChatMessageComponent(message));
                    } else {
                        // do nothing until client passes username validation
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        workerThread.start();
    }

    // username validation
    private static ConnectionStatus getConnectionStatusAfterUsernameValidation() {
         SwingWorker<ConnectionStatus, Void> worker = new SwingWorker<>() {
            @Override
            protected ConnectionStatus doInBackground() {
                try {
                    sendMessage(username);
                    String status = getLineValue(messagesQueue.take());
                    return switch (status) {
                        case "invalid" -> USERNAME_INVALID;
                        case "taken" -> USERNAME_TAKEN;
                        case "passed" -> {
                            passedUsernameValidation = true;
                            yield CONNECTED;
                        }
                        default -> PROTOCOL_VIOLATION;
                    };
                } catch (IOException _) {
                    return IO_EXCEPTION;
                } catch (InterruptedException _) {
                    return THREAD_INTERRUPTED;
                }
            }
        };
         worker.execute();
        try {
            return worker.get();
        } catch (InterruptedException | ExecutionException _) {
            return UNKNOWN_ERROR;
        }
    }

    public static ConnectionStatus tryToResendUsername(String newUsername) {
        username = newUsername;
        return getConnectionStatusAfterUsernameValidation();
    }

    // helpers
    private static String getLineValue(String line) {
        return line.substring(line.indexOf(':') + 2);
    }

    public static ConnectionStatus closeConnection() {
        isClosedManually = true;
        passedUsernameValidation = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                in = null;
                out = null;
            }
            return DISCONNECTED;
        } catch (IOException _) {
            return ERROR_CLOSING_CONNECTION;
        }
    }

    public static boolean isAlive() {
        return socket != null && !socket.isClosed();
    }

    public static boolean passedUsernameValidation() {
        return passedUsernameValidation;
    }

    // to/from server
    public static void sendMessage(String message) throws IOException {
        out.writeInt(message.length());
        out.writeBytes(message);
    }

    private static int readMessageSize() throws IOException {
        int size = in.readInt();
        if (size <= 0) {
            throw new IOException("send invalid message size: " + size);
        }
        return size;
    }

    private static String readMessageOfSize(int size) throws IOException {
        byte[] buf = new byte[size];
        in.readFully(buf);
        return new String(buf, StandardCharsets.UTF_8);
    }
}