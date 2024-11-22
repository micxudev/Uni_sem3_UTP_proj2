public class Formatter {

    // COMMANDS
    public static final String COMMAND_HELP   = "!help";
    public static final String COMMAND_BANNED = "!banned";
    public static final String COMMAND_ONLINE = "!online";

    public static final String COMMAND_MSG    = "/msg ";
    public static final String COMMAND_EXCEPT = "NOT ";

    public static final String COMMAND_UNKNOWN = "unknown command"; // message only

    // SERVER -> CLIENT:
    public static String getValidationFormatted(VALIDATION_STATUS status) {
        return "Username validation status: " + status; // not visible for UI
    }

    public static String getConnectionFormatted(CONNECTION_ACTION action, String username) {
        return username + " " + action + " the chat";
    }

    public static String getBannedPhrasesFormatted(Server server) {
        return "Banned phrases: " + server.getBannedPhrasesStr();
    }

    public static String getOnlineUsersFormatted(Server server) {
        return server.getOnlineUsersStr();
    }

    public static String getShutdownFormatted(Server server) {
        return "SERVER: server " + server.getName() + " went offline";
    }

    public static String getBannedPhraseCommandFormatted() {
        return "Your message contains banned phrases. Use !banned to see all banned phrases";
    }

    public static String getWelcomeFormatted(String username) {
        return "Welcome, " + username + "!";
    }

    public static String getHelpFormatted() {
        return "Usage/Instructions:\n" +
               COMMAND_HELP   + " - see this message again\n" +
               COMMAND_BANNED + " - see all banned phrases\n" +
               COMMAND_ONLINE + " - see all online users\n" +
               "message - send a message to every other connected client\n" +
               COMMAND_MSG + "<username> : message - send a message to a specific person\n" +
               COMMAND_MSG + "<username1> <username2> <username3> : message - send a message to multiple specific people\n" +
               COMMAND_MSG + COMMAND_EXCEPT + "<username1> <username2> <username3> : message - send a message to everyone, with exception to some people";
    }

    public static String getInvalidFormatted(String reason) {
        return "SERVER: " + reason;
    }
}

enum CONNECTION_ACTION {
    JOINED("joined"),
    LEFT("left"),;
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