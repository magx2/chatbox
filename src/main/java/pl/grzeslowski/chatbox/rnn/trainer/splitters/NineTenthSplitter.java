package pl.grzeslowski.chatbox.rnn.trainer.splitters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;


@Service
class NineTenthSplitter implements TestSetSplitter {
    private static final Logger log = LoggerFactory.getLogger(NineTenthSplitter.class);
    private Random random;

    @Autowired
    public NineTenthSplitter(Random random) {
        this.random = random;
    }

    @Override
    public <T> LearningSets<T> splitIntoSets(List<T> all) {
        log.info("Splitting 9/10");
        Collections.shuffle(all, random);
        int splitPoint = all.size() * 9 / 10;
        return new LearningSets<>(
                all.subList(0, splitPoint).stream(),
                all.subList(splitPoint, all.size()).stream()
        );
    }
}
