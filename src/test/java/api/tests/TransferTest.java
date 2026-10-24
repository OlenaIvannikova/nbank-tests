package api.tests;

import api.assertions.TransactionAssertions;
import api.models.*;
import base.BaseTest;
import api.constants.messages.SuccessMessage;
import common.generators.RandomModelGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static api.constants.messages.ErrorMessage.INVALID_TRANSFER;
import static common.generators.RandomData.generateTransferAmount;
import static api.requests.steps.AdminSteps.createTwoDifferentUsers;
import static api.requests.steps.AdminSteps.createUser;

public class TransferTest extends BaseTest {

    @Test
    public void userCanTransferMoneyToAnotherUserAccountTest() {

        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest sender = users.get(0);
        UserRequest receiver = users.get(1);

        UserSteps senderSteps = new UserSteps(
                sender.getUsername(),
                sender.getPassword()
        );
        UserSteps receiverSteps = new UserSteps(
                receiver.getUsername(),
                receiver.getPassword()
        );

        List<AccountResponse> accounts = UserSteps.createTwoAccountsWithNonZeroBalanceForDifferentUsers(users);
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        // Зафиксировали баланс отправителя и получателя ДО перевода
        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Выполнили перевод через Transfer API
        BigDecimal amount = generateTransferAmount(senderBalanceBefore);
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(senderAccount.getId());
        transferRequest.setReceiverAccountId(receiverAccount.getId());
        transferRequest.setAmount(amount);

        TransferResponse transferResponse = senderSteps
                .transferMoney(transferRequest, ResponseSpecs.requestReturnsOK())
                .as(TransferResponse.class);

        //Проверили сообщение об успешном трансфере
        softly.assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue());

        BigDecimal senderBalanceAfter = senderSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = receiverSteps.getAccountById(receiverAccount.getId()).getBalance();

        // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
        softly.assertThat(senderBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP));
        //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
        softly.assertThat(receiverBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP));

        List<Transaction> senderAccountTransactions = senderSteps.getAccountTransactions(senderAccount.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                senderAccountTransactions,
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()
        );

        List<Transaction> receiverAccountTransactions = receiverSteps.getAccountTransactions(receiverAccount.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                receiverAccountTransactions,
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()
        );
    }

    @Test
    public void userCanTransferMoneyBetweenOwnAccountsTest() {

        UserRequest user = createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        List<AccountResponse> accounts = userSteps.createTwoAccountsWithNonZeroBalanceForUser();
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        // Зафиксировали баланс отправителя и получателя ДО перевода
        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Выполнили перевод через Transfer API
        BigDecimal amount = generateTransferAmount(senderBalanceBefore);
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(senderAccount.getId());
        transferRequest.setReceiverAccountId(receiverAccount.getId());
        transferRequest.setAmount(amount);

        TransferResponse transferResponse = userSteps.transferMoney(transferRequest, ResponseSpecs.requestReturnsOK())
                .as(TransferResponse.class);

        //Проверили сообщение об успешном трансфере
        softly.assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue());

        BigDecimal senderBalanceAfter = userSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = userSteps.getAccountById(receiverAccount.getId()).getBalance();

        // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
        softly.assertThat(senderBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP));

        //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
        softly.assertThat(receiverBalanceAfter.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP));

        List<Transaction> senderAccountTransactions = userSteps.getAccountTransactions(senderAccount.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                senderAccountTransactions,
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()
        );

        List<Transaction> receiverAccountTransactions = userSteps.getAccountTransactions(receiverAccount.getId());
        TransactionAssertions.assertTransactionExists(
                softly,
                receiverAccountTransactions,
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()
        );
    }

    @ParameterizedTest
    @MethodSource("api.testdata.TransferValidationTestData#invalidTransferAmount")
    public void userCannotTransferMoneyWithInvalidAmountTest(BigDecimal amount, String expectedError) {

        UserRequest user = createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        List<AccountResponse> accounts = userSteps.createTwoAccountsWithNonZeroBalanceForUser();
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(senderAccount.getId());
        transferRequest.setReceiverAccountId(receiverAccount.getId());
        transferRequest.setAmount(amount);

        userSteps.transferMoney(
                transferRequest,
                ResponseSpecs.requestReturnsBadRequest(expectedError)
        );

        BigDecimal senderBalanceAfter = userSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = userSteps.getAccountById(receiverAccount.getId()).getBalance();

        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore);
        softly.assertThat(receiverBalanceAfter)
                .isEqualByComparingTo(receiverBalanceBefore);

        List<Transaction> senderAccountTransactions = userSteps.getAccountTransactions(senderAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                senderAccountTransactions,
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()
        );

        List<Transaction> receiverAccountTransactions = userSteps.getAccountTransactions(receiverAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                receiverAccountTransactions,
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()
        );
    }

    @Test
    public void userCannotTransferMoneyWithAmountExceedsSenderBalanceTest() {

        UserRequest user = createUser();
        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

        List<AccountResponse> accounts = userSteps.createTwoAccountsWithNonZeroBalanceForUser();
        AccountResponse senderAccount = accounts.get(0);
        AccountResponse receiverAccount = accounts.get(1);

        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        BigDecimal amount = generateTransferAmount(senderAccount.getBalance()).add(senderAccount.getBalance());
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(senderAccount.getId());
        transferRequest.setReceiverAccountId(receiverAccount.getId());
        transferRequest.setAmount(amount);

        userSteps.transferMoney(
                transferRequest,
                ResponseSpecs.requestReturnsBadRequest(INVALID_TRANSFER.getValue())
        );

        BigDecimal senderBalanceAfter = userSteps.getAccountById(senderAccount.getId()).getBalance();
        BigDecimal receiverBalanceAfter = userSteps.getAccountById(receiverAccount.getId()).getBalance();

        softly.assertThat(senderBalanceAfter)
                .isEqualByComparingTo(senderBalanceBefore);
        softly.assertThat(receiverBalanceAfter)
                .isEqualByComparingTo(receiverBalanceBefore);

        List<Transaction> senderAccountTransactions = userSteps.getAccountTransactions(senderAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                senderAccountTransactions,
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()
        );

        List<Transaction> receiverAccountTransactions = userSteps.getAccountTransactions(receiverAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                receiverAccountTransactions,
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()
        );
    }

    @Test
    public void userCannotTransferMoneyFromAccountOwnedByAnotherUserTest() {

        List<UserRequest> users = createTwoDifferentUsers();
        UserRequest sender = users.get(0);
        UserRequest receiver = users.get(1);

        UserSteps senderSteps = new UserSteps(
                sender.getUsername(), sender.getPassword());
        UserSteps receiverSteps = new UserSteps(
                receiver.getUsername(), receiver.getPassword());

        List<AccountResponse> accounts = UserSteps.createTwoAccountsWithNonZeroBalanceForDifferentUsers(users);
        AccountResponse ownAccount = accounts.get(0);
        AccountResponse accountOwnedByAnotherUser = accounts.get(1);

        BigDecimal ownAccountBalanceBefore = ownAccount.getBalance();
        BigDecimal accountOwnedByAnotherUserBalanceBefore = accountOwnedByAnotherUser.getBalance();

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(accountOwnedByAnotherUser.getId());
        transferRequest.setReceiverAccountId(ownAccount.getId());
        transferRequest.setAmount(generateTransferAmount(accountOwnedByAnotherUser.getBalance()));

        senderSteps.transferMoney(
                transferRequest,
                ResponseSpecs.accountAccessForbidden()
        );

        BigDecimal ownAccountBalanceAfter = senderSteps.getAccountById(ownAccount.getId()).getBalance();
        BigDecimal accountOwnedByAnotherUserBalanceAfter = receiverSteps.getAccountById(accountOwnedByAnotherUser.getId()).getBalance();

        softly.assertThat(ownAccountBalanceAfter)
                .isEqualByComparingTo(ownAccountBalanceBefore);
        softly.assertThat(accountOwnedByAnotherUserBalanceAfter)
                .isEqualByComparingTo(accountOwnedByAnotherUserBalanceBefore);

        List<Transaction> receiverAccountTransactions = receiverSteps.getAccountTransactions(accountOwnedByAnotherUser.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                receiverAccountTransactions,
                transferRequest.getAmount(),
                TransactionType.TRANSFER_OUT,
                accountOwnedByAnotherUser.getId()
        );

        List<Transaction> senderAccountTransactions = senderSteps.getAccountTransactions(ownAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                senderAccountTransactions,
                transferRequest.getAmount(),
                TransactionType.TRANSFER_IN,
                ownAccount.getId()
        );
    }

    @Test
    public void userCannotTransferMoneyFromNonExistentAccountTest() {

        UserRequest user = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(
                user.getUsername(),
                user.getPassword()
        );

        AccountResponse receiverAccount = userSteps.createAccount();
        BigDecimal receiverAccountBalanceBefore = receiverAccount.getBalance();

        Long nonExistentAccountId = Long.MAX_VALUE;

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(nonExistentAccountId);
        transferRequest.setReceiverAccountId(receiverAccount.getId());
        transferRequest.setAmount(TransferAmount.MAX_ALLOWED.getAmount());

        userSteps.transferMoney(
                transferRequest,
                ResponseSpecs.accountAccessForbidden()
        );

        BigDecimal receiverAccountBalanceAfter = userSteps.getAccountById(receiverAccount.getId()).getBalance();
        softly.assertThat(receiverAccountBalanceAfter)
                .isEqualByComparingTo(receiverAccountBalanceBefore);

        List<Transaction> receiverTransactions =
                userSteps.getAccountTransactions(receiverAccount.getId());
        TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                receiverTransactions,
                transferRequest.getAmount(),
                TransactionType.TRANSFER_IN,
                nonExistentAccountId
        );
    }
}
