package eu.mizerak.alemiz.translationlib.common.string;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class LocalStringBase<T> implements LocalString<T> {
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


    private final String key;

    @Getter
    private TranslationTerm term;

    protected final Map<Locale, String> formatted = new ConcurrentHashMap<>();
    protected final Map<String, Function<TranslationContext<T>, String>> arguments = new TreeMap<>();

    protected LocalStringBase(String key, TranslationTerm term) {
        this.key = key;
        this.term = term;
    }

    @Override
    public LocalStringBase<T> withArgument(String name, Object argument) {
        return this.withArgument(name, ctx -> String.valueOf(argument));
    }

    @Override
    public LocalStringBase<T> withArgument(String name, Function<TranslationContext<T>, String> mapper) {
        this.arguments.put(name, mapper);
        return this;
    }

    @Override
    public LocalStringBase<T> clearArguments() {
        this.arguments.clear();
        return this;
    }

    public Collection<String> getArgumentNames() {
        return Collections.unmodifiableCollection(this.arguments.keySet());
    }

    @Override
    public String getText(T object, Consumer<TranslationContext<T>> handler) {
        TranslationContext.Factory<?> factory = contextFactories.get(object.getClass());
        if (factory == null) {
            factory = contextFactories.get(object.getClass().getSuperclass());
            if (factory == null) {
                throw new IllegalStateException("Unregistered factory for type " + object.getClass());
            }
        }

        TranslationContext<T> ctx = ((TranslationContext.Factory<T>) factory).create(object);
        if (handler != null) {
            handler.accept(ctx);
        }

        String formatted = this.getFormatted(ctx.getLocale());
        if (!this.arguments.isEmpty()) {
            for (Map.Entry<String, Function<TranslationContext<T>, String>> entry : this.arguments.entrySet()) {
                formatted = formatted.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue().apply(ctx));
            }
        }
        return ctx.handlePostFormat(formatted);
    }

    @Override
    public String getFormatted(Locale locale) {
        String formatted = this.formatted.get(locale);
        if (formatted != null) {
            return formatted;
        }

        String text = term.getText(locale);
        if (text == null || text.trim().isEmpty()) {
            text = term.getText(this.getDefaultLocale());
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

    public abstract Locale getDefaultLocale();

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return "LocalString(key=" + this.key + ")";
    }
}
