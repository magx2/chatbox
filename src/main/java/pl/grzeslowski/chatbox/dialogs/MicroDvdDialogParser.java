package pl.grzeslowski.chatbox.dialogs;

import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collectors.toList;

@Service
public class MicroDvdDialogParser implements DialogParser {
    private static final Pattern LINE_PARSER_PATTERN = Pattern.compile(
            "\\{(\\d+)\\}\\{(\\d+)\\}(.+)"
    );

    @Value("${dialogParser.maxGapBetweenDialogs}")
    int maxGapBetweenDialogs;
    @Value("${dialogParser.fps}")
    int fps;

    @Override
    public Stream<Dialog> parse(Stream<String> lines) {
        return lines
                .map(this::parseDialogLine)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(new DialogLineCollector(maxGapBetweenDialogs))
                .stream()
                .filter(dialogLines -> dialogLines.size() >= 2)
                .map(dialogLines -> dialogLines.stream().map(DialogLine::getText).collect(toList()))
                .map(Dialog::new);
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

        public String getText() {
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
