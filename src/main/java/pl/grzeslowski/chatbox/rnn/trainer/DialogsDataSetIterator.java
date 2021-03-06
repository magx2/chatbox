package pl.grzeslowski.chatbox.rnn.trainer;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import pl.grzeslowski.chatbox.dialogs.VecDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

class DialogsDataSetIterator implements DataSetIterator {
    private final int batchSize;
    private final int maxWordsInDialog;
    private final int layerSize;
    private Iterator<VecDialog> iterator;
    private int cursor;

    DialogsDataSetIterator(Stream<VecDialog> dialogs, int batchSize, int maxWordsInDialog, int layerSize) {
        iterator = dialogs.iterator();

        this.batchSize = batchSize;
        this.maxWordsInDialog = maxWordsInDialog;
        this.layerSize = layerSize;
    }

    @Override
    public DataSet next(int howMuchToTake) {
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }

        List<VecDialog> toProcess = new ArrayList<>(howMuchToTake);
        for (int i = 0; i < howMuchToTake && iterator.hasNext(); i++) {
            toProcess.add(iterator.next());
            cursor++;
        }

        int maxLength = 0;
        for (VecDialog dialog : toProcess) {
            maxLength = Math.max(maxLength, dialog.getQuestionSize());
            maxLength = Math.max(maxLength, dialog.getAnswerSize());
        }

        checkArgument(maxLength <= maxWordsInDialog, format("maxLength = %s > maxWordsInDialog = %s", maxLength, maxWordsInDialog));

        final INDArray features = Nd4j.zeros(toProcess.size(), layerSize, maxLength);
        final INDArray labels = Nd4j.zeros(toProcess.size(), layerSize, maxLength);

        INDArray featuresMask = Nd4j.zeros(toProcess.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(toProcess.size(), maxLength);

        for (int i = 0; i < toProcess.size(); i++) {
            final VecDialog dialog = toProcess.get(i);

            putIntoArrayQuestions(features, featuresMask, i, dialog);
            putIntoArrayAnswers(labels, labelsMask, i, dialog, maxLength);
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }

    private void putIntoArrayQuestions(INDArray array, INDArray mask, int idx, VecDialog vecDialog) {
        int[] temp = new int[2];
        temp[0] = idx;

        List<INDArray> putFromHere = vecDialog.getQuestion();
        for (int k = 0; k < putFromHere.size(); k++) {
            INDArray vector = putFromHere.get(k);
            array.put(new INDArrayIndex[]{NDArrayIndex.point(idx), NDArrayIndex.all(), NDArrayIndex.point(k)}, vector);

            temp[1] = k;
            mask.putScalar(temp, 1.0);
        }
    }

    private void putIntoArrayAnswers(INDArray array, INDArray mask, int idx, VecDialog vecDialog, int maxLengthAnswers) {
        int[] temp = new int[2];
        temp[0] = idx;

        List<INDArray> putFromHere = vecDialog.getAnswer();
        int offset = maxLengthAnswers - putFromHere.size();
        for (int k = offset; k < putFromHere.size() + offset; k++) {
            INDArray vector = putFromHere.get(k - offset);
            array.put(new INDArrayIndex[]{NDArrayIndex.point(idx), NDArrayIndex.all(), NDArrayIndex.point(k)}, vector);

            temp[1] = k;
            mask.putScalar(temp, 1.0);
        }
    }

    @Override
    public int totalExamples() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int inputColumns() {
        return layerSize;
    }

    @Override
    public int totalOutcomes() {
        return layerSize;
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
    public int batch() {
        return batchSize;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("getPreProcessor not suported");
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("setPreProcessor not suported");

    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("getLabels not suported");
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public DataSet next() {
        return next(batch());
    }
}
