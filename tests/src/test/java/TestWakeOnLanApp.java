import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.windows.WindowsDriver;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.io.IOException;

import io.github.lawyiu.wake_on_lan.WakeOnLanApp;

class TestWakeOnLanApp {
    protected static AppiumDriverLocalService service;
    protected static WebDriver driver;
    protected static WakeOnLanApp app;
    protected static final int DEFAULT_PORT = 4723;
    protected static final String DEFAULT_PATH = "c:\\testing\\wake_on_lan\\wake_on_lan.exe";
    protected static Path settingsPath = Path.of(".\\settings.ini");

    @BeforeAll
    static void initAll() {
        AppiumServiceBuilder serviceBuilder = new AppiumServiceBuilder();

        int port = DEFAULT_PORT;
        String envPort = System.getenv("APPIUM_PORT");
        if (envPort != null) {
            port = Integer.parseInt(envPort);
        }

        serviceBuilder.usingPort(port);
        serviceBuilder.withTimeout(Duration.ofSeconds(300));
        service = AppiumDriverLocalService.buildService(serviceBuilder);
        service.start();
    }

    @BeforeEach
    void init() throws IOException {
        Files.deleteIfExists(settingsPath);

        String prog_path = System.getenv("PROG_PATH");
        if (prog_path == null) {
            prog_path = DEFAULT_PATH;
        }

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platformName", "Windows");
        caps.setCapability("appium:automationName", "Windows");
        caps.setCapability("appium:platformVersion", "10");
        caps.setCapability("appium:deviceName", "WindowsPC");
        caps.setCapability("appium:app", prog_path);
        driver = new WindowsDriver(service.getUrl(), caps);
        app = new WakeOnLanApp(driver);
    }

    @Disabled("This test does not pass on CI but passes locally.")
    @Test
    void clickingCloseButtonInTitleBarShouldCloseApp() {
        app.clickTitleBarCloseButton();

        assertThrows(NoSuchWindowException.class, () -> app.getTitle());
    }

    @Disabled("This test does not pass on CI but passes locally.")
    @Test
    void clickingCloseButtonShouldCloseApp() {
        app.clickCloseButton();

        assertThrows(NoSuchWindowException.class, () -> app.getTitle());
    }

    @Test
    void loopbackTest() {
        String loopbackIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        int port = 9;
        String expReceiveText = "Packet received";

        app.startReceiving();

        WakeOnLanApp.SendOptions options = new WakeOnLanApp.SendOptions(loopbackIP, testMAC, port);
        app.send(options);

        String outputText = app.getReceiveOutputText();
        assertTrue(outputText.contains(expReceiveText),
                String.format("Receive output did not contain \"%s\". Output text is \"%s\".", expReceiveText,
                        outputText));

        String expReceiveMAC = testMAC.replace(":", " ");
        assertTrue(outputText.contains(expReceiveMAC),
                String.format("Receive output did not contain \"%s\". Output text is \"%s\".", expReceiveMAC,
                        outputText));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(settingsPath);
        driver.quit();
    }

    @AfterAll
    static void tearDownAll() {
        if (service != null) {
            service.stop();
        }
    }
}
