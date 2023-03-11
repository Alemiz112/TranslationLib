package eu.mizerak.alemiz.translationlib.common;

import eu.mizerak.alemiz.translationlib.common.string.LocalString;
import eu.mizerak.alemiz.translationlib.common.structure.SimpleColorFormatter;
import eu.mizerak.alemiz.translationlib.common.structure.TranslationTerm;
import eu.mizerak.alemiz.translationlib.common.structure.User;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslateTest extends TestBase {

    @Test
    public void testFormatting() {
        this.registerFormatter(new SimpleColorFormatter());

        LocalString<User> string = LocalString.from("test_string", "<!blue>Hello <!red>world!");
        assertEquals("&bHello &cworld!", string.getTranslated(User.ENGLISH).getText());
    }

    @Test
    public void testUpdatedTerm() {
        this.registerFormatter(new SimpleColorFormatter());

        this.addTerm("test_string", "", Locale.ENGLISH, "<!green>Hello <!red>world!");

        LocalString<User> string = LocalString.from("test_string", "<!blue>Hello <!red>world!");
        assertEquals("&bHello &cworld!", string.getText(User.FRENCH));
    }

    @Test
    public void testTermOtherLang() {
        this.registerFormatter(new SimpleColorFormatter());

        TranslationTerm term = this.addTerm("test_string", "", Locale.ENGLISH, "<!green>Hello <!red>world!");
        term.getTranslations().put(Locale.FRENCH, "<!green>Bonjour <!red>monde!");

        LocalString<User> string = LocalString.from("test_string", "<!green>Hello <!red>world!");
        assertEquals("&aBonjour &cmonde!", string.getText(User.FRENCH));
    }

    @Test
    public void testUserArgs() {
        this.registerFormatter(new SimpleColorFormatter());

        LocalString<User> string = LocalString.<User>from("test_string", "<!blue>Hello {user}")
                .withArgument("user", ctx -> ctx.getObject().getName());

        assertEquals("&bHello " + User.ENGLISH.getName(), string.getText(User.ENGLISH));
    }
}
