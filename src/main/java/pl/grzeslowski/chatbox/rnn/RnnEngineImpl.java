package pl.grzeslowski.chatbox.rnn;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.dialogs.Dialog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@Service
class RnnEngineImpl implements RnnEngine {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RnnEngineImpl.class);

    @Value("${rnn.maxWordsInDialog}")
    private int maxWordsInDialog;
    @Value("${rnn.batchSize}")
    private int batchSize;
    @Value("${rnn.epochs}")
    private int epochs;
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
    public MultiLayerNetwork buildEngine(final Stream<Dialog> dialogs, final Word2Vec word2Vec) {
        final List<VecDialog> list = dialogs
                .map(dialog -> dialogToVec(dialog, word2Vec))
                .filter(dialog -> dialog.getQuestionSize() >= 1)
                .filter(dialog -> dialog.getAnswerSize() >= 1)
                .collect(toList());


        //Set up network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .updater(Updater.RMSPROP)
                .regularization(regularization)
                .l2(l2)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(gradientNormalizationThreshold)
                .learningRate(learningRate)
                .list()
                .layer(0, new GravesLSTM.Builder()
                        .nIn(layerSize)
                        .nOut(layer0Out)
                        .activation("softsign").build())
                .layer(1, new RnnOutputLayer.Builder()
                        .activation("softmax")
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .nIn(layer0Out)
                        .nOut(layerSize)
                        .build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(200));

        final DataSetIterator iterator = new DialogsDataSetIterator(list, batchSize, maxWordsInDialog, layerSize);

        for (int i = 0; i < epochs; i++) {
            log.info("Epoch {}...", i);
            net.fit(iterator);
        }

        return net;
    }

    private VecDialog dialogToVec(Dialog dialog, Word2Vec word2Vec) {
        final String question = findQuestion(dialog);
        final String answer = findAnswer(dialog);

        List<INDArray> questionArray = tokenize(question, word2Vec);
        List<INDArray> answerArray = tokenize(answer, word2Vec);

        return new VecDialog(dialog, questionArray, answerArray);
    }

    private List<INDArray> tokenize(String question, Word2Vec word2Vec) {
        return Arrays.stream(question.split("\\s")) // split on white space
                .filter(word2Vec::hasWord)
                .map(word2Vec::getWordVectorMatrix)
                .collect(toList());
    }

    private String findQuestion(Dialog dialog) {
        final List<String> dialogs = dialog.getDialog();
        checkArgument(dialogs.size() >= 2);
        return dialogs.subList(0, dialogs.size() - 1)
                .stream()
                .collect(Collectors.joining("\n"));
    }

    private String findAnswer(Dialog dialog) {
        final List<String> dialogs = dialog.getDialog();
        assert dialogs.size() >= 2;
        return dialogs.get(dialogs.size() - 1);
    }

    class VecDialog {
        private final Dialog dialog;
        private final List<INDArray> question;
        private final List<INDArray> answer;

        private VecDialog(Dialog dialog, List<INDArray> question, List<INDArray> answer) {
            this.dialog = dialog;
            this.question = question;
            this.answer = answer;
        }

        public List<INDArray> getQuestion() {
            return question;
        }

        public List<INDArray> getAnswer() {
            return answer;
        }

        public int getQuestionSize() {
            return question.size();
        }

        public int getAnswerSize() {
            return answer.size();
        }
    }
}
