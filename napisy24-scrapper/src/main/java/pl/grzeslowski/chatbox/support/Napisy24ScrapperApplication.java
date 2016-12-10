package pl.grzeslowski.chatbox.support;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import pl.grzeslowski.chatbox.support.webdriver.WebDriverFactory;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@SpringBootApplication
@ComponentScan(basePackageClasses = Napisy24ScrapperApplication.class)
public class Napisy24ScrapperApplication implements CommandLineRunner, AutoCloseable {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Napisy24ScrapperApplication.class);

    @Value("${howManyWebDrivers}")
    private int howManyWebDrivers;
    @Value("${napisy24.startId}")
    private int startId;
    @Value("${delays.waitAfterGetLink}")
    private int waitAfterGetLink;
    @Value("${delays.waitAfterClickDownload}")
    private int waitAfterClickDownload;

    @Autowired
    private WebDriverFactory webDriverFactory;
    private Set<ScrapRunnable> scrapRunnables;

    public static void main(String[] args) {
        SpringApplication.run(Napisy24ScrapperApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        final AtomicInteger id = new AtomicInteger(startId);
        scrapRunnables = Stream.generate(() -> new ScrapRunnable(id, webDriverFactory, waitAfterGetLink, waitAfterClickDownload))
                .limit(howManyWebDrivers)
                .collect(toSet());

        scrapRunnables.stream()
                .map(Thread::new)
                .forEach(thread -> {
                    log.info("Starting thread...");
                    thread.start();
                });
    }

    @Override
    public void close() throws Exception {
        log.info("Closing...");
        scrapRunnables.forEach(ScrapRunnable::close);
    }
}
