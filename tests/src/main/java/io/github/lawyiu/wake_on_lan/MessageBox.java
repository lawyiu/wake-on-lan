package io.github.lawyiu.wake_on_lan;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.pagefactory.WindowsFindBy;

public class MessageBox {
    WebDriver driver;

    @WindowsFindBy(accessibility = "qt_msgbox_label")
    protected WebElement msgboxLabelElm;

    @FindBy(name = "OK")
    protected WebElement msgboxBtnElm;

    @FindBy(xpath = "/Window/TitleBar/Button")
    protected WebElement titleBarCloseBtnElm;

    public MessageBox(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public void clickTitleBarCloseButton() {
        titleBarCloseBtnElm.click();
    }

    public void clickOKbutton() {
        msgboxBtnElm.click();
    }

    public String getMessage() {
        return msgboxLabelElm.getText();
    }
}
