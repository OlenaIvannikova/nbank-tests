package api.assertions;

import api.models.AccountResponse;
import api.models.TransactionType;
import common.helpers.StepLogger;
import java.math.BigDecimal;
import org.assertj.core.api.SoftAssertions;

public class AccountAssertions {

  private AccountAssertions() {}

  public static void assertDepositSuccessful(
      SoftAssertions softly, AccountResponse account, BigDecimal expectedBalance, Long accountId) {

    StepLogger.log(
        "Verify account balance after deposit",
        () -> softly.assertThat(account.getBalance()).isEqualByComparingTo(expectedBalance));

    StepLogger.log(
        "Verify deposit transaction exists",
        () ->
            TransactionAssertions.assertTransactionExists(
                softly,
                account.getTransactions(),
                expectedBalance,
                TransactionType.DEPOSIT,
                accountId));
  }
}
