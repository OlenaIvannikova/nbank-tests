package api.tests;

import static api.constants.messages.ErrorMessage.INVALID_TRANSFER;
import static api.requests.steps.AdminSteps.createTwoDifferentUsers;
import static api.requests.steps.AdminSteps.createUser;
import static common.generators.RandomData.generateTransferAmount;

import api.assertions.TransactionAssertions;
import api.constants.messages.SuccessMessage;
import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.*;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import api.specs.ResponseSpecs;
import base.BaseTest;
import common.annotations.APIVersion;
import common.generators.RandomModelGenerator;
import common.helpers.StepLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@APIVersion("with_database")
public class TransferTest extends BaseTest {

  @Test
  public void userCanTransferMoneyToAnotherUserAccountTest() {

    List<UserRequest> users =
        StepLogger.log("Create sender and receiver users", () -> createTwoDifferentUsers());
    UserRequest sender = users.get(0);
    UserRequest receiver = users.get(1);

    UserSteps senderSteps = new UserSteps(sender.getUsername(), sender.getPassword());
    UserSteps receiverSteps = new UserSteps(receiver.getUsername(), receiver.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create accounts with non-zero balances for both users",
            () -> UserSteps.createTwoAccountsWithNonZeroBalanceForDifferentUsers(users));
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

    TransferResponse transferResponse =
        StepLogger.log(
            "Execute money transfer between users",
            () ->
                senderSteps
                    .transferMoney(transferRequest, ResponseSpecs.requestReturnsOK())
                    .as(TransferResponse.class));

    // Проверили сообщение об успешном трансфере
    StepLogger.log(
        "Verify successful transfer response message",
        () ->
            softly
                .assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue()));

    // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
    AccountResponse senderAccountAfterTransfer =
        StepLogger.log(
            "Verify sender account balance after transfer",
            () -> {
              AccountResponse accountAfter = senderSteps.getAccountById(senderAccount.getId());

              softly
                  .assertThat(accountAfter.getBalance().setScale(2, RoundingMode.HALF_UP))
                  .isEqualByComparingTo(
                      senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP));

              return accountAfter;
            });

    //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
    AccountResponse receiverAccountAfterTransfer =
        StepLogger.log(
            "Verify receiver account balance after transfer",
            () -> {
              AccountResponse accountAfter = receiverSteps.getAccountById(receiverAccount.getId());

              softly
                  .assertThat(accountAfter.getBalance().setScale(2, RoundingMode.HALF_UP))
                  .isEqualByComparingTo(
                      receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP));

              return accountAfter;
            });

    List<Transaction> senderAccountTransactions =
        StepLogger.log(
            "Verify outgoing transfer transaction in sender account",
            () -> {
              List<Transaction> transactions =
                  senderSteps.getAccountTransactions(senderAccount.getId());

              TransactionAssertions.assertTransactionExists(
                  softly,
                  transactions,
                  amount,
                  TransactionType.TRANSFER_OUT,
                  receiverAccount.getId());

              return transactions;
            });

    List<Transaction> receiverAccountTransactions =
        StepLogger.log(
            "Verify incoming transfer transaction in receiver account",
            () -> {
              List<Transaction> transactions =
                  receiverSteps.getAccountTransactions(receiverAccount.getId());

              TransactionAssertions.assertTransactionExists(
                  softly, transactions, amount, TransactionType.TRANSFER_IN, senderAccount.getId());

              return transactions;
            });

    // DB check
    StepLogger.log(
        "Verify sender account data against database",
        () ->
            DaoAndModelAssertions.assertThat(
                    senderAccountAfterTransfer, DataBaseSteps.getAccountById(senderAccount.getId()))
                .match());

    StepLogger.log(
        "Verify receiver account data against database",
        () ->
            DaoAndModelAssertions.assertThat(
                    receiverAccountAfterTransfer,
                    DataBaseSteps.getAccountById(receiverAccount.getId()))
                .match());

    StepLogger.log(
        "Verify outgoing transfer transaction against database",
        () -> {
          Transaction transferOut =
              senderAccountTransactions.stream()
                  .filter(transaction ->
                      TransactionType.TRANSFER_OUT.name().equals(transaction.getType()))
                  .findFirst()
                  .orElseThrow(() -> new AssertionError("TRANSFER_OUT transaction not found"));

          DaoAndModelAssertions.assertThat(
                  transferOut,
                  DataBaseSteps.getAccountTransactionsByTransactionType(
                          senderAccount.getId(), TransactionType.TRANSFER_OUT)
                      .getFirst())
              .match();
        });

    StepLogger.log(
        "Verify incoming transfer transaction against database",
        () -> {
          Transaction transferIn =
              receiverAccountTransactions.stream()
                  .filter(transaction ->
                      TransactionType.TRANSFER_IN.name().equals(transaction.getType()))
                  .findFirst()
                  .orElseThrow(() -> new AssertionError("TRANSFER_IN transaction not found"));

          DaoAndModelAssertions.assertThat(
                  transferIn,
                  DataBaseSteps.getAccountTransactionsByTransactionType(
                          receiverAccount.getId(), TransactionType.TRANSFER_IN)
                      .getFirst())
              .match();
        });
  }

  @Test
  public void userCanTransferMoneyBetweenOwnAccountsTest() {

    UserRequest user = createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create two accounts with non-zero balances for user",
            () -> userSteps.createTwoAccountsWithNonZeroBalanceForUser());
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

    TransferResponse transferResponse =
        StepLogger.log(
            "Execute money transfer between user's own accounts",
            () ->
                userSteps
                    .transferMoney(transferRequest, ResponseSpecs.requestReturnsOK())
                    .as(TransferResponse.class));

    // Проверили сообщение об успешном трансфере
    StepLogger.log(
        "Verify successful transfer response message",
        () ->
            softly
                .assertThat(transferResponse.getMessage())
                .isEqualTo(SuccessMessage.TRANSFER_SUCCESSFUL.getValue()));

    // Проверили, что баланс отправителя уменьшился ровно на сумму трансфера
    AccountResponse senderAccountAfterTransfer =
        StepLogger.log(
            "Verify sender account balance after transfer",
            () -> {
              AccountResponse accountAfter = userSteps.getAccountById(senderAccount.getId());

              softly
                  .assertThat(accountAfter.getBalance().setScale(2, RoundingMode.HALF_UP))
                  .isEqualByComparingTo(
                      senderBalanceBefore.subtract(amount).setScale(2, RoundingMode.HALF_UP));

              return accountAfter;
            });

    //  Проверили, что баланс получателя увеличился ровно на сумму трансфера
    AccountResponse receiverAccountAfterTransfer =
        StepLogger.log(
            "Verify receiver account balance after transfer",
            () -> {
              AccountResponse accountAfter = userSteps.getAccountById(receiverAccount.getId());

              softly
                  .assertThat(accountAfter.getBalance().setScale(2, RoundingMode.HALF_UP))
                  .isEqualByComparingTo(
                      receiverBalanceBefore.add(amount).setScale(2, RoundingMode.HALF_UP));

              return accountAfter;
            });

    List<Transaction> senderAccountTransactions =
        StepLogger.log(
            "Verify outgoing transfer transaction in sender account",
            () -> {
              List<Transaction> transactions =
                  userSteps.getAccountTransactions(senderAccount.getId());

              TransactionAssertions.assertTransactionExists(
                  softly,
                  transactions,
                  amount,
                  TransactionType.TRANSFER_OUT,
                  receiverAccount.getId());

              return transactions;
            });

    List<Transaction> receiverAccountTransactions =
        StepLogger.log(
            "Verify incoming transfer transaction in receiver account",
            () -> {
              List<Transaction> transactions =
                  userSteps.getAccountTransactions(receiverAccount.getId());

              TransactionAssertions.assertTransactionExists(
                  softly, transactions, amount, TransactionType.TRANSFER_IN, senderAccount.getId());

              return transactions;
            });

    // DB check
    StepLogger.log(
        "Verify sender account data against database",
        () ->
            DaoAndModelAssertions.assertThat(
                    senderAccountAfterTransfer, DataBaseSteps.getAccountById(senderAccount.getId()))
                .match());
    StepLogger.log(
        "Verify receiver account data against database",
        () ->
            DaoAndModelAssertions.assertThat(
                    receiverAccountAfterTransfer,
                    DataBaseSteps.getAccountById(receiverAccount.getId()))
                .match());

    StepLogger.log(
        "Verify outgoing transfer transaction against database",
        () -> {
          Transaction transferOut =
              senderAccountTransactions.stream()
                  .filter(transaction ->
                      TransactionType.TRANSFER_OUT.name().equals(transaction.getType()))
                  .findFirst()
                  .orElseThrow(() -> new AssertionError("TRANSFER_OUT transaction not found"));

          DaoAndModelAssertions.assertThat(
                  transferOut,
                  DataBaseSteps.getAccountTransactionsByTransactionType(
                          senderAccount.getId(), TransactionType.TRANSFER_OUT)
                      .getFirst())
              .match();
        });

    StepLogger.log(
        "Verify incoming transfer transaction against database",
        () -> {
          Transaction transferIn =
              receiverAccountTransactions.stream()
                  .filter(transaction ->
                      TransactionType.TRANSFER_IN.name().equals(transaction.getType()))
                  .findFirst()
                  .orElseThrow(() -> new AssertionError("TRANSFER_IN transaction not found"));

          DaoAndModelAssertions.assertThat(
                  transferIn,
                  DataBaseSteps.getAccountTransactionsByTransactionType(
                          receiverAccount.getId(), TransactionType.TRANSFER_IN)
                      .getFirst())
              .match();
        });
  }

  @ParameterizedTest
  @MethodSource("api.testdata.TransferValidationTestData#invalidTransferAmount")
  public void userCannotTransferMoneyWithInvalidAmountTest(
      BigDecimal amount, String expectedError) {

    UserRequest user = createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create two accounts with non-zero balances for user",
            () -> userSteps.createTwoAccountsWithNonZeroBalanceForUser());
    AccountResponse senderAccount = accounts.get(0);
    AccountResponse receiverAccount = accounts.get(1);

    BigDecimal senderBalanceBefore = senderAccount.getBalance();
    BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

    TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
    transferRequest.setSenderAccountId(senderAccount.getId());
    transferRequest.setReceiverAccountId(receiverAccount.getId());
    transferRequest.setAmount(amount);

    StepLogger.log(
        "Verify that transfer with invalid amount is rejected",
        () ->
            userSteps.transferMoney(
                transferRequest, ResponseSpecs.requestReturnsBadRequest(expectedError)));

    StepLogger.log(
        "Verify sender account balance has not changed",
        () -> {
          BigDecimal senderBalanceAfter =
              userSteps.getAccountById(senderAccount.getId()).getBalance();

          softly.assertThat(senderBalanceAfter).isEqualByComparingTo(senderBalanceBefore);
        });

    StepLogger.log(
        "Verify receiver account balance has not changed",
        () -> {
          BigDecimal receiverBalanceAfter =
              userSteps.getAccountById(receiverAccount.getId()).getBalance();

          softly.assertThat(receiverBalanceAfter).isEqualByComparingTo(receiverBalanceBefore);
        });

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in sender account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                userSteps.getAccountTransactions(senderAccount.getId()),
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()));

    StepLogger.log(
        "Verify no incoming transfer transaction exists in receiver account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                userSteps.getAccountTransactions(receiverAccount.getId()),
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()));

    // DB check
    AccountDao senderAccountDao =
        StepLogger.log(
            "Get sender account from database",
            () -> DataBaseSteps.getAccountById(senderAccount.getId()));
    StepLogger.log(
        "Verify sender account data against database",
        () -> DaoAndModelAssertions.assertThat(senderAccount, senderAccountDao).match());

    AccountDao receiverAccountDao =
        StepLogger.log(
            "Get receiver account from database",
            () -> DataBaseSteps.getAccountById(receiverAccount.getId()));

    StepLogger.log(
        "Verify receiver account data against database",
        () -> DaoAndModelAssertions.assertThat(receiverAccount, receiverAccountDao).match());

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        senderAccount.getId(), TransactionType.TRANSFER_OUT))
                .isEmpty());

    StepLogger.log(
        "Verify no incoming transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        receiverAccount.getId(), TransactionType.TRANSFER_IN))
                .isEmpty());
  }

  @Test
  public void userCannotTransferMoneyWithAmountExceedsSenderBalanceTest() {

    UserRequest user = createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create two accounts with non-zero balances for user",
            () -> userSteps.createTwoAccountsWithNonZeroBalanceForUser());
    AccountResponse senderAccount = accounts.get(0);
    AccountResponse receiverAccount = accounts.get(1);

    BigDecimal senderBalanceBefore = senderAccount.getBalance();
    BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

    BigDecimal amount =
        generateTransferAmount(senderAccount.getBalance()).add(senderAccount.getBalance());
    TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
    transferRequest.setSenderAccountId(senderAccount.getId());
    transferRequest.setReceiverAccountId(receiverAccount.getId());
    transferRequest.setAmount(amount);

    StepLogger.log(
        "Verify that transfer exceeding sender balance is rejected",
        () ->
            userSteps.transferMoney(
                transferRequest,
                ResponseSpecs.requestReturnsBadRequest(INVALID_TRANSFER.getValue())));

    StepLogger.log(
        "Verify sender account balance has not changed",
        () -> {
          BigDecimal senderBalanceAfter =
              userSteps.getAccountById(senderAccount.getId()).getBalance();

          softly.assertThat(senderBalanceAfter).isEqualByComparingTo(senderBalanceBefore);
        });

    StepLogger.log(
        "Verify receiver account balance has not changed",
        () -> {
          BigDecimal receiverBalanceAfter =
              userSteps.getAccountById(receiverAccount.getId()).getBalance();

          softly.assertThat(receiverBalanceAfter).isEqualByComparingTo(receiverBalanceBefore);
        });

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in sender account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                userSteps.getAccountTransactions(senderAccount.getId()),
                amount,
                TransactionType.TRANSFER_OUT,
                receiverAccount.getId()));

    StepLogger.log(
        "Verify no incoming transfer transaction exists in receiver account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                userSteps.getAccountTransactions(receiverAccount.getId()),
                amount,
                TransactionType.TRANSFER_IN,
                senderAccount.getId()));

    // DB check
    AccountDao senderAccountDao =
        StepLogger.log(
            "Get sender account from database",
            () -> DataBaseSteps.getAccountById(senderAccount.getId()));

    StepLogger.log(
        "Verify sender account data against database",
        () -> DaoAndModelAssertions.assertThat(senderAccount, senderAccountDao).match());

    AccountDao receiverAccountDao =
        StepLogger.log(
            "Get receiver account from database",
            () -> DataBaseSteps.getAccountById(receiverAccount.getId()));

    StepLogger.log(
        "Verify receiver account data against database",
        () -> DaoAndModelAssertions.assertThat(receiverAccount, receiverAccountDao).match());

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        senderAccount.getId(), TransactionType.TRANSFER_OUT))
                .isEmpty());

    StepLogger.log(
        "Verify no incoming transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        receiverAccount.getId(), TransactionType.TRANSFER_IN))
                .isEmpty());
  }

  @Test
  public void userCannotTransferMoneyFromAccountOwnedByAnotherUserTest() {

    List<UserRequest> users =
        StepLogger.log("Create two different users", () -> createTwoDifferentUsers());
    UserRequest sender = users.get(0);
    UserRequest receiver = users.get(1);

    UserSteps senderSteps = new UserSteps(sender.getUsername(), sender.getPassword());
    UserSteps receiverSteps = new UserSteps(receiver.getUsername(), receiver.getPassword());

    List<AccountResponse> accounts =
        StepLogger.log(
            "Create accounts with non-zero balances for both users",
            () -> UserSteps.createTwoAccountsWithNonZeroBalanceForDifferentUsers(users));
    AccountResponse ownAccount = accounts.get(0);
    AccountResponse accountOwnedByAnotherUser = accounts.get(1);

    BigDecimal ownAccountBalanceBefore = ownAccount.getBalance();
    BigDecimal accountOwnedByAnotherUserBalanceBefore = accountOwnedByAnotherUser.getBalance();

    TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
    transferRequest.setSenderAccountId(accountOwnedByAnotherUser.getId());
    transferRequest.setReceiverAccountId(ownAccount.getId());
    transferRequest.setAmount(generateTransferAmount(accountOwnedByAnotherUser.getBalance()));

    StepLogger.log(
        "Verify that user cannot transfer money from another user's account",
        () -> senderSteps.transferMoney(transferRequest, ResponseSpecs.accountAccessForbidden()));

    StepLogger.log(
        "Verify own account balance has not changed",
        () -> {
          BigDecimal ownAccountBalanceAfter =
              senderSteps.getAccountById(ownAccount.getId()).getBalance();

          softly.assertThat(ownAccountBalanceAfter).isEqualByComparingTo(ownAccountBalanceBefore);
        });

    StepLogger.log(
        "Verify another user's account balance has not changed",
        () -> {
          BigDecimal accountBalanceAfter =
              receiverSteps.getAccountById(accountOwnedByAnotherUser.getId()).getBalance();

          softly
              .assertThat(accountBalanceAfter)
              .isEqualByComparingTo(accountOwnedByAnotherUserBalanceBefore);
        });

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in another user's account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                receiverSteps.getAccountTransactions(accountOwnedByAnotherUser.getId()),
                transferRequest.getAmount(),
                TransactionType.TRANSFER_OUT,
                accountOwnedByAnotherUser.getId()));

    StepLogger.log(
        "Verify no incoming transfer transaction exists in own account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                senderSteps.getAccountTransactions(ownAccount.getId()),
                transferRequest.getAmount(),
                TransactionType.TRANSFER_IN,
                ownAccount.getId()));

    // DB check
    AccountDao ownAccountDao =
        StepLogger.log(
            "Get own account from database",
            () -> DataBaseSteps.getAccountById(ownAccount.getId()));

    StepLogger.log(
        "Verify own account data against database",
        () -> DaoAndModelAssertions.assertThat(ownAccount, ownAccountDao).match());

    AccountDao anotherUserAccountDao =
        StepLogger.log(
            "Get another user's account from database",
            () -> DataBaseSteps.getAccountById(accountOwnedByAnotherUser.getId()));

    StepLogger.log(
        "Verify another user's account data against database",
        () ->
            DaoAndModelAssertions.assertThat(accountOwnedByAnotherUser, anotherUserAccountDao)
                .match());

    StepLogger.log(
        "Verify no outgoing transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        accountOwnedByAnotherUser.getId(), TransactionType.TRANSFER_OUT))
                .isEmpty());

    StepLogger.log(
        "Verify no incoming transfer transaction exists in database",
        () ->
            softly
                .assertThat(
                    DataBaseSteps.getAccountTransactionsByTransactionType(
                        ownAccount.getId(), TransactionType.TRANSFER_IN))
                .isEmpty());
  }

  @Test
  public void userCannotTransferMoneyFromNonExistentAccountTest() {

    UserRequest user = AdminSteps.createUser();
    UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());

    AccountResponse receiverAccount =
        StepLogger.log("Create receiver account", () -> userSteps.createAccount());
    BigDecimal receiverAccountBalanceBefore = receiverAccount.getBalance();

    Long nonExistentAccountId = Long.MAX_VALUE;

    TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
    transferRequest.setSenderAccountId(nonExistentAccountId);
    transferRequest.setReceiverAccountId(receiverAccount.getId());
    transferRequest.setAmount(TransferAmount.MAX_ALLOWED.getAmount());

    StepLogger.log(
        "Verify that user cannot transfer money from non-existent account",
        () -> userSteps.transferMoney(transferRequest, ResponseSpecs.accountAccessForbidden()));

    StepLogger.log(
        "Verify receiver account balance has not changed",
        () -> {
          BigDecimal receiverAccountBalanceAfter =
              userSteps.getAccountById(receiverAccount.getId()).getBalance();

          softly
              .assertThat(receiverAccountBalanceAfter)
              .isEqualByComparingTo(receiverAccountBalanceBefore);
        });

    StepLogger.log(
        "Verify no incoming transfer transaction exists in receiver account",
        () ->
            TransactionAssertions.assertTransactionDoesNotExist(
                softly,
                userSteps.getAccountTransactions(receiverAccount.getId()),
                transferRequest.getAmount(),
                TransactionType.TRANSFER_IN,
                nonExistentAccountId));

    // DB check
    StepLogger.log(
        "Verify non-existent sender account is not present in database",
        () -> softly.assertThat(DataBaseSteps.getAccountById(nonExistentAccountId)).isNull());
    AccountDao receiverAccountDao =
        StepLogger.log(
            "Get receiver account from database",
            () -> DataBaseSteps.getAccountById(receiverAccount.getId()));

    StepLogger.log(
        "Verify receiver account data against database",
        () -> DaoAndModelAssertions.assertThat(receiverAccount, receiverAccountDao).match());

    StepLogger.log(
        "Verify no transactions exist for non-existent sender account in database",
        () ->
            softly
                .assertThat(DataBaseSteps.getAccountTransactions(nonExistentAccountId))
                .isEmpty());
  }
}
