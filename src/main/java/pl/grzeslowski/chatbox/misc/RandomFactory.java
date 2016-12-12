package pl.grzeslowski.chatbox.misc;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;

public class RandomFactory implements FactoryBean<Random> {

    @Value("${seed}")
    private long seed;

    @Override
    public Random getObject() {
        return new Random(seed);
    }

    @Override
    public Class<?> getObjectType() {
        return Random.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
