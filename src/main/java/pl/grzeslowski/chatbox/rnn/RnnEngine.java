package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.graph.ComputationGraph;

public interface RnnEngine {
    ComputationGraph buildEngine();
}
