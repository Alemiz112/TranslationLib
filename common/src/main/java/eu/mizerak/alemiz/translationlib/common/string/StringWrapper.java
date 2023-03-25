package eu.mizerak.alemiz.translationlib.common.string;

import java.util.Locale;
import java.util.function.Function;

/**
 * A very simple implementation of a LocalString, which does not do any translations or formatting
 * nor does support language translations.
 */
public class StringWrapper<T> implements LocalString<T> {
    public static StringWrapper<Object> EMPTY = new StringWrapper<>("");

    private final String text;

    protected StringWrapper(String text) {
        this.text = text;
    }

    @Override
    public String getKey() {
        return "wrapper";
    }

    @Override
    public LocalString<T> reload() {
        throw new UnsupportedOperationException("Static string does not support reload");
    }

    @Override
    public LocalString<T> enableReload(boolean b) {
        throw new UnsupportedOperationException("Static string does not support reload");
    }

    @Override
    public LocalString<T> withArgument(String s, Function<TranslationContext<T>, String> function) {
        return this;
    }

    @Override
    public LocalString<T> clearArguments() {
        return this;
    }

    @Override
    public String getFormatted() {
        return this.text;
    }

    @Override
    public String getFormatted(Locale locale) {
        return this.text;
    }

    @Override
    public String getText(T t) {
        return this.text;
    }

    @Override
    public void uploadFallbackMessage() {
        throw new UnsupportedOperationException("Static can not be uploaded");
    }

    @Override
    public String toString() {
        return this.text;
    }
}
