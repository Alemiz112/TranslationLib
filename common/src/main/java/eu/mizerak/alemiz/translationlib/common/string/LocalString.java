package eu.mizerak.alemiz.translationlib.common.string;

import eu.mizerak.alemiz.translationlib.common.TranslationLibLoader;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public interface LocalString<T> {

    static <T> LocalString<T> from(String key, String defaultText) {
        return from(key, defaultText, TranslationLibLoader.get());
    }

    static <T> LocalString<T> from(String key, String defaultText, TranslationLibLoader loader) {
        return from(key, defaultText, loader, true);
    }

    static <T> LocalString<T> from(String key, String defaultText, TranslationLibLoader loader, boolean cache) {
        LocalString<Object> cached;
        if (cache && (cached = loader.getLocalString(null, key)) != null) {
            return (LocalString<T>) cached;
        }

        TranslationTerm term = loader.getTranslationterm(key);
        if (term == null) {
            term = TranslationTerm.createEmpty(key, defaultText, loader.getDefaultLocale());
            loader.addTermUpdate(term);
        } else {
            String text = term.getText(loader.getDefaultLocale());
            if (text == null) {
                throw new IllegalStateException("Term " + key + " has no default translation");
            }
        }

        LocalStringBase<T> string = new LocalStringImpl<>(key, term, loader, defaultText);
        string.enableReload(true);
        return string;
    }

    static <T> LocalString<T> immutable(String text) {
        TranslationTerm term = TranslationTerm.createEmpty("static", text, StaticLocalString.DEFAULT_LOCALE);
        return new StaticLocalString<>(term);
    }

    static <T> LocalString<T> wrapper(String text) {
        return new StringWrapper<>(text);
    }

    static <T> LocalString<T> empty() {
        return (LocalString<T>) StringWrapper.EMPTY;
    }

    static <T> PreparedLocalString<T> prepared(LocalString<T> string) {
        return new PreparedLocalString<>(string);
    }

    String getKey();

    LocalString<T> reload();

    LocalString<T> enableReload(boolean enable);

    default LocalString<T> withArgument(String name, Object argument) {
        return this.withArgument(name, ctx -> String.valueOf(argument));
    }

    default LocalString<T> withArgument(String name, LocalString<T> string) {
        return this.withArgument(name, ctx -> string.getText(ctx.getObject()));
    }

    LocalString<T> withArgument(String name, Function<TranslationContext<T>, String> mapper);

    LocalString<T> clearArguments();

    String getFormatted();

    String getFormatted(Locale locale);

    default String getText(T object) {
        return this.getText(object, null);
    }

    String getText(T object, Consumer<TranslationContext<T>> handler);

    void uploadFallbackMessage();

    default LocalString<T> append(String string) {
        return new JoinLocalString<>(this, LocalString.wrapper(string));
    }

    default LocalString<T> append(LocalString<T> string) {
        return new JoinLocalString<>(this, string);
    }

    default LocalString<T> append(LocalString<T> string, String delimiter) {
        return new JoinLocalString<>(this, string, delimiter);
    }

    default PreparedLocalString<T> prepared() {
        return prepared(this);
    }
}
