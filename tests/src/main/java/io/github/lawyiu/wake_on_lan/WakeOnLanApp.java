package io.github.lawyiu.wake_on_lan;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.Keys;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.WindowsFindBy;

public class WakeOnLanApp {
    protected String appTitle;
    protected final WebDriver driver;

    @FindBy(xpath= "/Window/TitleBar/Button[3]")
    protected WebElement titleBarCloseBtnElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.closeButton")
    protected WebElement closeBtnElm;

    @FindBy(xpath= "/Window/Group/Group/Tab/TabItem[1]")
    protected WebElement sendTabElm;

    @FindBy(xpath= "/Window/Group/Group/Tab/TabItem[2]")
    protected WebElement receiveTabElm;

    // Send page

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.IP_lineEdit")
    protected WebElement IPfieldElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.MAC_lineEdit")
    protected WebElement MACfieldElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.portLineEdit")
    protected WebElement portfieldElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.sendButton")
    protected WebElement sendBtnElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.profileNameComboBox")
    protected WebElement profileComboBoxElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.profileSaveButton")
    protected WebElement saveProfileBtnElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_5.profileDeleteButton")
    protected WebElement deleteProfileBtnElm;

    // Receive page

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_6.startButton")
    protected WebElement startStopBtnElm;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_6.packetInfoTextEdit")
    protected WebElement receiveOutputTextField;

    @WindowsFindBy(accessibility = "MainWindow.centralWidget.tabWidget.qt_tabwidget_stackedwidget.tab_6.receivePortLineEdit")
    protected WebElement receivePortFieldElm;

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

        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
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
        profileComboBoxElm.sendKeys(Keys.CONTROL + "a" + Keys.BACK_SPACE);
        profileComboBoxElm.sendKeys(profileName);
    }

    public String getProfileFieldText() {
        return profileComboBoxElm.getText();
    }

    public void resetProfileFieldToFirstItem() {
        sendTabElm.click();
        profileComboBoxElm.click();

        String prevText;
        String currText = getProfileFieldText();

        do {
            prevText = currText;
            profileComboBoxElm.sendKeys(Keys.UP);
            currText = getProfileFieldText();
        } while (!currText.equals(prevText));
    }

    public void selectProfile(String profile_text) {
        resetProfileFieldToFirstItem();

        String prevText;
        String currText = getProfileFieldText();

        while (!profile_text.equals(currText)) {
            prevText = currText;
            profileComboBoxElm.sendKeys(Keys.DOWN);
            currText = getProfileFieldText();

            if (currText.equals(prevText)) {
                throw new NotFoundException("Could not find profile: " + profile_text);
            }
        }
    }

    public void selectProfileIndex(int index) {
        resetProfileFieldToFirstItem();

        for (int i = 0; i < index; i++) {
            profileComboBoxElm.sendKeys(Keys.DOWN);
        }
    }

    public void fillInIPfield(String IPaddr) {
        sendTabElm.click();
        IPfieldElm.clear();
        IPfieldElm.click();
        IPfieldElm.sendKeys(IPaddr);
    }

    public String getIPfieldText() {
        return IPfieldElm.getText();
    }

    public void fillInMACfield(String MACaddr) {
        sendTabElm.click();
        MACfieldElm.clear();
        MACfieldElm.click();
        MACfieldElm.sendKeys(MACaddr);
    }

    public String getMACfieldText() {
        return MACfieldElm.getText();
    }

    public void fillInPortField(int port) {
        sendTabElm.click();
        portfieldElm.clear();
        portfieldElm.click();
        portfieldElm.sendKeys(String.valueOf(port));
    }

    public int getPortFieldNumber() {
        return Integer.valueOf(portfieldElm.getText());
    }

    public void clickSaveProfileButton() {
        sendTabElm.click();
        saveProfileBtnElm.click();
    }

    public void clickDeleteProfileButton() {
        sendTabElm.click();
        deleteProfileBtnElm.click();
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

        if (startStopBtnElm.getAttribute("Name").equals("Start")) {
            startStopBtnElm.click();
        }
    }

    public void stopReceiving() {
        receiveTabElm.click();

        if (startStopBtnElm.getAttribute("Name").equals("Stop")) {
            startStopBtnElm.click();
        }
    }

    public String getReceiveOutputText() {
        receiveTabElm.click();

        return receiveOutputTextField.getText();
    }

    public void setReceivePort(int port) {
        receiveTabElm.click();
        receivePortFieldElm.click();
        receivePortFieldElm.clear();
        receivePortFieldElm.sendKeys(String.valueOf(port));
    }

    public int getReceivePort() {
        receiveTabElm.click();
        return Integer.valueOf(receivePortFieldElm.getText());
    }
}
