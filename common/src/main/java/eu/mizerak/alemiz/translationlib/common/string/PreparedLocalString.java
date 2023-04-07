package eu.mizerak.alemiz.translationlib.common.string;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.mizerak.alemiz.translationlib.common.string.LocalStringBase.getContextFactory;

public class PreparedLocalString<T> implements LocalString<T> {

    private final LocalStringBase<T> string;
    private Map<String, Object> arguments;
    protected final Map<Locale, String> formatted = new ConcurrentHashMap<>();

    protected PreparedLocalString(LocalString<T> string) {
        if (!(string instanceof LocalStringBase)) {
            throw new IllegalArgumentException("Only LocalStringBase can be prepared, got " + string.getClass().getSimpleName());
        }
        this.string = (LocalStringBase) string;
    }

    @Override
    public String getKey() {
        return this.string.getKey();
    }

    @Override
    public PreparedLocalString<T> reload() {
        this.string.reload();
        return this;
    }

    @Override
    public PreparedLocalString<T> enableReload(boolean value) {
        this.string.enableReload(value);
        return this;
    }

    @Override
    public PreparedLocalString<T> withArgument(String name, Object argument) {
        if (this.arguments == null) {
            this.arguments = new HashMap<>();
        }
        this.arguments.put(name, argument);
        return this;
    }

    @Deprecated
    @Override
    public PreparedLocalString<T> withArgument(String name, Function<TranslationContext<T>, String> function) {
        if (this.arguments == null) {
            this.arguments = new HashMap<>();
        }
        this.arguments.put(name, function.apply(null));
        return this;
    }

    @Override
    public PreparedLocalString<T> clearArguments() {
        if (this.arguments != null) {
            this.arguments.clear();
        }
        return this;
    }

    @Override
    public String getFormatted() {
        return this.string.getFormatted();
    }

    @Override
    public String getFormatted(Locale locale) {
        String formatted = this.formatted.get(locale);
        if (formatted != null) {
            return formatted;
        }

        String text = this.string.getFormatted(locale);

        if (this.arguments != null) {
            for (Map.Entry<String, Object> entry : this.arguments.entrySet()) {
                text = text.replaceAll("\\{" + entry.getKey() + "\\}", String.valueOf(entry.getValue()));
            }
        }

        this.formatted.put(locale, text);
        return text;
    }

    @Override
    public String getText(T object, Consumer<TranslationContext<T>> handler) {
        TranslationContext<T> ctx = ((TranslationContext.Factory<T>) getContextFactory(object.getClass())).create(object);
        if (handler != null) {
            handler.accept(ctx);
        }

        String formatted = this.getFormatted(ctx.getLocale());

        if (!this.string.arguments.isEmpty()) {
            for (Map.Entry<String, Function<TranslationContext<T>, String>> entry : this.string.arguments.entrySet()) {
                formatted = formatted.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue().apply(ctx));
            }
        }
        return ctx.handlePostFormat(formatted);
    }

    @Override
    public void uploadFallbackMessage() {
        this.string.uploadFallbackMessage();
    }
}
