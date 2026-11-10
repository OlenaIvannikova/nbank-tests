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
public class DepositRequest extends BaseModel {

  private long id;
  private BigDecimal balance;

  private Long accountId;
  private BigDecimal amount;
  private String description;
}
