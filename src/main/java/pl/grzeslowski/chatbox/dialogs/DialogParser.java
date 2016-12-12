package pl.grzeslowski.chatbox.dialogs;

import java.util.stream.Stream;

public interface DialogParser {
    Stream<Dialog> load();
}
