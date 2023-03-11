package eu.mizerak.alemiz.translationlib.service.scrappers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.utils.Configuration;
import eu.mizerak.alemiz.translationlib.service.utils.Scheduler;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Named("traduora")
@Singleton
public class TraduoraScrapper implements TranslationDataScrapper {
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    @Inject
    Configuration config;

    @Inject
    OkHttpClient httpClient;

    private String serverAddress;
    private String clientSecret;
    private String clientId;

    private String projectId;

    private String accessToken;
    private long nextRefreshTime;

    @PostConstruct
    public void onInit() {
        this.serverAddress = config.getProperty("scrapper.traduora.address", true);
        this.clientSecret = config.getProperty("scrapper.traduora.client-secret", true);
        this.clientId = config.getProperty("scrapper.traduora.client-id", true);
        this.projectId = config.getProperty("scrapper.traduora.project-id", true);
        Scheduler.DEFAULT.execute(this::resolveToken);
    }

    private void resolveToken() {
        JsonObject body = new JsonObject();
        body.addProperty("grant_type", "client_credentials");
        body.addProperty("client_id", this.clientId);
        body.addProperty("client_secret", this.clientSecret);

        Request request = new Request.Builder()
                .url(this.serverAddress + "/api/v1/auth/token")
                .post(RequestBody.create(body.toString(), APPLICATION_JSON))
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                log.error("Unable to obtain Traduora authentication token: {}", response.message());
                this.accessToken = null;
                return;
            }

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            Duration duration = parseDuration(json.get("expires_in").getAsString());
            this.nextRefreshTime = System.currentTimeMillis() + duration.toMillis();
            this.accessToken = json.get("access_token").getAsString();
            log.info("Successfully authenticated to Traduora and obtained access token. Token will be refreshed in {} minutes!", duration.toMinutes());

            Scheduler.DEFAULT.schedule(this::resolveToken, duration.getSeconds() - 10, TimeUnit.SECONDS);
        } catch (IOException e) {
            log.error("Unable to refresh access token", e);
            this.accessToken = null;
        }
    }

    private Set<String> resolveLocales() throws IOException {
        Request request = new Request.Builder()
                .url(this.serverAddress + "/api/v1/projects/" + this.projectId + "/translations")
                .header("Authorization", "Bearer " + this.accessToken())
                .get()
                .build();


        try (Response response = this.httpClient.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                return null;
            }

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray array = json.getAsJsonArray("data");

            Set<String> translations = new HashSet<>();
            for (JsonElement element : array) {
                JsonObject locale = element.getAsJsonObject().getAsJsonObject("locale");
                translations.add(locale.get("code").getAsString());
            }
            return translations;
        }
    }

    @Override
    public Collection<TranslationTerm> resolveTerms() throws Exception {
        Set<String> locales = this.resolveLocales();
        return locales == null ? null : this.resolveTerms(locales);
    }

    @Override
    public Collection<TranslationTerm> resolveTerms(Set<String> locales) throws IOException {
        long startTime = System.currentTimeMillis();
        Map<String, TranslationTerm> terms = new TreeMap<>();

        for (String localeStr : locales) {
            String[] split = localeStr.split("_");
            Locale locale;
            if (split.length > 1) {
                locale = new Locale(split[0], split[1]);
            } else {
                locale = new Locale(split[0]);
            }

            HttpUrl url = HttpUrl.get(this.serverAddress + "/api/v1/projects/" + this.projectId + "/exports")
                    .newBuilder()
                    .addQueryParameter("locale", localeStr)
                    .addQueryParameter("format", "jsonflat")
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + this.accessToken())
                    .get()
                    .build();

            try (Response response = this.httpClient.newCall(request).execute()) {
                if (response.code() != 200 || response.body() == null) {
                    return null;
                }

                JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
                for (String key : json.keySet()) {
                    TranslationTerm term = terms.computeIfAbsent(key, k -> new TranslationTerm());
                    term.setKey(key);
                    term.getTranslations().put(locale, json.get(key).getAsString());
                }
            }
        }

        // Fetch tags
        Request request = new Request.Builder()
                .url(this.serverAddress + "/api/v1/projects/" + this.projectId + "/terms")
                .header("Authorization", "Bearer " + this.accessToken())
                .get()
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            if (response.code() != 200 || response.body() == null) {
                return null;
            }

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            for (JsonElement element : json.getAsJsonArray("data")) {
                JsonObject termJson = element.getAsJsonObject();
                String key = termJson.get("value").getAsString();

                TranslationTerm term = terms.get(key);
                if (term == null) {
                    continue;
                }

                for (JsonElement labelElement : termJson.getAsJsonArray("labels")) {
                    JsonElement label = labelElement.getAsJsonObject().get("value");
                    term.getTags().add(label.getAsString());
                }
            }
        }

        log.info("Resolved terms from Traduora in {} seconds", ((double) (System.currentTimeMillis() - startTime) / 1000));
        return terms.values();
    }

    private String accessToken() {
        if (this.accessToken == null) {
            throw new IllegalStateException("Invalid token");
        }

        if (System.currentTimeMillis() > this.nextRefreshTime) {
            throw new IllegalStateException("Token was not refreshed");
        }
        return this.accessToken;
    }

    private static Duration parseDuration(String text) {
        int duration = Integer.parseInt(text.substring(0, text.length() - 1));
        if (text.endsWith("s")) {
            return Duration.ofSeconds(duration);
        } else if (text.endsWith("m")) {
            return Duration.ofMinutes(duration);
        } else if (text.endsWith("h")) {
            return Duration.ofHours(duration);
        } else if (text.equals("d")) {
            return Duration.ofDays(duration);
        }
        throw new IllegalArgumentException("Failed to parse duration: " + text);
    }
}
