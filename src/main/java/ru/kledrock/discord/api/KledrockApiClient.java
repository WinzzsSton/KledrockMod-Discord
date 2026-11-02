package ru.kledrock.discord.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.*;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KledrockApiClient {
    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson = new Gson();
    private final List<Double> tpsHistory = new ArrayList<>();

    public KledrockApiClient() {
        this.baseUrl = System.getenv("MINECRAFT_API_URL");
        this.apiKey = System.getenv("MINECRAFT_API_KEY");
        this.client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
    }

    public String fetchStats() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/stats")
                .addHeader("X-Kledrock-Key", apiKey)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        }
        return null;
    }

    public EmbedBuilder buildStatsEmbed(String json) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        double tps = obj.get("tps").getAsDouble();
        int online = obj.get("online").getAsInt();
        int deaths = obj.get("deathsToday").getAsInt();
        int joins = obj.get("joinsToday").getAsInt();

        if (tpsHistory.size() > 20) tpsHistory.remove(0);
        tpsHistory.add(tps);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Ежедневная статистика сервера");
        embed.setColor(tps > 18 ? Color.GREEN : (tps > 15 ? Color.ORANGE : Color.RED));
        embed.addField("Текущий TPS", String.format("%.2f", tps), true);
        embed.addField("Онлайн", String.valueOf(online), true);
        embed.addField("Смертей за день", String.valueOf(deaths), true);
        embed.addField("Входов за день", String.valueOf(joins), true);
        embed.setImage("attachment://tps_chart.png");
        embed.setTimestamp(java.time.Instant.now());
        return embed;
    }

    public List<Double> getTpsHistory() { return tpsHistory; }

    public boolean submitAppeal(String punishmentId, String discordUserId, String text) {
        JsonObject json = new JsonObject();
        json.addProperty("punishmentId", punishmentId);
        json.addProperty("discordUserId", discordUserId);
        json.addProperty("text", text);

        RequestBody body = RequestBody.create(gson.toJson(json), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + "/api/appeal")
                .addHeader("X-Kledrock-Key", apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}