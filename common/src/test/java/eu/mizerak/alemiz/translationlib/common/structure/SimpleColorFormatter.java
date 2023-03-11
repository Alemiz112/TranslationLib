package eu.mizerak.alemiz.translationlib.common.structure;

import eu.mizerak.alemiz.translationlib.common.string.StringFormatter;

import java.util.regex.Matcher;

public class SimpleColorFormatter implements StringFormatter {

    @Override
    public String[] getFormatTags() {
        return new String[]{"red", "green", "blue"};
    }

    @Override
    public void format(Matcher matcher, String tag, StringBuffer buffer) {
        if (tag.equals("red")) {
            buffer.append("&c");
        }
    }

    @Override
    public String getReplacement(String tag) {
        switch (tag) {
            case "green":
                return "&a";
            case "blue":
                return "&b";
        }
        return null;
    }
}
