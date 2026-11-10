package api.assertions;

import api.models.AccountResponse;
import api.models.TransactionType;
import java.math.BigDecimal;
import org.assertj.core.api.SoftAssertions;

public class AccountAssertions {

  private AccountAssertions() {}

  public static void assertDepositSuccessful(
      SoftAssertions softly, AccountResponse account, BigDecimal expectedBalance, Long accountId) {

    softly.assertThat(account.getBalance()).isEqualByComparingTo(expectedBalance);

    TransactionAssertions.assertTransactionExists(
        softly, account.getTransactions(), expectedBalance, TransactionType.DEPOSIT, accountId);
  }
}
