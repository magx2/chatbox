package pl.grzeslowski.chatbox.preprocessor;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
class BasicTextPreprocessor implements TextPreprocessor {

    @Override
    public Stream<String> preprocess(String line) {
        return Stream.of(line)
                .map(this::removeNotNeededCurlyBrackets)
                .map(this::removeDash)
                .map(this::removeOddChars)
                .flatMap(this::removeNapisy24AndHatak);
    }

    private Stream<String> removeNapisy24AndHatak(String line) {
        final String lower = line.toLowerCase();
        if(lower.contains("napisy24") || lower.contains("hatak")) {
            return Stream.empty();
        } else {
            return Stream.of(line);
        }
    }

    private String remove(String line, String patternToRemove) {
        return line.replaceAll(patternToRemove, "");
    }

    private String removeNotNeededCurlyBrackets(String line) {
        final String firstIteration = remove(line, "\\{\\D+?.*?\\}");
        return remove(firstIteration, "\\{\\}");
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
                .map(l -> remove(l, "/"))
                .map(l -> remove(l, "\\\\"))
                .get();
    }
}