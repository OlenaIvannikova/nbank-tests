package api.models;

import java.math.BigDecimal;

public enum TransferAmount {

    NEGATIVE("-0.10"),
    ZERO("0.00"),
    MIN_ALLOWED("0.01"),
    MAX_ALLOWED("10000.00"),
    ABOVE_MAXIMUM("10000.10");

    private final BigDecimal amount;

    TransferAmount(String amount) {
        this.amount = new BigDecimal(amount);
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
