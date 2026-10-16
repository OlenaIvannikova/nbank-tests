package api.testdata;

import models.TransferAmount;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static constants.messages.ValidationMessage.*;

public class TransferValidationTestData {
    public static Stream<Arguments> validTransferAmount() {
        return Stream.of(
                Arguments.of(
                        TransferAmount.MIN_ALLOWED.getAmount()
                ),
                Arguments.of(
                        TransferAmount.MAX_ALLOWED.getAmount()
                )
        );
    }

    public static Stream<Arguments> invalidTransferAmount() {
        return Stream.of(
                Arguments.of(
                        TransferAmount.NEGATIVE.getAmount(),
                        TRANSFER_AMOUNT_TOO_SMALL.getValue()
                ),
                Arguments.of(
                        TransferAmount.ZERO.getAmount(),
                        TRANSFER_AMOUNT_TOO_SMALL.getValue()
                ),
                Arguments.of(
                        TransferAmount.ABOVE_MAXIMUM.getAmount(),
                        TRANSFER_AMOUNT_EXCEEDS_LIMIT.getValue()
                )
        );
    }
}
