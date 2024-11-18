public enum SEND_TYPE {
    TYPE ("type: "),
    NL ("\n"),
    CONNECTION    (TYPE + "connection" + NL),
    MESSAGE       (TYPE + "message" + NL),
    BANLIST       (TYPE + "banlist" + NL),
    ACTIVE        (TYPE + "active" + NL),
    SHUTDOWN      (TYPE + "shutdown"),
    VALIDATION    (TYPE + "validation" + NL),
    BANNED_PHRASE (TYPE + "bannedPhrase" + NL),;

    private final String type;

    SEND_TYPE(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}

enum CONNECTION_ACTION {
    ACTION  ("action: "),
    NL      ("\n"),
    ADDED   (ACTION + "added" + NL),
    REMOVED (ACTION + "removed" + NL),;

    private final String action;

    CONNECTION_ACTION(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return action;
    }
}

enum CONNECTION_USERNAME {
    USERNAME ("username: ");

    private final String username;

    CONNECTION_USERNAME(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}

enum MESSAGE_SENDER {
    SENDER ("sender: ");

    private final String sender;

    MESSAGE_SENDER(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return sender;
    }
}

enum VALIDATION_STATUS {
    STATUS           ("status: "),
    USERNAME_INVALID (STATUS + "username is invalid"),
    USERNAME_TAKEN   (STATUS + "username is taken"),
    PASSED           (STATUS + "passed successfully");

    private final String status;

    VALIDATION_STATUS(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}

enum BANNED_PHRASE {
    COMMAND ("command: "),
    VALUE ("!banned");

    private final String value;
    BANNED_PHRASE(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}