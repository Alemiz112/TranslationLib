package eu.mizerak.alemiz.translationlib.common;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.mizerak.alemiz.translationlib.common.client.RestClient;
import eu.mizerak.alemiz.translationlib.common.client.TranslationLibRestApi;
import eu.mizerak.alemiz.translationlib.common.gson.LocaleSerializer;
import eu.mizerak.alemiz.translationlib.common.string.LocalString;
import eu.mizerak.alemiz.translationlib.common.structure.RestStatus;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.common.utils.ThreadFactoryBuilder;
import io.avaje.http.client.HttpException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class TranslationLibLoader {
    private static TranslationLibLoader instance;
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Locale.class, new LocaleSerializer())
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final ScheduledExecutorService EXECUTUR = Executors.newScheduledThreadPool(1, ThreadFactoryBuilder.builder()
            .format("TranslationLib Executor")
            .build());

    public static void setDefault(TranslationLibLoader loader) {
        instance = loader;
    }

    public static TranslationLibLoader get() {
        return instance;
    }

    private final LoaderSettings settings;
    private final RestClient restClient;

    private final Map<String, TranslationTerm> translationTerms = new ConcurrentHashMap<>();
    private final Set<LocalString<?>> activeStrings = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, LocalString<?>> activeStringsMap = new ConcurrentHashMap<>();
    private final Set<String> loadedTags = Collections.synchronizedSet(new HashSet<>());
    private final Set<TranslationTerm> updateQueue = Collections.synchronizedSet(new HashSet<>());

    private ScheduledFuture<?> refreshFuture;

    protected TranslationLibLoader(LoaderSettings settings) {
        this.settings = settings;
        this.restClient = new RestClient(this, settings.serverAddress(), settings.serverToken());

        if (settings.refreshInterval() > 0) {
            this.refreshFuture = EXECUTUR.scheduleAtFixedRate(this::refresh, settings.refreshInterval(), settings.refreshInterval(), settings.timeUnit());
        }
    }

    public void refresh() {
        this.sendTermUpdates();
        try {
            this.loadTermsByTag(new HashSet<>(this.loadedTags));
            this.refreshStrings();
        } catch (HttpException e) {
            log.error("Exception caught while updating translations: {}", e.statusCode(), e);
        } catch (Exception e) {
            log.error("Exception caught while updating translations", e);
        }
    }

    public void sendTermUpdates() {
        try {
            List<TranslationTerm> terms = new ArrayList<>(this.updateQueue);
            for (TranslationTerm term : terms) {
                RestStatus status = this.getTranslationLibRestApi().termUpdate(term, this.settings.aggressiveUpdates());
                if (status.isSuccess()) {
                    this.updateQueue.remove(term);
                } else {
                    log.warn("Service responded to an update with error: {} cause={}", status.getMessage(), status.getError());
                }
            }
        } catch (HttpException e) {
            log.error("Exception caught while sending term updates: {}", e.statusCode(), e);
        } catch (Exception e) {
            log.error("Exception caught while updating translations", e);
        }
    }

    public void loadTermsByTag(Collection<String> tags) {
        this.clearAllTerms();
        for (String tag : tags) {
            log.info("Loading terms with tag {}...", tag);
            try {
                TranslationTerm[] terms = this.getTranslationLibRestApi().exportTerms(tag);
                this.loadTerms(Arrays.asList(terms), false);
                this.loadedTags.add(tag);
            } catch (HttpException e) {
                throw new IllegalStateException("Failed to load terms with tag " + tag, e);
            }
        }
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
        this.loadedTags.clear();
    }

    public void refreshStrings() {
        log.info("Refreshing {} translation strings", this.activeStrings.size());
        for (LocalString<?> string : this.activeStrings) {
            string.reload();
        }
    }

    public void addTermUpdate(TranslationTerm term) {
        if (this.settings.termUpdates()) {
            this.updateQueue.add(term);
        }
    }

    public void onStringSubscribe(LocalString<?> string) {
        this.activeStrings.add(string);
        this.activeStringsMap.putIfAbsent(string.getKey(), string);
    }

    public void onStringUnsubscribe(LocalString<?> string) {
        this.activeStrings.remove(string);

        LocalString<?> localString = this.activeStringsMap.get(string.getKey());
        if (localString == string) {
            this.activeStringsMap.remove(string.getKey());
        }
    }

    public void shutdown() {
        if (this.refreshFuture != null) {
            this.refreshFuture.cancel(false);
        }
    }

    public <T> LocalString<T> getLocalString(Class<T> clazz, String key) {
        return this.getLocalString(clazz, key, "Default text placeholder");
    }

    public <T> LocalString<T> getLocalString(Class<T> clazz, String key, String defaultText) {
        LocalString<?> string = this.activeStringsMap.get(key);
        if (string != null) {
            return (LocalString<T>) string;
        }
        return LocalString.from(key, defaultText, this, false);
    }

    public TranslationTerm getTranslationterm(String key) {
        return this.translationTerms.get(key);
    }

    public Locale getDefaultLocale() {
        return this.settings.defaultLocale();
    }

    public Collection<TranslationTerm> getLoadedTerms() {
        return Collections.unmodifiableCollection(this.translationTerms.values());
    }

    public Collection<LocalString<?>> getActiveStrings() {
        return Collections.unmodifiableCollection(this.activeStrings);
    }

    public TranslationLibRestApi getTranslationLibRestApi() {
        return this.restClient.getApi();
    }
}
