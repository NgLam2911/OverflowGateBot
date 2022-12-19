package OverflowGateBot.command.commands.subcommands.SharCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;

import java.util.HashMap;

import OverflowGateBot.command.BotSubcommandClass;;

public class AddCommand extends BotSubcommandClass {

    public AddCommand() {
        super("add", "Shar only");
        this.addOption(OptionType.STRING, "type", "Shar only", true, true).//
                addOption(OptionType.USER, "user", "Shar only", true).//
                addOption(OptionType.INTEGER, "point", "Shar only", true);
    }

    @Override
    public String getHelpString() {
        return "";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null)
            return;
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        OptionMapping pointOption = event.getOption("point");
        if (pointOption == null)
            return;
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        String type = typeOption.getAsString();
        User user = userOption.getAsUser();
        int point = pointOption.getAsInt();
        Member receiver = guild.getMember(user);

        if (receiver == null) {
            reply(event, "Không tìm thấy " + user.getName(), 10);
            return;
        }
        Boolean result = userHandler.add(receiver, type, point);
        if (result)
            reply(event, "Thêm thành công " + point + " " + type + " cho " + receiver.getEffectiveName(), 30);
        else
            reply(event, "Thêm không thành công " + point + " " + type + " cho " + receiver.getEffectiveName(), 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            userHandler.sorter.keySet().forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);
        }
    }
}
