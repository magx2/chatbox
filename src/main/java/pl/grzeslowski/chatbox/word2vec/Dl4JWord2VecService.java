package pl.grzeslowski.chatbox.word2vec;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.grzeslowski.chatbox.files.FileReader;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

@Service
class Dl4JWord2VecService implements Word2VecService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Dl4JWord2VecService.class);

    @Value("${seed}")
    private int seed;
    @Value("${dialogParser.pathToSubtitles}")
    private String pathToSubtitles;
    @Value("${word2vec.models.pathToModel}")
    private String pathToModel;
    @Value("${word2vec.models.pathToWordVectors}")
    private String pathToWordVectors;
    @Value("${word2vec.hyper.minWordFrequency}")
    private int minWordFrequency;
    @Value("${word2vec.hyper.iterations}")
    private int iterations;
    @Value("${word2vec.hyper.layerSize}")
    private int layerSize;
    @Value("${word2vec.hyper.windowsSize}")
    private int windowsSize;

    @Autowired
    private FileReader fileReader;

    @Override
    public Word2Vec computeModel() {
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
        log.info("Load & vectorize Sentences...");
        SentenceIterator iterator = new DirSentenceIterator(fileReader, pathToSubtitles);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        log.info("Building model....");
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
        try {
            return Optional.of(WordVectorSerializer.readWord2VecModel(new File(pathToModel)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
