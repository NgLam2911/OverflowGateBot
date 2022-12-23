package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;

import java.util.concurrent.TimeUnit;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class AvatarCommand extends BotSubcommandClass {
    public AvatarCommand() {
        super("avatar", "Hiển thị ảnh của người dùng");
        this.addOption(OptionType.USER, "user", "Tên", true);
    }

    @Override
    public String getHelpString() {
        return "Hiển thị ảnh của người dùng";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        User user = userOption.getAsUser();
        event.getHook().sendMessage("\n" + user.getAvatarUrl())
                .queue(_message -> _message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

}