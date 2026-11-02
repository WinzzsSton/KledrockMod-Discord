package ru.kledrock.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import ru.kledrock.discord.api.KledrockApiClient;
import ru.kledrock.discord.listeners.AppealCommand;
import ru.kledrock.discord.listeners.ButtonListener;
import ru.kledrock.discord.listeners.StatusCommand;
import ru.kledrock.discord.utils.ChartGenerator;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KledrockBot {
    public static void main(String[] args) throws Exception {
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("DISCORD_TOKEN is not set!");
            System.exit(1);
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("KledrockMod | /status"))
                .addEventListeners(new AppealCommand(), new ButtonListener(), new StatusCommand())
                .build();

        jda.awaitReady();
        System.out.println("KledrockMod Discord Bot started as " + jda.getSelfUser().getName());

        jda.updateCommands().addCommands(
                Commands.slash("appeal", "Подать апелляцию")
                        .addOption(OptionType.STRING, "nick", "Ваш ник", true)
                        .addOption(OptionType.STRING, "id", "ID наказания", true)
                        .addOption(OptionType.STRING, "text", "Текст апелляции", true),
                Commands.slash("status", "Показать статус сервера и график TPS")
        ).queue();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                TextChannel statsChannel = jda.getTextChannelById("1566642816497623160");
                if (statsChannel != null) {
                    KledrockApiClient api = new KledrockApiClient();
                    String json = api.fetchStats();
                    if (json != null) {
                        File chartFile = ChartGenerator.generateTpsChart(api.getTpsHistory());
                        statsChannel.sendMessageEmbeds(api.buildStatsEmbed(json).build())
                                .addFiles(FileUpload.fromData(chartFile, "tps_chart.png"))
                                .queue();
                        chartFile.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 24, TimeUnit.HOURS);
    }
}