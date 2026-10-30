package api.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DbColumn {

    // TABLE: customers
    ID("id"),
    USERNAME("username"),
    PASSWORD("password"),
    ROLE("role"),
    NAME("name"),

    // TABLE: accounts
    ACCOUNT_NUMBER("account_number"),
    BALANCE("balance"),
    CUSTOMER_ID("customer_id"),

    // TABLE: transactions
    ACCOUNT_ID("account_id"),
    TRANSACTION_ID("id"),
    AMOUNT("amount"),
    TRANSACTION_TYPE("type"),
    RELATED_ACCOUNT_ID("related_account_id");

    private final String columnName;
}
