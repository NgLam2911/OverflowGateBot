package OverflowGateBot.BotCommands.Commands.SubCommands.BotCommands;


import OverflowGateBot.BotCommands.Class.BotCommandClass;
import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.commandHandler;

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
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping commandOption = event.getOption("command");
        if (commandOption == null)
            return;
        OptionMapping subcommandOption = event.getOption("subcommand");
        if (subcommandOption == null)
            return;
        String command = commandOption.getAsString();
        String subcommand = subcommandOption.getAsString();
        if (commandHandler.commands.containsKey(command))
            reply(event, "/" + command + " " + subcommand + "\n" + commandHandler.commands.get(command).getHelpString(subcommand), 30);
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String focus = event.getFocusedOption().getName();
        if (focus.equals("command"))
            sendAutoComplete(event, commandHandler.commands.keySet());
        else if (focus.equals("subcommand")) {
            OptionMapping commandOption = event.getOption("command");
            if (commandOption == null)
                return;
            String command = commandOption.getAsString();
            BotCommandClass subcommands = commandHandler.commands.get(command);
            if (subcommands == null)
                return;
            sendAutoComplete(event, subcommands.subcommands.keySet());

        }
    }
}
