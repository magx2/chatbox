package pl.grzeslowski.chatbox.preprocessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.grzeslowski.chatbox.TestApplicationConfiguration;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.fest.assertions.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
public class BasicTextPreprocessorTest {

    @Autowired
    private TextPreprocessor preprocessor;

    @Test
    public void cleanCurlyBrackets() {

        // given
        String line = "{9111}{9124}{Y:i}Może ma rację.";
        String expecting = "{9111}{9124}Może ma rację.";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }
    @Test
    public void cleanCurlyBracketsWithNumbers() {

        // given
        String line = "[312][352]{c:$4444ff}CZERWONY KARZEŁ seria X{c:$c0c0ff}Odcinek 2 Ojcowie i";
        String expecting = "[312][352]CZERWONY KARZEŁ seria XOdcinek 2 Ojcowie i";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }

    @Test
    public void cleanCurlyBracketsThatAreTwice() {

        // given
        String line = "{147574}{147641}{y:i}Czasami myślę, {y:i}że dobrze byłoby zginąć.";
        String expecting = "{147574}{147641}Czasami myślę, że dobrze byłoby zginąć.";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }

    @Test
    public void shouldRemoveDash() {

        // given
        String line = "{9111}{9124} - Może ma rację.";
        String expecting = "{9111}{9124}  Może ma rację.";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }

    @Test
    public void shouldRemoveOddChars() {

        // given
        String line = "{9111}{9124} # $ % & | <>=:; Może ma rację.";
        String expecting = "{9111}{9124}       Może ma rację.";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }

    @Test
    public void shouldRemoveCurlyBrackets() {

        // given
        String line = "{9111}{9124} {} to ja";
        String expecting = "{9111}{9124}  to ja";

        // when
        final String preprocessed = preprocessor.preprocess(line).findFirst().get();

        // then
        assertThat(preprocessed).isEqualTo(expecting);
    }

    @Test
    public void shouldRemoveLineContainingWordNapisy24() {

        // given
        String line = "{9111}{9124} NaPiSy24 to ja";

        // when
        final List<String> preprocessed = preprocessor.preprocess(line)
                .collect(toList());

        // then
        assertThat(preprocessed).isEmpty();
    }

    @Test
    public void shouldRemoveLineContainingWordHatak() {

        // given
        String line = "{9111}{9124} GrupaHaTaK to ja";

        // when
        final List<String> preprocessed = preprocessor.preprocess(line)
                .collect(toList());

        // then
        assertThat(preprocessed).isEmpty();
    }
}
