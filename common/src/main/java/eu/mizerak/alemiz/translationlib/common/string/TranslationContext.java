package eu.mizerak.alemiz.translationlib.common.string;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public abstract class TranslationContext<T> {

    private final LocalString<T> string;
    private final T object;

    public TranslationContext(T object, LocalString<T> string) {
        this.object = object;
        this.string = string;
    }

    public abstract Locale getLocale();

    public String getText() {
        String formatted = this.string.getFormatted(this.getLocale());
        if (!this.string.getArguments().isEmpty()) {
            for (Map.Entry<String, Function<TranslationContext<T>, String>> entry : this.string.getArguments().entrySet()) {
                formatted = formatted.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue().apply(this));
            }
        }
        return formatted;
    }

    public T getObject() {
        return this.object;
    }

    public interface Factory<T> {
        TranslationContext<T> create(T object, LocalString<T> string);
    }
}
