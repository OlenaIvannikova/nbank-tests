package api.models;

import java.math.BigDecimal;

public enum TransferAmount {
  NEGATIVE("-0.01"),
  ZERO("0.00"),
  MIN_ALLOWED("0.01"),
  JUST_BELOW_MAXIMUM("9999.99"),
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
