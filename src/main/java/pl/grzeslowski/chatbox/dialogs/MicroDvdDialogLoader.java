package pl.grzeslowski.chatbox.dialogs;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.files.FileReader;
import pl.grzeslowski.chatbox.rnn.trainer.splitters.TestSetSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@Service
class MicroDvdDialogLoader implements DialogLoader {
    private static final Logger log = LoggerFactory.getLogger(MicroDvdDialogLoader.class);
    private static final Pattern LINE_PARSER_PATTERN = Pattern.compile(
            "[\\[{]" +
                    "(\\d+)" +
                    "[]}]" +
                    "[\\[{]" +
                    "(\\d+)" +
                    "[]}]" +
                    "(.+)"
    );
    private final FileReader fileReader;
    @Value("${dialogLoader.maxGapBetweenDialogs}")
    private int maxGapBetweenDialogs;
    @Value("${dialogLoader.fps}")
    private int fps;

    @Autowired
    public MicroDvdDialogLoader(FileReader fileReader) {
        this.fileReader = checkNotNull(fileReader);
    }

    @Override
    public TestSetSplitter.LearningSets<Stream<Dialog>> loadTrainData() {
        log.info("Creating stream with all dialogs");

        final TestSetSplitter.LearningSets<Stream<String>> learningSets = fileReader.subtitlesLines();
        return new TestSetSplitter.LearningSets<>(
                map(learningSets.getTrainingSet()),
                map(learningSets.getTestingSet())
        );
    }

    private Stream<Stream<Dialog>> map(Stream<Stream<String>> learningStream) {
        return learningStream
                .map(stream -> stream.map(this::parseDialogLine).filter(Optional::isPresent).map(Optional::get))
                .map(stream -> stream.collect(new DialogLineCollector(maxGapBetweenDialogs)))
                .map(list -> list.stream()
                        .filter(dialogLines -> dialogLines.size() >= 2))
                .map(stream -> stream
                        .map(dialogLines -> dialogLines.stream().map(DialogLine::getText)))
                .map(stream -> stream.map(Dialog::new));
    }

    private Optional<DialogLine> parseDialogLine(String line) {
        final Matcher matcher = LINE_PARSER_PATTERN.matcher(line);
        if (matcher.matches()) {
            final int startFrame = Integer.parseInt(matcher.group(1));
            final int stopFrame = Integer.parseInt(matcher.group(2));
            final String text = matcher.group(3);

            return Optional.of(new DialogLine(startFrame / fps, stopFrame / fps, text));
        } else {
            return Optional.empty();
        }
    }

    private static class DialogLine {
        final int startTime;
        final int stopTime;
        final String text;

        private DialogLine(int startTime, int stopTime, String text) {
            this.startTime = startTime;
            this.stopTime = stopTime;
            this.text = text;
        }

        String getText() {
            return text;
        }
    }

    private static class DialogLineCollector implements Collector<DialogLine, List<List<DialogLine>>, List<List<DialogLine>>> {
        private final int maxGapBetweenDialogs;

        DialogLineCollector(int maxGapBetweenDialogs) {
            this.maxGapBetweenDialogs = maxGapBetweenDialogs;
        }

        @Override
        public Supplier<List<List<DialogLine>>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<List<DialogLine>>, DialogLine> accumulator() {
            return (dialogLines, dialogLine) -> {
                if (dialogLines.isEmpty()) {
                    final List<DialogLine> l = new ArrayList<>();
                    l.add(dialogLine);
                    dialogLines.add(l);
                } else {
                    List<DialogLine> lastDialogLines = dialogLines.get(dialogLines.size() - 1);
                    DialogLine lastDialogLine = lastDialogLines.get(lastDialogLines.size() - 1);
                    if (isTheSameDialogLien(lastDialogLine, dialogLine)) {
                        lastDialogLines.add(dialogLine);
                    } else {
                        final List<DialogLine> l = new ArrayList<>();
                        l.add(dialogLine);
                        dialogLines.add(l);
                    }
                }
            };
        }

        private boolean isTheSameDialogLien(DialogLine lastDialogLine, DialogLine dialogLine) {
            final int stopTime = lastDialogLine.stopTime;
            final int startTime = dialogLine.startTime;
            return startTime - stopTime <= maxGapBetweenDialogs;
        }

        @Override
        public BinaryOperator<List<List<DialogLine>>> combiner() {
            return (lists1, lists2) -> {
                List<List<DialogLine>> list = new ArrayList<>(lists1.size() + lists2.size());
                list.addAll(lists1);
                list.addAll(lists2);
                return list;
            };
        }

        @Override
        public Function<List<List<DialogLine>>, List<List<DialogLine>>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Sets.newHashSet(IDENTITY_FINISH);
        }
    }

}
