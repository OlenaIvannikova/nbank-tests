package api.models;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest extends BaseModel {

  private long senderAccountId;
  private long receiverAccountId;
  private BigDecimal amount;
}
