package OverflowGateBot;

import arc.files.*;
import arc.util.*;
import arc.util.io.Streams;
import mindustry.*;
import mindustry.game.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;
import javax.imageio.*;

import org.jetbrains.annotations.NotNull;

import OverflowGateBot.GuildConfigHandler.ArchiveChannel;
import OverflowGateBot.JSONHandler.JSONData;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import mindustry.type.ItemStack;

import static OverflowGateBot.OverflowGateBot.*;

public class MessagesHandler extends ListenerAdapter {

    public final JDA jda;
    public final String prefix = ">";

    public final Integer messageAliveTime = 30;

    public List<Guild> guilds;

    public MessagesHandler() {
        try {
            File file = new File("token.json");
            if (!file.exists()) {
                file.createNewFile();
            }
            JSONHandler jsonHandler = new JSONHandler();

            JSONData reader = (jsonHandler.new JSONReader("token.json")).read();
            String token = reader.readString("token");

            // Build jda
            jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_REACTIONS).setMemberCachePolicy(MemberCachePolicy.ALL).disableCache(CacheFlag.VOICE_STATE).build();
            jda.awaitReady();

            jda.addEventListener(this);

            jda.getPresence().setActivity(Activity.of(ActivityType.PLAYING, " /help để bắt đầu"));

            // Get all guilds and register slash commands
            guilds = jda.getGuilds();

            for (Guild guild : guilds) {

                // Load all channels from file
                loadChannels(guild);

                // Shar commands

                guild.upsertCommand(Commands.slash("save", "Shar only")).queue();
                guild.upsertCommand(Commands.slash("load", "Shar only")).queue();

                // Admin commands

                // - Server status
                guild.upsertCommand(Commands.slash("reloadserver", "Tải lại tất cả máy chủ (Admin only)")).queue();
                guild.upsertCommand(Commands.slash("refreshserver", "Làm mới danh sách máy chủ (Admin only)")).queue();

                // - Bot config

                guild.upsertCommand(Commands.slash("setschematicchannel", "Đưa kênh này trở thành kênh bản thiết kế (Admin only)")).queue();
                guild.upsertCommand(Commands.slash("setmapchannel", "Đưa kênh này trở thành kênh bản đồ (Admin only)")).queue();
                guild.upsertCommand(Commands.slash("setadminrole", "Cài đặt vai trò admin cho máy chủ").addOption(OptionType.ROLE, "adminrole", "Vai trò admin", true)).queue();

                // User commands

                // - Mindustry embed

                guild.upsertCommand(Commands.slash("postmap", "Chuyển tập tin bản đồ thành hình ảnh").addOption(OptionType.ATTACHMENT, "mapfile", "Tập tin map.msv", true)).queue();
                guild.upsertCommand(Commands.slash("maplist", "In danh sách bản đồ")).queue();

                // - User system

                guild.upsertCommand(Commands.slash("info", "Thông tin của thành viên").addOption(OptionType.USER, "user", "Tên thành viên", false)).queue();
                guild.upsertCommand(Commands.slash("leaderboard", "Hiển thị bảng xếp hạng lv")).queue();
                guild.upsertCommand(Commands.slash("help", "Danh sách các lệnh")).queue();
                guild.upsertCommand(Commands.slash("setnickname", "Đặt biệt danh").addOption(OptionType.STRING, "nickname", "Biệt danh muốn đặt", true).addOption(OptionType.USER, "user", "Tên người muốn đổi(Admin only")).queue();
                guild.upsertCommand(Commands.slash("postschem", "Chuyển tập tin bản thiết kế thành hình ảnh").addOption(OptionType.ATTACHMENT, "schematicfile", "file to review", true)).queue();
                guild.upsertCommand(Commands.slash("hidelv", "Ẩn level của bản thân").addOption(OptionType.BOOLEAN, "hide", "Ẩn", true)).queue();
                guild.upsertCommand(Commands.slash("ping", "Ping một máy chủ thông qua ip").addOption(OptionType.STRING, "ip", "Ip của máy chủ", true)).queue();
                guild.upsertCommand(Commands.slash("say", "Nói gì đó").addOption(OptionType.STRING, "content", "Nội dung", true)).queue();
                guild.upsertCommand(Commands.slash("daily", "Điểm danh")).queue();

                // -
            }

            Log.info("Bot online.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadChannels(Guild guild) {
        // TODO Load all channels from file
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Message message = event.getMessage();
        if (message.getAuthor().isBot())
            return;
        // Process the message
        handleMessage(message);

    }

    public void handleMessage(Message message) {

        List<Attachment> attachments = message.getAttachments();
        // Schematic preview
        if ((isSchematicText(message) && attachments.isEmpty()) || isSchematicFile(attachments)) {
            System.out.println("[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + message.getMember().getEffectiveName() + ": sent a schematic");
            sendSchematicPreview(message, message.getChannel());
            return;
        }

        if (isMapFile(attachments)) {
            sendMapPreview(message, message.getChannel());
            return;
        }

        // Update exp on message sent
        userHandler.messageSent(message);

        System.out.println("[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + message.getMember().getEffectiveName() + ": " + message.getContentRaw());

    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        if (event.getMember().getUser().isBot())
            return;
        userHandler.setDisplayName(event.getEntity());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        System.out.println("[" + event.getGuild().getName() + "] " + " <" + event.getChannel().getName() + "> " + event.getMember().getEffectiveName() + ": used " + event.getName());

        event.deferReply(true);
        commandHandler.handleCommand(event);

        event.getHook().deleteOriginal().queueAfter(messageAliveTime, TimeUnit.SECONDS);

    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        return;
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        userHandler.addNewMember(event.getMember());
    }

    public boolean isSchematicText(Message message) {
        return message.getContentRaw().startsWith(ContentHandler.schemHeader) && message.getAttachments().isEmpty();
    }

    public boolean isSchematicFile(Attachment attachment) {
        return attachment.getFileExtension() != null && attachment.getFileExtension().equals(Vars.schematicExtension);
    }

    public boolean isSchematicFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isSchematicFile(a))
                return true;
        }
        return false;
    }

    public boolean isMapFile(Attachment attachment) {
        return attachment.getFileName().endsWith(".msav");
    }

    public boolean isMapFile(Message message) {
        for (Attachment a : message.getAttachments()) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    public boolean isMapFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    public String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public boolean inChannels(Guild guild, Channel channel, HashMap<String, List<ArchiveChannel>> guildChannelIds) {
        String guildId = guild.getId();
        if (guildChannelIds.containsKey(guildId))
            return false;

        String channelId = channel.getId();
        List<ArchiveChannel> channels = guildChannelIds.get(guildId);
        if (channels == null)
            return false;
        for (ArchiveChannel c : channels) {
            if (c.channelId == channelId)
                return true;
        }
        return false;
    }

    public void sendMapPreview(Attachment attachment, Member member, MessageChannel channel) {
        try {
            ContentHandler.Map map = contentHandler.readMap(onet.download(attachment.getUrl()));
            new File("cache/").mkdir();
            new File("cache/temp/").mkdir();
            File mapFile = new File("cache/temp/" + attachment.getFileName());
            Fi imageFile = Fi.get("cache/temp/image_" + attachment.getFileName().replace(".msav", ".png"));
            Streams.copy(onet.download(attachment.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile.file());

            EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + imageFile.name()).setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl()).setTitle(map.name == null ? attachment.getFileName().replace(".msav", "") : map.name);

            if (map.description != null)
                builder.setFooter(map.description);
            channel.sendFile(mapFile).addFile(imageFile.file()).setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMapPreview(Message message, MessageChannel channel) {
        for (int i = 0; i < message.getAttachments().size(); i++) {
            Attachment attachment = message.getAttachments().get(i);
            if (isMapFile(attachment)) {
                sendMapPreview(attachment, message.getMember(), channel);
                message.delete().queue();
            }
        }
    }

    public void sendMapPreview(SlashCommandInteractionEvent event) {
        Attachment attachment = event.getOption("mapfile").getAsAttachment();

        if (!isMapFile(attachment)) {
            event.reply("File được chọn không phải là file bản đồ");
            return;
        }

        Member member = event.getMember();
        sendMapPreview(attachment, member, event.getChannel());
        event.reply("Gửi thành công.");
    }

    public void sendSchematicPreview(SlashCommandInteractionEvent event) {
        Attachment attachment = event.getOption("schematicfile").getAsAttachment();
        if (!isSchematicFile(attachment)) {
            event.reply("File được chọn không phải là file bản thiết kế");
            return;
        }
        Member member = event.getMember();
        try {
            sendSchematicPreview(contentHandler.parseSchematicURL(attachment.getUrl()), member, event.getChannel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSchematicPreview(Message message, MessageChannel channel) {
        try {
            if (isSchematicText(message)) {
                sendSchematicPreview(contentHandler.parseSchematic(message.getContentRaw()), message.getMember(), channel);
            } else {
                for (int i = 0; i < message.getAttachments().size(); i++) {
                    Attachment attachment = message.getAttachments().get(i);
                    if (isSchematicFile(attachment)) {
                        sendSchematicPreview(contentHandler.parseSchematicURL(attachment.getUrl()), message.getMember(), channel);
                    }
                }
            }
        } catch (Exception e) {
            Log.err(e);
        }
        message.delete().queue();
    }

    public void sendSchematicPreview(Schematic schem, Member member, MessageChannel channel) {
        /*
         * if (!inChannels(member.getGuild(), channel, schematicChannel) &&
         * !(channel.getType() == ChannelType.GUILD_PUBLIC_THREAD)) {
         * sendTempMessage(channel, "Không gửi bản thiết kế vào kênh này",
         * messageAliveTime); return; }
         */
        try {
            BufferedImage preview = contentHandler.previewSchematic(schem);
            String sname = schem.name().replace("/", "_").replace(" ", "_");
            if (sname.isEmpty())
                sname = "empty";

            new File("cache").mkdir();
            File previewFile = new File("cache/temp/img_" + UUID.randomUUID() + ".png");
            File schemFile = new File("cache/temp/" + sname + "." + Vars.schematicExtension);
            Schematics.write(schem, new Fi(schemFile));
            ImageIO.write(preview, "png", previewFile);

            EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + previewFile.getName()).setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl()).setTitle(schem.name());

            if (!schem.description().isEmpty())
                builder.setFooter(schem.description());
            StringBuilder field = new StringBuilder();

            // Schem heigh, width
            field.append("Size:" + String.valueOf(schem.width) + "x" + String.valueOf(schem.height) + "\n");

            // Item requirements
            for (ItemStack stack : schem.requirements()) {
                List<Emote> emotes = member.getGuild().getEmotesByName(stack.item.name.replace("-", ""), true);
                if (!emotes.isEmpty())
                    field.append(emotes.get(0).getAsMention()).append(stack.amount).append("  ");
                else
                    field.append(stack.item.name + ": " + stack.amount + " ");
            }

            // Power input/output

            int powerProduction = (int) Math.round(schem.powerProduction()) * 60;
            int powerConsumption = (int) Math.round(schem.powerConsumption()) * 60;
            if (powerProduction != 0)
                field.append("\nNăng lượng tạo ra: " + String.valueOf(powerProduction));
            if (powerConsumption != 0)
                field.append("\nNăng lượng sử dụng: " + String.valueOf(powerConsumption));

            builder.addField("*Thông tin:*", field.toString(), true);
            // send embed
            channel.sendFile(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Message send commands
    public void sendTempMessage(Message message, String content, int sec) {
        sendTempMessage(message.getChannel(), content, sec);
    }

    public void sendTempMessage(SlashCommandInteractionEvent event, String content, int sec) {
        sendTempMessage(event.getChannel(), content, sec);
    }

    public void sendTempMessage(MessageChannel channel, String content, int sec) {
        channel.sendMessage("```" + content + "```").queue(m -> {
            m.delete().queueAfter(sec, TimeUnit.SECONDS);
        });
    }

    public void replyTempMessage(Message message, String content, int sec) {
        message.reply("```" + content + "```").queue(m -> {
            m.delete().queueAfter(sec, TimeUnit.SECONDS);
        });
    }

}