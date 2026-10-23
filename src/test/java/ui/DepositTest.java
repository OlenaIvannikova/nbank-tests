package ui;

import api.assertions.TransactionAssertions;
import api.models.AccountResponse;
import api.models.DepositBalance;
import api.models.Transaction;
import api.models.TransactionType;
import base.BaseUITest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import api.requests.steps.UserSteps;
import ui.pages.UserDashboardPage;

import java.math.BigDecimal;
import java.util.List;

import static ui.pages.AlertMessage.*;

public class DepositTest extends BaseUITest {

    @Test
    @UserSession
    public void userCanDepositMoneyIntoAccountTest() {
        UserSteps userSteps = SessionStorage.getSteps();
        AccountResponse account = userSteps.createAccount();
        BigDecimal accountBalanceBefore = account.getBalance();

        String balance = DepositBalance.ALLOWED.getBalance().toPlainString();
        new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoney(account.getAccountNumber(), balance)
                .checkAlertMessageAndAccept(ACCOUNT_DEPOSITED.format(
                        balance,
                        account.getAccountNumber()
                ));

        BigDecimal accountBalanceAfter = userSteps.getAccountById(account.getId()).getBalance();
        softly.assertThat(accountBalanceBefore.add(DepositBalance.ALLOWED.getBalance()))
                .isEqualByComparingTo(accountBalanceAfter);

        List<Transaction> accountTransactions = userSteps.getAccountTransactions(account.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                accountTransactions,
                DepositBalance.ALLOWED.getBalance(),
                TransactionType.DEPOSIT,
                account.getId()
        );
    }

    @Test
    @UserSession
    public void userCannotDepositMoneyWithInvalidBalanceTest() {
        UserSteps userSteps = SessionStorage.getSteps();
        AccountResponse account = userSteps.createAccount();

        BigDecimal accountBalanceBefore = account.getBalance();
        List<Transaction> transactionsBefore =
                userSteps.getAccountTransactions(account.getId());

        String balance = DepositBalance.ABOVE_MAXIMUM.getBalance().toPlainString();
        new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoney(account.getAccountNumber(), balance)
                .checkAlertMessageAndAccept(DEPOSIT_AMOUNT_EXCEEDS_LIMIT.getValue());

        // баланс счета не должен измениться
        BigDecimal accountBalanceAfter = userSteps.getAccountById(account.getId()).getBalance();
        softly.assertThat(accountBalanceBefore)
                .isEqualByComparingTo(accountBalanceAfter);

        // отклонённый депозит не должен создавать никаких транзакций
        List<Transaction> transactionsAfter =
                userSteps.getAccountTransactions(account.getId());
        softly.assertThat(transactionsAfter)
                .containsExactlyElementsOf(transactionsBefore);
    }

    @Test
    @UserSession
    public void userCannotDepositMoneyWithBlankBalanceTest() {
        UserSteps userSteps = SessionStorage.getSteps();
        AccountResponse account = userSteps.createAccount();

        BigDecimal accountBalanceBefore = account.getBalance();
        List<Transaction> transactionsBefore =
                userSteps.getAccountTransactions(account.getId());

        new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoneyWithoutAmount(account.getAccountNumber())
                .checkAlertMessageAndAccept(DEPOSIT_AMOUNT_BLANK.getValue());

        // баланс счета не должен измениться
        BigDecimal accountBalanceAfter = userSteps.getAccountById(account.getId()).getBalance();
        softly.assertThat(accountBalanceBefore)
                .isEqualByComparingTo(accountBalanceAfter);

        // отклонённый депозит не должен создавать никаких транзакций
        List<Transaction> transactionsAfter =
                userSteps.getAccountTransactions(account.getId());
        softly.assertThat(transactionsAfter)
                .containsExactlyElementsOf(transactionsBefore);
    }

    @Test
    @UserSession
    public void userCannotDepositMoneyWithoutSelectedAccountTest() {
        UserSteps userSteps = SessionStorage.getSteps();
        AccountResponse account = userSteps.createAccount();

        BigDecimal accountBalanceBefore = account.getBalance();
        List<Transaction> transactionsBefore =
                userSteps.getAccountTransactions(account.getId());

        String balance = DepositBalance.ALLOWED.getBalance().toPlainString();
        new UserDashboardPage()
                .open()
                .clickDepositMoneyButton()
                .depositMoneyWithoutAccount(balance)
                .checkAlertMessageAndAccept(NO_ACCOUNT_SELECTED.getValue());

        // баланс счета не должен измениться
        BigDecimal accountBalanceAfter = userSteps.getAccountById(account.getId()).getBalance();
        softly.assertThat(accountBalanceBefore)
                .isEqualByComparingTo(accountBalanceAfter);

        // отклонённый депозит не должен создавать никаких транзакций
        List<Transaction> transactionsAfter =
                userSteps.getAccountTransactions(account.getId());
        softly.assertThat(transactionsAfter)
                .containsExactlyElementsOf(transactionsBefore);
    }
}
