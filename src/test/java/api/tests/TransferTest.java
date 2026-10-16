package api.tests;

import base.BaseTest;
import constants.messages.SuccessMessage;
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

import static constants.messages.ErrorMessage.INVALID_TRANSFER;
import static generators.RandomData.generateTransferAmount;
import static requests.steps.AdminSteps.createTwoDifferentUsers;
import static requests.steps.AdminSteps.createUser;
import static requests.steps.TransferSteps.createTransferRequest;
import static requests.steps.TransferSteps.executeTransfer;
import static requests.steps.UserSteps.*;

public class TransferTest extends BaseTest {

    @Test
    public void userCanTransferMoneyToAnotherUserAccount() {

        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest sender = users.get(0);
        UserRequest receiver = users.get(1);

        List<AccountResponse> accounts = createTwoAccountsWithNonZeroBalanceForDifferentUsers(users);
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        // Зафиксировали баланс отправителя и получателя ДО перевода
        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Выполнили перевод через Transfer API
        BigDecimal amount = generateTransferAmount(senderBalanceBefore);
        TransferRequest transferRequest = createTransferRequest(
                senderAccount,
                receiverAccount,
                amount);

        TransferResponse transferResponse = executeTransfer(sender, transferRequest);

        //Проверили сообщение об успешном трансфере
        softly.assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue());

        BigDecimal senderBalanceAfter = getAccountById(sender, senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = getAccountById(receiver, receiverAccount.getId()).getBalance();

        // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore.subtract(amount));

        //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
        softly.assertThat(receiverBalanceAfter)
                .isEqualByComparingTo(receiverBalanceBefore.add(amount));
    }


    @Test
    public void userCanTransferMoneyBetweenOwnAccounts() {

        UserRequest user = createUser();

        List<AccountResponse> accounts = createTwoAccountsWithNonZeroBalanceForUser(user);
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        // Зафиксировали баланс отправителя и получателя ДО перевода
        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Выполнили перевод через Transfer API
        BigDecimal amount = generateTransferAmount(senderBalanceBefore);
        TransferRequest request = createTransferRequest(senderAccount, receiverAccount, amount);
        TransferResponse transferResponse = executeTransfer(user, request);

        //Проверили сообщение об успешном трансфере
        softly.assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue());

        BigDecimal senderBalanceAfter = getAccountById(user, senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = getAccountById(user, receiverAccount.getId()).getBalance();

        // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore.subtract(amount));

        //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
        softly.assertThat(receiverBalanceAfter)
                .isEqualByComparingTo(receiverBalanceBefore.add(amount));

    }

    @ParameterizedTest
    @MethodSource("api.testdata.TransferValidationTestData#invalidTransferAmount")
    public void userCannotTransferMoneyWithInvalidAmount(BigDecimal amount, String expectedError) {

        UserRequest user = createUser();

        List<AccountResponse> accounts = createTwoAccountsWithNonZeroBalanceForUser(user);
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        TransferRequest transferRequest = createTransferRequest(senderAccount, receiverAccount, amount);

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(expectedError))
                .post(transferRequest);

    }

    @Test
    public void userCannotTransferMoneyWithAmountExceedsSenderBalance() {

        UserRequest user = createUser();

        List<AccountResponse> accounts = createTwoAccountsWithNonZeroBalanceForUser(user);
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        BigDecimal amount = generateTransferAmount(senderAccount.getBalance()).add(senderAccount.getBalance());
        TransferRequest transferRequest = createTransferRequest(senderAccount, receiverAccount, amount);

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(INVALID_TRANSFER.getValue()))
                .post(transferRequest);
    }

    @Test
    public void userCannotTransferMoneyFromAccountOwnedByAnotherUser() {

        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest sender = users.get(0);

        List<AccountResponse> accounts = createTwoAccountsWithNonZeroBalanceForDifferentUsers(users);
        AccountResponse ownAccount = accounts.get(0);
        AccountResponse accountOwnedByAnotherUser = accounts.get(1);

        TransferRequest transferRequest = createTransferRequest(accountOwnedByAnotherUser, ownAccount, generateTransferAmount(accountOwnedByAnotherUser.getBalance()));

        new CrudRequester(
                RequestSpecs.authAsUser(sender.getUsername(), sender.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.accountAccessForbidden())
                .post(transferRequest);
    }

    @Test
    public void userCannotTransferMoneyFromNonExistentAccount() {

        UserRequest user = AdminSteps.createUser();
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.accountAccessForbidden())
                .post(transferRequest);
    }

}
