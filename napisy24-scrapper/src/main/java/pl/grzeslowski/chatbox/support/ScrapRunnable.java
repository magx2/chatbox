package pl.grzeslowski.chatbox.support;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import pl.grzeslowski.chatbox.support.webdriver.WebDriverFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class ScrapRunnable implements Runnable, AutoCloseable {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ScrapRunnable.class);
    private static final String SUBTITLES_TYPE = "MicroDVD";

    private final AtomicInteger id;
    private final WebDriverFactory webDriverFactory;

    private final int waitAfterGetLink;
    private final int waitAfterClickDownload;

    private final AtomicBoolean run = new AtomicBoolean(true);

    ScrapRunnable(AtomicInteger id, WebDriverFactory webDriverFactory, int waitAfterGetLink, int waitAfterClickDownload) {
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
                    findSubtitlesLink(webDriver).ifPresent(clickSubtitlesLink());
                } catch (Exception e) {
                    log.warn("Got error while downloading subtitles with ID {}. Error message: {}.",
                            processId, e.getMessage());
                }
                id.updateAndGet(operand -> {
                    if (operand > 0) {
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
        log.info("Thread has ended");
    }

    private Consumer<WebElement> clickSubtitlesLink() {
        return element -> {
            element.click();
            try {
                TimeUnit.SECONDS.sleep(waitAfterClickDownload);
            } catch (InterruptedException e) {
                // ignore
            }
        };
    }

    private Optional<WebElement> findSubtitlesLink(WebDriver webDriver) {
        WebDriverWait waiter = new WebDriverWait(webDriver, waitAfterGetLink);
        final By subtitlesLink = By.linkText(SUBTITLES_TYPE);
        waiter.ignoring(NoSuchElementException.class);
        try {
            return Optional.of(waiter.until(presenceOfElementLocated(subtitlesLink)));
        } catch (org.openqa.selenium.TimeoutException e) {
            return Optional.empty();
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
