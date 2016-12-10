package pl.grzeslowski.chatbox.support.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Service
public class WebDriverFactory implements FactoryBean<WebDriver> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(WebDriverFactory.class);

    @Value("${napisy24.log.email}")
    private String email;
    @Value("${napisy24.log.password}")
    private String password;
    @Value("${delays.waitAfterMainPage}")
    private int waitAfterMainPage;
    @Value("${delays.waitAfterSubmitLogin}")
    private int waitAfterSubmitLogin;

    @Autowired
    public WebDriverFactory(
            @Value("${driverPath}") String driverPath
    ) {
        final File chromedriver = new File(driverPath);
        final String chromedriverAbsolutePath = chromedriver.getAbsolutePath();
        if (!chromedriver.exists()) {
            throw new IllegalStateException(format("Cannot find chromedriver in path %s!", chromedriverAbsolutePath));
        }
        log.info("webdriver.chrome.driver => {}", chromedriverAbsolutePath);
        System.setProperty("webdriver.chrome.driver", chromedriverAbsolutePath);
    }

    @Override
    public WebDriver getObject() {
        WebDriver driver = new ChromeDriver();
        logIntoNapisy24(driver);
        return driver;
    }

    private void logIntoNapisy24(WebDriver driver) {
        try {
            driver.get("http://napisy24.pl");
            TimeUnit.SECONDS.sleep(waitAfterMainPage);
            driver.findElement(By.name("username")).sendKeys(email);
            driver.findElement(By.name("passwd")).sendKeys(password);
            driver.findElement(By.id("login-form")).submit();
            TimeUnit.SECONDS.sleep(waitAfterSubmitLogin);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return WebDriver.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
