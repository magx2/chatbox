package pl.grzeslowski.chatbox.preprocessor;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class BasicTextPreprocessor implements TextPreprocessor {

    @Override
    public String preprocess(String line) {
        //noinspection OptionalGetWithoutIsPresent
        return Optional.of(line)
                .map(this::removeNotNeededCurlyBrackets)
                .map(this::removeDash)
                .map(this::removeOddChars)
                .get();
    }

    private String remove(String line, String patternToRemove) {
        return line.replaceAll(patternToRemove, "");
    }

    private String removeNotNeededCurlyBrackets(String line) {
        return remove(line, "\\{\\D*\\}");
    }

    private String removeDash(String line) {
        return remove(line, "-");
    }

    private String removeOddChars(String line) {
        //noinspection OptionalGetWithoutIsPresent
        return Optional.of(line)
                .map(l -> remove(l, "#"))
                .map(l -> remove(l, "\\$"))
                .map(l -> remove(l, "%"))
                .map(l -> remove(l, "&"))
                .map(l -> remove(l, "\\|"))
                .map(l -> remove(l, "<"))
                .map(l -> remove(l, ">"))
                .map(l -> remove(l, "="))
                .map(l -> remove(l, ":"))
                .map(l -> remove(l, ";"))
                .get();
    }
}