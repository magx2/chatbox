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
import pl.grzeslowski.chatbox.dialogs.DialogLoader;
import pl.grzeslowski.chatbox.dialogs.VecDialog;
import pl.grzeslowski.chatbox.dialogs.VecDialogFunction;
import pl.grzeslowski.chatbox.misc.RandomFactory;
import pl.grzeslowski.chatbox.rnn.RnnEngine;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@Service
class TrainerImpl implements Trainer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TrainerImpl.class);

    private final DialogLoader dialogLoader;
    private final RandomFactory randomFactory;
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
    public TrainerImpl(DialogLoader dialogLoader, RandomFactory randomFactory, RnnEngine rnnEngine, VecDialogFunction vecDialogFunction) {
        this.dialogLoader = checkNotNull(dialogLoader);
        this.randomFactory = checkNotNull(randomFactory);
        this.rnnEngine = checkNotNull(rnnEngine);
        this.vecDialogFunction = checkNotNull(vecDialogFunction);
    }

    @Override
    public MultiLayerNetwork trainAndTest() {
        final List<VecDialog> list = dialogLoader.load()
                .map(vecDialogFunction)
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
}
