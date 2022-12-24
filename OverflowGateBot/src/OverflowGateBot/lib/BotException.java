package OverflowGateBot.lib;

public enum BotException {

    GUILD_IS_NULL("Guild is not exists"),
    MEMBER_IS_NULL("Member is not exists"),
    GUILD_DATA_IS_NULL("Guild data is not exists"),
    USER_DATA_IS_NULL("User data is not exists"),

    ;

    private final String message;

    BotException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
