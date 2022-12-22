package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.HashMap;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;
import OverflowGateBot.main.GuildHandler.GuildCache;

import static OverflowGateBot.OverflowGateBot.guildHandler;

public class SetRoleCommand extends BotSubcommandClass {
    public SetRoleCommand() {
        super("setrole", "Cài đặt các vai trò của máy chủ");
        this.addOption(OptionType.ROLE, "role", "Vai trò muốn gán", true).//
                addOption(OptionType.INTEGER, "level", "Cấp độ cần thiết để nhận vai trò", true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các vai trò của máy chủ:\n\t<type>: loại vai trò muốn đặt\n\t<role>: vai trò muốn gán\n\t<level>: cấp độ cần thiết để có được vai trò(-1 để xóa)";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping roleOption = event.getOption("role");
        if (roleOption == null)
            return;

        String roleId = roleOption.getAsRole().getId();
        Guild guild = event.getGuild();
        if (guild == null)
            return;
        OptionMapping levelOption = event.getOption("level");
        if (levelOption == null)
            return;
        int level = levelOption.getAsInt();
        GuildCache guildData = guildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");
        if (level <= -1) {
            if (guildData.data._removeRole(roleId))
                reply(event, "Xóa vai trò thành công", 30);
            else
                reply(event, "Xóa vai trò thất bại", 30);
        } else {
            if (guildData.data._addRole(roleId, level))
                reply(event, "Thêm vai trò thành công", 30);
            else
                reply(event, "Thêm vai trò thất bại", 30);
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("type")) {
            Guild guild = event.getGuild();
            if (guild == null)
                return;

            GuildCache guildData = guildHandler.getGuild(guild);
            HashMap<String, String> options = new HashMap<String, String>();
            guildData.data.levelRoleId.keySet().forEach(t -> {
                if (t == null)
                    return;
                Role role = guild.getRoleById(t);
                if (role == null)
                    return;
                options.put(role.getName() + "     lv" + guildData.data.levelRoleId.get(t), t);
            });
            sendAutoComplete(event, options);
        }
    }

}
