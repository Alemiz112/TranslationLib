package eu.mizerak.alemiz.translationlib.service.scrappers.traduora;

import com.google.gson.*;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.service.scrappers.TranslationDataScrapper;
import eu.mizerak.alemiz.translationlib.service.utils.Configuration;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;
import io.avaje.http.client.gson.GsonBodyAdapter;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
@Named("traduora")
@Singleton
public class TraduoraScrapper implements TranslationDataScrapper {
    @Inject
    Configuration config;

    @Inject
    Gson gson;

    private HttpClient httpClient;

    private String serverAddress;
    private String projectId;

    @PostConstruct
    public void onInit() {
        this.serverAddress = config.getProperty("scrapper.traduora.address", true);
        this.projectId = config.getProperty("scrapper.traduora.project-id", true);
        String clientSecret = config.getProperty("scrapper.traduora.client-secret", true);
        String clientId = config.getProperty("scrapper.traduora.client-id", true);

        this.httpClient = HttpClient.builder()
                .baseUrl(this.serverAddress)
                .bodyAdapter(new GsonBodyAdapter(gson))
                .authTokenProvider(new TraduoraAuthProvider(this.serverAddress, clientSecret, clientId))
                .build();
    }

    private Set<String> resolveLocales() {
        TraduoraClientApi clientApi = this.httpClient.create(TraduoraClientApi.class);

        JsonObject json = clientApi.resolveLocales(this.projectId);
        JsonArray array = json.getAsJsonArray("data");

        Set<String> translations = new HashSet<>();
        for (JsonElement element : array) {
            JsonObject locale = element.getAsJsonObject().getAsJsonObject("locale");
            translations.add(locale.get("code").getAsString());
        }
        return translations;
    }

    @Override
    public Collection<TranslationTerm> resolveTerms() throws HttpException {
        Set<String> locales = this.resolveLocales();
        return this.resolveTerms(locales);
    }

    @Override
    public Collection<TranslationTerm> resolveTerms(@NotNull Set<String> locales) throws HttpException {
        TraduoraClientApi clientApi = this.httpClient.create(TraduoraClientApi.class);

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

            JsonObject json = clientApi.resolveTranslations(this.projectId, localeStr, "jsonflat");

            for (String key : json.keySet()) {
                TranslationTerm term = terms.computeIfAbsent(key, k -> new TranslationTerm());
                term.setKey(key);
                term.getTranslations().put(locale, json.get(key).getAsString());
            }
        }

        for (TraduoraTerm traduoraTerm : clientApi.resolveTerms(this.projectId)) {
            TranslationTerm term = terms.get(traduoraTerm.getKey());
            if (term == null) {
                continue;
            }

            term.setInternalId(traduoraTerm.getId());

            for (TraduoraTerm.Label label : traduoraTerm.getLabels()) {
                term.getTags().add(label.getName());
            }
        }
        log.info("Resolved terms from Traduora in {} seconds", ((double) (System.currentTimeMillis() - startTime) / 1000));
        return terms.values();
    }

    @Override
    public String addTerm(@NotNull TranslationTerm term, boolean replace) throws HttpException {
        TraduoraTerm traduoraTerm = new TraduoraTerm();
        traduoraTerm.setKey(term.getKey());
        if (replace && term.getInternalId() != null) {
            this.removeTerm(term);
        }

        TraduoraClientApi clientApi = this.httpClient.create(TraduoraClientApi.class);
        traduoraTerm = clientApi.addTerm(traduoraTerm, this.projectId);

        String termId = traduoraTerm.getId();
        // for (Map.Entry<Locale, String> entry : term.getTranslations().entrySet()) {
            // String locale = entry.getKey().getLanguage() + "_" + entry.getKey().getCountry();
            // TODO: updating is broken :(
            // clientApi.updateTermTranslation(new AddTranslationRequest(termId, entry.getValue()), this.projectId, locale);
        // }
        return termId;
    }

    @Override
    public void removeTerm(@NotNull TranslationTerm term) throws HttpException {
        this.httpClient.create(TraduoraClientApi.class).deleteTerm(this.projectId, term.getInternalId());
    }
}