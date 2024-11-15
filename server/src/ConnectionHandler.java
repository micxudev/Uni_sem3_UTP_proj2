import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final Logger logger;
    private String username;
    private String logUsername;
    private PrintWriter out;

    public ConnectionHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.logger = Logger.getInstance();
    }

    @Override
    public void run() {
        logger.info("Run validation for: " + socket.getRemoteSocketAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            // validation
            while (true) {
                username = in.readLine();
                if (username == null) {
                    throw new IOException("connection closed before valid username received");
                }
                logUsername = username + " (" + socket.getRemoteSocketAddress() + ") ";
                if (!server.isValidUsername(username)) {
                    sendMessage("username is invalid");
                    logger.info(logUsername + "tried to connect with invalid username");
                    continue;
                }
                if (server.isTakenUsername(username)) {
                    sendMessage("username is taken");
                    logger.info(logUsername + "tried to connect with name taken");
                    continue;
                }
                break;
            }
            logger.info(logUsername + "passed validation");
            server.addUserToActive(username, this);
            // start reading messages
            HashSet<String> bannedPhrases = server.getBannedPhrases();
            logger.info("Started reading messages from" + logUsername);
            while (true) {
                StringBuilder message = new StringBuilder();
                boolean containedBannedPhrase = false;
                String line;
                // read lines until: 1. !banned 2. end-of-message signal or 3. banned phrase is encountered
                while ((line = in.readLine()) != null) {
                    if (line.equals("!banned")) {
                        sendMessage(server.getBannedPhrasesStr());
                        break;
                    }
                    if (line.equals("\0\0")) {
                        break; // end-of-message signal
                    }
                    for (String bannedPhrase : bannedPhrases) {
                        if (line.contains(bannedPhrase)) {
                            sendMessage("Your message contains a banned phrase. Use !banned to see banned phrases");
                            containedBannedPhrase = true;
                            break;
                        }
                    }
                    if (containedBannedPhrase) {
                        logger.info(logUsername + "sent a message with a banned phrase");
                        break;
                    }
                    message.append(line).append("\n");
                }
                if (containedBannedPhrase) {
                    continue; // start new message iteration
                }
                // collect recipients
                ArrayList<String> recipients = new ArrayList<>();
                String recipient;
                while ((recipient = in.readLine()) != null) {
                    if (recipient.equals("\0\0")) {
                        break; // end of recipients signal
                    }
                    if (!recipient.trim().isEmpty()) {
                        recipients.add(recipient);
                    }
                }
                server.shareMessageWith(username, recipients, message.toString().trim());
                logger.info(logUsername + "tried to send a message to (" + recipients.size() + ") recipients");
            }
        } catch (IOException e) {
            logger.info(logUsername + "disconnected: " + e.getMessage());
        } finally {
            server.removeUserFromActive(username);
            cleanUpResources();
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            logger.warn("Error cleaning resources for " + logUsername + ": " + e.getMessage());
        }
    }
}