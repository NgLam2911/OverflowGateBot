package OverflowGateBot.lib.discord.command.commands.subcommands.BotCommands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.commandHandler;

import java.util.HashMap;

import OverflowGateBot.lib.discord.command.BotCommandClass;
import OverflowGateBot.lib.discord.command.BotSubcommandClass;

public class HelpCommand extends BotSubcommandClass {
    public HelpCommand() {
        super("help", "Hiển thị thông tin các lệnh");
        this.addOption(OptionType.STRING, "command", "Tên lệnh", true, true).//
                addOption(OptionType.STRING, "subcommand", "Tên lệnh", true, true);
    }

    @Override
    public String getHelpString() {
        return "Help";
    }

    // TODO Helpppppppppp
    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        OptionMapping commandOption = event.getOption("command");
        if (commandOption == null)
            return;
        OptionMapping subcommandOption = event.getOption("subcommand");
        if (subcommandOption == null)
            return;
        String command = commandOption.getAsString();
        String subcommand = subcommandOption.getAsString();
        if (commandHandler.commands.containsKey(command))
            reply(event, "/" + command + " " + subcommand + "\n"
                    + commandHandler.commands.get(command).getHelpString(subcommand), 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("command")) {
            HashMap<String, String> options = new HashMap<String, String>();
            commandHandler.commands.keySet().forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);

        } else if (focus.equals("subcommand")) {
            OptionMapping commandOption = event.getOption("command");
            if (commandOption == null)
                return;
            String command = commandOption.getAsString();
            BotCommandClass subcommands = commandHandler.commands.get(command);
            if (subcommands == null)
                return;
            HashMap<String, String> options = new HashMap<String, String>();
            subcommands.subcommands.keySet().forEach(t -> options.put(t, t));
            sendAutoComplete(event, options);

        }
    }
}
