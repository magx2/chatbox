package pl.grzeslowski.chatbox.dialogs;

import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.word2vec.Word2VecService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@Service
class VecDialogFunctionImpl implements VecDialogFunction {
    private final Word2VecService word2VecService;

    @Autowired
    public VecDialogFunctionImpl(Word2VecService word2VecService) {
        this.word2VecService = checkNotNull(word2VecService);
    }

    @Override
    public VecDialog apply(Dialog dialog) {
        final Word2Vec word2Vec = word2VecService.computeModel(); // TODO don't like this. It look like this is computed with every invocation of apply method

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
}
