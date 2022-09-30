package OverflowGateBot;


import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import static OverflowGateBot.OverflowGateBot.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CommandHandler {

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();

        // Shar commands

        // - Save command
        if (command.equals("save")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                reply(event, "Đang lưu...", 10);
                try {
                    serverStatus.save();
                    userHandler.save();
                    guildConfigHandler.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
        }

        // - Load command
        else if (command.equals("load")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                reply(event, "Đang tải...", 10);
                try {
                    serverStatus.load();
                    userHandler.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Send event link to all connected channels
        } else if (command.equals("event")) {

        }

        // Admin commands

        // - Reload server status
        else if (command.equals("reloadserver")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
                reply(event, "Đang làm mới", 10);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Add role to guild admin role
        } else if (command.equals("setadminrole")) {
            OptionMapping adminRoleOption = event.getOption("adminrole");
            if (adminRoleOption == null)
                return;
            Role adminRole = adminRoleOption.getAsRole();

            Guild guild = event.getGuild();
            if (guild == null)
                return;

            if (guildConfigHandler.isAdmin(event.getMember())) {
                guildConfigHandler.adminRole.put(guild.getId(), adminRole.getId());
                reply(event, "Thêm thành công vai trò " + adminRole.getName() + " làm admin", 30);

            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Add channel to guild schematic channel
        } else if (command.equals("setschematicchannel")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                guildConfigHandler.addToChannel(event, guildConfigHandler.schematicChannel);
                reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản thiết kế", 30);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Add channel to guild map channel
        } else if (command.equals("setmapchannel")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                guildConfigHandler.addToChannel(event, guildConfigHandler.mapChannel);
                reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản đồ", 30);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Set channel to be guild universe chat channel
        } else if (command.equals("setuniversechannel")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                guildConfigHandler.setChannel(event, guildConfigHandler.universeChatChannel);
                reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh tin nhắn vũ trụ", 30);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            // - Set channel to be guild server status chat channel
        } else if (command.equals("setserverstatuschannel")) {
            if (guildConfigHandler.isAdmin(event.getMember())) {
                guildConfigHandler.setChannel(event, guildConfigHandler.serverStatusChannel);
                reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh thông tin máy chủ", 30);
            } else
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

        }

        // User commands

        // - Send all survival map and it wave record
        else if (command.equals("maplist")) {
            replyEmbeds(event, serverStatus.survivalMapLeadther(), 30);

            // - Display user info
        } else if (command.equals("info")) {
            if (event.getOption("user") == null) {
                replyEmbeds(event, userHandler.getInfo(event.getMember(), event.getTextChannel()), 30);
            } else {
                OptionMapping userOption = event.getOption("user");
                if (userOption == null)
                    return;
                User user = userOption.getAsUser();

                Guild guild = event.getGuild();
                if (guild == null)
                    return;

                replyEmbeds(event, userHandler.getInfo(guild.getMember(user), event.getTextChannel()), 30);
            }

            // - Refresh server status
        } else if (command.equals("refreshserver")) {
            serverStatus.refreshServerStat(event.getGuild(), event.getMessageChannel());
            reply(event, "Đang làm mới...", 10);

            // - Display top user in all servers
        } else if (command.equals("leaderboard")) {
            replyEmbeds(event, userHandler.getLeaderBoard(), 30);

            // - Help command
        } else if (command.equals("help")) {
            return;

            // - Set user nickname
        } else if (command.equals("setnickname")) {
            Guild guild = event.getGuild();
            if (guild == null)
                return;
            OptionMapping userOption = event.getOption("user");

            OptionMapping nicknameOption = event.getOption("nickname");
            if (nicknameOption == null)
                return;

            if (userOption != null) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    User user = userOption.getAsUser();
                    userHandler.setNickName(guild.getMember(user), nicknameOption.getAsString());
                    reply(event, "Đổi biệt danh thành " + nicknameOption.getAsString(), 10);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
            } else {
                userHandler.setNickName(event.getMember(), nicknameOption.getAsString());
                reply(event, "Đổi biệt danh thành " + nicknameOption.getAsString(), 10);
            }
            // - Post a schematic in current channel
        } else if (command.equals("postschem")) {
            messagesHandler.sendSchematicPreview(event);
            userHandler.addMoney(event.getMember(), 10);

            // - Post a map on current channel
        } else if (command.equals("postmap")) {
            messagesHandler.sendMapPreview(event);
            userHandler.addMoney(event.getMember(), 30);

            // - Hide user level
        } else if (command.equals("hidelv")) {
            Boolean hidelv;
            OptionMapping hideOption = event.getOption("hide");
            if (hideOption == null)
                hidelv = true;
            else
                hidelv = hideOption.getAsBoolean();
            userHandler.hidelv(event.getMember(), hidelv);
            if (hidelv)
                reply(event, "Đã ẩn level", 10);
            else
                reply(event, "Đã tắt ẩn level", 10);

            // - Ping a mindustry server
        } else if (command.equals("ping")) {
            OptionMapping ipOption = event.getOption("ip");
            if (ipOption == null)
                return;
            String ip = ipOption.getAsString();
            onet.pingServer(ip, result -> {
                EmbedBuilder builder = serverStatus.serverStatusBuilder(ip, result);
                replyEmbeds(event, builder, 30);
            });

            // - Make bot say something in current channel
        } else if (command.equals("say")) {
            OptionMapping contentOption = event.getOption("content");
            if (contentOption == null)
                return;
            String content = contentOption.getAsString();
            event.getTextChannel().sendMessage(content).queue();
            event.deferReply(true).queue();
            event.getHook().sendMessage("Đã gửi thành công tin nhắn: " + content).queue(_message -> _message.delete().queueAfter(30, TimeUnit.SECONDS));

            // - Get daily reward
        } else if (command.equals("daily")) {
            int money = userHandler.getDaily(event.getMember());
            if (money > 0)
                reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + "MM", 30);
            else
                reply(event, "Bạn đã điểm danh hôm nay", 30);

            // - Wrong command lol
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