package OverflowGateBot.lib.discord.command.commands.subcommands.AdminCommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.guildHandler;

import java.util.HashMap;

import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class SetRoleCommand extends BotSubcommandClass {
    public SetRoleCommand() {
        super("setrole", "Cài đặt các vai trò của máy chủ");
        this.addOption(OptionType.STRING, "type", "Loại vai trò muốn đặt", true, true).//
                addOption(OptionType.ROLE, "role", "Vai trò muốn gán", true);
    }

    @Override
    public String getHelpString() {
        return "Cài đặt các vai trò của máy chủ:\n\t<type>: loại vai trò muốn đặt\n\t<role>: vai trò muốn gán";
    }
    /*
     * @SuppressWarnings("unchecked")
     * 
     * @Override
     * public void onCommand(SlashCommandInteractionEvent event) {
     * OptionMapping typeOption = event.getOption("type");
     * if (typeOption == null)
     * return;
     * 
     * String type = typeOption.getAsString();
     * OptionMapping roleOption = event.getOption("role");
     * if (roleOption == null)
     * return;
     * String roleId = roleOption.getAsRole().getId();
     * Guild guild = event.getGuild();
     * if (guild == null)
     * return;
     * 
     * HashMap<String, Object> guildInfo =
     * guildHandler.guildConfigs.get(guild.getId());
     * if (guildInfo == null)
     * return;
     * HashMap<String, String> role = (HashMap<String, String>) guildInfo.get(type);
     * if (role == null || !role.containsKey(type)) {
     * ((HashMap<String, String>) guildInfo.get(type)).put(type, roleId);
     * reply(event, "Đã thêm " + roleOption.getAsRole().getName() +
     * " vào danh sách " + type, 10);
     * 
     * } else {
     * ((HashMap<String, String>) guildInfo.get(type)).remove(type);
     * reply(event, "Đã xóa " + roleOption.getAsRole().getName() +
     * " khỏi danh sách " + type, 10);
     * }
     * }
     * 
     * @Override
     * public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
     * String focus = event.getFocusedOption().getName();
     * if (focus.equals("type")) {
     * HashMap<String, String> options = new HashMap<String, String>();
     * guildHandler.guildRoles.forEach(t -> options.put(t, t));
     * sendAutoComplete(event, options);
     * }
     * }
     */
}
