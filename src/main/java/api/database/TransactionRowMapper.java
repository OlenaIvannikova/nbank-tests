package api.database;

import api.dao.TransactionDao;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRowMapper implements RowMapper<TransactionDao> {
  @Override
  public TransactionDao map(ResultSet resultSet) throws SQLException {
    return TransactionDao.builder()
        .id(resultSet.getLong(DbColumn.TRANSACTION_ID.getColumnName()))
        .amount(resultSet.getBigDecimal(DbColumn.AMOUNT.getColumnName()))
        .type(resultSet.getString(DbColumn.TRANSACTION_TYPE.getColumnName()))
        .accountId(resultSet.getLong(DbColumn.ID.getColumnName()))
        .relatedAccountId(resultSet.getLong(DbColumn.RELATED_ACCOUNT_ID.getColumnName()))
        .build();
  }
}
