package pl.grzeslowski.chatbox.dialogs;

import java.util.List;
import java.util.Set;

public interface DialogParser {
    Set<Dialog> parse(List<String> lines);
}
