package OverflowGateBot.BotCommands.Commands.SubCommands.UserCommands;

import OverflowGateBot.BotCommands.Class.BotSubcommandClass;
import OverflowGateBot.user.DiscordUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import static OverflowGateBot.OverflowGateBot.userHandler;
import static OverflowGateBot.OverflowGateBot.guessTheNumberHandler;

public class GuessTheNumberCommand extends BotSubcommandClass {
    public GuessTheNumberCommand() {
        super("guessnumber", "Xố số đê");
        this.addOption(OptionType.INTEGER, "number", "Số muốn đoán", true);
    }

    @Override
    public String getHelpString() {
        return "Bot sẽ đưa ra một số ngẫu nhiên từ 0 đến 1000, nếu bạn nhập đúng sẽ được MM, nhưng nếu đoán sai thì sẽ bị mất MM, tổng số MM nhận được là 90% số MM đã được tích trữ";
    }

    @Override
    public void onCommand(SlashCommandInteractionEvent event) {
        OptionMapping numberOption = event.getOption("number");
        if (numberOption == null)
            return;
        int number = numberOption.getAsInt();
        Member member = event.getMember();
        DiscordUser user = userHandler.getUser(member);

        if (user.money < 20) {
            reply(event, "Bạn cần tối thiểu 20MM để sử dụng lệnh này", number);
            return;
        }
        user.addMoney(-20);

        int reward = guessTheNumberHandler.onGuess(number, 20);
        if (reward == -1) {
            reply(event, "Đoán sai mất rồi, không phải " + number + " đâu,\nBạn đã mất 20MM\nTổng số tiền tích trữ: "
                    + guessTheNumberHandler.money, 10);
        } else {
            reply(event, "Bạn đã đoán đúng\nSố MM nhận được: " + reward, 30);
        }
    }
}
