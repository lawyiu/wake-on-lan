import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.NoSuchWindowException;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsStartScreenRecordingOptions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.Base64;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.lawyiu.wake_on_lan.WakeOnLanApp;
import io.github.lawyiu.wake_on_lan.WakeOnLanApp.SendOptions;
import io.github.lawyiu.wake_on_lan.MessageBox;

class TestWakeOnLanApp {
    protected static AppiumDriverLocalService service;
    protected static WindowsDriver driver;
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

        driver.startRecordingScreen(new WindowsStartScreenRecordingOptions().disableForcedRestart());

        app = new WakeOnLanApp(driver);
    }

    @Test
    void clickingCloseButtonInTitleBarShouldCloseApp() {
        app.clickTitleBarCloseButton();

        assertThrows(NoSuchWindowException.class, () -> app.getTitle());
    }

    @Test
    void clickingCloseButtonShouldCloseApp() {
        app.clickCloseButton();

        assertThrows(NoSuchWindowException.class, () -> app.getTitle());
    }

    @ParameterizedTest(name = "loopbackTest({arguments})")
    @ValueSource(ints = {0, 7, 8, 9, 65535})
    void loopbackTest(int port) {
        String loopbackIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        String expReceiveText = "Packet received";

        // For testing default port as well
        if (port != 0) {
            app.setReceivePort(port);
        }
        app.startReceiving();

        app.fillInIPfield(loopbackIP);
        app.fillInMACfield(testMAC);

        // For testing default port as well
        if (port != 0) {
            app.fillInPortField(port);
        }

        app.clickSendButton();

        String outputText = app.getReceiveOutputText();
        assertTrue(outputText.contains(expReceiveText),
                String.format("Receive output did not contain \"%s\". Output text is \"%s\".", expReceiveText,
                        outputText));

        String expReceiveMAC = testMAC.replace(":", " ");
        assertTrue(outputText.contains(expReceiveMAC),
                String.format("Receive output did not contain \"%s\". Output text is \"%s\".", expReceiveMAC,
                        outputText));
    }

    @Test
    void clearButtonShouldClearReceiveOutput() {
        String loopbackIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        String expReceiveText = "Packet received";
        int testPort = 9;

        app.startReceiving();

        SendOptions sendOptions = new SendOptions(loopbackIP, testMAC, testPort);
        app.send(sendOptions);

        String outputText = app.getReceiveOutputText();
        assertTrue(outputText.contains(expReceiveText),
                String.format("Receive output did not contain \"%s\". Output text is \"%s\".", expReceiveText,
                        outputText));

        app.clickReceiveOutputClearButton();

        outputText = app.getReceiveOutputText();
        assertTrue(outputText.isEmpty());
    }

    @Test
    void emptySendShouldShowErrorMessageAlert() {
        app.clickSendButton();

        String appWindowHandle = driver.getWindowHandle();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());

        windowHandles.remove(appWindowHandle);
        driver.switchTo().window(windowHandles.iterator().next());
        MessageBox errorMsgBox = new MessageBox(driver);

        assertEquals("ERROR!", errorMsgBox.getTitle());
        assertEquals("No host name given", errorMsgBox.getMessage());

        errorMsgBox.clickOKbutton();
        driver.switchTo().window(appWindowHandle);
    }

    @Test
    void savingProfileShouldLoadOnNextAppStart() {
        String testProfileName = "profile 0";
        String testIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        int testPort = 8;

        app.fillInProfileField(testProfileName);
        app.fillInIPfield(testIP);
        app.fillInMACfield(testMAC);
        app.fillInPortField(testPort);

        app.clickSaveProfileButton();
        app.clickCloseButton();

        driver.launchApp();
        app = new WakeOnLanApp(driver);

        assertEquals(testProfileName, app.getProfileFieldText());
        assertEquals(testIP, app.getIPfieldText());
        assertEquals(testMAC, app.getMACfieldText());
        assertEquals(testPort, app.getPortFieldNumber());
    }

    @Test
    void deletingProfileShouldResetFields() {
        String testProfileName = "profile 0";
        String testIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        int testPort = 8;

        app.fillInProfileField(testProfileName);
        app.fillInIPfield(testIP);
        app.fillInMACfield(testMAC);
        app.fillInPortField(testPort);

        app.clickSaveProfileButton();
        app.clickDeleteProfileButton();

        assertEquals("", app.getProfileFieldText());
        assertEquals("", app.getIPfieldText());
        assertEquals("", app.getMACfieldText());
        assertEquals(9, app.getPortFieldNumber());
    }

    @Test
    void deletingProfileShouldPersistAfterLaunch() {
        String testProfileName = "profile 0";
        String testIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        int testPort = 8;

        app.fillInProfileField(testProfileName);
        app.fillInIPfield(testIP);
        app.fillInMACfield(testMAC);
        app.fillInPortField(testPort);

        app.clickSaveProfileButton();
        app.clickCloseButton();

        driver.launchApp();
        app = new WakeOnLanApp(driver);

        app.clickDeleteProfileButton();
        app.clickCloseButton();

        driver.launchApp();
        app = new WakeOnLanApp(driver);

        assertEquals("", app.getProfileFieldText());
        assertEquals("", app.getIPfieldText());
        assertEquals("", app.getMACfieldText());
        assertEquals(9, app.getPortFieldNumber());
    }

    @Test
    void shouldBeAbleTodeleteProfileAfterLaunch() {
        String testProfileName = "profile 0";
        String testIP = "127.0.0.1";
        String testMAC = "ab:cd:ef:01:23:45";
        int testPort = 8;

        app.fillInProfileField(testProfileName);
        app.fillInIPfield(testIP);
        app.fillInMACfield(testMAC);
        app.fillInPortField(testPort);

        app.clickSaveProfileButton();
        app.clickCloseButton();

        driver.launchApp();
        app = new WakeOnLanApp(driver);

        app.clickDeleteProfileButton();

        assertEquals("", app.getProfileFieldText());
        assertEquals("", app.getIPfieldText());
        assertEquals("", app.getMACfieldText());
        assertEquals(9, app.getPortFieldNumber());
    }

    @Test
    void switchingProfilesShouldSwitchFieldsContent() {
        String testProfileName0 = "profile 0";
        String testIP0 = "127.0.0.1";
        String testMAC0 = "ab:cd:ef:01:23:45";
        int testPort0 = 8;

        app.fillInProfileField(testProfileName0);
        app.fillInIPfield(testIP0);
        app.fillInMACfield(testMAC0);
        app.fillInPortField(testPort0);

        app.clickSaveProfileButton();

        String testProfileName1 = "profile 1";
        String testIP1 = "127.0.0.2";
        String testMAC1 = "01:23:45:ab:cd:ef";
        int testPort1 = 7;

        app.fillInProfileField(testProfileName1);
        app.fillInIPfield(testIP1);
        app.fillInMACfield(testMAC1);
        app.fillInPortField(testPort1);

        app.clickSaveProfileButton();

        app.selectProfile(testProfileName0);
        assertEquals(testProfileName0, app.getProfileFieldText());
        assertEquals(testIP0, app.getIPfieldText());
        assertEquals(testMAC0, app.getMACfieldText());
        assertEquals(testPort0, app.getPortFieldNumber());

        app.selectProfile(testProfileName1);
        assertEquals(testProfileName1, app.getProfileFieldText());
        assertEquals(testIP1, app.getIPfieldText());
        assertEquals(testMAC1, app.getMACfieldText());
        assertEquals(testPort1, app.getPortFieldNumber());
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws IOException {
        Files.deleteIfExists(settingsPath);

        String vidData = driver.stopRecordingScreen();
        byte[] decodedVid = Base64.getDecoder().decode(vidData);

        FileOutputStream fsOut = new FileOutputStream(testInfo.getDisplayName() + ".mp4");
        fsOut.write(decodedVid);
        fsOut.close();

        driver.quit();
    }

    @AfterAll
    static void tearDownAll() {
        if (service != null) {
            service.stop();
        }
    }
}
