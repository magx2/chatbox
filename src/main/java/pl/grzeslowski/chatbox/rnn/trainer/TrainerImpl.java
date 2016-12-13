package pl.grzeslowski.chatbox.rnn.trainer;

import org.deeplearning4j.eval.Evaluation;
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
import pl.grzeslowski.chatbox.dialogs.VecDialog;
import pl.grzeslowski.chatbox.dialogs.VecDialogFunction;
import pl.grzeslowski.chatbox.rnn.RnnEngine;
import pl.grzeslowski.chatbox.rnn.trainer.splitters.TestSetSplitter;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
class TrainerImpl implements Trainer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TrainerImpl.class);

    private final DialogLoader dialogLoader;
    private final RnnEngine rnnEngine;
    private final VecDialogFunction vecDialogFunction;

    @Value("${rnn.maxWordsInDialog}")
    private int maxWordsInDialog;
    @Value("${rnn.batchSize}")
    private int batchSize;
    @Value("${rnn.epochs}")
    private int epochs;
    @Value("${word2vec.hyper.layerSize}")
    private int layerSize;

    @Autowired
    public TrainerImpl(DialogLoader dialogLoader, RnnEngine rnnEngine, VecDialogFunction vecDialogFunction) {
        this.dialogLoader = checkNotNull(dialogLoader);
        this.rnnEngine = checkNotNull(rnnEngine);
        this.vecDialogFunction = checkNotNull(vecDialogFunction);
    }

    private DataSetIterator createDateSetIterator(Stream<Stream<Dialog>> stream) {
        final Stream<VecDialog> vecDialogs = stream.flatMap(s -> s.map(vecDialogFunction))
                .filter(dialog -> dialog.getQuestionSize() >= 1)
                .filter(dialog -> dialog.getAnswerSize() >= 1)
                .filter(dialog -> dialog.getQuestionSize() <= maxWordsInDialog)
                .filter(dialog -> dialog.getAnswerSize() <= maxWordsInDialog);
        return new DialogsDataSetIterator(vecDialogs, batchSize, maxWordsInDialog, layerSize);
    }

    @Override
    public MultiLayerNetwork trainAndTest() {
        final TestSetSplitter.LearningSets<Stream<Dialog>> learningSets = dialogLoader.loadTrainData();

        final MultiLayerNetwork net = rnnEngine.buildEngine();
        log.info("Initializing model");
        net.init();
        net.setListeners(new ScoreIterationListener(200));

        final DataSetIterator train = createDateSetIterator(learningSets.getTrainingSet());
        final DataSetIterator test = createDateSetIterator(learningSets.getTestingSet());

        log.info("Starting learning");
        net.fit(train);
        log.info("Training complete. Starting evaluation:");

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
        log.info("Evaluation output:\n{}", evaluation.stats());

        return net;
    }
}
