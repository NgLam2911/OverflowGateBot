package OverflowGateBot.main.handler;

import arc.files.*;
import arc.util.*;
import arc.util.io.Streams;

import mindustry.*;
import mindustry.game.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;
import javax.imageio.*;

import org.bson.Document;

import OverflowGateBot.main.handler.DatabaseHandler.LOG_TYPE;
import OverflowGateBot.main.mindustry.ContentHandler;
import OverflowGateBot.main.user.GuildData;
import OverflowGateBot.main.user.UserData;
import OverflowGateBot.main.user.GuildData.CHANNEL_TYPE;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import mindustry.type.ItemStack;

import static OverflowGateBot.OverflowGateBot.*;

public final class MessageHandler extends ListenerAdapter {

    private static MessageHandler instance = new MessageHandler();

    public final Integer messageAliveTime = 30;

    public HashMap<String, TextChannel> serverChatChannel = new HashMap<String, TextChannel>();

    private MessageHandler() {

        jda.addEventListener(this);
        Log.info("Message handler up");
    }

    @Override
    protected void finalize() {
        Log.info("Message handler down");
    }

    public static MessageHandler getInstance() {
        return instance;
    }

    public static String getMessageSender(Message message) {
        Member member = message.getMember();
        if (member == null)
            return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + "Unknown";
        return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> "
                + member.getEffectiveName();
    }

    public static String getMessageSender(@Nonnull SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        String guildName;
        if (guild == null)
            guildName = "Unknown";
        else
            guildName = guild.getName();
        if (member == null)
            return "[" + guildName + "] " + " <" + event.getChannel().getName() + "> " + "Unknown";
        return "[" + guildName + "] " + " <" + event.getChannel().getName() + "> " + member.getEffectiveName();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getAuthor().isBot())
            return;
        // Process the message
        handleMessage(message);

    }

    public static void handleMessage(Message message) {
        // Log all message that has been sent
        List<Attachment> attachments = message.getAttachments();
        Member member = message.getMember();

        if (UserHandler.isShar(member) && message.getContentDisplay().equals("/reset command")) {
            replyMessage(message, "Working", 30);
            Log.info("Resetting command");
            UpdatableHandler.updateCommand();
            message.delete().queue();
        }

        // Schematic preview
        if ((isSchematicText(message) && attachments.isEmpty()) || isSchematicFile(attachments)) {
            Log.info(getMessageSender(message) + ": sent a schematic ");
            sendSchematicPreview(message);
        }

        else if (isMapFile(attachments)) {
            sendMapPreview(message, message.getChannel());
        }

        // Delete in channel that it should not be
        GuildData guildData = GuildHandler.getGuild(message.getGuild());
        if (guildData._containsChannel(CHANNEL_TYPE.SCHEMATIC.name(), message.getTextChannel().getId()) || //
                guildData._containsChannel(CHANNEL_TYPE.MAP.name(), message.getTextChannel().getId())) {
            if (!message.getContentRaw().isEmpty()) {
                message.delete().queue();
                replyMessage(message, "Vui l??ng kh??ng g???i tin nh???n v??o k??nh n??y!", 30);
            }
        }

        // Update exp on message sent
        UserHandler.onMessage(message);
        DatabaseHandler.log(LOG_TYPE.MESSAGE, new Document()//
                .append("message", getMessageSender(message) + ": " + message.getContentDisplay())//
                .append("messageId", message.getId())//
                .append("userId", member == null ? null : member.getId())//
                .append("guildId", message.getGuild().getId()));

        // Log member message/file/image url to terminals
        if (!message.getContentRaw().isEmpty())
            Log.info(getMessageSender(message) + ": " + message.getContentDisplay());

        else if (!message.getAttachments().isEmpty())
            message.getAttachments().forEach(attachment -> {
                Log.info(getMessageSender(message) + ": " + attachment.getUrl());
            });

    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        Member member = event.getMember();
        Member bot = event.getGuild().getMember(jda.getSelfUser());
        if (member == bot)
            return;
        Member target = event.getEntity();
        UserData userData = UserHandler.getUserNoCache(target);
        if (userData == null)
            throw new IllegalStateException("No user data found");

        if (bot == null)
            throw new IllegalStateException("Bot not in guild");
        userData._displayLevelName();

    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        DatabaseHandler.log(LOG_TYPE.MESSAGE_DELETED, new Document("messageId", event.getMessageId()));
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        // Send invite link to member who left the guild
        User user = event.getUser();
        List<TextChannel> inviteChannels = event.getGuild().getTextChannels();
        if (!inviteChannels.isEmpty()) {
            Invite invite = inviteChannels.get(0).createInvite().complete();
            user.openPrivateChannel().queue(channel -> channel.sendMessage(invite.getUrl()).queue());
        }
        log(event.getGuild(), user.getName() + " r???i m??y ch???");
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        UserHandler.addUser(event.getMember());
        log(event.getGuild(), event.getMember().getEffectiveName() + " tham gia m??y ch???");
    }

    public static void log(Guild guild, @Nonnull String content) {
        GuildData guildData = GuildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");

        List<TextChannel> botLogChannel = guildData._getChannels(CHANNEL_TYPE.BOT_LOG.name());
        if (botLogChannel == null) {
            Log.info("Bot log channel for guild <" + guild.getName() + "> does not exists");
        } else
            botLogChannel.forEach(c -> c.sendMessage("```" + content + "```").queue());
    }

    public static boolean isSchematicText(Message message) {
        return message.getContentRaw().startsWith(ContentHandler.schemHeader) && message.getAttachments().isEmpty();
    }

    public static boolean isSchematicFile(Attachment attachment) {
        String fileExtension = attachment.getFileExtension();
        if (fileExtension == null)
            return true;
        return fileExtension.equals(Vars.schematicExtension);
    }

    public static boolean isSchematicFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isSchematicFile(a))
                return true;
        }
        return false;
    }

    public static boolean isMapFile(Attachment attachment) {
        return attachment.getFileName().endsWith(".msav") || attachment.getFileExtension() == null;
    }

    public static boolean isMapFile(Message message) {
        for (Attachment a : message.getAttachments()) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    public static boolean isMapFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    // Its bad lol
    public static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public static boolean isChannel(Guild guild, Channel channel,
            HashMap<String, HashMap<String, String>> guildChannelIds) {
        if (guildChannelIds.containsKey(guild.getId())) {
            if (guildChannelIds.get(guild.getId()).containsKey(channel.getId()))
                return true;
        }
        return false;
    }

    public static boolean hasChannel(@Nonnull String guildId, @Nonnull String channelId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild == null)
            return false;
        List<GuildChannel> channel = guild.getChannels();
        for (GuildChannel c : channel) {
            if (c.getId().equals(channelId))
                return true;
        }
        return false;
    }

    public static void sendMapPreview(Attachment attachment, Member member, MessageChannel channel) {
        try {

            ContentHandler.Map map = ContentHandler.readMap(NetworkHandler.download(attachment.getUrl()));
            new File("cache/").mkdir();
            new File("cache/temp/").mkdir();
            File mapFile = new File("cache/temp/" + attachment.getFileName());
            Fi imageFile = Fi.get("cache/temp/image_" + attachment.getFileName().replace(".msav", ".png"));
            Streams.copy(NetworkHandler.download(attachment.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile.file());

            EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + imageFile.name())
                    .setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(),
                            member.getEffectiveAvatarUrl())
                    .setTitle(map.name == null ? attachment.getFileName().replace(".msav", "") : map.name);
            builder.addField("Size: ", map.image.getWidth() + "x" + map.image.getHeight(), false);
            if (map.description != null)
                builder.setFooter(map.description);

            File f = imageFile.file();
            if (f == null)
                return;
            channel.sendFile(mapFile).addFile(f).setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMapPreview(Message message, MessageChannel channel) {
        for (int i = 0; i < message.getAttachments().size(); i++) {
            Attachment attachment = message.getAttachments().get(i);
            if (isMapFile(attachment)) {
                sendMapPreview(attachment, message.getMember(), channel);
                message.delete().queue();
            }
        }
    }

    public static void sendMapPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("mapfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!isMapFile(attachment)) {
            event.reply("File ???????c ch???n kh??ng ph???i l?? file b???n ?????");
            return;
        }

        Member member = event.getMember();
        sendMapPreview(attachment, member, event.getChannel());
        event.reply("G???i th??nh c??ng.");
    }

    public static void sendSchematicPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("schematicfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!isSchematicFile(attachment)) {
            event.reply("File ???????c ch???n kh??ng ph???i l?? file b???n thi???t k???");
            return;
        }
        Member member = event.getMember();
        try {
            sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), member, event.getChannel());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSchematicPreview(Message message) {
        try {
            if (isSchematicText(message)) {
                sendSchematicPreview(ContentHandler.parseSchematic(message.getContentRaw()), message);
            } else {
                for (int i = 0; i < message.getAttachments().size(); i++) {
                    Attachment attachment = message.getAttachments().get(i);

                    if (isSchematicFile(attachment)) {
                        sendSchematicPreview(ContentHandler.parseSchematicURL(attachment.getUrl()), message);
                    }
                }
            }
        } catch (Exception e) {
            Log.err(e);
        }
        message.delete().queue();
    }

    public static void sendSchematicPreview(Schematic schem, Message message) {
        try {
            File schemFile = getSchematicFile(schem);
            File previewFile = getSchematicPreviewFile(schem);
            EmbedBuilder builder = getSchematicEmbedBuilder(schem, previewFile, message.getMember());

            message.reply(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();
        } catch (Exception e) {
            replyMessage(message.getChannel(), "L???i: " + e.getMessage(), 30);
        }
    }

    public static void sendSchematicPreview(Schematic schem, Member member, MessageChannel channel) {
        try {
            File schemFile = getSchematicFile(schem);
            File previewFile = getSchematicPreviewFile(schem);
            EmbedBuilder builder = getSchematicEmbedBuilder(schem, previewFile, member);

            channel.sendFile(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();
        } catch (Exception e) {
            replyMessage(channel, "L???i: " + e.getMessage(), 30);
        }
    }

    public static EmbedBuilder getSchematicEmbedBuilder(Schematic schem, File previewFile, Member member) {
        EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + previewFile.getName())
                .setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl())
                .setTitle(schem.name());

        if (!schem.description().isEmpty())
            builder.setFooter(schem.description());
        StringBuilder field = new StringBuilder();

        // Schem heigh, width
        field.append("- K??ch th?????c:" + String.valueOf(schem.width) + "x" + String.valueOf(schem.height) + "\n");
        field.append("- T??i nguy??n c???n: ");
        // Item requirements
        for (ItemStack stack : schem.requirements()) {
            String itemName = stack.item.name.replace("-", "");
            if (itemName == null)
                continue;
            List<Emote> emotes = member.getGuild().getEmotesByName(itemName, true);
            if (!emotes.isEmpty())
                field.append(emotes.get(0).getAsMention()).append(stack.amount).append("  ");
            else
                field.append(stack.item.name + ": " + stack.amount + " ");
        }

        // Power input/output

        int powerProduction = (int) Math.round(schem.powerProduction()) * 60;
        int powerConsumption = (int) Math.round(schem.powerConsumption()) * 60;
        if (powerConsumption != 0)
            field.append("\n- N??ng l?????ng s??? d???ng: " + String.valueOf(powerConsumption));

        if (powerProduction != 0)
            field.append("\n- N??ng l?????ng t???o ra: " + String.valueOf(powerProduction));

        builder.addField("*Th??ng tin*", field.toString(), true);

        return builder;
    }

    public static @Nonnull File getSchematicFile(Schematic schem) throws IOException {
        String sname = schem.name().replace("/", "_").replace(" ", "_").replace(":", "_");
        new File("cache").mkdir();
        if (sname.isEmpty())
            sname = "empty";
        File schemFile = new File("cache/temp/" + sname + "." + Vars.schematicExtension);
        Schematics.write(schem, new Fi(schemFile));
        return schemFile;
    }

    public static @Nonnull File getSchematicPreviewFile(Schematic schem) throws Exception {

        BufferedImage preview = ContentHandler.previewSchematic(schem);
        new File("cache").mkdir();
        File previewFile = new File("cache/temp/img_" + UUID.randomUUID() + ".png");
        ImageIO.write(preview, "png", previewFile);

        return previewFile;

    }

    // Message send commands
    public static void replyMessage(SlashCommandInteractionEvent event, String content, int deleteAfter) {
        replyMessage(event.getChannel(), content, deleteAfter);
    }

    public static void replyMessage(MessageChannel channel, String content, int deleteAfter) {
        if (channel != null)
            channel.sendMessage("```" + content + "```")
                    .queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
        else {
            Log.info("Message sent in forum channel");
        }
    }

    public static void replyMessage(Message message, String content, int deleteAfter) {
        message.reply("```" + content + "```").queue(m -> m.delete().queueAfter(deleteAfter, TimeUnit.SECONDS));
    }

    public static void replyMessage(Message message, String content) {
        message.reply("```" + content + "```").queue();
    }
}
