package pl.grzeslowski.chatbox.dialogs;


import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class Dialog {
    private final List<String> dialog;

    public Dialog(List<String> dialog) {
        checkArgument(dialog.size() >= 2);
        this.dialog = dialog;
    }

    public List<String> getDialog() {
        return dialog;
    }
}
