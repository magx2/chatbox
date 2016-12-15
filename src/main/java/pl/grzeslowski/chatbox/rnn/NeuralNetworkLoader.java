package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.util.Optional;

public interface NeuralNetworkLoader {
    Optional<MultiLayerNetwork> load();
}
