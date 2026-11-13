package api.testdata;

import static api.constants.messages.ValidationMessage.DEPOSIT_AMOUNT_EXCEEDS_LIMIT;
import static api.constants.messages.ValidationMessage.DEPOSIT_AMOUNT_TOO_SMALL;

import api.models.DepositBalance;
import common.generators.RandomData;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class DepositValidationTestData {

  public static Stream<Arguments> validDepositBalance() {
    return Stream.of(
        Arguments.of(DepositBalance.MIN_ALLOWED.getBalance()),
        Arguments.of(RandomData.generateRandomDepositAmount()),
        Arguments.of(DepositBalance.JUST_BELOW_MAXIMUM.getBalance()),
        Arguments.of(DepositBalance.MAX_ALLOWED.getBalance()));
  }

  public static Stream<Arguments> invalidDepositBalance() {
    return Stream.of(
        Arguments.of(DepositBalance.NEGATIVE.getBalance(), DEPOSIT_AMOUNT_TOO_SMALL.getValue()),
        Arguments.of(DepositBalance.ZERO.getBalance(), DEPOSIT_AMOUNT_TOO_SMALL.getValue()),
        Arguments.of(
            DepositBalance.ABOVE_MAXIMUM.getBalance(), DEPOSIT_AMOUNT_EXCEEDS_LIMIT.getValue()));
  }
}
