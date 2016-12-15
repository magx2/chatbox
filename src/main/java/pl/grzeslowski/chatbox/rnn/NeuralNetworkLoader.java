package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.graph.ComputationGraph;

import java.util.Optional;

public interface NeuralNetworkLoader {
    Optional<ComputationGraph> load();
}
