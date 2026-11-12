package api.tests;

import static api.requests.steps.AdminSteps.createTwoDifferentUsers;
import static org.assertj.core.api.Assertions.assertThat;

import api.assertions.AccountAssertions;
import api.assertions.TransactionAssertions;
import api.dao.TransactionDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.*;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import common.utils.RetryUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@APIVersion("with_database")
public class DepositTest extends BaseTest {

  @ParameterizedTest
  @MethodSource("api.testdata.DepositValidationTestData#validDepositBalance")
  public void userCanDepositMoneyIntoAccountTest(BigDecimal balance) {

    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());
    AccountResponse account =
        StepLogger.log("Create account for user", () -> userSteps.createAccount());

    DepositRequest deposit =
        StepLogger.log(
            "Prepare deposit request",
            () -> {
              DepositRequest request = RandomModelGenerator.generate(DepositRequest.class);
              request.setId(account.getId());
              request.setBalance(balance);
              return request;
            });

    AccountResponse accountDetails =
        StepLogger.log(
            "Deposit money into account",
            () ->
                userSteps
                    .depositMoney(deposit, ResponseSpecs.requestReturnsOK())
                    .as(AccountResponse.class));

    AccountAssertions.assertDepositSuccessful(
        softly, accountDetails, deposit.getBalance(), account.getId());

    List<Transaction> accountTransactions = userSteps.getAccountTransactions(account.getId());

    StepLogger.log(
        "Verify deposit transaction in transaction history",
        () ->
            TransactionAssertions.assertTransactionExists(
                softly,
                accountTransactions,
                deposit.getBalance(),
                TransactionType.DEPOSIT,
                account.getId()));

    List<TransactionDao> depositTransactions =
        DataBaseSteps.getAccountTransactionsByTransactionType(
            account.getId(), TransactionType.DEPOSIT);

    TransactionDao depositTransactionDao =
        StepLogger.log("Get deposit transaction", depositTransactions::getFirst);

    StepLogger.log(
        "Verify deposit transaction matches database record",
        () -> DaoAndModelAssertions.assertThat(deposit, depositTransactionDao).match());
    StepLogger.log(
        "Verify database transaction type",
        () ->
            softly
                .assertThat(depositTransactionDao.getType())
                .isEqualTo(TransactionType.DEPOSIT.toString()));
  }

  @Test
  public void userCannotDepositMoneyIntoNonExistentAccountTest() {
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    long nonExistentAccountId = Long.MAX_VALUE;

    // проверяем что такого аккаунта точно нет в системе
    List<AccountResponse> userAccounts =
        RetryUtils.retry(
            userSteps::getAccounts,
            accounts ->
                accounts != null
                    && accounts.stream().noneMatch(a -> a.getId().equals(nonExistentAccountId)),
            5,
            1000);
    StepLogger.log(
        "Verify that account does not exist in user's account list",
        () ->
            assertThat(userAccounts)
                .extracting(AccountResponse::getId)
                .doesNotContain(nonExistentAccountId));

    DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
    deposit.setId(nonExistentAccountId);
    StepLogger.log(
        "Verify that user cannot deposit money into non-existent account",
        () -> userSteps.depositMoney(deposit, ResponseSpecs.accountAccessForbidden()));

    Endpoint endpoint = Endpoint.ACCOUNT_TRANSACTIONS;
    String paramName = endpoint.getPathParamNames().getFirst();
    StepLogger.log(
        "Verify that user cannot access transactions of non-existent account",
        () ->
            new CrudRequester(
                    RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                    endpoint,
                    ResponseSpecs.accountAccessDenied())
                .getAllByPathParams(Map.of(paramName, nonExistentAccountId)));

    List<TransactionDao> transactions = DataBaseSteps.getAccountTransactions(nonExistentAccountId);
    StepLogger.log(
        "Verify that no transactions exist for non-existent account in database",
        () -> assertThat(transactions).isEmpty());
  }

  @Test
  public void userCannotDepositMoneyIntoAccountOwnedByAnotherUserTest() {
    List<UserRequest> users =
        StepLogger.log("Create two different users", () -> createTwoDifferentUsers());
    UserRequest user1 = users.get(0);
    UserRequest user2 = users.get(1);

    UserSteps userSteps1 = new UserSteps(user1.getUsername(), user1.getPassword());
    UserSteps userSteps2 = new UserSteps(user2.getUsername(), user2.getPassword());

    AccountResponse accountOwnedByAnotherUser =
        StepLogger.log("Create account for the second user", () -> userSteps2.createAccount());
    BigDecimal accountOwnedByAnotherUserBalanceBefore = accountOwnedByAnotherUser.getBalance();

    List<Transaction> transactionsBefore =
        StepLogger.log(
            "Get account transactions before deposit attempt",
            () -> userSteps2.getAccountTransactions(accountOwnedByAnotherUser.getId()));

    DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
    deposit.setId(accountOwnedByAnotherUser.getId());
    StepLogger.log(
        "Verify that the first user cannot deposit money into the second user's account",
        () -> userSteps1.depositMoney(deposit, ResponseSpecs.accountAccessForbidden()));

    // баланс счета не должен измениться
    BigDecimal accountOwnedByAnotherUserBalanceAfter =
        StepLogger.log(
            "Get account balance after rejected deposit",
            () -> userSteps2.getAccountById(accountOwnedByAnotherUser.getId()).getBalance());
    StepLogger.log(
        "Verify that account balance has not changed",
        () ->
            softly
                .assertThat(accountOwnedByAnotherUserBalanceBefore)
                .isEqualByComparingTo(accountOwnedByAnotherUserBalanceAfter));

    // отклонённый депозит не должен создавать никаких транзакций
    List<Transaction> transactionsAfter =
        StepLogger.log(
            "Get account transactions after rejected deposit",
            () -> userSteps2.getAccountTransactions(accountOwnedByAnotherUser.getId()));
    StepLogger.log(
        "Verify that account transaction history has not changed",
        () -> softly.assertThat(transactionsAfter).containsExactlyElementsOf(transactionsBefore));

    List<TransactionDao> transactions =
        DataBaseSteps.getAccountTransactions(accountOwnedByAnotherUser.getId());
    StepLogger.log(
        "Verify that no transactions were created in database",
        () -> assertThat(transactions).isEmpty());
  }

  @ParameterizedTest
  @MethodSource("api.testdata.DepositValidationTestData#invalidDepositBalance")
  public void userCannotDepositMoneyWithInvalidBalanceTest(
      BigDecimal balance, String expectedError) {
    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    AccountResponse account =
        StepLogger.log("Create account for user", () -> userSteps.createAccount());

    BigDecimal accountBalanceBefore = account.getBalance();
    List<Transaction> transactionsBefore =
        StepLogger.log(
            "Get account transactions before deposit attempt",
            () -> userSteps.getAccountTransactions(account.getId()));

    DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
    deposit.setId(account.getId());
    deposit.setBalance(balance);
    StepLogger.log(
        "Verify that deposit with invalid balance is rejected",
        () ->
            userSteps.depositMoney(deposit, ResponseSpecs.requestReturnsBadRequest(expectedError)));

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
        "Verify that account transaction history has not changed",
        () -> softly.assertThat(transactionsAfter).containsExactlyElementsOf(transactionsBefore));

    List<TransactionDao> transactions = DataBaseSteps.getAccountTransactions(account.getId());
    StepLogger.log(
        "Verify that no transactions were created in database",
        () -> assertThat(transactions).isEmpty());
  }
}
