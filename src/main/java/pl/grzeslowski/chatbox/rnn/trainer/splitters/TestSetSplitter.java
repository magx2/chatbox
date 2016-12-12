package pl.grzeslowski.chatbox.rnn.trainer.splitters;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public interface TestSetSplitter {
    <T> LearningSets<T> splitIntoSets(List<T> all);

    final class LearningSets<R> {
        private final Stream<R> trainingSet;
        private final Stream<R> testingSet;

        public LearningSets(Stream<R> trainingSet, Stream<R> testingSet) {
            this.trainingSet = checkNotNull(trainingSet);
            this.testingSet = checkNotNull(testingSet);
        }

        public Stream<R> getTrainingSet() {
            return trainingSet;
        }

        public Stream<R> getTestingSet() {
            return testingSet;
        }
    }
}
