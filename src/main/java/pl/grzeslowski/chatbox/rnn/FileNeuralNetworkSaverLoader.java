package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import static java.lang.String.format;

@Service
class FileNeuralNetworkSaverLoader implements NeuralNetworkSaver, NeuralNetworkLoader {
    @Value("${rnn.pathToSaveModel}")
    private File pathToSaveModel;

    @Override
    public void save(MultiLayerNetwork model) {
        try {
            ModelSerializer.writeModel(model, pathToSaveModel, true);
        } catch (IOException e) {
            throw new UncheckedIOException(format("Cannot save model to file %s.", pathToSaveModel.getAbsolutePath()), e);
        }
    }

    @Override
    public Optional<ComputationGraph> load() {
        if (pathToSaveModel.exists()) {
            try {
//                final MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(pathToSaveModel, true);
                final ComputationGraph model = ModelSerializer.restoreComputationGraph(pathToSaveModel, true);
                return Optional.of(model);
            } catch (IOException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
