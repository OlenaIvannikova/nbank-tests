package api.database;

import api.dao.AccountDao;
import api.dao.TransactionDao;
import api.dao.UserDao;
import java.util.Map;

// находит нужный mapper для класса UserDao/AccountDao
public final class RowMappers {

  // хранит соответствие между DAO-классом и его mapper
  private static final Map<Class<?>, RowMapper<?>> MAPPERS =
      Map.of(
          UserDao.class, new UserRowMapper(),
          AccountDao.class, new AccountRowMapper(),
          TransactionDao.class, new TransactionRowMapper());

  private RowMappers() {}

  @SuppressWarnings("unchecked")
  public static <T> RowMapper<T> get(Class<T> clazz) {
    RowMapper<?> mapper = MAPPERS.get(clazz);

    if (mapper == null) {
      throw new IllegalArgumentException("No RowMapper registered for " + clazz.getSimpleName());
    }

    return (RowMapper<T>) mapper;
  }
}
