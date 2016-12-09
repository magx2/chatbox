package pl.grzeslowski.chatbox.dialogs;

import java.util.List;
import java.util.stream.Stream;

public interface DialogParser {
    Stream<Dialog> parse(Stream<String> lines);

    default Stream<Dialog> parse(List<String> lines) {
        return parse(lines.stream());
    }
}
