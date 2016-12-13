package pl.grzeslowski.chatbox.word2vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.files.FileReader;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Service
class Dl4JWord2VecService implements FactoryBean<Word2Vec> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Dl4JWord2VecService.class);
    private final FileReader fileReader;
    @Value("${seed}")
    private int seed;
    @Value("${dialogLoader.pathToSubtitles}")
    private String pathToSubtitles;
    @Value("${word2vec.models.pathToModel}")
    private File pathToModel;
    @Value("${word2vec.models.pathToWordVectors}")
    private File pathToWordVectors;
    @Value("${word2vec.hyper.minWordFrequency}")
    private int minWordFrequency;
    @Value("${word2vec.hyper.iterations}")
    private int iterations;
    @Value("${word2vec.hyper.layerSize}")
    private int layerSize;
    @Value("${word2vec.hyper.windowsSize}")
    private int windowsSize;

    @Autowired
    public Dl4JWord2VecService(FileReader fileReader) {
        this.fileReader = checkNotNull(fileReader);
    }

    @Override
    public Word2Vec getObject() throws Exception {
        final Optional<Word2Vec> word2Vec = loadModel();
        if(word2Vec.isPresent()) {
            log.info("Loaded saved word2vec model");
            return word2Vec.get();
        } else {
            try {
                return computeModelAndSave();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private Word2Vec computeModelAndSave() throws IOException {
        log.info("Computing word2vec");
        SentenceIterator iterator = new DirSentenceIterator(fileReader, pathToSubtitles);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(minWordFrequency)
                .iterations(iterations)
                .layerSize(layerSize)
                .seed(seed)
                .windowSize(windowsSize)
                .iterate(iterator)
                .tokenizerFactory(tokenizerFactory)
                .build();

        log.info("Fitting Word2Vec model...");
        vec.fit();

        log.info("Writing model to file...");
        WordVectorSerializer.writeWord2VecModel(vec, pathToModel);

        log.info("Writing word vectors to text file...");
        WordVectorSerializer.writeWordVectors(vec, pathToWordVectors);

        return vec;
    }

    private Optional<Word2Vec> loadModel() {
        log.info("Trying to load word2vec model...");
        try {
            return Optional.of(WordVectorSerializer.readWord2VecModel(pathToModel));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Class<?> getObjectType() {
        return Word2Vec.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
