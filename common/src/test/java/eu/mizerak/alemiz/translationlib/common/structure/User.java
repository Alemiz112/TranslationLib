package eu.mizerak.alemiz.translationlib.common.structure;

import lombok.Data;

import java.util.Locale;

@Data
public class User {
    public static final User ENGLISH = new User("english_user", Locale.ENGLISH);
    public static final User FRENCH = new User("french_user", Locale.FRENCH);

    private final String name;
    private final Locale locale;
}
