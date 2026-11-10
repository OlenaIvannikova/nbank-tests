package ui.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;

public class DepositMoneyPage extends BasePage<DepositMoneyPage> {
  private SelenideElement depositMoneyButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));

  @Override
  public String url() {
    return "/deposit";
  }

  public DepositMoneyPage depositMoney(String accountNumber, String balance) {
    RetryUtils.retry(
        () -> {
          selectAccountBox.selectOptionContainingText(accountNumber);
          return selectAccountBox.getSelectedOption().getText().contains(accountNumber);
        },
        selected -> selected,
        5,
        500);
    amountInput.clear();
    amountInput.sendKeys(balance);
    depositMoneyButton.click();
    return this;
  }

  public DepositMoneyPage depositMoneyWithoutAmount(String accountNumber) {
    RetryUtils.retry(
        () -> {
          selectAccountBox.selectOptionContainingText(accountNumber);
          return selectAccountBox.getSelectedOption().getText().contains(accountNumber);
        },
        selected -> selected,
        5,
        500);
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
