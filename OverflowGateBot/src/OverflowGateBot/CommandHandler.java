package OverflowGateBot;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static OverflowGateBot.OverflowGateBot.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("postmap")) {
            messages.sendMapPreview(event);
            return;
        }

        else if (command.equals("maplist")) {
            replyEmbeds(event, serverStatus.survivalMapLeadther(), 30);
            return;
        }

        else if (command.equals("info")) {
            if (event.getOptions().size() != 0)
                replyEmbeds(event, userHandler.getInfo(event.getMember(), event.getTextChannel()), 30);
            return;
        }

        else if (command.equals("refreshserver")) {
            serverStatus.refreshServerStat(event.getGuild(), event.getMessageChannel());
            reply(event, "Đang làm mới...", 10);
            return;
        }

        else if (command.equals("save")) {
            if (messages.isAdmin(event.getMember())) {
                reply(event, "Đang lưu...", 10);
                try {
                    serverStatus.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    userHandler.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        else if (command.equals("load")) {
            if (messages.isAdmin(event.getMember())) {
                reply(event, "Đang tải...", 10);
                try {
                    serverStatus.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    userHandler.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        else if (command.equals("leaderboard")) {
            replyEmbeds(event, userHandler.getLeaderBoard(), 30);
            return;
        }

        else if (command.equals("help")) {
            return;
        }

        else if (command.equals("reloadserver")) {
            if (messages.isAdmin(event.getMember())) {
                serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
                reply(event, "Đang làm mới", 10);
                return;
            }
            reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            return;
        }

        else if (command.equals("setnickname")) {
            User user = event.getOption("user") == null ? null : event.getOption("user").getAsUser();

            if (user != null && messages.isAdmin(event.getMember())) {
                userHandler.setNickName(event.getGuild().getMember(user), event.getOption("nickname").getAsString());
                reply(event, "Đổi biệt danh thành " + event.getOption("nickname").getAsString(), 10);

            } else {
                userHandler.setNickName(event.getMember(), event.getOption("nickname").getAsString());
                reply(event, "Đổi biệt danh thành " + event.getOption("nickname").getAsString(), 10);
            }
            return;

        } else if (command.equals("postschem")) {
            messages.sendSchematicPreview(event);
            return;

        } else if (command.equals("hidelv")) {
            userHandler.hidelv(event.getMember(), event.getOption("hide").getAsBoolean());
            if (event.getOption("hide").getAsBoolean())
                reply(event, "Đã ẩn level", 10);
            else
                reply(event, "Đã tắt ẩn level", 10);
            return;

        } else if (command.equals("ping")) {
            String ip = event.getOption("ip").getAsString();

            onet.pingServer(ip, result -> {
                EmbedBuilder builder = serverStatus.serverStatusBuilder(ip, result);
                replyEmbeds(event, builder, 30);
            });
            return;

        } else if (command.equals("say")) {
            String content = event.getOption("content").getAsString();
            event.reply(content);

        } else if (command.equals("daily")) {
            int money = userHandler.getDaily(event.getMember());
            if (money > 0)
                reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money, 30);
            else
                reply(event, "Bạn đã điểm danh hôm nay", 30);

        } else
            reply(event, "Lệnh sai", 10);

    }

    void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.replyEmbeds(builder.build()).queue(_message -> _message.deleteOriginal().queueAfter(sec, TimeUnit.SECONDS));
    }

    void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.reply("```" + content + "```").queue(_message -> _message.deleteOriginal().queueAfter(sec, TimeUnit.SECONDS));
    }
}