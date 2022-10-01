package OverflowGateBot.main;


import org.jetbrains.annotations.NotNull;

import OverflowGateBot.user.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import static OverflowGateBot.OverflowGateBot.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class CommandHandler extends ListenerAdapter {

    public List<Guild> guilds;
    JDA jda = messagesHandler.jda;

    public CommandHandler() {

        jda.addEventListener(this);

        // Shar commands
        jda.upsertCommand(Commands.slash("shar", "Shar only").addSubcommands(//
                new SubcommandData("save", "Shar only"), //
                new SubcommandData("load", "Shar only"), //
                new SubcommandData("event", "Shar only").addOption(OptionType.STRING, "content", "Nội dung"), //
                new SubcommandData("say", "Nói gì đó").addOption(OptionType.STRING, "content", "Nội dung", true).addOption(OptionType.STRING, "guild", "Máy chủ muốn gửi", false, true).addOption(OptionType.STRING, "channel", "Kênh muốn gửi", false, true))//
        ).queue();

        // Admin commands

        // - Server status
        jda.upsertCommand(Commands.slash("reloadserver", "Tải lại tất cả máy chủ (Admin only)")).queue();
        jda.upsertCommand(Commands.slash("refreshserver", "Làm mới danh sách máy chủ (Admin only)")).queue();

        // - Bot config

        jda.upsertCommand(Commands.slash("setschematicchannel", "Đưa kênh này trở thành kênh bản thiết kế (Admin only)")).queue();
        jda.upsertCommand(Commands.slash("setmapchannel", "Đưa kênh này trở thành kênh bản đồ (Admin only)")).queue();
        jda.upsertCommand(Commands.slash("setuniversechannel", "Đưa kênh này trở thành kênh tin nhắn vũ trụ (Admin only)")).queue();
        jda.upsertCommand(Commands.slash("setserverstatus", "Đưa kênh này trở thành kênh thông tin máy chủ (Admin only)")).queue();

        jda.upsertCommand(Commands.slash("setadminrole", "Cài đặt vai trò admin cho máy chủ").addOption(OptionType.ROLE, "adminrole", "Vai trò admin", true)).queue();

        // User commands

        // - Mindustry embed

        jda.upsertCommand(Commands.slash("postmap", "Chuyển tập tin bản đồ thành hình ảnh").addOption(OptionType.ATTACHMENT, "mapfile", "Tập tin map.msv", true)).queue();
        jda.upsertCommand(Commands.slash("maplist", "In danh sách bản đồ")).queue();

        // - User system

        jda.upsertCommand(Commands.slash("info", "Thông tin của thành viên").addOption(OptionType.USER, "user", "Tên thành viên", false)).queue();
        jda.upsertCommand(Commands.slash("leaderboard", "Hiển thị bảng xếp hạng").addOption(OptionType.STRING, "orderby", "Tên bảng xếp hạng", false, true)).queue();
        jda.upsertCommand(Commands.slash("help", "Danh sách các lệnh")).queue();
        jda.upsertCommand(Commands.slash("setnickname", "Đặt biệt danh").addOption(OptionType.STRING, "nickname", "Biệt danh muốn đặt", true).addOption(OptionType.USER, "user", "Tên người muốn đổi(Admin only")).queue();
        jda.upsertCommand(Commands.slash("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh").addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true)).queue();
        jda.upsertCommand(Commands.slash("hidelv", "Ẩn level của bản thân").addOption(OptionType.BOOLEAN, "hide", "Ẩn", true)).queue();
        jda.upsertCommand(Commands.slash("ping", "Ping một máy chủ thông qua ip").addOption(OptionType.STRING, "ip", "Ip của máy chủ", true)).queue();
        jda.upsertCommand(Commands.slash("daily", "Điểm danh")).queue();

        // -

    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        System.out.println(messagesHandler.getMessageSender(event) + ": used " + event.getName() + " " + event.getSubcommandName() + " " + event.getOptions().toString());

        event.deferReply(true);
        handleCommand(event);

        event.getHook().deleteOriginal().queueAfter(messagesHandler.messageAliveTime, TimeUnit.SECONDS);

    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        String command = event.getName();
        String subcommand = event.getSubcommandName();
        String focus = event.getFocusedOption().getName();
        Member member = event.getMember();

        // Shar command group
        if (command.equals("shar")) {
            if (subcommand == null)
                return;

            if (subcommand.equals("say")) {
                if (guildConfigHandler.isAdmin(member)) {
                    if (focus.equals("guild"))
                        sendAutoComplete(event, guildConfigHandler.getGuildsName().keySet());

                    else if (focus.equals("channel")) {
                        // Get all channel form selected guild
                        OptionMapping guildIdOption = event.getOption("guild");
                        if (guildIdOption == null)
                            return;
                        HashMap<String, String> guilds = guildConfigHandler.getGuildsName();
                        String guildName = guildIdOption.getAsString();

                        if (guilds.containsKey(guildName)) {
                            String guildId = guilds.get(guildName);
                            if (guildId == null) {
                                System.out.println("Not found guild " + guildName);
                                return;
                            }
                            sendAutoComplete(event, guildConfigHandler.getChannelsName(guildId).keySet());
                        }
                    }
                }
            }


            // User command
        } else {
            if (command.equals("leaderboard")) {
                if (focus.equals("orderby")) {
                    sendAutoComplete(event, userHandler.sorter.keySet());
                }
            }
        }
    }

    // Auto complete handler
    public void sendAutoComplete(@Nonnull CommandAutoCompleteInteractionEvent event, Set<String> list) {
        if (list == null || list.isEmpty())
            return;
        List<Command.Choice> options = new ArrayList<Command.Choice>();
        String focusString = event.getFocusedOption().getValue().toLowerCase();

        for (String value : list) {
            if (value.toLowerCase().startsWith(focusString))
                options.add(new Command.Choice(value, value));
        }

        if (options.isEmpty()) {
            System.out.println("No options available");
            return;
        }
        event.replyChoices(options).queue();
    }

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String subcommand = event.getSubcommandName();
        Member member = event.getMember();

        // Shar commands
        if (command.equals("shar")) {
            if (subcommand == null)
                return;
            // - Save command
            if (subcommand.equals("save")) {
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
            else if (subcommand.equals("load")) {
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

                // - Make bot say something in current channel
            } else if (subcommand.equals("say")) {

                OptionMapping guildOption = event.getOption("guild");
                OptionMapping channelOption = event.getOption("channel");
                OptionMapping contentOption = event.getOption("content");
                if (contentOption == null)
                    return;
                String content = contentOption.getAsString();

                if (guildOption == null && channelOption == null) {
                    event.getTextChannel().sendMessage(content).queue();
                    event.deferReply(true).queue();
                    event.getHook().sendMessage("Đã gửi thành công tin nhắn: " + content).queue();

                } else if (guildOption != null && channelOption != null) {
                    List<Guild> guilds = jda.getGuildsByName(guildOption.getAsString(), false);
                    if (guilds.isEmpty())
                        return;
                    Guild guild = guilds.get(0);
                    List<TextChannel> channels = guild.getTextChannelsByName(channelOption.getAsString(), false);
                    if (channels.isEmpty())
                        return;
                    TextChannel channel = channels.get(0);
                    channel.sendMessage(content).queue();
                    event.deferReply(true).queue();
                    event.getHook().sendMessage("Đã gửi thành công tin nhắn đến: " + content).queue();
                }

                // - Send event link to all connected channels
            } else if (subcommand.equals("event")) {

            }

            // Admin commands
        } else if (command.equals("admin")) {
            if (subcommand == null)
                return;
            // - Reload server status
            if (subcommand.equals("reloadserver")) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
                    reply(event, "Đang làm mới", 10);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

                // - Add role to guild admin role
            } else if (subcommand.equals("setadminrole")) {
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
            } else if (subcommand.equals("setschematicchannel")) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    guildConfigHandler.addToChannel(event, guildConfigHandler.schematicChannel);
                    reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản thiết kế", 30);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

                // - Add channel to guild map channel
            } else if (subcommand.equals("setmapchannel")) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    guildConfigHandler.addToChannel(event, guildConfigHandler.mapChannel);
                    reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản đồ", 30);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

                // - Set channel to be guild universe chat channel
            } else if (subcommand.equals("setuniversechannel")) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    guildConfigHandler.setChannel(event, guildConfigHandler.universeChatChannel);
                    reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh tin nhắn vũ trụ", 30);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

                // - Set channel to be guild server status chat channel
            } else if (subcommand.equals("setserverstatuschannel")) {
                if (guildConfigHandler.isAdmin(event.getMember())) {
                    guildConfigHandler.setChannel(event, guildConfigHandler.serverStatusChannel);
                    reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh thông tin máy chủ", 30);
                } else
                    reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);

            }
        }


        // User commands

        // - Send all survival map and its wave record
        else if (command.equals("maplist")) {
            replyEmbeds(event, serverStatus.survivalMapLeaderboard(), 30);

            // - Display user info
        } else if (command.equals("info")) {
            if (event.getOption("user") == null) {
                replyEmbeds(event, userHandler.getUserInfo(event.getMember()), 30);
            } else {
                OptionMapping userOption = event.getOption("user");
                if (userOption == null)
                    return;
                User user = userOption.getAsUser();

                Guild guild = event.getGuild();
                if (guild == null)
                    return;

                replyEmbeds(event, userHandler.getUserInfo(guild.getMember(user)), 30);
            }

            // - Refresh server status
        } else if (command.equals("refreshserver")) {
            serverStatus.refreshServerStat(event.getGuild(), event.getMessageChannel());
            reply(event, "Đang làm mới...", 10);

            // - Display top user in all servers
        } else if (command.equals("leaderboard")) {
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
            if (position > 10)
                builder.addField("Hạng: " + position, userHandler.getUserStat(user, orderBy), false);
            replyEmbeds(event, builder, 30);

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
