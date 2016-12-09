package pl.grzeslowski.chatbox.preprocessor;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class BasicTextPreprocessor implements TextPreprocessor {

    @Override
    public String preprocess(String line) {
        return Optional.of(line)
                .map(this::removeNotNeededCurlyBrackets)
                .get();
    }

    private String removeNotNeededCurlyBrackets(String line) {
        return line.replaceAll("\\{\\D*\\}", "");
    }
}
