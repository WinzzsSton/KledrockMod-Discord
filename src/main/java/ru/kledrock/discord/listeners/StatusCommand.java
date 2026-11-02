package ru.kledrock.discord.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import ru.kledrock.discord.api.KledrockApiClient;
import ru.kledrock.discord.utils.ChartGenerator;

import java.io.File;

public class StatusCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("status")) return;
        event.deferReply().queue();

        new Thread(() -> {
            try {
                KledrockApiClient api = new KledrockApiClient();
                String json = api.fetchStats();
                if (json != null) {
                    File chartFile = ChartGenerator.generateTpsChart(api.getTpsHistory());
                    event.getHook().sendMessageEmbeds(api.buildStatsEmbed(json).build())
                            .addFiles(FileUpload.fromData(chartFile, "tps_chart.png"))
                            .queue();
                    chartFile.delete();
                } else {
                    event.getHook().sendMessage("❌ Ошибка получения статистики.").queue();
                }
            } catch (Exception e) {
                event.getHook().sendMessage("❌ Ошибка.").queue();
            }
        }).start();
    }
}