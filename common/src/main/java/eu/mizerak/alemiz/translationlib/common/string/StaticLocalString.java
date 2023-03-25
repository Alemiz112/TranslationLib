package eu.mizerak.alemiz.translationlib.common.string;

import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;

import java.util.Locale;

/**
 * A implementation of a LocalString, which does support formatting, but does not support translations.
 */
public class StaticLocalString<T> extends LocalStringBase<T> {
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    protected StaticLocalString(TranslationTerm term) {
        super(term.getKey(), term);
    }

    @Override
    public LocalStringBase<T> reload() {
        throw new UnsupportedOperationException("Static string does not support reload");
    }

    @Override
    public LocalStringBase<T> enableReload(boolean enable) {
        throw new UnsupportedOperationException("Static string does not support reload");
    }

    @Override
    public String getFormatted() {
        return this.getFormatted(DEFAULT_LOCALE);
    }

    @Override
    public void uploadFallbackMessage() {
        throw new UnsupportedOperationException("Static can not be uploaded");
    }

    @Override
    public String getFormatted(Locale locale) {
        return super.getFormatted(DEFAULT_LOCALE);
    }

    @Override
    public Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    @Override
    public String toString() {
        return "StaticLocalString(text=" + this.getFormatted() + ")";
    }
}
