package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
    private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage depositMoney(String accountNumber, String balance) {
        selectAccountBox.selectOptionContainingText(accountNumber);
        amountInput.clear();
        amountInput.sendKeys(balance);
        depositMoneyButton.click();
        return this;
    }

    public DepositMoneyPage depositMoneyWithoutAmount(String accountNumber) {
        selectAccountBox.selectOptionContainingText(accountNumber);
        amountInput.clear();
        depositMoneyButton.click();
        return this;
    }

    public DepositMoneyPage depositMoneyWithoutAccount(String balance) {
        amountInput.clear();
        depositMoneyButton.click();
        return this;
    }

}
