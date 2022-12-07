package OverflowGateBot.command.commands.subcommands.UserCommands;


import OverflowGateBot.user.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;

import OverflowGateBot.command.BotSubcommandClass;

public class LeaderboardCommand extends BotSubcommandClass {
    public LeaderboardCommand() {
        super("leaderboard", "Hiện thị bản xếp hạng của người dùng");
        this.addOption(OptionType.STRING, "orderby", "Tên bảng xếp hạng", false, true);
    }

    @Override
    public String getHelpString() {
        return "Hiện thị bản xếp hạng của người dùng:\n\t<orderby>: Xếp theo:\n\t\t- MONEY: Xếp theo tiền\n\t\t- LEVEL: Xếp theo cấp\n\t\t- PVPPINT: Xếp theo điểm pvp";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        OptionMapping orderOption = event.getOption("orderby");
        String orderBy;

        // Default is sort by level
        if (orderOption == null)
            orderBy = "Level";
        else
            orderBy = orderOption.getAsString();

        EmbedBuilder builder = userHandler.getLeaderBoard(orderBy);
        DiscordUser user = userHandler.getUser(member);
        int position = userHandler.getPosition(user, orderBy);

        // Display sender position if its not contained in the leaderboard
        if (position > 10)
            builder.addField("Hạng: " + position, userHandler.getUserStat(user, orderBy), false);
        replyEmbeds(event, builder, 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("orderby")) {
            sendAutoComplete(event, userHandler.sorter.keySet());
        }
    }
}
