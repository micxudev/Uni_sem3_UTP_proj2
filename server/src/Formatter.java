public class Formatter {

    // SERVER -> CLIENT:

    public static String getConnectionFormatted(CONNECTION_ACTION action, String username) {
        return username + " " + action + " the chat";
    }

    public static String getBannedPhrasesFormatted(Server server) {
        return "Banned phrases: " + server.getBannedPhrasesStr();
    }

    public static String getActiveUsersFormatted(Server server) {
        return "Active users: " + server.getActiveUsersStr();
    }

    public static String getShutdownFormatted() {
        return "SERVER: Server went offline";
    }

    public static String getValidationFormatted(VALIDATION_STATUS status) {
        return "Username validation status: " + status;
    }

    public static String getBannedPhraseCommandFormatted() {
        return "Your message contains banned phrases. Use !banned to see all banned phrases";
    }

    public static String getWelcomeFormatted(String username) {
        return "Welcome, " + username + "!";
    }

    public static String getHelpFormatted() {
        return "Usage:\n" +
               "!help - see this message\n" +
               "!banned - see all banned phrases\n" +
               "message - send a message to every other connected client\n" +
               "/msg <username> : message - send a message to a specific person\n" +
               "/msg <username1> <username2> <username3> : message - send a message to multiple specific people\n" +
               "/msg NOT <username1> <username2> <username3> : message - send a message to everyone, with exception to some people";
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