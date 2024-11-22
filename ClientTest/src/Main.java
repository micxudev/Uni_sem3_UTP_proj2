import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    private static DataInputStream in;
    private static DataOutputStream out;

    public static void main() throws IOException {
        Socket socket = new Socket("localhost", 25575);
        System.out.println("INFO: Created socket.");

        Thread serverInputThread = new Thread(() -> {
            System.out.println("INFO: Server input thread started.");

            try {
                in = new DataInputStream(socket.getInputStream());
                while (true) {
                    int size = readMessageSize();
                    String message = readMessageOfSize(size);
                    System.out.println(message);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("IllegalArg sent by the server: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error reading from the server: " + e.getMessage());
            } finally {
                System.out.println("INFO: serverInputThread stopped running");
            }
        });
        serverInputThread.start();

        // Read messages from the console:
        Thread consoleInputThread = new Thread(() -> {
            System.out.println("INFO: Console input thread started.");

            try (BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in))) {
                out = new DataOutputStream(socket.getOutputStream());

                // Read messages from the console:
                String line;
                while (true) {
                    line = systemIn.readLine();
                    if (line != null) {
                        sendToServer(line);
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
}