package eu.mizerak.alemiz.translationlib.common.string;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public abstract class TranslationContext<T> {
    private final T object;
    private Map<String, Function<T, String>> localArguments;

    public TranslationContext(T object) {
        this.object = object;
    }

    public abstract Locale getLocale();

    public T getObject() {
        return this.object;
    }

    public TranslationContext<T> argument(String argument, Object value) {
        return this.argument(argument, t -> String.valueOf(value));
    }

    public TranslationContext<T> argument(String argument, Function<T, String> function) {
        if (this.localArguments == null) {
            this.localArguments = new HashMap<>();
        }
        this.localArguments.put(argument, function);
        return this;
    }

    public String handlePostFormat(String string) {
        if (this.localArguments != null) {
            for (Map.Entry<String, Function<T, String>> entry : this.localArguments.entrySet()) {
                string = string.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue().apply(this.object));
            }
        }
        return string;
    }

    public interface Factory<T> {
        TranslationContext<T> create(T object);
    }
}
