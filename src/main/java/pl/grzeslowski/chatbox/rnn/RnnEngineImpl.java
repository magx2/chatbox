package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.graph.rnn.DuplicateToTimeSeriesVertex;
import org.deeplearning4j.nn.conf.graph.rnn.LastTimeStepVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
class RnnEngineImpl implements RnnEngine {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RnnEngineImpl.class);

    @Value("${rnn.iterations}")
    private int iterations;
    @Value("${rnn.layers.l0.nout}")
    private int layer0Out;
    @Value("${rnn.regularization}")
    private boolean regularization;
    @Value("${rnn.learningRate}")
    private double learningRate;
    @Value("${rnn.l2}")
    private double l2;
    @Value("${rnn.gradientNormalizationThreshold}")
    private double gradientNormalizationThreshold;
    @Value("${word2vec.hyper.layerSize}")
    private int layerSize;

    @Override
    public ComputationGraph buildEngine() {
        log.info("Creating new RNN model...");
        ComputationGraphConfiguration configuration = new NeuralNetConfiguration.Builder()
                //.regularization(true).l2(0.000005)
                .weightInit(WeightInit.XAVIER)
                .learningRate(0.5)
                .updater(Updater.RMSPROP)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .seed(1337)
                .graphBuilder()
                .addInputs("additionIn", "sumOut")
                .setInputTypes(InputType.recurrent(layerSize), InputType.recurrent(layerSize))

                .addLayer("encoder",
                        new GravesLSTM.Builder().nIn(layerSize).nOut(200).activation("softsign").build(),
                        "additionIn")
                .addVertex("lastTimeStep",
                        new LastTimeStepVertex("additionIn"),
                        "encoder")
                .addVertex("duplicateTimeStep",
                        new DuplicateToTimeSeriesVertex("sumOut"),
                        "lastTimeStep")

                .addLayer("decoder",
                        new GravesLSTM.Builder().nIn(layerSize + 200).nOut(128).activation("softsign").build(),
                        "sumOut", "duplicateTimeStep")

                .addLayer("output",
                        new RnnOutputLayer.Builder().nIn(128).nOut(layerSize).activation("softmax").lossFunction(LossFunctions.LossFunction.MCXENT).build(),
                        "decoder")

                .setOutputs("output")
                .pretrain(false).backprop(true)
                .build();

        return new ComputationGraph(configuration);
    }
}
