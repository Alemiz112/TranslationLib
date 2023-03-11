package eu.mizerak.alemiz.translationlib.common.string;

import eu.mizerak.alemiz.translationlib.common.TranslationLibLoader;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LocalString<T> {
    private static final Map<StringFormatter, FormatterHandler> formatters = new ConcurrentHashMap<>();
    private static final Map<Class<?>, TranslationContext.Factory<?>> contextFactories = new ConcurrentHashMap<>();

    public static void registerFormatter(StringFormatter formatter) {
        formatters.put(formatter, new FormatterHandler(formatter));
    }

    public static void unregisterFormatter(StringFormatter formatter) {
        formatters.remove(formatter);
    }

    public static void registerContextFactory(Class<?> clazz, TranslationContext.Factory<?> factory) {
        contextFactories.put(clazz, factory);
    }

    public static void unregisterContextFactory(Class<?> clazz) {
        contextFactories.remove(clazz);
    }


    @Getter
    private final String key;
    private final TranslationLibLoader loader;

    @Getter
    private TranslationTerm term;
    private final Map<Locale, String> formatted = new ConcurrentHashMap<>();

    private final Map<String, Function<TranslationContext<T>, String>> arguments = new TreeMap<>();

    public static <T> LocalString<T> from(String key, String defaultText) {
        return from(key, defaultText, TranslationLibLoader.get());
    }

    public static <T> LocalString<T> from(String key, String defaultText, TranslationLibLoader loader) {
        TranslationTerm term = loader.getTranslationterm(key);
        if (term == null) {
            term = TranslationTerm.createEmpty(key, defaultText, loader.getDefaultLocale());
            loader.addTermUpdate(term);
        } else {
            String text = term.getText(loader.getDefaultLocale());
            if (text == null) {
                throw new IllegalStateException("Term " + key + " has no default translation");
            }

            if (!defaultText.equals(text)) {
                term = TranslationTerm.createEmpty(key, defaultText, loader.getDefaultLocale());
                loader.addTermUpdate(term);
            }
        }
        return new LocalString<>(key, term, loader);
    }

    private LocalString(String key, TranslationTerm term, TranslationLibLoader loader) {
        this.key = key;
        this.loader = loader;
        this.term = term;
        this.enableReload(true);
    }

    public LocalString<T> reload() {
        TranslationTerm term = this.loader.getTranslationterm(key);
        if (term == null) {
            return this;
        }

        if (term.hasLocale(this.loader.getDefaultLocale())) {
            this.term = term;
            this.formatted.clear();
        }
        return this;
    }

    public LocalString<T> enableReload(boolean enable) {
        if (enable) {
            this.loader.onStringSubscribe(this);
        } else {
            this.loader.onStringUnsubscribe(this);
        }
        return this;
    }

    public LocalString<T> withArgument(String name, Object argument) {
        return this.withArgument(name, ctx -> String.valueOf(argument));
    }

    public LocalString<T> withArgument(String name, Function<TranslationContext<T>, String> mapper) {
        this.arguments.put(name, mapper);
        return this;
    }

    public LocalString<T> clearArguments() {
        this.arguments.clear();
        return this;
    }

    protected Map<String, Function<TranslationContext<T>, String>> getArguments() {
        return this.arguments;
    }

    public Collection<String> getArgumentNames() {
        return Collections.unmodifiableCollection(this.arguments.keySet());
    }

    public TranslationContext<T> getTranslated(T object) {
        TranslationContext.Factory<?> factory = contextFactories.get(object.getClass());
        if (factory == null) {
            factory = contextFactories.get(object.getClass().getSuperclass());
            if (factory == null) {
                throw new IllegalStateException("Unregistered factory for type " + object.getClass());
            }
        }
        return ((TranslationContext.Factory<T>) factory).create(object, this);
    }

    public String getText(T object) {
        return this.getTranslated(object).getText();
    }

    public String getFormatted(Locale locale) {
        String formatted = this.formatted.get(locale);
        if (formatted != null) {
            return formatted;
        }

        String text = term.getText(locale);
        if (text == null) {
            text = term.getText(loader.getDefaultLocale());
        }

        this.formatted.put(locale, formatted = this.format(text));
        return formatted;
    }

    private String format(String text) {
        for (FormatterHandler formatter : formatters.values()) {
            text = formatter.format(text);
        }
        return text;

    }

    @Override
    public String toString() {
        return "LocalString(key=" + this.key + ")";
    }
}
