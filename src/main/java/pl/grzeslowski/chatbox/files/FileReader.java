package pl.grzeslowski.chatbox.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.preprocessor.TextPreprocessor;
import pl.grzeslowski.chatbox.rnn.trainer.splitters.TestSetSplitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@Service
public class FileReader {
    private static final Logger log = LoggerFactory.getLogger(FileReader.class);
    private final List<Charset> charsets = Stream.of("UTF-8", "Windows-1250", "ISO-8859-1", "ISO-8859-2", "US-ASCII")
            .map(Charset::forName)
            .collect(toList());
    private final TextPreprocessor textPreprocessor;
    private final TestSetSplitter testSetSplitter;

    @Value("${dialogLoader.pathToSubtitles}")
    private String pathToSubtitles;
    @Value("${subtitles.path}")
    private String subtitlesPath;

    @Autowired
    public FileReader(TextPreprocessor textPreprocessor, TestSetSplitter testSetSplitter) {
        this.textPreprocessor = checkNotNull(textPreprocessor);
        this.testSetSplitter = checkNotNull(testSetSplitter);
    }

    private Stream<String> readFile(Path path, Charset charset) {
        try (Stream<String> stream = Files.lines(path, charset)) {
            return stream.collect(toList())
                    .stream()
                    .flatMap(textPreprocessor::preprocess);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<Stream<String>> readFile(Path path) {
        log.trace("Reading file {}.", path.toFile().getName());
        return charsets.stream()
                .map(charset -> {
                    try {
                        return readFile(path, charset);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    public Stream<Path> findAllFilesInDir(String dir) {
        try {
            return Files.walk(Paths.get(dir))
                    .filter(file -> Files.isRegularFile(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public TestSetSplitter.LearningSets<Stream<String>> subtitlesLines() {
        log.info("Creating stream of all files in dir {}.", pathToSubtitles);

        List<Path> subtitles = findAllFilesInDir(pathToSubtitles).collect(toList());
        final TestSetSplitter.LearningSets<Path> pathLearningSets = testSetSplitter.splitIntoSets(subtitles);
        return new TestSetSplitter.LearningSets<>(
                readFromStreamOfFileNames(pathLearningSets.getTrainingSet()),
                readFromStreamOfFileNames(pathLearningSets.getTestingSet())
        );
    }

    private Stream<Stream<String>> readFromStreamOfFileNames(Stream<Path> stream) {
        return stream
                .map(this::readFile)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
