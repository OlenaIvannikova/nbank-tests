package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboardPage extends BasePage<UserDashboardPage> {

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

    public DepositMoneyPage clickDepositMoneyButton() {
        depositMoneyButton.click();
        return new DepositMoneyPage();
    }
}
