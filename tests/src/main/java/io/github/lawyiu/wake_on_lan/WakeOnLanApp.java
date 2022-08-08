package io.github.lawyiu.wake_on_lan;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import io.appium.java_client.AppiumBy;

public class WakeOnLanApp {
    protected String appTitle;
    protected final WebDriver driver;

    protected final By byTitleBarCloseBtn = By.xpath("/Window/TitleBar/Button[3]");
    protected final WebElement titleBarCloseBtnElm;

    protected final By byCloseBtn = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.closeButton");
    protected final WebElement closeBtnElm;

    protected final By bySendTab = By.xpath("/Window/Group/Group/Tab/TabItem[1]");
    protected final WebElement sendTabElm;

    protected final By byReceiveTab = By.xpath("/Window/Group/Group/Tab/TabItem[2]");
    protected final WebElement receiveTabElm;

    protected final By byIPfield = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.IP_lineEdit");
    protected final WebElement IPfieldElm;

    protected final By byMACfield = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.MAC_lineEdit");
    protected final WebElement MACfieldElm;

    protected final By byPortfield = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.portLineEdit");
    protected final WebElement portfieldElm;

    protected final By bySendBtn = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.sendButton");
    protected final WebElement sendBtnElm;

    protected final By byStartStopBtn = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_6.startButton");
    protected WebElement startStopBtnElm;

    protected final By byReceiveOutputTextField = AppiumBy.accessibilityId(
            "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_6.packetInfoTextEdit");
    protected WebElement receiveOutputTextField;

    protected final By byProfileComboBox = AppiumBy
            .accessibilityId("MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.profileNameComboBox");
    protected WebElement profileComboBoxElm;

    public static class SendOptions {
        protected String IPaddr;
        protected String MACaddr;
        protected int port;

        public SendOptions(String IPaddr, String MACaddr, int port) {
            this.IPaddr = IPaddr;
            this.MACaddr = MACaddr;
            this.port = port;
        }

        public String getIPaddr() {
            return IPaddr;
        }

        public String getMACaddr() {
            return MACaddr;
        }

        public int getPort() {
            return port;
        }
    }

    public WakeOnLanApp(WebDriver driver) {
        this.driver = driver;
        appTitle = this.getTitle();

        titleBarCloseBtnElm = driver.findElement(byTitleBarCloseBtn);
        closeBtnElm = driver.findElement(byCloseBtn);
        sendTabElm = driver.findElement(bySendTab);
        receiveTabElm = driver.findElement(byReceiveTab);
        IPfieldElm = driver.findElement(byIPfield);
        MACfieldElm = driver.findElement(byMACfield);
        portfieldElm = driver.findElement(byPortfield);
        sendBtnElm = driver.findElement(bySendBtn);
        profileComboBoxElm = driver.findElement(byProfileComboBox);
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public void clickTitleBarCloseButton() {
        titleBarCloseBtnElm.click();
    }

    public void clickCloseButton() {
        sendTabElm.click();
        closeBtnElm.click();
    }

    public void fillInProfileField(String profileName) {
        sendTabElm.click();
        profileComboBoxElm.click();
        profileComboBoxElm.sendKeys(profileName);
    }

    public void fillInIPfield(String IPaddr) {
        sendTabElm.click();
        IPfieldElm.clear();
        IPfieldElm.click();
        IPfieldElm.sendKeys(IPaddr);
    }

    public void fillInMACfield(String MACaddr) {
        sendTabElm.click();
        MACfieldElm.clear();
        MACfieldElm.click();
        MACfieldElm.sendKeys(MACaddr);
    }

    public void fillInPortField(int port) {
        sendTabElm.click();
        portfieldElm.clear();
        portfieldElm.click();
        portfieldElm.sendKeys(String.valueOf(port));
    }

    public void send(SendOptions options) {
        sendTabElm.click();
        fillInIPfield(options.getIPaddr());
        fillInMACfield(options.getMACaddr());
        fillInPortField(options.getPort());
        sendBtnElm.click();
    }

    public void clickSendButton() {
        sendTabElm.click();
        sendBtnElm.click();
    }

    public void startReceiving() {
        receiveTabElm.click();

        if (startStopBtnElm == null) {
            startStopBtnElm = driver.findElement(byStartStopBtn);
        }

        if (startStopBtnElm.getAttribute("Name").equals("Start")) {
            startStopBtnElm.click();
        }
    }

    public void stopReceiving() {
        receiveTabElm.click();

        if (startStopBtnElm == null) {
            startStopBtnElm = driver.findElement(byStartStopBtn);
        }

        if (startStopBtnElm.getAttribute("Name").equals("Stop")) {
            startStopBtnElm.click();
        }
    }

    public String getReceiveOutputText() {
        receiveTabElm.click();

        if (receiveOutputTextField == null) {
            receiveOutputTextField = driver.findElement(byReceiveOutputTextField);
        }

        return receiveOutputTextField.getText();
    }
}
