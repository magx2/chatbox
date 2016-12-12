package pl.grzeslowski.chatbox.dialogs;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class Dialog {
    private final List<String> dialog;

    public Dialog(List<String> dialog) {
        checkArgument(dialog.size() >= 2);
        this.dialog = dialog;
    }

    public Dialog(Stream<String> dialog) {
        this(dialog.collect(Collectors.toList()));
    }

    public List<String> getDialog() {
        return dialog;
    }
}
