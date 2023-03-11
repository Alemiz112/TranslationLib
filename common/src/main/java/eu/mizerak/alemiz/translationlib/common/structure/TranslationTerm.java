package eu.mizerak.alemiz.translationlib.common.structure;

import lombok.Data;

import java.util.*;

@Data
public class TranslationTerm {
    private String key;
    private Set<String> tags = new HashSet<>();
    private Map<Locale, String> translations = new HashMap<>();

    public static TranslationTerm createEmpty(String key, String text, Locale locale) {
        TranslationTerm term = new TranslationTerm();
        term.setKey(key);
        term.getTranslations().put(locale, text);
        return term;
    }

    public String getText(Locale locale) {
        return this.translations.get(locale);
    }

    public boolean hasLocale(Locale locale) {
        return this.translations.containsKey(locale);
    }
}
