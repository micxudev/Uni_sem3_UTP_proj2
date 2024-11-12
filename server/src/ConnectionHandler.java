import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable {
    private final Socket connectionSocket;
    private final Logger logger;
    private final Server server;

    public ConnectionHandler(Socket connectionSocket, Server server) {
        this.connectionSocket = connectionSocket;
        this.server = server;
        this.logger = Logger.getInstance();
    }

    @Override
    public void run() {
        logger.info("Run new connection: " + connectionSocket.getRemoteSocketAddress());

        try (DataInputStream in = new DataInputStream(connectionSocket.getInputStream())) {
            while (true) {
                try {
                    int msgLen = in.readInt();

                    if (msgLen <= 0) {
                        logger.warn(connectionSocket.getRemoteSocketAddress() + " sent invalid message length: " + msgLen + ". Disconnecting...");
                        break;
                    }

                    int BUF_CAP = 4096;

                    if (msgLen > BUF_CAP) {
                        byte[] buffer = new byte[BUF_CAP];

                        for (int i = 0, fullChunks = msgLen / BUF_CAP; i < fullChunks; i++) {
                            in.readFully(buffer);
                            String chunkMessage = new String(buffer, StandardCharsets.UTF_8);
                            System.out.println("[" + connectionSocket.getRemoteSocketAddress() + " CHUNK " + (i + 1) + "]:\n" + chunkMessage);
                        }

                        int leftovers = msgLen % BUF_CAP;
                        if (leftovers > 0) {
                            byte[] leftoversBuffer = new byte[leftovers];
                            in.readFully(leftoversBuffer);
                            String finalMessage = new String(leftoversBuffer, StandardCharsets.UTF_8);
                            System.out.println("[" + connectionSocket.getRemoteSocketAddress() + " FINAL CHUNK]:\n" + finalMessage);
                        }
                    } else {
                        byte[] buffer = new byte[msgLen];
                        in.readFully(buffer);
                        String message = new String(buffer, StandardCharsets.UTF_8);
                        System.out.println("[" + connectionSocket.getRemoteSocketAddress() + " SAYS]:\n" + message);
                    }
                } catch (EOFException e) {
                    logger.info(connectionSocket.getRemoteSocketAddress() + " disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            logger.warn(connectionSocket.getRemoteSocketAddress() + " disconnected with: " + e.getMessage());
        } finally {
            server.removeConnectionHandler(this);
        }
    }

    public void sendMessage(String message, boolean closeConnection) {
        try {
            PrintWriter out = new PrintWriter(connectionSocket.getOutputStream(), true);
            out.println(message);
            if (closeConnection) connectionSocket.close();
        } catch (IOException e) {
            logger.error("Error sending message to " + connectionSocket.getRemoteSocketAddress() + ". " + e.getMessage(), e);
        }
    }

    public Socket getConnectionSocket() {
        return connectionSocket;
    }
}