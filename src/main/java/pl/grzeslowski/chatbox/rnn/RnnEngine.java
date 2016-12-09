package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import pl.grzeslowski.chatbox.dialogs.Dialog;

import java.util.stream.Stream;

public interface RnnEngine {
    MultiLayerNetwork buildEngine(Stream<Dialog> dialogs, Word2Vec word2Vec);
}
