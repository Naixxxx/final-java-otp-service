package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.config.AppSettings;
import dev.naixxxx.guardcode.domain.DeliveryChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TelegramCodeSender implements CodeSender {
    private static final Logger log = LoggerFactory.getLogger(TelegramCodeSender.class);
    private final HttpClient client = HttpClient.newHttpClient();
    private final String botToken;
    private final String apiTemplate;

    public TelegramCodeSender(AppSettings settings) {
        this.botToken = settings.get("telegram.bot.token");
        this.apiTemplate = settings.get("telegram.api.url", "https://api.telegram.org/bot%s/sendMessage");
    }

    @Override public DeliveryChannel channel() { return DeliveryChannel.TELEGRAM; }

    @Override public void send(String destination, String code) {
        // destination = Telegram chat_id конкретного пользователя
        String message = "Your OTP code: " + code;
        String body = "chat_id=" + enc(destination) + "&text=" + enc(message);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiTemplate.formatted(botToken)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new DeliveryException("Telegram API status " + response.statusCode(), null);
            log.info("OTP Telegram message sent to chat {}", destination);
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new DeliveryException("Telegram delivery failed", e);
        }
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
