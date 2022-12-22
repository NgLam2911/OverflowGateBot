package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.GuildHandler.GuildCache;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.*;

public class SetAdminCommand extends BotSubcommandClass {

    public SetAdminCommand() {
        super("setadmin", "Cài đặt vai trò admin cho máy chủ");
        this.addOption(OptionType.ROLE, "role", "Vai trò admin", true, true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt vai trò của máy chủ:\n\t<role>: vai trò admin\n\tThêm lần nữa để xóa";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            return;

        Role role = roleOption.getAsRole();
        String roleId = role.getId();

        GuildCache guildData = guildHandler.getGuild(event.getGuild());
        if (guildData == null)
            throw new IllegalStateException("No guild data found");

        if (guildData.data.adminRoleId.contains(roleId)) {
            if (guildData.data.adminRoleId.remove(roleId))
                reply(event, "Xóa vai trò thành công", 30);
            else
                reply(event, "Xóa vai trò thất bại", 30);
        } else {
            if (guildData.data.adminRoleId.add(roleId))
                reply(event, "Thêm vai trò thành công", 30);
            else
                reply(event, "Thêm vai trò thất bại", 30);
        }
    }
}
