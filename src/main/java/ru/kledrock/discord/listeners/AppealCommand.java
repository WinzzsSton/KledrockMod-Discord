package ru.kledrock.discord.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ru.kledrock.discord.api.KledrockApiClient;

import java.awt.Color;

public class AppealCommand extends ListenerAdapter {

    private static final String APPEALS_CHANNEL_ID = "1566642176933371985";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("appeal")) return;

        String nick = event.getOption("nick").getAsString();
        String punishmentId = event.getOption("id").getAsString();
        String text = event.getOption("text").getAsString();
        String discordUserId = event.getUser().getId();

        event.deferReply(true).queue();

        new Thread(() -> {
            try {
                KledrockApiClient api = new KledrockApiClient();
                boolean success = api.submitAppeal(punishmentId, discordUserId, text);
                
                if (success) {
                    event.getHook().sendMessage("✅ Ваша апелляция успешно отправлена на рассмотрение модераторам.").queue();

                    TextChannel channel = event.getJDA().getTextChannelById(APPEALS_CHANNEL_ID);
                    if (channel != null) {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("📩 Новая апелляция #" + punishmentId);
                        embed.setColor(new Color(255, 165, 0));
                        embed.addField("Игрок", nick, true);
                        embed.addField("Discord", event.getUser().getAsMention(), true);
                        embed.addField("Текст апелляции", text, false);
                        embed.setFooter("ID наказания: " + punishmentId);

                        Button accept = Button.success("appeal_accept_" + punishmentId + "_" + discordUserId + "_" + nick, "Принять");
                        Button deny = Button.danger("appeal_deny_" + punishmentId + "_" + discordUserId + "_" + nick, "Отклонить");

                        channel.sendMessageEmbeds(embed.build()).setActionRow(accept, deny).queue();
                    }
                } else {
                    event.getHook().sendMessage("❌ Сервер Minecraft отклонил запрос. Проверьте ID наказания.").queue();
                }
            } catch (Exception e) {
                event.getHook().sendMessage("❌ Ошибка связи с сервером Minecraft.").queue();
                e.printStackTrace();
            }
        }).start();
    }
}