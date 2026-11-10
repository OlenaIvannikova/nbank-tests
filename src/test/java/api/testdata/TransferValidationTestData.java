package api.testdata;

import static api.constants.messages.ValidationMessage.TRANSFER_AMOUNT_EXCEEDS_LIMIT;
import static api.constants.messages.ValidationMessage.TRANSFER_AMOUNT_TOO_SMALL;

import api.models.TransferAmount;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public class TransferValidationTestData {
  public static Stream<Arguments> validTransferAmount() {
    return Stream.of(
        Arguments.of(TransferAmount.MIN_ALLOWED.getAmount()),
        Arguments.of(TransferAmount.JUST_BELOW_MAXIMUM.getAmount()),
        Arguments.of(TransferAmount.MAX_ALLOWED.getAmount()));
  }

  public static Stream<Arguments> invalidTransferAmount() {
    return Stream.of(
        Arguments.of(TransferAmount.NEGATIVE.getAmount(), TRANSFER_AMOUNT_TOO_SMALL.getValue()),
        Arguments.of(TransferAmount.ZERO.getAmount(), TRANSFER_AMOUNT_TOO_SMALL.getValue()),
        Arguments.of(
            TransferAmount.ABOVE_MAXIMUM.getAmount(), TRANSFER_AMOUNT_EXCEEDS_LIMIT.getValue()));
  }
}
