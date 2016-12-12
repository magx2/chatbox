package pl.grzeslowski.chatbox.dialogs;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

public class VecDialog {
    private final Dialog dialog;
    private final List<INDArray> question;
    private final List<INDArray> answer;

    protected VecDialog(Dialog dialog, List<INDArray> question, List<INDArray> answer) {
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
