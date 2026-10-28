package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboardPage extends BasePage<UserDashboardPage> {

    public static final String WELCOME_PREFIX = "Welcome, ";
    public static final String DEFAULT_NAME = "noname!";

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccountButton = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboardPage createNewAccount() {
        createNewAccountButton.click();
        return this;
    }

    public String checkAlertMessageAndAccept() {
        Alert alert = getWait().until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        alert.accept();
        return alertText;
    }

    public DepositMoneyPage clickDepositMoneyButton() {
        depositMoneyButton.click();
        return new DepositMoneyPage();
    }
}
