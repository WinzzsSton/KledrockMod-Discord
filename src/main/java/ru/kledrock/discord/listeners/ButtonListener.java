package ru.kledrock.discord.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;

public class ButtonListener extends ListenerAdapter {

    private static final String LOG_CHANNEL_ID = "1566642193899192371";

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        
        if (id.startsWith("appeal_accept_")) {
            String[] parts = id.split("_");
            String punishmentId = parts[2];
            String discordUserId = parts[3];
            String nick = parts[4];

            event.editMessageEmbeds(new EmbedBuilder()
                    .setDescription("✅ Апелляция #" + punishmentId + " была **ПРИНЯТА** модератором " + event.getUser().getAsMention())
                    .setColor(Color.GREEN)
                    .build()).setComponents().queue();

            sendLog(event, "✅ Апелляция принята", punishmentId, nick, discordUserId, "Принял: " + event.getUser().getAsMention(), Color.GREEN);
            
        } else if (id.startsWith("appeal_deny_")) {
            String[] parts = id.split("_");
            String punishmentId = parts[2];
            String discordUserId = parts[3];
            String nick = parts[4];

            event.editMessageEmbeds(new EmbedBuilder()
                    .setDescription("❌ Апелляция #" + punishmentId + " была **ОТКЛОНЕНА** модератором " + event.getUser().getAsMention())
                    .setColor(Color.RED)
                    .build()).setComponents().queue();

            sendLog(event, "❌ Апелляция отклонена", punishmentId, nick, discordUserId, "Отклонил: " + event.getUser().getAsMention(), Color.RED);
        }
    }

    private void sendLog(ButtonInteractionEvent event, String title, String punishId, String nick, String discordId, String moderator, Color color) {
        TextChannel logChannel = event.getJDA().getTextChannelById(LOG_CHANNEL_ID);
        if (logChannel != null) {
            EmbedBuilder logEmbed = new EmbedBuilder();
            logEmbed.setTitle(title);
            logEmbed.setColor(color);
            logEmbed.addField("ID наказания", punishId, true);
            logEmbed.addField("Игрок", nick, true);
            logEmbed.addField("Discord автора", "<@" + discordId + ">", true);
            logEmbed.addField("Модератор", moderator, false);
            logChannel.sendMessageEmbeds(logEmbed.build()).queue();
        }
    }
}