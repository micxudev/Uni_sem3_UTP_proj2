import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    private static Thread serverInputThread;
    private static Thread consoleInputThread;

    private static DataInputStream in;
    private static DataOutputStream out;

    private static boolean run = true;

    private static final String END_OF_MSG_SIGNAL = "\0\0\n";

    private static final String allTestUsersStr = getAllTestUsersStr();



    public static void main() throws IOException {
        Socket socket = new Socket("localhost", 25575);
        System.out.println("INFO: Created socket.");

        serverInputThread = new Thread (() -> {
            System.out.println("INFO: Server input thread started.");

            try {
                in = new DataInputStream(socket.getInputStream());
                while (true) {
                    int size = readMessageSize();
                    String message = readMessageOfSize(size);
                    System.out.println("Server says:\n" + message);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("IllegalArg sent by the server: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error reading from the server: " + e.getMessage());
            } finally {
                System.out.println("INFO: serverInputThread stopped running");
                run = false;
            }
        });
        serverInputThread.start();

        consoleInputThread = new Thread (() -> {
            System.out.println("INFO: Console input thread started.");

            try (BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in))) {
                out = new DataOutputStream(socket.getOutputStream());


                {
                    // Test username validation ( allowed: [a-z0-9_] len: [5;32] )

                    // INVALID
                    String username0 = "USER1";
                    sendToServer(username0);

                    // INVALID
                    String username1 = "use";
                    sendToServer(username1);

                    // SHOULD PASS (if not taken)
                    String username2 = "user111";
                    sendToServer(username2);
                }


                {
                    // Test to get banned phrases list
                    String bannedListRequestMessage = "!banned";
                    sendToServer(bannedListRequestMessage);
                }


                {
                    // Test sending messages (NO banned phrases):
                    String message = "message line1\n" +
                                     "message line2\n" +
                                     "message line3\n" +
                                     "message last line";
                    sendToServer(formatMessage(message));
                }


                {
                    // WITH BAN PHRASE:
                    String message = "message line1\n" +
                                     "message line2\n" +
                                     "this line has some words and banned phrase1, yeah...\n" +
                                     "message last line";
                    sendToServer(formatMessage(message));
                }


                // Read messages from the console:
                String line;
                while (run) {
                    line = systemIn.readLine();
                    if (line != null) {
                        System.out.println("Read from the console: " + line);
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from console: " + e.getMessage());
            } finally {
                System.out.println("consoleInputThread stopped running.");
            }
        });
        consoleInputThread.start();
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

    private static void sendToServer(String message) throws IOException {
        out.writeInt(message.length());
        out.writeBytes(message);
    }

    private static String getAllTestUsersStr() {
        StringBuilder allUsersStr = new StringBuilder(4096);
        for (int i = 100; i <= 200; i++) {
            allUsersStr.append("user").append(i).append(" ");
        }
        return allUsersStr.toString();
    }

    private static String formatMessage(String message) {
        return message + END_OF_MSG_SIGNAL + allTestUsersStr;
    }
}