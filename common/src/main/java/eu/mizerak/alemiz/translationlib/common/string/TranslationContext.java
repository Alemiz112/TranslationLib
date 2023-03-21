package eu.mizerak.alemiz.translationlib.common.string;

import java.util.Locale;

public abstract class TranslationContext<T> {
    private final T object;

    public TranslationContext(T object) {
        this.object = object;
    }

    public abstract Locale getLocale();

    public T getObject() {
        return this.object;
    }

    public interface Factory<T> {
        TranslationContext<T> create(T object);
    }
}
