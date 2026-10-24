package api.models;

import java.math.BigDecimal;

public enum DepositBalance {

    NEGATIVE("-0.10"),
    ZERO("0.00"),
    MIN_ALLOWED("0.01"),
    ALLOWED("100.00"),
    JUST_BELOW_MAXIMUM("4999.99"),
    MAX_ALLOWED("5000.00"),
    ABOVE_MAXIMUM("5000.10");

    private final BigDecimal balance;

    DepositBalance(String balance) {
        this.balance = new BigDecimal(balance);
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
