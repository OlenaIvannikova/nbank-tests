package api.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// выполняет SQL
public class DBExecutor {

  private DBExecutor() {}

  public static <T> T executeQuery(SqlQuery query, RowMapper<T> mapper) {
    try (Connection connection = ConnectionProvider.getConnection();
        // 1. создаёт PreparedStatement (подготавливает SQL)
        PreparedStatement statement = connection.prepareStatement(query.getSql())) {

      List<Object> parameters = query.getParameters();

      // 2. подставляет параметры (передаёт значение параметров)
      for (int i = 0; i < parameters.size(); i++) {
        statement.setObject(i + 1, parameters.get(i));
      }

      // 3. отправляет запрос в БД, выполняет его и получает ResultSet
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return mapper.map(
              rs); // 4. передаёт ResultSet соответствующему mapper(преобразует текущую строку в
          // Java-объект)
        }
        return null;
      }

    } catch (SQLException e) {
      throw new RuntimeException("Database query failed", e);
    }
  }

  public static <T> List<T> executeQueryList(SqlQuery query, RowMapper<T> rowMapper) {
    List<T> results = new ArrayList<>();

    try (Connection connection = ConnectionProvider.getConnection();
        PreparedStatement statement = connection.prepareStatement(query.getSql())) {

      for (int i = 0; i < query.getParameters().size(); i++) {
        statement.setObject(i + 1, query.getParameters().get(i));
      }

      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          results.add(rowMapper.map(resultSet));
        }
      }

      return results;

    } catch (SQLException e) {
      throw new RuntimeException("Failed to execute SQL query", e);
    }
  }
}
