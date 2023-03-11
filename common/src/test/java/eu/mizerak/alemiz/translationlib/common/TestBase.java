package eu.mizerak.alemiz.translationlib.common;

import eu.mizerak.alemiz.translationlib.common.string.LocalString;
import eu.mizerak.alemiz.translationlib.common.string.StringFormatter;
import eu.mizerak.alemiz.translationlib.common.string.TranslationContext;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.common.structure.User;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Getter
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestBase {
    private TranslationLibLoader loader;
    private final List<StringFormatter> formatters = new ArrayList<>();

    @BeforeAll
    public void init() {
        this.loader = TranslationLibLoader.builder()
                .defaultLocale(Locale.ENGLISH)
                .build();
        TranslationLibLoader.setDefault(loader);

        LocalString.registerContextFactory(User.class, (TranslationContext.Factory<User>) (object, string) -> new TranslationContext<User>(object, string) {
            @Override
            public Locale getLocale() {
                return this.getObject().getLocale();
            }
        });
    }

    @AfterEach
    public void finish() {
        this.formatters.forEach(LocalString::unregisterFormatter);
    }

    public TranslationTerm addTerm(String key, String tag, Locale locale, String translation) {
        TranslationTerm term = new TranslationTerm();
        term.setKey(key);
        term.getTags().add(tag);
        term.getTranslations().put(locale, translation);
        return this.addTerm(term);
    }

    public TranslationTerm addTerm(TranslationTerm term) {
        this.loader.loadTerms(Collections.singleton(term), false);
        return term;
    }

    public void registerFormatter(StringFormatter formatter) {
        this.formatters.add(formatter);
        LocalString.registerFormatter(formatter);
    }
}
