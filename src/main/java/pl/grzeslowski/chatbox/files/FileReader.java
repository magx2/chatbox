package pl.grzeslowski.chatbox.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.preprocessor.TextPreprocessor;

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

import static java.util.stream.Collectors.toList;

@Service
public class FileReader {
    private final List<Charset> charsets = Stream.of("UTF-8", "Windows-1250", "ISO-8859-1", "ISO-8859-2", "US-ASCII")
            .map(Charset::forName)
            .collect(toList());
    private final TextPreprocessor textPreprocessor;
    @Value("${dialogParser.pathToSubtitles}")
    private String pathToSubtitles;
    @Value("${subtitles.path}")
    private String subtitlesPath;

    @Autowired
    public FileReader(TextPreprocessor textPreprocessor) {
        this.textPreprocessor = textPreprocessor;
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

    public Stream<String> subtitlesLines() {
        return findAllFilesInDir(pathToSubtitles)
                .map(this::readFile)
                .filter(Optional::isPresent)
                .flatMap(Optional::get);
    }
}
