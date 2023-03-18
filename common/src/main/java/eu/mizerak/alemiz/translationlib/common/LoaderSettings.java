package eu.mizerak.alemiz.translationlib.common;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoaderSettings {
    private Locale defaultLocale;

    private String serverAddress;
    private String serverToken;

    @Setter(AccessLevel.PRIVATE)
    private int refreshInterval = -1;
    @Setter(AccessLevel.PRIVATE)
    private TimeUnit timeUnit;

    private boolean termUpdates;
    private boolean aggressiveUpdates;

    public LoaderSettings refreshTask(int interval, TimeUnit timeUnit) {
        this.refreshInterval = interval;
        this.timeUnit = timeUnit;
        return this;
    }

    public static LoaderSettings builder() {
        return new LoaderSettings();
    }

    public TranslationLibLoader createLoader() {
        return new TranslationLibLoader(this);
    }
}
