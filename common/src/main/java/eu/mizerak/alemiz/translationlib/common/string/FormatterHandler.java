package eu.mizerak.alemiz.translationlib.common.string;

import lombok.Data;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class FormatterHandler {
    private final StringFormatter formatter;
    private final Map<String, Pattern> patterns = new TreeMap<>();

    public FormatterHandler(StringFormatter formatter) {
        this.formatter = formatter;

        for (String tag : formatter.getFormatTags()) {
            if (this.patterns.containsKey(tag)) {
                throw new IllegalStateException("Tag " + tag + " is already present");
            }
            this.patterns.put(tag, Pattern.compile("<!" + tag + ">"));
        }
    }

    public String format(String text) {
        for (Map.Entry<String, Pattern> entry : this.patterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(text).reset();
            if (!matcher.find()) {
                continue;
            }

            int appendPosition = 0;
            StringBuffer buffer = new StringBuffer();

            do {
                int end = matcher.end();
                buffer.append(text, appendPosition, matcher.start());

                String replacement = this.formatter.getReplacement(entry.getKey());
                if (replacement == null) {
                    this.formatter.format(matcher, entry.getKey(), buffer);
                } else {
                    buffer.append(replacement);
                }
                appendPosition = end;
            } while (matcher.find());
            text = buffer.append(text, appendPosition, text.length()).toString();
        }
        return text;
    }
}
