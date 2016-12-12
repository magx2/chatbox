package pl.grzeslowski.chatbox.rnn.trainer;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public interface Trainer {
    MultiLayerNetwork train();
}
