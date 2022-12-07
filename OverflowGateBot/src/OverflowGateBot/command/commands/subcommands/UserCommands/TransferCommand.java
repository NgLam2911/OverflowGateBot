package OverflowGateBot.command.commands.subcommands.UserCommands;

import OverflowGateBot.command.BotSubcommandClass;
import OverflowGateBot.user.DiscordUser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;

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

    // TODO TransferCommand
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
        DiscordUser dUserSender = userHandler.getUser(event.getMember());
        DiscordUser dUserReceiver = userHandler.getUser(receiver);
        if (dUserSender == null || dUserReceiver == null) {
            System.out.println("No receiver found in database");
            return;
        }
        int result = -1;
        if (type.equals("PVPPoint"))
            result = userHandler.transferPVPPoint(dUserSender, dUserReceiver, point);
        else if (type.equals("Money"))
            result = userHandler.transferMoney(dUserSender, dUserReceiver, point);

        if (result == -1)
            reply(event, "Bạn không có đủ điểm để chuyển", 10);
        else
            reply(event, "Chuyển thành công " + result + " điểm pvp đến " + dUserReceiver.getDisplayName(), 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            sendAutoComplete(event, typeOption);
        }
    }
}
