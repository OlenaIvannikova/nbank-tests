package api.database;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// Хранит таблицу, тип запроса и условия
@Getter
@Builder
public class DBRequest {
    private RequestType requestType;
    private Table table;
    private List<Condition> conditions;

    public <T> T extractAs(Class<T> clazz) {
        SqlQuery query = SqlBuilder.build(this);
        return DBExecutor.executeQuery(query, RowMappers.get(clazz));
    }

    public <T> List<T> extractAllAs(Class<T> clazz) {
        SqlQuery query = SqlBuilder.build(this);
        return DBExecutor.executeQueryList(
                query,
                RowMappers.get(clazz)
        );
    }
}
