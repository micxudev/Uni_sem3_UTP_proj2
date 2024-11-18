import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final Logger logger;
    private String logUsername;
    private String username;
    private PrintWriter out;
    private boolean passedValidation;

    public ConnectionHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.logger = Logger.getInstance();
        this.logUsername = "(" + socket.getRemoteSocketAddress() + ") ";
    }

    @Override
    public void run() {
        server.addConnectionToActive(this);
        logger.info("Run validation for: " + socket.getRemoteSocketAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // validation
            while (!passedValidation) {
                username = in.readLine();
                if (username == null) {
                    throw new IOException("connection closed before valid username received");
                }
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
                passedValidation = true;
                logUsername = username + " (" + socket.getRemoteSocketAddress() + ") ";
                logger.info(logUsername + "passed validation");
            }

            // send all active users
            sendMessage(SEND_TYPE.ACTIVE + server.getActiveUsersStr());

            // add as active
            server.addUserToActive(username, this);

            // start reading messages
            logger.info("Started reading messages from: " + logUsername);
            HashSet<String> bannedPhrases = server.getBannedPhrases();

            int INIT_MSG_CAP = 512;
            StringBuilder message = new StringBuilder(INIT_MSG_CAP);
            String line;
            boolean hasBannedPhrase = false;

            // read until disconnected
            while (true) {
                line = in.readLine();

                // case: disconnected
                if (line == null) {
                    throw new IOException("sent null");
                }

                // case: !banned
                if (line.trim().equals("!banned")) {
                    sendMessage(SEND_TYPE.BANLIST + server.getBannedPhrasesStr());
                    continue;
                }

                // case: end of message signal
                if (line.equals("\0\0")) {
                    // according to the protocol, next line should be a list of recipients
                    String recipientsStr = in.readLine();
                    String[] recipientsArr = recipientsStr.split(" ");
                    ArrayList<String> recipients = new ArrayList<>(List.of(recipientsArr));
                    server.shareMessageWith(username, recipients, message.toString().trim());
                    message = new StringBuilder(INIT_MSG_CAP);
                    continue;
                }

                // check for banned phrase (linear search, slow for large bannedPhrases size)
                for (String bannedPhrase : bannedPhrases) {
                    if (line.toLowerCase().contains(bannedPhrase)) {
                        hasBannedPhrase = true;
                        break;
                    }
                }
                if (hasBannedPhrase) {
                    sendMessage(SEND_TYPE.BANNED_PHRASE.toString() + BANNED_PHRASE.COMMAND + BANNED_PHRASE.VALUE);
                    logger.info(logUsername + "sent a message with a banned phrase");
                    message = new StringBuilder(INIT_MSG_CAP);
                    hasBannedPhrase = false;
                    continue;
                }

                message.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.info(logUsername + "disconnected: " + e.getMessage());
        } finally {
            if (passedValidation && server.isRunning()) {
                server.removeUserFromActive(username);
            }
            if (server.isRunning()) {
                server.removeConnectionFromActive(this);
                cleanUpResources();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
        if (out.checkError()) {
            logger.warn("Failed to send message to " + logUsername);
        }
    }

    public void cleanUpResources() {
        try {
            socket.close();
            out = null;
        } catch (IOException e) {
            logger.warn("Error cleaning resources for " + logUsername + ": " + e.getMessage());
        }
    }
}