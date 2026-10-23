package api.assertions;

import api.models.Transaction;
import api.models.TransactionType;
import org.assertj.core.api.SoftAssertions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionAssertions {

    private TransactionAssertions() {
    }

    public static void assertTransactionExists(
            SoftAssertions softly,
            List<Transaction> transactions,
            BigDecimal expectedAmount,
            TransactionType expectedType,
            Long expectedAccountId) {

        softly.assertThat(transactions)
                .anySatisfy(transaction -> {
                    assertThat(transaction.getAmount())
                            .isEqualByComparingTo(expectedAmount);

                    assertThat(TransactionType.valueOf(transaction.getType()))
                            .isEqualTo(expectedType);

                    assertThat(transaction.getRelatedAccountId())
                            .isEqualTo(expectedAccountId);
                });
    }

    public static void assertTransactionDoesNotExist(
            SoftAssertions softly,
            List<Transaction> transactions,
            BigDecimal expectedAmount,
            TransactionType expectedType,
            Long expectedRelatedAccountId) {

        softly.assertThat(transactions)
                .noneMatch(transaction ->
                        transaction.getAmount().compareTo(expectedAmount) == 0
                                && TransactionType.valueOf(transaction.getType()) == expectedType
                                && Objects.equals(
                                transaction.getRelatedAccountId(),
                                expectedRelatedAccountId
                        )
                );
    }
}
