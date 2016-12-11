package pl.grzeslowski.chatbox.support;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import pl.grzeslowski.chatbox.support.webdriver.WebDriverFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ScrapRunnable implements Runnable, AutoCloseable {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScrapRunnable.class);
    public static final String SUBTITLES_TYPE = "MicroDVD";

    private final AtomicInteger id;
    private final WebDriverFactory webDriverFactory;

    private final int waitAfterGetLink;
    private final int waitAfterClickDownload;

    private final AtomicBoolean run = new AtomicBoolean(true);

    public ScrapRunnable(AtomicInteger id, WebDriverFactory webDriverFactory, int waitAfterGetLink, int waitAfterClickDownload) {
        this.id = id;
        this.webDriverFactory = webDriverFactory;
        this.waitAfterGetLink = waitAfterGetLink;
        this.waitAfterClickDownload = waitAfterClickDownload;
    }

    @Override
    public void run() {
        final WebDriver webDriver = webDriverFactory.getObject();
        try {
            do {
                final int processId = id.getAndAdd(-1);
                log.info("Processing ID {}", processId);
                try {
                    webDriver.get(createLink(processId));
                    TimeUnit.SECONDS.sleep(waitAfterGetLink);
                    webDriver.findElement(By.linkText(SUBTITLES_TYPE)).click();
                    TimeUnit.SECONDS.sleep(waitAfterClickDownload);

                } catch (Exception e) {
                    log.warn("Got error while downloading subtitles with ID {}. Error message: {}.",
                            processId, e.getMessage());
                }
                id.updateAndGet(operand -> {
                    if(operand > 0) {
                        return operand;
                    } else {
                        log.info("Went down to 0!");
                        return 100_000;
                    }
                });
            } while (run.get());
        } finally {
            webDriver.quit();
        }

    }

    private String createLink(int id) {
        return "http://napisy24.pl/download?napisId=" + id + "&typ=mdvd";
    }

    @Override
    public void close() {
        log.info("Closing runnable...");
        run.set(false);
    }
}
