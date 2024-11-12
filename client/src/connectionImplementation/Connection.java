package connectionImplementation;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static connectionImplementation.ConnectionStatus.*;

public class Connection {
    private static final ConnectionController connectionController = ConnectionPanel.getConnectionController();
    private static Socket socket;
    private static Thread connectionMonitor;
    private static boolean isClosedManually;

    private Connection() {}

    public static void setNewConnection(Socket newSocket) {
        socket = newSocket;
        isClosedManually = false;
        startConnectionMonitorThread();
    }

    public static boolean isConnectionAlive() {
        return socket != null && !socket.isClosed() && connectionMonitor != null && connectionMonitor.isAlive();
    }

    private static void startConnectionMonitorThread() {
        connectionMonitor = new Thread(() -> {
            // TODO: finish.
            //  Read and accumulate chunks
            //  When done send to chat or connection controller to add on the message panel.
            try (InputStream in = socket.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                StringBuilder sb = new StringBuilder();

                while((bytesRead = in.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                    System.out.println("[SERVER SAYS]:\n" + sb);
                    //addChunksOnMessagePanel(List<String> chunks)
                }
                connectionController.serverClosedConnection("server closed connection");
            } catch (IOException e) {
                if (!isClosedManually) {
                    connectionController.serverClosedConnection("abruptly closed connection");
                    System.err.println("IOException: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        }, "ConnectionMonitor thread");
        connectionMonitor.start();
    }

    public static ConnectionStatus closeConnection() {
        isClosedManually = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (connectionMonitor != null && connectionMonitor.isAlive()) {
                connectionMonitor.join();
            }
            return DISCONNECTED;
        } catch (IOException _) {
            return ERROR_CLOSING_CONNECTION;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return THREAD_INTERRUPTED;
        }
    }

    public static List<String> sendMessageToAServer(String message) throws IOException {
        // TODO: byte[] bytes = message.getBytes(StandardCharsets.UTF_8); may create a large array
        //  it might be better to process and send in chunks of 4KB
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