package OverflowGateBot.lib.discord.command.commands.subcommands.UserCommands;


import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TransferCommand extends BotSubcommandClass {

    Set<String> typeOption = new HashSet<>();

    public TransferCommand() {
        super("transfer", "Chuyển chỉ số cho người khác");
        this.addOption(OptionType.STRING, "type", "Loại chỉ số muốn chuyển", true, true).//
                addOption(OptionType.USER, "user", "Người muốn chuyển", true).//
                addOption(OptionType.INTEGER, "point", "Số điểm muốn chuyển", true);
        typeOption.add("PVPPoint");
        typeOption.add("Money");
    }

    @Override
    public String getHelpString() {
        return "Chuyển chỉ số cho người khác:\n\t<type>: Loại chỉ số muốn chuyển:\n\t\t- MONEY: chuyển tiền\n\t\t- PVPPoint: chuyển điểm pvp\n\t<user>: Tên người muốn chuyển cho\n\t<point>: số điểm muốn chuyển";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        OptionMapping typeOption = event.getOption("type");
        if (typeOption == null)
            return;
        OptionMapping userOption = event.getOption("user");
        if (userOption == null)
            return;
        OptionMapping pointOption = event.getOption("point");
        if (pointOption == null)
            return;
        String type = typeOption.getAsString();
        User user = userOption.getAsUser();
        int point = pointOption.getAsInt();
        Member receiver = guild.getMember(user);
        if (receiver == null) {
            System.out.println("No receiver found for user " + user);
            return;
        }
       

        // TODO
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            HashMap<String, String> options = new HashMap<String, String>();
            typeOption.forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);
        }
    }
}
