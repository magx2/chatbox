package pl.grzeslowski.chatbox.preprocessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.grzeslowski.chatbox.TestApplicationConfiguration;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationConfiguration.class)
public class BasicTextPreprocessorTest {

    @Autowired
    private TextPreprocessor preprocessor;

    @Test
    public void cleanCurlyBrackets() {

        // given
        String line = "{9111}{9124}{Y:i}Może ma rację.";
        String expecing = "{9111}{9124}{Y:i}Może ma rację.";

        // when
        final String preprocessed = preprocessor.preprocess(line);

        // then
        assertThat(preprocessed).isEqualTo(expecing);
    }


}
