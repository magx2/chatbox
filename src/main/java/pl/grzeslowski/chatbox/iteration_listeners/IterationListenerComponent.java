package pl.grzeslowski.chatbox.iteration_listeners;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
class IterationListenerComponent {

    @Value("${iterationListeners.fileStatsStorage}")
    private File fileStatsStorage;
    @Value("${iterationListeners.printIterations}")
    private int printIterations;

    @Bean
    StatsListener statsListener() {
        UIServer uiServer = UIServer.getInstance();
//        fileStatsStorage.delete();
        StatsStorage statsStorage = new FileStatsStorage(fileStatsStorage);
        uiServer.attach(statsStorage);
        return new StatsListener(statsStorage);
    }

    @Bean
    ScoreIterationListener scoreIterationListener() {
        return new ScoreIterationListener(printIterations);
    }
}
