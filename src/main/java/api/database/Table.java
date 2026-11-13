package api.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Table {
  ACCOUNTS("accounts"),
  CUSTOMERS("customers"),
  TRANSACTIONS("transactions");

  private final String tableName;
}
