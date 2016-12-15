package pl.grzeslowski.chatbox.rnn.trainer;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import pl.grzeslowski.chatbox.dialogs.Dialog;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("Duplicates")
class DialogsMultipleDataSetIterator implements MultiDataSetIterator {
    private final int maxWordsInDialog;
    private final int layerSize;
    private final Word2Vec word2Vec;
    private final Iterator<Dialog> iterator;

    DialogsMultipleDataSetIterator(Stream<Dialog> dialogs, Word2Vec word2Vec, int maxWordsInDialog, int layerSize) {
        this.word2Vec = word2Vec;
        iterator = dialogs.iterator();

        this.maxWordsInDialog = maxWordsInDialog;
        this.layerSize = layerSize;
    }

    @Override
    public MultiDataSet next(final int howMuchToTake) {
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }

        //Initialize everything with zeros - will eventually fill with one hot vectors
        INDArray encoderSeq = Nd4j.zeros(howMuchToTake, layerSize, maxWordsInDialog);
        INDArray outputSeq = Nd4j.zeros(howMuchToTake, layerSize, maxWordsInDialog);

        //Since these are fixed length sequences of timestep
        //Masks are not required
        INDArray encoderMask = Nd4j.zeros(howMuchToTake, maxWordsInDialog);
        INDArray outputMask = Nd4j.zeros(howMuchToTake, maxWordsInDialog);

        int[] tmp = new int[2];
        for (int sampleNumber = 0; sampleNumber < howMuchToTake; sampleNumber++) {
            tmp[0] = sampleNumber;

            final Dialog dialogToProcess = iterator.next();
            final List<String> questions = questions(dialogToProcess);
            final List<String> answer = answer(dialogToProcess);

            checkArgument(questions.size() > 0);
            checkArgument(answer.size() > 0);

            for (int questionNumber = 0; questionNumber < questions.size(); questionNumber++) {
                tmp[1] = questionNumber;
                final String word = questions.get(questionNumber);
                final INDArray matrix = word2Vec.getWordVectorMatrix(word);

                encoderSeq.put(new INDArrayIndex[]{
                        NDArrayIndex.point(sampleNumber),
                        NDArrayIndex.all(),
                        NDArrayIndex.point(questionNumber)
                }, matrix);

                encoderMask.putScalar(tmp, 1.0);
            }

            for (int answerNumber = 0; answerNumber < answer.size(); answerNumber++) {
                tmp[1] = answerNumber;
                final String word = answer.get(answerNumber);
                final INDArray matrix = word2Vec.getWordVectorMatrix(word);

                outputSeq.put(new INDArrayIndex[]{
                        NDArrayIndex.point(sampleNumber),
                        NDArrayIndex.all(),
                        NDArrayIndex.point(answerNumber)
                }, matrix);

                outputMask.putScalar(tmp, 1.0);
            }
        }

        INDArray[] inputs = new INDArray[]{encoderSeq};
        INDArray[] inputMasks = new INDArray[]{encoderMask};
        INDArray[] labels = new INDArray[]{outputSeq};
        INDArray[] labelMasks = new INDArray[]{outputMask};

        return new org.nd4j.linalg.dataset.MultiDataSet(inputs, labels, inputMasks, labelMasks);
    }

    private List<String> questions(Dialog dialog) {
        final List<String> list = dialog.getDialog();
        return list.subList(0, list.size() - 1)
                .stream()
                .flatMap(line -> Arrays.stream(line.split("\\s"))
                        .filter(word2Vec::hasWord))
                .collect(toList());
    }

    private List<String> answer(Dialog dialog) {
        final List<String> list = dialog.getDialog();
        return Arrays.stream(list.get(list.size() - 1).split("\\s"))
                .filter(word2Vec::hasWord)
                .collect(toList());
    }


    @Override
    public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public MultiDataSet next() {
        return next(1);
    }
}
