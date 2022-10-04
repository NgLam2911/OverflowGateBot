package OverflowGateBot.main;


import org.jetbrains.annotations.NotNull;

import OverflowGateBot.user.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class CommandHandler extends ListenerAdapter {

    public List<Guild> guilds;
    JDA jda = messagesHandler.jda;

    public CommandHandler() {

        jda.addEventListener(this);
        jda.upsertCommand(Commands.slash("registerguild", "Shar only")).queue();
        jda.upsertCommand(Commands.slash("unregisterguild", "Shar only")).queue();
    }

    void registerCommand(Guild guild) {
        // Shar commands
        guild.upsertCommand(Commands.slash("shar", "Shar only").addSubcommands(//
                new SubcommandData("save", "Shar only"), //
                new SubcommandData("load", "Shar only"), //
                new SubcommandData("add", "Shar only").addOption(OptionType.STRING, "type", "Shar only", true, true).addOption(OptionType.USER, "user", "Shar only", true).addOption(OptionType.INTEGER, "point", "Shar only", true), //
                new SubcommandData("event", "Shar only").addOption(OptionType.STRING, "content", "Nội dung"), //
                new SubcommandData("say", "Shar only").addOption(OptionType.STRING, "content", "Nội dung", true).addOption(OptionType.STRING, "guild", "Máy chủ muốn gửi", false, true).addOption(OptionType.STRING, "channel", "Kênh muốn gửi", false, true))//
        ).queue();

        // Admin commands

        // - Server status
        guild.upsertCommand(Commands.slash("admin", "Lệnh dành cho admin").addSubcommands(//
                new SubcommandData("reloadserver", "Tải lại tất cả máy chủ (Admin only)"), //
                new SubcommandData("refreshserver", "Làm mới danh sách máy chủ (Admin only)"), //

                // - Guild config

                new SubcommandData("setschematicchannel", "Đưa kênh này trở thành kênh bản thiết kế (Admin only)"), //
                new SubcommandData("setmapchannel", "Đưa kênh này trở thành kênh bản đồ (Admin only)"), //
                new SubcommandData("setuniversechannel", "Đưa kênh này trở thành kênh tin nhắn vũ trụ (Admin only)"), //
                new SubcommandData("setserverstatus", "Đưa kênh này trở thành kênh thông tin máy chủ (Admin only)"), //
                new SubcommandData("setadminrole", "Cài đặt vai trò admin cho máy chủ").addOption(OptionType.ROLE, "adminrole", "Vai trò admin", true), //
                new SubcommandData("setmemberrole", "Cài đặt vai trò member cho máy chủ").addOption(OptionType.ROLE, "memberrole", "Vai trò member", true))//
        ).queue();

        // User commands

        // - Mindustry embed

        guild.upsertCommand(Commands.slash("mindustry", "Lệnh mindustry").addSubcommands(//
                new SubcommandData("postmap", "Chuyển tập tin bản đồ thành hình ảnh").addOption(OptionType.ATTACHMENT, "mapfile", "Tập tin map.msv", true), //
                new SubcommandData("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh").addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true), //
                new SubcommandData("ping", "Ping một máy chủ thông qua ip").addOption(OptionType.STRING, "ip", "Ip của máy chủ", true), //
                new SubcommandData("maplist", "In danh sách bản đồ"))//
        ).queue();

        // - Bot info

        guild.upsertCommand(Commands.slash("bot", "Lệnh thuộc về bot").addSubcommands(//
                new SubcommandData("help", "Danh sách các lệnh"), //
                new SubcommandData("allguild", "Hiển thị các máy chủ mà bot đang ở"), //
                new SubcommandData("guild", "Hiển thị thông tin máy chủ").addOption(OptionType.STRING, "guild", "Tên máy chủ", true, true), //
                new SubcommandData("info", "Thông tin về bot"))//
        ).queue();

        // - User system

        guild.upsertCommand(Commands.slash("user", "Lệnh thuộc về  hệ thống quản lí người dùng").addSubcommands(//
                new SubcommandData("info", "Thông tin của thành viên").addOption(OptionType.USER, "user", "Tên thành viên", false), //
                new SubcommandData("leaderboard", "Hiển thị bảng xếp hạng").addOption(OptionType.STRING, "orderby", "Tên bảng xếp hạng", false, true), //
                new SubcommandData("setnickname", "Đặt biệt danh").addOption(OptionType.STRING, "nickname", "Biệt danh muốn đặt", true).addOption(OptionType.USER, "user", "Tên người muốn đổi(Admin only"), //
                new SubcommandData("hidelv", "Ẩn level của bản thân").addOption(OptionType.BOOLEAN, "hide", "Ẩn", true), //
                new SubcommandData("transferpvppoint", "Chuyển điểm pvp của bản thân sang người khác").addOption(OptionType.USER, "user", "Người muốn chuyển", true).addOption(OptionType.INTEGER, "point", "Số điểm muốn chuyển", true), //
                new SubcommandData("transferpoint", "Chuyển điểm (tiền) của bản thân sang người khác").addOption(OptionType.USER, "user", "Người muốn chuyển", true).addOption(OptionType.INTEGER, "point", "Số điểm muốn chuyển", true), //
                new SubcommandData("daily", "Điểm danh"))//
        ).queue();
    }

    void unregisterCommand(Guild guild) {
        guild.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                command.delete().queue();
            }
        });
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        System.out.println(messagesHandler.getMessageSender(event) + ": used " + event.getName() + " " + event.getSubcommandName() + " " + event.getOptions().toString());

        event.deferReply().queue();
        handleCommand(event);

        event.getHook().deleteOriginal().queueAfter(messagesHandler.messageAliveTime, TimeUnit.SECONDS);

    }

    @Override
    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        String command = event.getName();
        String subcommand = event.getSubcommandName();
        String focus = event.getFocusedOption().getName();
        Member member = event.getMember();

        // No subcommand -> return immediately
        if (subcommand == null)
            return;

        // Shar command group
        if (command.equals("shar")) {

            // Check if member is Shar
            if (member == null)
                return;

            // Shar ID
            if (!member.getId().equals("719322804549320725"))
                return;

            // Say something with bot
            if (subcommand.equals("say")) {
                // Show all guilds
                if (focus.equals("guild"))
                    sendAutoComplete(event, guildConfigHandler.getGuildsName().keySet());

                // Show all channels
                else if (focus.equals("channel")) {
                    // Get all channel form selected guild
                    OptionMapping guildIdOption = event.getOption("guild");
                    if (guildIdOption == null)
                        return;
                    HashMap<String, String> guilds = guildConfigHandler.getGuildsName();
                    String guildName = guildIdOption.getAsString();

                    if (!guilds.containsKey(guildName))
                        return;

                    String guildId = guilds.get(guildName);
                    if (guildId == null) {
                        System.out.println("Not found guild " + guildName);
                        return;
                    }
                    sendAutoComplete(event, guildConfigHandler.getChannelsName(guildId).keySet());

                }
            } else if (subcommand.equals("add")) {
                // Type of point/ stat to add
                if (focus.equals("type")) {
                    sendAutoComplete(event, userHandler.sorter.keySet());
                }
            }

            // bot command
        } else if (command.equals("bot")) {
            if (subcommand.equals("guild")) {
                // Show all guilds
                if (focus.equals("guild")) {
                    Set<String> guildNames = new HashSet<String>();
                    for (Guild g : jda.getGuilds())
                        guildNames.add(g.getName());
                    sendAutoComplete(event, guildNames);
                }
            }
            // User command
        } else if (command.equals("user")) {
            if (subcommand.equals("leaderboard")) {
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
            System.out.println("No options available for " + event.getFocusedOption().toString());
            return;
        }
        event.replyChoices(options).queue();
    }

    public void handleCommand(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String subcommand = event.getSubcommandName();
        Member member = event.getMember();
        Guild guild = event.getGuild();

        // Null check
        if (guild == null)
            return;

        // OH NO member is null
        if (member == null)
            return;

        // Get bot member
        Member botMember = guild.getMember(jda.getSelfUser());
        if (botMember == null)
            return;

        // If bot don't have manager server permission then return
        if (!botMember.hasPermission(Permission.ADMINISTRATOR)) {
            reply(event, "Vui lòng cho bot vai trò người quản lí để sử dụng bot", 30);
            return;
        }

        // Shar permission to use bot
        if (!guildConfigHandler.guildIds.contains(guild.getId()) && !member.getId().equals("719322804549320725")) {
            reply(event, "Máy chủ của bạn chưa được duyệt, liên hệ admin Shar để được duyệt", 30);
            return;
        }

        // Global command to register guild
        if (command.equals("registerguild")) {

            if (!member.getId().equals("719322804549320725")) {
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
                return;
            }

            // Add guild to registered guilds list
            boolean result = guildConfigHandler.addGuild(guild.getId());
            registerCommand(event.getGuild());
            if (result) {
                userHandler.loadGuild(guild.getId());
                guildConfigHandler.save();
                reply(event, "Đã duyệt máy chủ", 30);
            } else
                reply(event, "Máy chủ đã được duyệt trước đó", 30);
        }

        if (command.equals("unregisterguild")) {

            if (!member.getId().equals("719322804549320725")) {
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
                return;
            }

            boolean result = guildConfigHandler.guildIds.remove(guild.getId());
            if (result) {
                unregisterCommand(event.getGuild());
                guildConfigHandler.save();
                reply(event, "Đã gỡ duyệt máy chủ", 30);
            }
            reply(event, "Máy chủ chưa được duyệt trước đó", 30);
        }

        // Shar commands
        else if (command.equals("shar")) {

            if (!member.getId().equals("719322804549320725")) {
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
                return;
            }

            // Null check
            if (subcommand == null)
                return;

            // - Save command
            if (subcommand.equals("save")) {
                reply(event, "Đang lưu...", 10);
                try {
                    serverStatus.save();
                    userHandler.save();
                    guildConfigHandler.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // - Load command
            else if (subcommand.equals("load")) {
                reply(event, "Đang tải...", 10);
                try {
                    serverStatus.load();
                    userHandler.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                    Guild firstGuild = guilds.get(0);
                    List<TextChannel> channels = firstGuild.getTextChannelsByName(channelOption.getAsString(), false);
                    if (channels.isEmpty())
                        return;
                    TextChannel channel = channels.get(0);
                    channel.sendMessage(content).queue();
                    event.deferReply(true).queue();
                    event.getHook().sendMessage("Đã gửi thành công tin nhắn đến: " + content).queue();
                }

                // - Send event link to all connected channels
            } else if (subcommand.equals("event")) {

                // Add stat to a user
            } else if (subcommand.equals("add")) {
                OptionMapping typeOption = event.getOption("type");
                if (typeOption == null)
                    return;
                OptionMapping userOption = event.getOption("user");
                if (userOption == null)
                    return;
                OptionMapping pointOption = event.getOption("point");
                if (pointOption == null)
                    return;
                String type = typeOption.getAsString();
                User user = userOption.getAsUser();
                int point = pointOption.getAsInt();
                Member receiver = guild.getMember(user);
                if (receiver == null) {
                    reply(event, "Không tìm thấy " + user.getName(), 10);
                    return;
                }
                Boolean result = userHandler.add(receiver, type, point);
                if (result)
                    reply(event, "Thêm thành công " + point + " " + type + " cho " + receiver.getEffectiveName(), 30);
                else
                    reply(event, "Thêm không thành công " + point + " " + type + " cho " + receiver.getEffectiveName(), 30);
            }

            // Admin related commands
        } else if (command.equals("admin")) {
            // Null check again
            if (subcommand == null)
                return;

            if (!guildConfigHandler.isAdmin(event.getMember())) {
                reply(event, "Bạn không có quyền để sử dụng lệnh này", 10);
                return;
            }

            // - Reload server status
            if (subcommand.equals("reloadserver")) {
                serverStatus.reloadServer(event.getGuild(), event.getMessageChannel());
                reply(event, "Đang làm mới", 10);

                // - Add role to guild admin role
            } else if (subcommand.equals("setadminrole")) {
                OptionMapping adminRoleOption = event.getOption("adminrole");
                if (adminRoleOption == null)
                    return;
                Role adminRole = adminRoleOption.getAsRole();
                guildConfigHandler.adminRole.put(guild.getId(), adminRole.getId());
                reply(event, "Thêm thành công vai trò " + adminRole.getName() + " làm admin", 30);

                // - Add role to guild member role
            } else if (subcommand.equals("setmemberrole")) {
                OptionMapping memberRoleOption = event.getOption("memberrole");
                if (memberRoleOption == null)
                    return;
                Role memberRole = memberRoleOption.getAsRole();
                guildConfigHandler.memberRole.put(guild.getId(), memberRole.getId());
                reply(event, "Thêm thành công vai trò " + memberRole.getName() + " làm member", 30);

                // - Add channel to guild schematic channel
            } else if (subcommand.equals("setschematicchannel")) {
                guildConfigHandler.setChannel(event, guildConfigHandler.schematicChannel);
                reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản thiết kế", 30);


                // - Add channel to guild map channel
            } else if (subcommand.equals("setmapchannel")) {

                guildConfigHandler.setChannel(event, guildConfigHandler.mapChannel);
                reply(event, "Thêm thành công kênh " + event.getChannel().getName() + " vào kênh bản đồ", 30);

                // - Set channel to be guild universe chat channel
            } else if (subcommand.equals("setuniversechannel")) {
                guildConfigHandler.setChannel(event, guildConfigHandler.universeChatChannel);
                reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh tin nhắn vũ trụ", 30);


                // - Set channel to be guild server status chat channel
            } else if (subcommand.equals("setserverstatuschannel")) {
                guildConfigHandler.setChannel(event, guildConfigHandler.serverStatusChannel);
                reply(event, "Đặt thành công kênh " + event.getChannel().getName() + " thành kênh thông tin máy chủ", 30);

            }

        }

        // Bot related commands
        else if (command.equals("bot")) {
            // Null check n times
            if (subcommand == null)
                return;
            // - Help command
            if (subcommand.equals("help")) {
                EmbedBuilder builder = new EmbedBuilder();
                guild.retrieveCommands().queue(commands -> {
                    for (Command c : commands) {
                        builder.addField(c.getName(), c.getDescription(), false);
                    }
                });
                replyEmbeds(event, builder, 30);

                // - All server command
            } else if (subcommand.equals("allguild")) {
                List<Guild> guilds = jda.getGuilds();
                EmbedBuilder builder = new EmbedBuilder();
                StringBuilder field = new StringBuilder();
                for (Guild g : guilds) {
                    String registered = guildConfigHandler.guildIds.contains(g.getId()) ? "Đã được duyệt" : "Chưa được duyệt";
                    field.append("_" + g.getName() + "_: " + registered + "\n");
                }
                builder.addField("_Máy chủ_", field.toString(), false);
                replyEmbeds(event, builder, 30);

                // Server command
            } else if (subcommand.equals("guild")) {
                OptionMapping guildOption = event.getOption("guild");
                if (guildOption == null) {
                    reply(event, "Tên máy chủ không tồn tại", 10);
                    return;
                }
                // Get the guild base on name
                String guildName = guildOption.getAsString();
                List<Guild> guilds = jda.getGuildsByName(guildOption.getAsString(), false);
                if (guilds.isEmpty())
                    return;
                Guild firstGuild = guilds.get(0);
                EmbedBuilder builder = new EmbedBuilder();
                StringBuilder field = new StringBuilder();
                builder.setAuthor(guildName, null, firstGuild.getIconUrl());
                Member owner = firstGuild.getOwner();
                if (owner != null)
                    field.append("Chủ máy chủ: " + owner.getEffectiveName() + "\n");
                field.append("Số thành viên: " + firstGuild.getMemberCount() + "\n" + //
                        "Link mời: " + firstGuild.getTextChannels().get(0).createInvite().complete().getUrl());
                builder.addField("Thông tin cơ bản:", field.toString(), false);
                replyEmbeds(event, builder, 30);
            }

            // Mindustry related commands
        } else if (command.equals("mindustry")) {
            // LOL I hate null check
            if (subcommand == null)
                return;

            // - Send all survival map and its wave record
            if (subcommand.equals("maplist"))
                replyEmbeds(event, serverStatus.survivalMapLeaderboard(), 30);

            // - Refresh server status
            else if (subcommand.equals("refreshserver")) {
                serverStatus.refreshServerStat(event.getGuild(), event.getMessageChannel());
                reply(event, "Đang làm mới...", 10);

                // - Post a schematic in current channel
            } else if (subcommand.equals("postschem")) {
                messagesHandler.sendSchematicPreview(event);
                userHandler.addMoney(event.getMember(), 10);

                // - Post a map on current channel
            } else if (subcommand.equals("postmap")) {
                messagesHandler.sendMapPreview(event);
                userHandler.addMoney(event.getMember(), 30);
                // - Ping a mindustry server
            } else if (subcommand.equals("ping")) {
                OptionMapping ipOption = event.getOption("ip");
                if (ipOption == null)
                    return;
                String ip = ipOption.getAsString();
                onet.pingServer(ip, result -> {
                    EmbedBuilder builder = serverStatus.serverStatusBuilder(ip, result);
                    replyEmbeds(event, builder, 30);
                });
            }

            // User commands
        } else if (command.equals("user")) {
            // Hate it
            if (subcommand == null)
                return;
            // - Display user info
            if (subcommand.equals("info")) {
                if (event.getOption("user") == null) {
                    replyEmbeds(event, userHandler.getUserInfo(event.getMember()), 30);
                } else {
                    OptionMapping userOption = event.getOption("user");
                    if (userOption == null)
                        return;
                    User user = userOption.getAsUser();

                    replyEmbeds(event, userHandler.getUserInfo(guild.getMember(user)), 30);
                }

                // - Display top user in all servers
            } else if (subcommand.equals("leaderboard")) {
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

                // - Set user nickname
            } else if (subcommand.equals("setnickname")) {
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

                // - Hide user level
            } else if (subcommand.equals("hidelv")) {
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

                // - Get daily reward
            } else if (subcommand.equals("daily")) {
                int money = userHandler.getDaily(event.getMember());
                if (money > 0)
                    reply(event, "Điểm dành thanh công\nĐiểm nhận được: " + money + "MM", 30);
                else
                    reply(event, "Bạn đã điểm danh hôm nay", 10);

                // Transfer money
            } else if (subcommand.equals("transferpoint")) {
                OptionMapping userOption = event.getOption("user");
                if (userOption == null)
                    return;
                OptionMapping pointOption = event.getOption("point");
                if (pointOption == null)
                    return;
                User user = userOption.getAsUser();
                int point = pointOption.getAsInt();
                Member receiver = guild.getMember(user);
                if (receiver == null) {
                    System.out.println("No receiver found for user " + user);
                    return;
                }
                DiscordUser dUserSender = userHandler.getUser(member);
                DiscordUser dUserReceiver = userHandler.getUser(receiver);
                if (dUserSender == null || dUserReceiver == null) {
                    System.out.println("No receiver found in database");
                    return;
                }
                int result = userHandler.transferMoney(dUserSender, dUserReceiver, point);
                if (result == -1)
                    reply(event, "Bạn không có đủ điểm để chuyển", 10);
                else
                    reply(event, "Chuyển thành công " + result + " điểm đến " + dUserReceiver.getDisplayName(), 30);

                // Transfer pvp point
            } else if (subcommand.equals("transferpvppoint")) {
                OptionMapping userOption = event.getOption("user");
                if (userOption == null)
                    return;
                OptionMapping pointOption = event.getOption("point");
                if (pointOption == null)
                    return;
                User user = userOption.getAsUser();
                int point = pointOption.getAsInt();
                Member receiver = guild.getMember(user);
                if (receiver == null) {
                    System.out.println("No receiver found for user " + user);
                    return;
                }
                DiscordUser dUserSender = userHandler.getUser(member);
                DiscordUser dUserReceiver = userHandler.getUser(receiver);
                if (dUserSender == null || dUserReceiver == null) {
                    System.out.println("No receiver found in database");
                    return;
                }
                int result = userHandler.transferPVPPoint(dUserSender, dUserReceiver, point);
                if (result == -1)
                    reply(event, "Bạn không có đủ điểm để chuyển", 10);
                else
                    reply(event, "Chuyển thành công " + result + " điểm pvp đến " + dUserReceiver.getDisplayName(), 30);
            }
        } else
            // - Wrong command lol
            reply(event, "Lệnh sai", 10);

    }

    void replyEmbeds(SlashCommandInteractionEvent event, EmbedBuilder builder, int sec) {
        event.getHook().sendMessageEmbeds(builder.build()).queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }

    void reply(SlashCommandInteractionEvent event, String content, int sec) {
        event.getHook().sendMessage("```" + content + "```").queue(_message -> _message.delete().queueAfter(sec, TimeUnit.SECONDS));
    }
}
