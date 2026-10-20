package api.tests;

import base.BaseTest;
import generators.RandomModelGenerator;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static requests.steps.AdminSteps.createTwoDifferentUsers;
import static requests.steps.UserSteps.*;

public class DepositTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("api.testdata.DepositValidationTestData#validDepositBalance")
    public void userCanDepositMoneyIntoAccount(BigDecimal balance) {

        UserRequest user = AdminSteps.createUser();
        AccountResponse account = createAccount(user);

        DepositRequest deposit = DepositRequest.builder()
                .id(account.getId())
                .balance(balance)
                .build();

        AccountResponse accountDetails = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(deposit)
                .extract()
                .as(AccountResponse.class);

        softly.assertThat(accountDetails.getBalance())
                .isEqualByComparingTo(deposit.getBalance());

        softly.assertThat(accountDetails.getTransactions())
                .anySatisfy(transaction -> {
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(deposit.getBalance());
                    assertThat(TransactionType.valueOf(transaction.getType()))
                            .isEqualTo(TransactionType.DEPOSIT);
                    assertThat(transaction.getRelatedAccountId())
                            .isEqualTo(account.getId());
                });

        List<Transaction> accountTransactions = getAccountTransactions(user, account.getId());

        softly.assertThat(accountTransactions)
                .anySatisfy(transaction -> {
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(deposit.getBalance());
                    assertThat(TransactionType.valueOf(transaction.getType()))
                            .isEqualTo(TransactionType.DEPOSIT);
                    assertThat(transaction.getRelatedAccountId())
                            .isEqualTo(account.getId());
                });
    }

    @Test
    public void userCannotDepositMoneyIntoNonExistentAccount() {
        UserRequest user = AdminSteps.createUser();

        long nonExistentAccountId = Long.MAX_VALUE;
        // проверяем что такого аккаунта точно нет в системе
        assertThat(getAccounts(user))
                .extracting(AccountResponse::getId)
                .doesNotContain(nonExistentAccountId);

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(nonExistentAccountId);

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.accountAccessForbidden())
                .post(deposit);

        Endpoint endpoint = Endpoint.ACCOUNT_TRANSACTIONS;
        String paramName = endpoint.getPathParamNames().getFirst();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                endpoint,
                ResponseSpecs.accountAccessDenied())
                .getByPathParams(Map.of(paramName, nonExistentAccountId));
    }

    @Test
    public void userCannotDepositMoneyIntoAccountOwnedByAnotherUser() {
        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest user = users.get(0);

        AccountResponse accountOwnedByAnotherUser = createAccount(users.get(1));

        DepositRequest deposit = RandomModelGenerator.generate(DepositRequest.class);
        deposit.setId(accountOwnedByAnotherUser.getId());

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.accountAccessForbidden())
                .post(deposit);

        List<Transaction> accountTransactions = getAccountTransactions(users.get(1), accountOwnedByAnotherUser.getId());
        softly.assertThat(accountTransactions)
                .noneSatisfy(transaction -> {
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(deposit.getBalance());
                    assertThat(TransactionType.valueOf(transaction.getType()))
                            .isEqualTo(TransactionType.DEPOSIT);
                    assertThat(transaction.getRelatedAccountId())
                            .isEqualTo(accountOwnedByAnotherUser.getId());
                });
    }

    @ParameterizedTest
    @MethodSource("api.testdata.DepositValidationTestData#invalidDepositBalance")
    public void userCannotDepositMoneyWithInvalidBalance(BigDecimal balance, String expectedError) {
        UserRequest user = AdminSteps.createUser();
        AccountResponse account = createAccount(user);

        DepositRequest deposit = DepositRequest.builder()
                .id(account.getId())
                .balance(balance)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(expectedError))
                .post(deposit);

        List<Transaction> accountTransactions = getAccountTransactions(user, account.getId());
        softly.assertThat(accountTransactions)
                .noneSatisfy(transaction -> {
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(deposit.getBalance());
                    assertThat(TransactionType.valueOf(transaction.getType()))
                            .isEqualTo(TransactionType.DEPOSIT);
                    assertThat(transaction.getRelatedAccountId())
                            .isEqualTo(account.getId());
                });
    }
}
