package pl.grzeslowski.chatbox.dialogs;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicroDvdDialogParserTest {
    @Autowired
    private MicroDvdDialogParser parser;

    @Test
    public void shouldParseBasicDialog() {

        // given
        final List<String> lines = Arrays.asList(
                "{12601}{12673}Dobrze i to słyszeć. Jeszcze raz.",
                "{12675}{12808}Znów nie żyjesz.",
                "{12810}{13004}Te miny nie są niewypałami."
        );

        //when
        final Set<Dialog> dialogs = parser.parse(lines).collect(Collectors.toSet());

        // then
        assertThat(dialogs).hasSize(1);
        final Dialog dialog = dialogs.iterator().next();
        final List<String> d = dialog.getDialog();
        assertThat(d).hasSize(3);

        assertThat(d.get(0)).isEqualTo("Dobrze i to słyszeć. Jeszcze raz.");
        assertThat(d.get(1)).isEqualTo("Znów nie żyjesz.");
        assertThat(d.get(2)).isEqualTo("Te miny nie są niewypałami.");
    }

    @Test
    public void shouldOmitLinesWithOnlyOneDialog() {

        // given
        final List<String> lines = Arrays.asList(
                "{12601}{12673}Dobrze i to słyszeć. Jeszcze raz.",
                "{12675}{12808}Znów nie żyjesz.",
                "{13810}{13004}Te miny nie są niewypałami."
        );

        //when
        final Set<Dialog> dialogs = parser.parse(lines).collect(Collectors.toSet());

        // then
        assertThat(dialogs).hasSize(1);
        final Dialog dialog = dialogs.iterator().next();
        final List<String> d = dialog.getDialog();
        assertThat(d).hasSize(2);

        assertThat(d.get(0)).isEqualTo("Dobrze i to słyszeć. Jeszcze raz.");
        assertThat(d.get(1)).isEqualTo("Znów nie żyjesz.");
    }

    @Test
    public void shouldParse2Dialog() {

        // given
        final List<String> lines = Arrays.asList(
                "{12601}{12673}Dobrze i to słyszeć. Jeszcze raz.",
                "{12675}{12808}Znów nie żyjesz.",
                "{13810}{13850}Te miny nie są niewypałami.",
                "{13855}{13857}Jeśli zrobicie coś źle,"
        );

        //when
        final List<Dialog> dialogs = parser.parse(lines).collect(Collectors.toList());

        // then
        assertThat(dialogs).hasSize(2);

        {
            final Dialog dialog = dialogs.get(0);
            final List<String> d = dialog.getDialog();
            assertThat(d).hasSize(2);

            assertThat(d.get(0)).isEqualTo("Dobrze i to słyszeć. Jeszcze raz.");
            assertThat(d.get(1)).isEqualTo("Znów nie żyjesz.");
        }

        {
            final Dialog dialog = dialogs.get(1);
            final List<String> d = dialog.getDialog();
            assertThat(d).hasSize(2);

            assertThat(d.get(0)).isEqualTo("Te miny nie są niewypałami.");
            assertThat(d.get(1)).isEqualTo("Jeśli zrobicie coś źle,");
        }
    }
}
