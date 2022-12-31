package OverflowGateBot.main;

public enum BotException {

    GUILD_IS_NULL("Guild is not exists"),
    MEMBER_IS_NULL("Member is not exists"),
    CHANNEL_IS_NULL("Channel is not exists"),

    OPTION_IS_NULL("Command option is null"),

    GUILD_DATA_IS_NULL("Guild data is not exists"),
    USER_DATA_IS_NULL("User data is not exists"),

    TABLE_NOT_SENT("Table is not sent yet"),
    TABLE_NO_CONTENT("Table content is null");

    

    private final String message;

    BotException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
