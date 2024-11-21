public class Formatter {
    // SERVER -> CLIENT:
    //type: connection, message, banlist, active, shutdown, validation, bannedPhrase, welcome

    /**
     * TYPE_CONNECTION
     * connection - notify all active users about connection change
     * type: connection\n
     * action: added/removed\n
     * username: user_123
    */
    public static String getConnectionFormatted(CONNECTION_ACTION action, String username) {
        return "type: connection\n" +
                "action: " + action + "\n" +
                "username: " + username;
    }


    /**
     * TYPE_MESSAGE
     * message - send message from sender to specified users in format:
     * type: message\n
     * sender: sender's_username\n
     * messageLine1\n
     * messageLine2\n
     * messageLine3\n
     * message last line
    */
    public static String getMessageFormatted(String senderUsername, String message) {
        return "type: message\n" +
               "sender: " + senderUsername + "\n" +
               message;
    }


    /**
     * TYPE_BANLIST
     * banlist - send a list of banned phrases in one line separated by coma
     * type: banlist\n
     * bannedPhrases: bannedPhrasesFormatedString
    */
    public static String getBannedPhrasesFormatted(Server server) {
        return "type: banlist\n" +
                "bannedPhrases: " + server.getBannedPhrasesStr();
    }


    /**
     * TYPE_ACTIVE
     * active - send a list of active users after validation
     * type: active\n
     * users: user1 user2 user2
    */
    public static String getActiveUsersFormatted(Server server) {
        return "type: active\n" +
                "users: " + server.getActiveUsersStr();
    }


    /**
     * TYPE_SHUTDOWN
     * shutdown - send a message that server goes down
     * type: shutdown
    */
    public static String getShutdownFormatted() {
        return "type: shutdown";
    }


    /**
     * TYPE_VALIDATION
     * validation - message about username validation status
     * type: validation\n
     * status: invalid/taken/passed
    */
    public static String getValidationFormatted(VALIDATION_STATUS status) {
        return "type: validation\n" +
                "status: " + status;
    }


    /**
     * TYPE_BANNED_PHRASE
     * bannedPhrase - message that user's message contains a banned phrase
     * type: bannedPhrase\n
     * command: !banned
    */
    public static String getBannedPhraseCommandFormatted() {
        return "type: bannedPhrase\n" +
                "command: !banned";
    }

    /**
     * TYPE_WELCOME
     * welcome - welcome message with instructions
     * type: welcome\n
     * username: username\n
     * usage:\n
     * instructions line1
     * instructions line2
     * ...
     * instructions last line
     */
    public static String getWelcomeFormatted(String username) {
        return "type: welcome\n" +
                "username: " + username + "\n" +
                "usage:\n" +
                "message - send a message to every other connected client\n" +
                "/msg <username> : message - send a message to a specific person\n" +
                "/msg <username1> <username2> <username3> : message - send a message to multiple specific people\n" +
                "/msg NOT <username1> <username2> <username3> : message - send a message to everyone, with exception to some people";
    }
}

enum CONNECTION_ACTION {
    ADDED   ("added"),
    REMOVED ("removed");
    private final String action;
    CONNECTION_ACTION(String action) {
        this.action = action;
    }
    @Override
    public String toString() { return action; }
}

enum VALIDATION_STATUS {
    USERNAME_INVALID ("invalid"),
    USERNAME_TAKEN   ("taken"),
    PASSED           ("passed");
    private final String status;
    VALIDATION_STATUS(String status) {
        this.status = status;
    }
    @Override
    public String toString() { return status; }
}