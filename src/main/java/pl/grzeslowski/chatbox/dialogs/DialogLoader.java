package pl.grzeslowski.chatbox.dialogs;

import java.util.stream.Stream;

public interface DialogLoader {
    Stream<Dialog> load();
}
