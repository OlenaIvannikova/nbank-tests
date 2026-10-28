package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    private SelenideElement recipientAccountInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement confirmCheckBox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage transferMoney(String senderAccountNumber, String recipientAccountNumber, String amount) {
        RetryUtils.retry(
                () -> {
                    selectAccountBox.selectOptionContainingText(senderAccountNumber);
                    return selectAccountBox.getSelectedOption().getText()
                            .contains(senderAccountNumber);
                },
                selected -> selected,
                5,
                1000
        );
        recipientAccountInput.sendKeys(recipientAccountNumber);
        amountInput.clear();
        amountInput.sendKeys(amount);
        if (!confirmCheckBox.isSelected()) {
            confirmCheckBox.click();
        }
        sendTransferButton.click();
        return this;
    }

    public TransferPage transferMoneyWithoutSelectedRecipientAccount(String senderAccountNumber, String amount) {
        RetryUtils.retry(
                () -> {
                    selectAccountBox.selectOptionContainingText(senderAccountNumber);
                    return selectAccountBox.getSelectedOption().getText()
                            .contains(senderAccountNumber);
                },
                selected -> selected,
                5,
                2000
        );
        recipientAccountInput.clear();
        amountInput.clear();
        amountInput.sendKeys(amount);
        if (!confirmCheckBox.isSelected()) {
            confirmCheckBox.click();
        }
        sendTransferButton.click();
        return this;
    }
}
