package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
