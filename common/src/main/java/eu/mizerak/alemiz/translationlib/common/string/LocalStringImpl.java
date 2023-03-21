package eu.mizerak.alemiz.translationlib.common.string;

import eu.mizerak.alemiz.translationlib.common.TranslationLibLoader;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LocalStringImpl<T> extends LocalStringBase<T> {

    private final TranslationLibLoader loader;

    @Getter
    private final String fallbackMessage;

    @Getter
    private TranslationTerm term;

    protected LocalStringImpl(String key, TranslationTerm term, TranslationLibLoader loader, String fallbackMessage) {
        super(key, term);
        this.loader = loader;
        this.fallbackMessage = fallbackMessage;
    }

    @Override
    public LocalStringImpl<T> reload() {
        TranslationTerm term = this.loader.getTranslationterm(this.getKey());
        if (term == null) {
            return this;
        }

        if (term.hasLocale(this.loader.getDefaultLocale())) {
            this.term = term;
            this.formatted.clear();
        }
        return this;
    }

    @Override
    public LocalStringImpl<T> enableReload(boolean enable) {
        if (enable) {
            this.loader.onStringSubscribe(this);
        } else {
            this.loader.onStringUnsubscribe(this);
        }
        return this;
    }

    @Override
    public String getFormatted() {
        return this.getFormatted(this.loader.getDefaultLocale());
    }

    @Override
    public void uploadFallbackMessage() {
        this.loader.addTermUpdate(TranslationTerm.createEmpty(this.getKey(), this.fallbackMessage, loader.getDefaultLocale()));
    }

    @Override
    public Locale getDefaultLocale() {
        return this.loader.getDefaultLocale();
    }
}
