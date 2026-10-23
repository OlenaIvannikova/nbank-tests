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
public class TransferResponse extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private BigDecimal amount;
    private String message;
}


