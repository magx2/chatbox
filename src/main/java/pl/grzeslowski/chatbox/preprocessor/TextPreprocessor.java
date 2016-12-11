package pl.grzeslowski.chatbox.preprocessor;

import java.util.stream.Stream;

public interface TextPreprocessor {
    Stream<String> preprocess(String line);
}
