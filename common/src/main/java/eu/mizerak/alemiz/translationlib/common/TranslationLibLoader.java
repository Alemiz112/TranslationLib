package eu.mizerak.alemiz.translationlib.common;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.mizerak.alemiz.translationlib.common.gson.LocaleSerializer;
import eu.mizerak.alemiz.translationlib.common.string.LocalString;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Builder
public class TranslationLibLoader {
    private static TranslationLibLoader instance;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Locale.class, new LocaleSerializer())
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static void setDefault(TranslationLibLoader loader) {
        instance = loader;
    }

    public static TranslationLibLoader get() {
        return instance;
    }

    @Getter
    private final Locale defaultLocale;

    private final Map<String, TranslationTerm> translationTerms = new ConcurrentHashMap<>();

    private final Set<LocalString<?>> activeStrings = Collections.synchronizedSet(new HashSet<>());

    public void loadTermsFromJson(Path path, boolean clearOld) throws IOException {
        if (!Files.isRegularFile(path)) {
            return;
        }

        TranslationTerm[] terms;
        try (InputStream stream = Files.newInputStream(path)) {
             terms = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), TranslationTerm[].class);
        }
        this.loadTerms(Arrays.asList(terms), clearOld);
    }

    public void loadTerms(Collection<TranslationTerm> terms, boolean clearOld) {
        if (clearOld) {
            this.clearAllTerms();
        }

        for (TranslationTerm term : terms) {
            this.translationTerms.put(term.getKey(), term);
        }

        log.info("Loaded {} translation terms", terms.size());
    }

    public void clearAllTerms() {
        this.translationTerms.clear();
    }

    public void refreshStrings() {
        log.info("Refreshing {} translation strings", this.activeStrings.size());
        for (LocalString<?> string : this.activeStrings) {
            string.reload();
        }
    }

    public void addTermUpdate(TranslationTerm term) {
        // TODO:
    }

    public void onStringSubscribe(LocalString<?> string) {
        this.activeStrings.add(string);
    }

    public void onStringUnsubscribe(LocalString<?> string) {
        this.activeStrings.remove(string);
    }

    public TranslationTerm getTranslationterm(String key) {
        return this.translationTerms.get(key);
    }
}
