package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public interface RnnEngine {
    MultiLayerNetwork buildEngine();
}
