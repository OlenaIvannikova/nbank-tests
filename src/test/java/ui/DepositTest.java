package ui;

import static ui.pages.AlertMessage.*;

import api.assertions.TransactionAssertions;
import api.models.AccountResponse;
import api.models.DepositBalance;
import api.models.Transaction;
import api.models.TransactionType;
import api.requests.steps.UserSteps;
import base.BaseUITest;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.helpers.StepLogger;
import common.storage.SessionStorage;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import ui.pages.UserDashboardPage;

@APIVersion("with_database")
public class DepositTest extends BaseUITest {

  @Test
  @UserSession
  public void userCanDepositMoneyIntoAccountTest() {
    UserSteps userSteps = SessionStorage.getSteps();
    AccountResponse account =
        StepLogger.log("Create a new account", () -> userSteps.createAccount());
    BigDecimal accountBalanceBefore = account.getBalance();

    String balance = DepositBalance.ALLOWED.getBalance().toPlainString();
    StepLogger.log(
        "User deposits money into account through UI",
        () ->
            new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoney(account.getAccountNumber(), balance)
                .checkAlertMessageAndAccept(
                    ACCOUNT_DEPOSITED.format(balance, account.getAccountNumber())));

    BigDecimal accountBalanceAfter =
        StepLogger.log(
            "Get account balance after deposit through API",
            () -> userSteps.getAccountById(account.getId()).getBalance());
    StepLogger.log(
        "Verify that account balance was increased by the deposited amount",
        () ->
            softly
                .assertThat(accountBalanceBefore.add(DepositBalance.ALLOWED.getBalance()))
                .isEqualByComparingTo(accountBalanceAfter));

    List<Transaction> accountTransactions =
        StepLogger.log(
            "Get account transactions through API",
            () -> userSteps.getAccountTransactions(account.getId()));
    StepLogger.log(
        "Verify that deposit transaction exists",
        () ->
            TransactionAssertions.assertTransactionExists(
                softly,
                accountTransactions,
                DepositBalance.ALLOWED.getBalance(),
                TransactionType.DEPOSIT,
                account.getId()));
  }

  @Test
  @UserSession
  public void userCannotDepositMoneyWithInvalidBalanceTest() {
    UserSteps userSteps = SessionStorage.getSteps();
    AccountResponse account =
        StepLogger.log("Create a new account", () -> userSteps.createAccount());

    BigDecimal accountBalanceBefore = account.getBalance();
    List<Transaction> transactionsBefore =
        StepLogger.log(
            "Get account transactions before deposit",
            () -> userSteps.getAccountTransactions(account.getId()));

    String balance = DepositBalance.ABOVE_MAXIMUM.getBalance().toPlainString();
    StepLogger.log(
        "User attempts to deposit an amount exceeding the maximum limit through UI",
        () ->
            new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoney(account.getAccountNumber(), balance)
                .checkAlertMessageAndAccept(DEPOSIT_AMOUNT_EXCEEDS_LIMIT.getValue()));

    // баланс счета не должен измениться
    BigDecimal accountBalanceAfter =
        StepLogger.log(
            "Get account balance after rejected deposit",
            () -> userSteps.getAccountById(account.getId()).getBalance());
    StepLogger.log(
        "Verify that account balance has not changed",
        () -> softly.assertThat(accountBalanceBefore).isEqualByComparingTo(accountBalanceAfter));

    // отклонённый депозит не должен создавать никаких транзакций
    List<Transaction> transactionsAfter =
        StepLogger.log(
            "Get account transactions after rejected deposit",
            () -> userSteps.getAccountTransactions(account.getId()));

    StepLogger.log(
        "Verify that rejected deposit did not create any transactions",
        () -> softly.assertThat(transactionsAfter).containsExactlyElementsOf(transactionsBefore));
  }

  @Test
  @UserSession
  public void userCannotDepositMoneyWithBlankBalanceTest() {
    UserSteps userSteps = SessionStorage.getSteps();
    AccountResponse account =
        StepLogger.log("Create a new account", () -> userSteps.createAccount());

    BigDecimal accountBalanceBefore = account.getBalance();
    List<Transaction> transactionsBefore =
        StepLogger.log(
            "Get account transactions before deposit",
            () -> userSteps.getAccountTransactions(account.getId()));

    StepLogger.log(
        "User attempts to deposit money with blank amount through UI",
        () ->
            new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoneyWithoutAmount(account.getAccountNumber())
                .checkAlertMessageAndAccept(DEPOSIT_AMOUNT_BLANK.getValue()));

    // баланс счета не должен измениться
    BigDecimal accountBalanceAfter =
        StepLogger.log(
            "Get account balance after rejected deposit",
            () -> userSteps.getAccountById(account.getId()).getBalance());
    StepLogger.log(
        "Verify that account balance has not changed",
        () -> softly.assertThat(accountBalanceBefore).isEqualByComparingTo(accountBalanceAfter));

    // отклонённый депозит не должен создавать никаких транзакций
    List<Transaction> transactionsAfter =
        StepLogger.log(
            "Get account transactions after rejected deposit",
            () -> userSteps.getAccountTransactions(account.getId()));
    StepLogger.log(
        "Verify that rejected deposit did not create any transactions",
        () -> softly.assertThat(transactionsAfter).containsExactlyElementsOf(transactionsBefore));
  }

  @Test
  @UserSession
  public void userCannotDepositMoneyWithoutSelectedAccountTest() {
    UserSteps userSteps = SessionStorage.getSteps();
    AccountResponse account =
        StepLogger.log("Create a new account", () -> userSteps.createAccount());

    BigDecimal accountBalanceBefore = account.getBalance();
    List<Transaction> transactionsBefore =
        StepLogger.log(
            "Get account transactions before deposit",
            () -> userSteps.getAccountTransactions(account.getId()));

    String balance = DepositBalance.ALLOWED.getBalance().toPlainString();
    StepLogger.log(
        "User attempts to deposit money without selecting an account through UI",
        () ->
            new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoneyWithoutAccount(balance)
                .checkAlertMessageAndAccept(NO_ACCOUNT_SELECTED.getValue()));

    // баланс счета не должен измениться
    BigDecimal accountBalanceAfter =
        StepLogger.log(
            "Get account balance after rejected deposit",
            () -> userSteps.getAccountById(account.getId()).getBalance());
    StepLogger.log(
        "Verify that account balance has not changed",
        () -> softly.assertThat(accountBalanceBefore).isEqualByComparingTo(accountBalanceAfter));

    // отклонённый депозит не должен создавать никаких транзакций
    List<Transaction> transactionsAfter =
        StepLogger.log(
            "Get account transactions after rejected deposit",
            () -> userSteps.getAccountTransactions(account.getId()));
    StepLogger.log(
        "Verify that rejected deposit did not create any transactions",
        () -> softly.assertThat(transactionsAfter).containsExactlyElementsOf(transactionsBefore));
  }
}
