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
                    sendMessage(Formatter.getValidationFormatted(VALIDATION_STATUS.USERNAME_INVALID));
                    logger.info(logUsername + "tried to connect with invalid username: " + username);
                    continue;
                }
                if (server.isTakenUsername(username)) {
                    sendMessage(Formatter.getValidationFormatted(VALIDATION_STATUS.USERNAME_TAKEN));
                    logger.info(logUsername + "tried to connect with taken username: " + username);
                    continue;
                }
                sendMessage(Formatter.getValidationFormatted(VALIDATION_STATUS.PASSED));
                passedUsernameValidation = true;
                logUsername = username + " (" + socket.getRemoteSocketAddress() + ") ";
                logger.info(logUsername + "passed validation");
            }

            // send welcome message and instructions
            sendMessage(Formatter.getWelcomeFormatted(username));
            sendMessage(Formatter.getHelpFormatted());

            // send all active users
            sendMessage(Formatter.getOnlineUsersFormatted(server));

            // add as active user
            server.addUserToActive(username, this);

            // start reading messages, read messages until client disconnects
            while (true) {
                int size = readMessageSize();
                String messageRead = readMessageOfSize(size);

                // check for commands
                if (messageRead.startsWith("!")) {
                    switch (messageRead.trim()) {
                        case Formatter.COMMAND_HELP   -> sendMessage(Formatter.getHelpFormatted());
                        case Formatter.COMMAND_BANNED -> sendMessage(Formatter.getBannedPhrasesFormatted(server));
                        case Formatter.COMMAND_ONLINE -> sendMessage(Formatter.getOnlineUsersFormatted(server));
                        default ->  sendMessage(Formatter.getInvalidFormatted(Formatter.COMMAND_UNKNOWN));
                    }
                    continue;
                }

                // split read message into 2 parts
                try {
                    String[] messageParts = server.parseReceivedMessage(username, messageRead);
                    String message = messageParts[0];
                    String recipientsStr = messageParts[1];
                    ArrayList<String> recipientsList = new ArrayList<>(List.of(recipientsStr.split("\\s+")));

                    // attempt to send the message to specified users
                    server.attemptToShareMessageWith(this, message, recipientsList);
                } catch (IllegalArgumentException e) {
                    sendMessage(Formatter.getInvalidFormatted(e.getMessage()));
                }

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

    public String getLogUsername() {
        return logUsername;
    }
}