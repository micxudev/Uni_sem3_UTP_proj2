import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final Logger logger;
    private String logUsername;
    private String username;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean passedUsernameValidation;

    public ConnectionHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.logger = Logger.getInstance();
        this.logUsername = "(" + socket.getRemoteSocketAddress() + ") ";
    }

    @Override
    public void run() {
        // add as active server connection
        server.addConnectionToActive(this);
        logger.info("Run validation for: " + socket.getRemoteSocketAddress());
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // username validation
            while (!passedUsernameValidation) {
                int size = readMessageSize();
                username = readMessageOfSize(size);
                if (!server.isValidUsername(username)) {
                    sendMessage(SEND_TYPE.VALIDATION + VALIDATION_STATUS.USERNAME_INVALID.toString());
                    logger.info(logUsername + "tried to connect with invalid username: " + username);
                    continue;
                }
                if (server.isTakenUsername(username)) {
                    sendMessage(SEND_TYPE.VALIDATION + VALIDATION_STATUS.USERNAME_TAKEN.toString());
                    logger.info(logUsername + "tried to connect with taken username: " + username);
                    continue;
                }
                sendMessage(SEND_TYPE.VALIDATION + VALIDATION_STATUS.PASSED.toString());
                passedUsernameValidation = true;
                logUsername = username + " (" + socket.getRemoteSocketAddress() + ") ";
                logger.info(logUsername + "passed validation");
            }

            // send all active users
            sendMessage(SEND_TYPE.ACTIVE + server.getActiveUsersStr());

            // add as active user
            server.addUserToActive(username, this);

            // start reading messages, read messages until client disconnects
            while (true) {
                int size = readMessageSize();
                String messageRead = readMessageOfSize(size);

                // message type: !banned
                if (messageRead.equals("!banned")) {
                    sendMessage(SEND_TYPE.BANLIST + server.getBannedPhrasesStr());
                    continue;
                }

                // split read message into 2 parts according to the protocol
                // messageParts[0] - message itself
                // messageParts[1] - recipients (separated by space)
                String[] messageParts = messageRead.split("\0\0\n");
                if (messageParts.length != 2) {
                    throw new IllegalArgumentException("sent invalid message format");
                }

                // attempt to send the message to specified users
                String[] recipients = messageParts[1].split(" ");
                server.attemptToShareMessageWith(this, messageParts[0], new ArrayList<>(List.of(recipients)));
            }
        } catch (IllegalArgumentException e) {
            logger.info(logUsername + "disconnected with IllegalArg: " + e.getMessage());
        } catch (EOFException e) {
            logger.info(logUsername + "disconnected with EOF: " + e.getMessage());
        } catch (IOException e) {
            logger.info(logUsername + "disconnected with IO: " + e.getMessage());
        } finally {
            if (passedUsernameValidation && server.isRunning()) {
                server.removeUserFromActive(username);
            }
            if (server.isRunning()) {
                server.removeConnectionFromActive(this);
                cleanUpResources();
            }
        }
    }

    private int readMessageSize() throws IOException {
        int size = in.readInt();
        if (size <= 0) {
            throw new IOException("send invalid message size: " + size);
        }
        return size;
    }

    private String readMessageOfSize(int size) throws IOException {
        byte[] buf = new byte[size];
        in.readFully(buf);
        return new String(buf, StandardCharsets.UTF_8);
    }

    public void sendMessage(String message) {
        try {
            out.writeInt(message.length());
            out.writeBytes(message);
        } catch (IOException e) {
            logger.warn("Failed to send message to " + logUsername + ": " + e.getMessage());
        }
    }

    public void cleanUpResources() {
        try {
            socket.close();
            in.close();
            out.close();
            in = null;
            out = null;
        } catch (IOException e) {
            logger.warn("Error cleaning resources for " + logUsername + ": " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    public String getLogUsername() {
        return logUsername;
    }
}