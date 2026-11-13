package api.database;

import api.dao.AccountDao;
import java.sql.ResultSet;
import java.sql.SQLException;

// Читает строки ResultSet и создаёт объект AccountDao
public class AccountRowMapper implements RowMapper<AccountDao> {
  @Override
  public AccountDao map(ResultSet resultSet) throws SQLException {
    return AccountDao.builder()
        .id(resultSet.getLong(DbColumn.ID.getColumnName()))
        .accountNumber(resultSet.getString(DbColumn.ACCOUNT_NUMBER.getColumnName()))
        .balance(resultSet.getBigDecimal(DbColumn.BALANCE.getColumnName()))
        .customerId(resultSet.getLong(DbColumn.CUSTOMER_ID.getColumnName()))
        .build();
  }
}
