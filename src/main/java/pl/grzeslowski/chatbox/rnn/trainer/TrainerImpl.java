package pl.grzeslowski.chatbox.rnn.trainer;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.dialogs.Dialog;
import pl.grzeslowski.chatbox.dialogs.DialogLoader;
import pl.grzeslowski.chatbox.misc.RandomFactory;
import pl.grzeslowski.chatbox.rnn.RnnEngine;
import pl.grzeslowski.chatbox.word2vec.Word2VecService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@Service
class TrainerImpl implements Trainer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TrainerImpl.class);

    private final DialogLoader dialogLoader;
    private final RandomFactory randomFactory;
    private final Word2VecService word2VecService;
    private final RnnEngine rnnEngine;

    @Value("${rnn.maxWordsInDialog}")
    private int maxWordsInDialog;
    @Value("${rnn.batchSize}")
    private int batchSize;
    @Value("${rnn.epochs}")
    private int epochs;
    @Value("${word2vec.hyper.layerSize}")
    private int layerSize;

    @Autowired
    public TrainerImpl(DialogLoader dialogLoader, RandomFactory randomFactory, Word2VecService word2VecService, RnnEngine rnnEngine) {
        this.dialogLoader = checkNotNull(dialogLoader);
        this.randomFactory = checkNotNull(randomFactory);
        this.word2VecService = checkNotNull(word2VecService);
        this.rnnEngine = checkNotNull(rnnEngine);
    }

    @Override
    public MultiLayerNetwork trainAndTest() {
        final Word2Vec word2Vec = word2VecService.computeModel();
        final List<VecDialog> list = dialogLoader.load()
                .map(dialog -> dialogToVec(dialog, word2Vec))
                .filter(dialog -> dialog.getQuestionSize() >= 1)
                .filter(dialog -> dialog.getAnswerSize() >= 1)
                .filter(dialog -> dialog.getQuestionSize() <= maxWordsInDialog)
                .filter(dialog -> dialog.getAnswerSize() <= maxWordsInDialog)
                .collect(toList());
        final MultiLayerNetwork net = rnnEngine.buildEngine();
        net.init();
        net.setListeners(new ScoreIterationListener(200));

        Collections.shuffle(list, randomFactory.getObject());
        int splitPoint = list.size() * 9 / 10;

        final DataSetIterator train = new DialogsDataSetIterator(list.subList(0, splitPoint), batchSize, maxWordsInDialog, layerSize);
        final DataSetIterator test = new DialogsDataSetIterator(list.subList(splitPoint, list.size()), batchSize, maxWordsInDialog, layerSize);

        for (int i = 0; i < epochs; i++) {
            log.info("Epoch {}...", i);
            net.fit(train);

            train.reset();
            log.info("Epoch " + i + " complete. Starting evaluation:");

            Evaluation evaluation = new Evaluation();
            while (test.hasNext()) {
                DataSet t = test.next();
                INDArray features = t.getFeatureMatrix();
                INDArray labels = t.getLabels();
                INDArray inMask = t.getFeaturesMaskArray();
                INDArray outMask = t.getLabelsMaskArray();
                INDArray predicted = net.output(features, false, inMask, outMask);

                evaluation.evalTimeSeries(labels, predicted, outMask);
            }
            test.reset();
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
