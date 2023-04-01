package eu.mizerak.alemiz.translationlib.common.string;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PreparedLocalString<T> implements LocalString<T> {

    private final LocalStringBase<T> string;
    private Map<String, Object> arguments;
    protected final Set<Locale> formatted = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
        if (this.formatted.contains(locale)) {
            return this.string.getFormatted(locale);
        }

        String formatted = this.string.getFormatted(locale);
        if (this.arguments != null) {
            for (Map.Entry<String, Object> entry : this.arguments.entrySet()) {
                formatted = formatted.replace("\\{" + entry.getKey() + "\\}", String.valueOf(entry.getValue()));
            }
        }

        this.formatted.add(locale);
        this.string.setFormatted(locale, formatted);
        return formatted;
    }

    @Override
    public String getText(T object, Consumer<TranslationContext<T>> handler) {
        return this.string.getText(object, handler);
    }

    @Override
    public void uploadFallbackMessage() {
        this.string.uploadFallbackMessage();
    }
}
