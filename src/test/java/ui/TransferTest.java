package ui;

import base.BaseUITest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import api.models.AccountResponse;
import api.models.TransferAmount;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.TransferPage;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static common.generators.RandomData.generateTransferAmount;
import static ui.pages.AlertMessage.*;

public class TransferTest extends BaseUITest {

    @Test
    @UserSession(value = 2)
    public void userCanTransferMoneyToAnotherUserAccountTest() {
        UserSteps senderSteps = SessionStorage.getSteps(1);
        UserSteps receiverSteps = SessionStorage.getSteps(2);

        AccountResponse senderAccount = senderSteps.createAccountWithNonZeroBalance();
        AccountResponse receiverAccount = receiverSteps.createAccount();

        // Зафиксировали баланс отправителя и получателя ДО перевода
        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        BigDecimal amount = generateTransferAmount(senderBalanceBefore);
        new TransferPage()
                .open()
                .transferMoney(senderAccount.getAccountNumber(), receiverAccount.getAccountNumber(), amount.toPlainString())
                .checkAlertMessageAndAccept(TRANSFER_SUCCESSFUL.format(
                        amount.toPlainString(),
                        receiverAccount.getAccountNumber()));

        BigDecimal senderBalanceAfter = senderSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = receiverSteps.getAccountById(receiverAccount.getId()).getBalance();

        // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
        softly.assertThat(senderBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP));

        //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
        softly.assertThat(receiverBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @UserSession(value = 2)
    public void userCannotTransferMoneyWithInvalidAmountTest() {
        UserSteps senderSteps = SessionStorage.getSteps(1);
        UserSteps receiverSteps = SessionStorage.getSteps(2);

        AccountResponse senderAccount = senderSteps.createAccountWithNonZeroBalance();
        AccountResponse receiverAccount = receiverSteps.createAccount();

        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        BigDecimal amount = TransferAmount.ABOVE_MAXIMUM.getAmount();
        new TransferPage()
                .open()
                .transferMoney(senderAccount.getAccountNumber(), receiverAccount.getAccountNumber(), amount.toPlainString())
                .checkAlertMessageAndAccept(TRANSFER_AMOUNT_EXCEEDS_LIMIT.getValue());

        BigDecimal senderBalanceAfter = senderSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = receiverSteps.getAccountById(receiverAccount.getId()).getBalance();

        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore);
        softly.assertThat(receiverBalanceAfter)
                .isEqualByComparingTo(receiverBalanceBefore);
    }


    @Test
    @UserSession
    public void userCannotTransferMoneyWithoutSelectedRecipientAccountTest() {
        UserSteps senderSteps = SessionStorage.getSteps();
        AccountResponse senderAccount = senderSteps.createAccountWithNonZeroBalance();
        BigDecimal senderBalanceBefore = senderAccount.getBalance();

        BigDecimal amount = generateTransferAmount(senderAccount.getBalance());
        new TransferPage()
                .open()
                .transferMoneyWithoutSelectedRecipientAccount(senderAccount.getAccountNumber(), amount.toPlainString())
                .checkAlertMessageAndAccept(RECIPIENT_ACCOUNT_BLANK.getValue());

        BigDecimal senderBalanceAfter = senderSteps.getAccountById(senderAccount.getId()).getBalance();
        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore);
    }

    @Test
    @UserSession
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        UserSteps senderSteps = SessionStorage.getSteps();
        AccountResponse senderAccount = senderSteps.createAccountWithNonZeroBalance();
        BigDecimal senderBalanceBefore = senderAccount.getBalance();

        Long nonExistentAccountId = Long.MAX_VALUE;
        BigDecimal amount = generateTransferAmount(senderAccount.getBalance());
        new TransferPage()
                .open()
                .transferMoney(senderAccount.getAccountNumber(), String.valueOf(nonExistentAccountId), amount.toPlainString())
                .checkAlertMessageAndAccept(NO_USER_FOUND_WITH_ACCOUNT_NUMBER.getValue());

        BigDecimal senderBalanceAfter = senderSteps.getAccountById(senderAccount.getId()).getBalance();
        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore);
    }

}
