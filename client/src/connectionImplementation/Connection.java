package connectionImplementation;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

import static connectionImplementation.ConnectionStatus.*;

public class Connection {
    //private static final ConnectionController connectionController = ConnectionPanel.getConnectionController();
    private static Socket socket;
    private static Thread readThread;
    private static Thread workerThread;
    private static String username;
    private static final BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();
    //private static boolean isClosedManually;

    private static BufferedReader in;
    private static PrintWriter out;

    private static boolean passedUsernameValidation;

    private Connection() {}

    public static ConnectionStatus setNewConnection(Socket newSocket, String newUsername) {
        socket = newSocket;
        username = newUsername;
        messagesQueue.clear();
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            startReadingThread();
        } catch (IOException _) {
            return IO_EXCEPTION;
        }
        return getConnectionStatusAfterUsernameValidation();
    }

    // create thread for reading from the server
    private static void startReadingThread() {
        readThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    messagesQueue.offer(line);
                }
                // server sent null, process: (notify UI)
            } catch (IOException _) {
                // socket was closed and exception raised, process: (notify UI)
            } finally {
                // do smth in both cases
            }
        });
        readThread.start();
    }

    // processes the messages from the queue and takes actions
    private static void startWorkerThread() {
        workerThread = new Thread(() -> {
            while (true) {
                try {
                    String line = messagesQueue.take();

                } catch (InterruptedException e) {
                    System.out.println("WORKER INTERRUPTED WHILE PROCESSING MESSAGES: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        });
        workerThread.start();
    }


    // type: validation
    private static ConnectionStatus getConnectionStatusAfterUsernameValidation() {
         SwingWorker<ConnectionStatus, Void> worker = new SwingWorker<>() {
            @Override
            protected ConnectionStatus doInBackground() {
                try {
                    // username validation
                    out.println(username);
                    if(out.checkError()) {
                        return IO_EXCEPTION;
                    }

                    String validationType = in.readLine();
                    if (!"type: validation".equals(validationType)) {
                        return PROTOCOL_VIOLATION;
                    }

                    String status = getLineValue(in.readLine());
                    return switch (status) {
                        case "username is invalid" -> USERNAME_INVALID;
                        case "username is taken" -> USERNAME_TAKEN;
                        case "passed successfully" -> {
                            passedUsernameValidation = true;
                            yield CONNECTED;
                        }
                        default -> PROTOCOL_VIOLATION;
                    };
                } catch (IOException _) {
                    return IO_EXCEPTION;
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

    /*connectionThread = new Thread(() -> {
            try {
                // read current active users
                String activeType = in.readLine();
                assert activeType.equals("type: active");
                String users = in.readLine();
                String[] usersArray = users.split(" ");
                if (usersArray.length > 0) {
                    // add chat for every user
                }
                System.out.println("READ USERS");

                String line;
                while ((line = in.readLine()) != null) {
                    // read and process messages
                    System.out.println("Received: " + line);
                }
                connectionController.serverClosedConnection("server closed connection");
            } catch (IOException e) {
                if (!isClosedManually) {
                    connectionController.serverClosedConnection("abruptly closed connection");
                    System.err.println("(connectionThread finished with exception) IOException: " + e.getMessage());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("CAUGHT IllegalArgumentException");
                connectionController.serverClosedConnection(e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing the socket: " + e.getMessage());
                }
                System.out.println("CONNECTION THREAD TERMINATED");
            }
        });
        connectionThread.start();
*/

    private static String getLineValue(String line) {
        return line.substring(line.indexOf(':') + 2);
    }

    public static ConnectionStatus closeConnection() {
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


    public static List<String> sendMessageToAServer(String message) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        int msgLen = bytes.length;
        int chunkLen = 4096;

        out.writeInt(msgLen);
        List<String> chunks = new ArrayList<>();

        if (msgLen > chunkLen) {
            int fullChunks = msgLen / chunkLen;
            for (int i = 0; i < fullChunks; i++) {
                String chunk = new String(bytes, i * chunkLen, chunkLen, StandardCharsets.UTF_8);
                chunks.add(chunk);
                out.write(bytes, i * chunkLen, chunkLen);
            }

            int leftovers = msgLen % chunkLen;
            if (leftovers > 0) {
                String chunk = new String(bytes, fullChunks * chunkLen, leftovers, StandardCharsets.UTF_8);
                chunks.add(chunk);
                out.write(bytes, fullChunks * chunkLen, leftovers);
            }
        } else {
            String chunk = new String(bytes, StandardCharsets.UTF_8);
            chunks.add(chunk);
            out.write(bytes);
        }
        out.flush();
        return chunks;
    }
}