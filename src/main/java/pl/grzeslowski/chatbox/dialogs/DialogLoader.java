package pl.grzeslowski.chatbox.dialogs;

import pl.grzeslowski.chatbox.rnn.trainer.splitters.TestSetSplitter;

import java.util.stream.Stream;

public interface DialogLoader {
    TestSetSplitter.LearningSets<Stream<Dialog>> loadTrainData();
}
