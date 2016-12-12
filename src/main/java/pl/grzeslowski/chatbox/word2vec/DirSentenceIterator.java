package pl.grzeslowski.chatbox.word2vec;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import pl.grzeslowski.chatbox.files.FileReader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class DirSentenceIterator implements SentenceIterator {
    private final FileReader fileReader;
    private final String dir;
    private Iterator<Path> iterator;
    private SentencePreProcessor preProcessor;

    DirSentenceIterator(FileReader fileReader, String dir) {
        this.fileReader = checkNotNull(fileReader);
        this.dir = checkNotNull(dir);
        checkArgument(!dir.isEmpty());
        final File file = new File(dir);
        checkArgument(file.exists());
        checkArgument(file.isDirectory());
        initIterator();
    }

    private void initIterator() {
        iterator = fileReader.findAllFilesInDir(dir).collect(Collectors.toSet()).iterator();
    }

    @Override
    public String nextSentence() {
        checkArgument(hasNext(), "Iterator does not have next elem!");
        final Path toRead = iterator.next();
        final Optional<Stream<String>> stringStream = fileReader.readFile(toRead);
        if(!stringStream.isPresent()) {
            return nextSentence();
        } else {
            return stringStream.get()
                    .map(line -> line.replaceAll("\\{", " "))
                    .map(line -> line.replaceAll("}", " "))
                    .collect(Collectors.joining("\n"));
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public void reset() {
        initIterator();
    }

    @Override
    public void finish() {
        iterator = null;
    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }
}
