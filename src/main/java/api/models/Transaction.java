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
public class Transaction extends BaseModel {

    private long id;
    private BigDecimal amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;

}
