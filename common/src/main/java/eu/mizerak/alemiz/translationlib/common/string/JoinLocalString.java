package eu.mizerak.alemiz.translationlib.common.string;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class JoinLocalString<T> implements LocalString<T> {

    private final LocalString<T> left;
    private final LocalString<T> right;

    private final String delimiter;

    public JoinLocalString(LocalString<T> left, LocalString<T> right) {
        this(left, right, "");
    }

    public JoinLocalString(LocalString<T> left, LocalString<T> right, String delimiter) {
        this.left = left;
        this.right = right;
        this.delimiter = delimiter;
    }

    @Override
    public String getKey() {
        return this.left.getKey() + "_" + this.right.getKey();
    }

    @Override
    public LocalString<T> reload() {
        this.left.reload();
        this.right.reload();
        return this;
    }

    @Override
    public LocalString<T> enableReload(boolean enable) {
        this.left.enableReload(enable);
        this.right.enableReload(enable);
        return this;
    }

    @Override
    public LocalString<T> withArgument(String name, Function<TranslationContext<T>, String> mapper) {
        throw new UnsupportedOperationException("Joined string does not support arguments");
    }

    @Override
    public LocalString<T> clearArguments() {
        throw new UnsupportedOperationException("Joined string does not support arguments");
    }

    @Override
    public String getFormatted() {
        return this.left.getFormatted() + this.delimiter + this.right.getFormatted();
    }

    @Override
    public String getFormatted(Locale locale) {
        return this.left.getFormatted(locale) + this.delimiter + this.right.getFormatted(locale);
    }

    @Override
    public String getText(T object, Consumer<TranslationContext<T>> handler) {
        return this.left.getText(object, handler) + this.delimiter + this.right.getText(object, handler);
    }

    @Override
    public void uploadFallbackMessage() {
        this.left.uploadFallbackMessage();
        this.right.uploadFallbackMessage();
    }
}
