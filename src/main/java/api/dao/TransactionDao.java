package api.dao;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDao {
  private long id;
  private BigDecimal amount;
  private String type;
  private long accountId;
  private long relatedAccountId;
}
