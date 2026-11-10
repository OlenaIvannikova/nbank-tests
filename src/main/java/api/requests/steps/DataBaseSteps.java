package api.requests.steps;

import api.dao.AccountDao;
import api.dao.TransactionDao;
import api.dao.UserDao;
import api.database.*;
import api.models.TransactionType;
import java.util.List;

public class DataBaseSteps {

  public static UserDao getUserByUsername(String username) {
    return DBRequest.builder()
        .requestType(RequestType.SELECT)
        .table(Table.CUSTOMERS)
        .conditions(List.of(Condition.equalTo(DbColumn.USERNAME, username)))
        .build()
        .extractAs(UserDao.class);
  }

  public static AccountDao getAccountByAccountNumber(String accountNumber) {
    return DBRequest.builder()
        .requestType(RequestType.SELECT)
        .table(Table.ACCOUNTS)
        .conditions(List.of(Condition.equalTo(DbColumn.ACCOUNT_NUMBER, accountNumber)))
        .build()
        .extractAs(AccountDao.class);
  }

  public static AccountDao getAccountById(long accountId) {
    return DBRequest.builder()
        .requestType(RequestType.SELECT)
        .table(Table.ACCOUNTS)
        .conditions(List.of(Condition.equalTo(DbColumn.ID, accountId)))
        .build()
        .extractAs(AccountDao.class);
  }

  public static List<TransactionDao> getAccountTransactions(long accountId) {
    return DBRequest.builder()
        .requestType(RequestType.SELECT)
        .table(Table.TRANSACTIONS)
        .conditions(List.of(Condition.equalTo(DbColumn.ACCOUNT_ID, accountId)))
        .build()
        .extractAllAs(TransactionDao.class);
  }

  public static List<TransactionDao> getAccountTransactionsByTransactionType(
      long аccountId, TransactionType type) {
    return DBRequest.builder()
        .requestType(RequestType.SELECT)
        .table(Table.TRANSACTIONS)
        .conditions(
            List.of(
                Condition.equalTo(DbColumn.ACCOUNT_ID, аccountId),
                Condition.equalTo(DbColumn.TRANSACTION_TYPE, type.name())))
        .build()
        .extractAllAs(TransactionDao.class);
  }
}
