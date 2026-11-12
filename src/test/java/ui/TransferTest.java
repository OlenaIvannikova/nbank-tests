package ui;

import static common.generators.RandomData.generateTransferAmount;
import static ui.pages.AlertMessage.*;

import api.models.AccountResponse;
import api.models.TransferAmount;
import api.requests.steps.UserSteps;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.helpers.StepLogger;
import common.storage.SessionStorage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;
import ui.pages.TransferPage;

@APIVersion("with_database")
public class TransferTest extends BaseUITest {

  @Test
  @UserSession(value = 2)
  public void userCanTransferMoneyToAnotherUserAccountTest() {
    UserSteps senderSteps = SessionStorage.getSteps(1);
    UserSteps receiverSteps = SessionStorage.getSteps(2);

    AccountResponse senderAccount =
        StepLogger.log(
            "Create sender account with non-zero balance",
            () -> senderSteps.createAccountWithNonZeroBalance());
    AccountResponse receiverAccount =
        StepLogger.log("Create receiver account", () -> receiverSteps.createAccount());

    // Зафиксировали баланс отправителя и получателя ДО перевода
    BigDecimal senderBalanceBefore = senderAccount.getBalance();
    BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

    BigDecimal amount = generateTransferAmount(senderBalanceBefore);
    StepLogger.log(
        "User transfers money to another user's account through UI",
        () ->
            new TransferPage()
                .open()
                .transferMoney(
                    senderAccount.getAccountNumber(),
                    receiverAccount.getAccountNumber(),
                    amount.toPlainString())
                .checkAlertMessageAndAccept(
                    TRANSFER_SUCCESSFUL.format(
                        amount.toPlainString(), receiverAccount.getAccountNumber())));

    BigDecimal senderBalanceAfter =
        StepLogger.log(
            "Get sender account balance after transfer",
            () -> senderSteps.getAccountById(senderAccount.getId()).getBalance());
    BigDecimal receiverBalanceAfter =
        StepLogger.log(
            "Get receiver account balance after transfer",
            () -> receiverSteps.getAccountById(receiverAccount.getId()).getBalance());

    // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
    StepLogger.log(
        "Verify that sender balance decreased by the transfer amount",
        () ->
            softly
                .assertThat(senderBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(
                    senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP)));

    // Проверили, что баланс получателя увеличился ровно на сумму трансфера
    StepLogger.log(
        "Verify that receiver balance increased by the transfer amount",
        () ->
            softly
                .assertThat(receiverBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(
                    receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP)));
  }

  @Test
  @UserSession(value = 2)
  public void userCannotTransferMoneyWithInvalidAmountTest() {
    UserSteps senderSteps = SessionStorage.getSteps(1);
    UserSteps receiverSteps = SessionStorage.getSteps(2);

    AccountResponse senderAccount =
        StepLogger.log(
            "Create sender account with non-zero balance",
            () -> senderSteps.createAccountWithNonZeroBalance());
    AccountResponse receiverAccount =
        StepLogger.log("Create receiver account", () -> receiverSteps.createAccount());

    BigDecimal senderBalanceBefore = senderAccount.getBalance();
    BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

    BigDecimal amount = TransferAmount.ABOVE_MAXIMUM.getAmount();
    StepLogger.log(
        "User attempts to transfer an amount exceeding the maximum limit through UI",
        () ->
            new TransferPage()
                .open()
                .transferMoney(
                    senderAccount.getAccountNumber(),
                    receiverAccount.getAccountNumber(),
                    amount.toPlainString())
                .checkAlertMessageAndAccept(TRANSFER_AMOUNT_EXCEEDS_LIMIT.getValue()));

    BigDecimal senderBalanceAfter =
        StepLogger.log(
            "Get sender account balance after rejected transfer",
            () -> senderSteps.getAccountById(senderAccount.getId()).getBalance());
    BigDecimal receiverBalanceAfter =
        StepLogger.log(
            "Get receiver account balance after rejected transfer",
            () -> receiverSteps.getAccountById(receiverAccount.getId()).getBalance());

    StepLogger.log(
        "Verify that sender balance has not changed",
        () -> softly.assertThat(senderBalanceAfter).isEqualByComparingTo(senderBalanceBefore));
    StepLogger.log(
        "Verify that receiver balance has not changed",
        () -> softly.assertThat(receiverBalanceAfter).isEqualByComparingTo(receiverBalanceBefore));
  }

  @Test
  @UserSession
  public void userCannotTransferMoneyWithoutSelectedRecipientAccountTest() {
    UserSteps senderSteps = SessionStorage.getSteps();
    AccountResponse senderAccount =
        StepLogger.log(
            "Create sender account with non-zero balance",
            () -> senderSteps.createAccountWithNonZeroBalance());
    BigDecimal senderBalanceBefore = senderAccount.getBalance();

    BigDecimal amount = generateTransferAmount(senderAccount.getBalance());
    StepLogger.log(
        "User attempts to transfer money without selecting recipient account through UI",
        () ->
            new TransferPage()
                .open()
                .transferMoneyWithoutSelectedRecipientAccount(
                    senderAccount.getAccountNumber(), amount.toPlainString())
                .checkAlertMessageAndAccept(RECIPIENT_ACCOUNT_BLANK.getValue()));

    BigDecimal senderBalanceAfter =
        StepLogger.log(
            "Get sender account balance after rejected transfer",
            () -> senderSteps.getAccountById(senderAccount.getId()).getBalance());

    StepLogger.log(
        "Verify that sender balance has not changed",
        () -> softly.assertThat(senderBalanceAfter).isEqualByComparingTo(senderBalanceBefore));
  }

  @Test
  @UserSession
  public void userCannotTransferMoneyToNonExistentAccountTest() {
    UserSteps senderSteps = SessionStorage.getSteps();
    AccountResponse senderAccount =
        StepLogger.log(
            "Create sender account with non-zero balance",
            () -> senderSteps.createAccountWithNonZeroBalance());
    BigDecimal senderBalanceBefore = senderAccount.getBalance();

    Long nonExistentAccountId = Long.MAX_VALUE;
    BigDecimal amount = generateTransferAmount(senderAccount.getBalance());
    StepLogger.log(
        "User attempts to transfer money to a non-existent account through UI",
        () ->
            new TransferPage()
                .open()
                .transferMoney(
                    senderAccount.getAccountNumber(),
                    String.valueOf(nonExistentAccountId),
                    amount.toPlainString())
                .checkAlertMessageAndAccept(NO_USER_FOUND_WITH_ACCOUNT_NUMBER.getValue()));

    BigDecimal senderBalanceAfter =
        StepLogger.log(
            "Get sender account balance after rejected transfer",
            () -> senderSteps.getAccountById(senderAccount.getId()).getBalance());

    StepLogger.log(
        "Verify that sender balance has not changed",
        () -> softly.assertThat(senderBalanceAfter).isEqualByComparingTo(senderBalanceBefore));
  }
}
