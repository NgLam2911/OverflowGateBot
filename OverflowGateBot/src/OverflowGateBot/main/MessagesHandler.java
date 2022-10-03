package OverflowGateBot.main;


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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;
import javax.imageio.*;

import OverflowGateBot.main.GuildConfigHandler.ArchiveChannel;
import OverflowGateBot.mindustry.ContentHandler;
import OverflowGateBot.misc.JSONHandler;
import OverflowGateBot.misc.JSONHandler.JSONData;

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

    public HashMap<String, TextChannel> serverChatChannel = new HashMap<String, TextChannel>();

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

            jda.getPresence().setActivity(Activity.of(ActivityType.PLAYING, " /bot help để bắt đầu"));

            Log.info("Bot online.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMessageSender(Message message) {
        Member member = message.getMember();
        if (member == null)
            return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + "Unknown";
        return "[" + message.getGuild().getName() + "] " + " <" + message.getChannel().getName() + "> " + member.getEffectiveName();
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

        List<Attachment> attachments = message.getAttachments();
        // Schematic preview
        if ((isSchematicText(message) && attachments.isEmpty()) || isSchematicFile(attachments)) {
            System.out.println(getMessageSender(message) + ": sent a schematic");
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
        if (inChannel(message.getGuild(), message.getChannel(), guildConfigHandler.schematicChannel) || inChannel(message.getGuild(), message.getChannel(), guildConfigHandler.mapChannel)) {
            replyTempMessage(message, "Vui lòng không gửi tin nhắn vào kênh này!", 30);
            message.delete().queue();
            return;
        }

        // Update exp on message sent
        userHandler.onMessage(message);


        // Send message to all needed channels
        if (!message.getContentRaw().isBlank()) {
            for (TextChannel c : serverChatChannel.values()) {
                if (!message.getChannel().getName().equals(c.getName()))
                    c.sendMessage(getMessageSender(message) + ": " + message.getContentRaw() + message.getContentDisplay()).queue();
            }
        }

    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        // You can't change your nickname XD
        if (event.getMember().getUser().isBot())
            return;
        userHandler.setDisplayName(event.getEntity());
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        // Send invite link to member who left the guild
        User user = event.getUser();
        List<NewsChannel> inviteChannels = event.getGuild().getNewsChannels();
        if (inviteChannels.isEmpty())
            return;
        Invite invite = inviteChannels.get(0).createInvite().complete();
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(invite.getUrl())).queue();
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
        String fileExtension = attachment.getFileExtension();
        if (fileExtension == null)
            return false;
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

    // Its bad lol
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

    public boolean inChannel(Guild guild, Channel channel, HashMap<String, ArchiveChannel> channelIds) {
        if (channelIds.containsKey(guild.getId()))
            if (channelIds.get(guild.getId()).channelId.equals(channel.getId()))
                return true;
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
            ContentHandler.Map map = contentHandler.readMap(onet.download(attachment.getUrl()));
            new File("cache/").mkdir();
            new File("cache/temp/").mkdir();
            File mapFile = new File("cache/temp/" + attachment.getFileName());
            Fi imageFile = Fi.get("cache/temp/image_" + attachment.getFileName().replace(".msav", ".png"));
            Streams.copy(onet.download(attachment.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile.file());

            EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + imageFile.name()).setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl()).setTitle(map.name == null ? attachment.getFileName().replace(".msav", "") : map.name);
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
            String sname = schem.name().replace("/", "_").replace(" ", "_").replace(":", "_");
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
