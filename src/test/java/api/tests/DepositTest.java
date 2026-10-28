package api.tests;

import api.assertions.AccountAssertions;
import api.assertions.TransactionAssertions;
import api.models.*;
import base.BaseTest;
import common.generators.RandomModelGenerator;
import common.utils.RetryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static api.requests.steps.AdminSteps.createTwoDifferentUsers;

public class DepositTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("api.testdata.DepositValidationTestData#validDepositBalance")
    public void userCanDepositMoneyIntoAccountTest(BigDecimal balance) {

        UserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        AccountResponse account = userSteps.createAccount();

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(account.getId());
        deposit.setBalance(balance);

        AccountResponse accountDetails = userSteps.depositMoney(
                deposit,
                ResponseSpecs.requestReturnsOK()
        ).as(AccountResponse.class);

        AccountAssertions.assertDepositSuccessful(
                softly,
                accountDetails,
                deposit.getBalance(),
                account.getId()
        );

        List<Transaction> accountTransactions =
                userSteps.getAccountTransactions(account.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                accountTransactions,
                deposit.getBalance(),
                TransactionType.DEPOSIT,
                account.getId()
        );
    }

    @Test
    public void userCannotDepositMoneyIntoNonExistentAccountTest() {
        UserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        long nonExistentAccountId = Long.MAX_VALUE;

        // проверяем что такого аккаунта точно нет в системе
        List<AccountResponse> userAccounts = RetryUtils.retry(
                userSteps::getAccounts,
                accounts -> accounts != null && accounts.stream()
                        .noneMatch(a -> a.getId().equals(nonExistentAccountId)),
                5,
                1000
        );
        assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .doesNotContain(nonExistentAccountId);

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(nonExistentAccountId);
        userSteps.depositMoney(deposit, ResponseSpecs.accountAccessForbidden());

        Endpoint endpoint = Endpoint.ACCOUNT_TRANSACTIONS;
        String paramName = endpoint.getPathParamNames().getFirst();
        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                endpoint,
                ResponseSpecs.accountAccessDenied())
                .getAllByPathParams(Map.of(paramName, nonExistentAccountId));
    }

    @Test
    public void userCannotDepositMoneyIntoAccountOwnedByAnotherUserTest() {
        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest user1 = users.get(0);
        UserRequest user2 = users.get(1);

        UserSteps userSteps1 = new UserSteps(
                user1.getUsername(), user1.getPassword());
        UserSteps userSteps2 = new UserSteps(
                user2.getUsername(), user2.getPassword());

        AccountResponse accountOwnedByAnotherUser = userSteps2.createAccount();
        BigDecimal accountOwnedByAnotherUserBalanceBefore = accountOwnedByAnotherUser.getBalance();

        List<Transaction> transactionsBefore =
                userSteps2.getAccountTransactions(accountOwnedByAnotherUser.getId());

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(accountOwnedByAnotherUser.getId());
        userSteps1.depositMoney(deposit, ResponseSpecs.accountAccessForbidden());

        // баланс счета не должен измениться
        BigDecimal accountOwnedByAnotherUserBalanceAfter = userSteps2.getAccountById(accountOwnedByAnotherUser.getId()).getBalance();
        softly.assertThat(accountOwnedByAnotherUserBalanceBefore)
                .isEqualByComparingTo(accountOwnedByAnotherUserBalanceAfter);

        // отклонённый депозит не должен создавать никаких транзакций
        List<Transaction> transactionsAfter =
                userSteps2.getAccountTransactions(accountOwnedByAnotherUser.getId());
        softly.assertThat(transactionsAfter)
                .containsExactlyElementsOf(transactionsBefore);
    }

    @ParameterizedTest
    @MethodSource("api.testdata.DepositValidationTestData#invalidDepositBalance")
    public void userCannotDepositMoneyWithInvalidBalanceTest(BigDecimal balance, String expectedError) {
        UserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        AccountResponse account = userSteps.createAccount();

        BigDecimal accountBalanceBefore = account.getBalance();
        List<Transaction> transactionsBefore =
                userSteps.getAccountTransactions(account.getId());

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(account.getId());
        deposit.setBalance(balance);
        userSteps.depositMoney(deposit, ResponseSpecs.requestReturnsBadRequest(expectedError));

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
