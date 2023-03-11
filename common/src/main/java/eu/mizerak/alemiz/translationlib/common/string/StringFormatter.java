package eu.mizerak.alemiz.translationlib.common.string;

import java.util.regex.Matcher;

public interface StringFormatter {

    String[] getFormatTags();

    default void format(Matcher matcher, String tag, StringBuffer buffer) {
    }

    default String getReplacement(String tag) {
        return null;
    }
}
