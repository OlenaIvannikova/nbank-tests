package api.models;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse extends BaseModel {

  private Long id;
  private String accountNumber;
  private BigDecimal balance;
  private List<Transaction> transactions;
}
