package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private BigDecimal amount;

    public static TransferRequest createTransferRequest(
            AccountResponse sender,
            AccountResponse receiver,
            BigDecimal amount) {

        return TransferRequest.builder()
                .senderAccountId(sender.getId())
                .receiverAccountId(receiver.getId())
                .amount(amount)
                .build();
    }

}
