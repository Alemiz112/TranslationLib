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
        assertEquals("&bHello &cworld!", string.getText(User.ENGLISH));
    }

    @Test
    public void testTermOtherLang() {
        this.registerFormatter(new SimpleColorFormatter());

        TranslationTerm term = this.addTerm("test_string2", "", Locale.ENGLISH, "<!green>Hello <!red>world!");
        term.getTranslations().put(Locale.FRENCH, "<!green>Bonjour <!red>monde!");

        LocalString<User> string = LocalString.from("test_string2", "<!green>Hello <!red>world!");
        assertEquals("&aBonjour &cmonde!", string.getText(User.FRENCH));
    }

    @Test
    public void testUserArgs() {
        this.registerFormatter(new SimpleColorFormatter());

        LocalString<User> string = LocalString.<User>from("test_string3", "<!blue>Hello {user}")
                .withArgument("user", ctx -> ctx.getObject().getName());

        assertEquals("&bHello " + User.ENGLISH.getName(), string.getText(User.ENGLISH));
    }

    @Test
    public void testStatic() {
        LocalString<User> string = LocalString.<User>immutable("Hello {user}")
                .withArgument("user", ctx -> ctx.getObject().getName());

        assertEquals("Hello " + User.ENGLISH.getName(), string.getText(User.ENGLISH));

        LocalString<User> string1 = LocalString.<User>immutable("Hi {user}")
                .withArgument("user", ctx -> ctx.getObject().getName());

        assertEquals("Hi " + User.ENGLISH.getName(), string1.getText(User.ENGLISH));
    }

    @Test
    public void testJoinedStrings() {
        LocalString<User> string = LocalString.<User>from("test_string4", "Hello {user}")
                .withArgument("user", ctx -> ctx.getObject().getName());
        LocalString<User> string2 = LocalString.immutable(" this is test");

        String text = string.append(string2).getText(User.ENGLISH);
        assertEquals("Hello " + User.ENGLISH.getName() + " this is test", text);
    }
}
