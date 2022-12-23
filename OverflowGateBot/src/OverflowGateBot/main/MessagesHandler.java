package OverflowGateBot.main;

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

import OverflowGateBot.lib.data.GuildData;
import OverflowGateBot.lib.data.UserData;
import OverflowGateBot.lib.data.GuildData.CHANNEL_TYPE;
import OverflowGateBot.lib.mindustry.ContentHandler;
import OverflowGateBot.main.DatabaseHandler.LOG_TYPE;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import mindustry.type.ItemStack;

import static OverflowGateBot.OverflowGateBot.*;

public class MessagesHandler extends ListenerAdapter {

    public final Integer messageAliveTime = 30;

    public HashMap<String, TextChannel> serverChatChannel = new HashMap<String, TextChannel>();

    public MessagesHandler() {

        jda.addEventListener(this);
        System.out.println("Message handler up");
    }

    public String getMessageSender(Message message) {
        Member member = message.getMember();
        if (member == null)
            return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + "Unknown";
        return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> "
                + member.getEffectiveName();
    }

    public String getMessageSender(@Nonnull SlashCommandInteractionEvent event) {
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

    public void handleMessage(Message message) {
        // Log all message that has been sent
        List<Attachment> attachments = message.getAttachments();

        // Schematic preview
        if ((isSchematicText(message) && attachments.isEmpty()) || isSchematicFile(attachments)) {
            System.out.println(getMessageSender(message) + ": sent a schematic ");
            sendSchematicPreview(message, message.getChannel());
        }

        else if (isMapFile(attachments)) {
            sendMapPreview(message, message.getChannel());
        }

        // Log member message/file/image url to terminal
        if (!message.getContentRaw().isEmpty())
            System.out.println(getMessageSender(message) + ": " + message.getContentDisplay());
        else if (!message.getAttachments().isEmpty())
            message.getAttachments().forEach(attachment -> {
                System.out.println(getMessageSender(message) + ": " + attachment.getUrl());
            });

        // Delete in channel that it should not be
        GuildData guildData = guildHandler.getGuild(message.getGuild());

        if (guildData._containsChannel(CHANNEL_TYPE.SCHEMATIC.name(), message.getTextChannel().getId()) || //
                guildData._containsChannel(CHANNEL_TYPE.MAP.name(), message.getTextChannel().getId())) {

            message.delete().queue();
            replyTempMessage(message, "Vui lòng không gửi tin nhắn vào kênh này!", 30);
            return;
        }

        // Update exp on message sent
        userHandler.onMessage(message);
        DatabaseHandler.log(LOG_TYPE.MESSAGE, getMessageSender(message) + ": " + message.getContentDisplay());
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        Member target = event.getEntity();
        UserData userData = userHandler.getUserAwait(target);
        if (userData == null)
            throw new IllegalStateException("No user data found");

        Member bot = event.getGuild().getMember(jda.getSelfUser());
        if (bot == null)
            throw new IllegalStateException("Bot not in guild");

        if (bot.canInteract(target)) {
            String name = target.getEffectiveName();
            String nickname = name.substring(name.indexOf("]") + 1, 0);
            if (userData.hideLevel)
                target.modifyNickname(nickname).queue();
            else
                target.modifyNickname("[Level" + userData.level + "] " + nickname).queue();
        }

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
        log(user.getName() + " rời máy chủ", event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        userHandler.addUser(event.getMember());
        log(event.getMember().getEffectiveName() + " tham gia máy chủ", event.getGuild());
    }

    public void log(@Nonnull String content, Guild guild) {
        GuildData guildData = guildHandler.getGuild(guild);
        if (guildData == null)
            throw new IllegalStateException("No guild data found");

        List<TextChannel> botLogChannel = guildData._getChannels(CHANNEL_TYPE.BOT_LOG.name());
        if (botLogChannel == null) {
            System.out.println("Bot log channel for guild " + guild.getName() + " does not exists");

        } else
            botLogChannel.forEach(c -> c.sendMessage("```" + content + "```").queue());
    }

    public boolean isSchematicText(Message message) {
        return message.getContentRaw().startsWith(ContentHandler.schemHeader) && message.getAttachments().isEmpty();
    }

    public boolean isSchematicFile(Attachment attachment) {
        String fileExtension = attachment.getFileExtension();
        if (fileExtension == null)
            return true;
        return fileExtension.equals(Vars.schematicExtension);
    }

    public boolean isSchematicFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isSchematicFile(a))
                return true;
        }
        return false;
    }

    public boolean isMapFile(Attachment attachment) {
        return attachment.getFileName().endsWith(".msav") || attachment.getFileExtension() == null;
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

    // Its bad lol
    public String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public boolean isChannel(Guild guild, Channel channel, HashMap<String, HashMap<String, String>> guildChannelIds) {
        if (guildChannelIds.containsKey(guild.getId())) {
            if (guildChannelIds.get(guild.getId()).containsKey(channel.getId()))
                return true;
        }
        return false;
    }

    public boolean hasChannel(@Nonnull String guildId, @Nonnull String channelId) {
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

    public void sendMapPreview(Attachment attachment, Member member, MessageChannel channel) {
        try {

            ContentHandler.Map map = contentHandler.readMap(networkHandler.download(attachment.getUrl()));
            new File("cache/").mkdir();
            new File("cache/temp/").mkdir();
            File mapFile = new File("cache/temp/" + attachment.getFileName());
            Fi imageFile = Fi.get("cache/temp/image_" + attachment.getFileName().replace(".msav", ".png"));
            Streams.copy(networkHandler.download(attachment.getUrl()), new FileOutputStream(mapFile));
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
        OptionMapping fileOption = event.getOption("mapfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

        if (!isMapFile(attachment)) {
            event.reply("File được chọn không phải là file bản đồ");
            return;
        }

        Member member = event.getMember();
        sendMapPreview(attachment, member, event.getChannel());
        event.reply("Gửi thành công.");
    }

    public void sendSchematicPreview(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("mapfile");
        if (fileOption == null)
            return;
        Attachment attachment = fileOption.getAsAttachment();

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
                sendSchematicPreview(contentHandler.parseSchematic(message.getContentRaw()), message.getMember(),
                        channel);
            } else {
                for (int i = 0; i < message.getAttachments().size(); i++) {
                    Attachment attachment = message.getAttachments().get(i);
                    if (isSchematicFile(attachment)) {
                        sendSchematicPreview(contentHandler.parseSchematicURL(attachment.getUrl()), message.getMember(),
                                channel);
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
            String sname = schem.name().replace("/", "_").replace(" ", "_").replace(":", "_");
            if (sname.isEmpty())
                sname = "empty";

            new File("cache").mkdir();
            File previewFile = new File("cache/temp/img_" + UUID.randomUUID() + ".png");
            File schemFile = new File("cache/temp/" + sname + "." + Vars.schematicExtension);
            Schematics.write(schem, new Fi(schemFile));
            ImageIO.write(preview, "png", previewFile);

            EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + previewFile.getName())
                    .setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(),
                            member.getEffectiveAvatarUrl())
                    .setTitle(schem.name());

            if (!schem.description().isEmpty())
                builder.setFooter(schem.description());
            StringBuilder field = new StringBuilder();

            // Schem heigh, width
            field.append("Size:" + String.valueOf(schem.width) + "x" + String.valueOf(schem.height) + "\n");

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
            if (powerProduction != 0)
                field.append("\nNăng lượng tạo ra: " + String.valueOf(powerProduction));
            if (powerConsumption != 0)
                field.append("\nNăng lượng sử dụng: " + String.valueOf(powerConsumption));

            builder.addField("*Thông tin:*", field.toString(), true);
            // send embed
            channel.sendFile(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            e.printStackTrace();
            sendTempMessage(channel, "Bản thiết kế lỗi: " + e.getMessage(), 30);
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
