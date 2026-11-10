package api.database;

import java.util.ArrayList;
import java.util.List;

// Формирует SQL и список параметров
public class SqlBuilder {
  private SqlBuilder() {}

  public static SqlQuery build(DBRequest request) {
    if (request.getRequestType() != RequestType.SELECT) {
      throw new UnsupportedOperationException(
          "Request type not implemented: " + request.getRequestType());
    }

    StringBuilder sql = new StringBuilder("SELECT * FROM " + request.getTable().getTableName());

    List<Object> parameters = new ArrayList<>();
    List<Condition> conditions = request.getConditions();

    if (conditions != null && !conditions.isEmpty()) {
      sql.append(" WHERE ");

      for (int i = 0; i < conditions.size(); i++) {
        Condition condition = conditions.get(i);

        if (i > 0) {
          sql.append(" AND ");
        }

        sql.append(condition.getColumn().getColumnName())
            .append(" ")
            .append(condition.getOperator())
            .append(" ?");

        parameters.add(condition.getValue());
      }
    }

    return new SqlQuery(sql.toString(), parameters);
  }
}
