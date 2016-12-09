package pl.grzeslowski.chatbox.files;

import org.springframework.stereotype.Service;

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

    public Stream<String> readFile(Path path, Charset charset) {
        try (Stream<String> stream = Files.lines(path, charset)) {
            return stream.collect(toList()).stream();
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

    public void joinAllFilesInDirToSingleFile(String dir, String newFile) {
        final List<String> blob = findAllFilesInDir(dir)
                .map(this::readFile)
                .filter(Optional::isPresent)
                .flatMap(Optional::get)
                .collect(toList());
        try {
            Files.write(Paths.get(newFile), blob, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
