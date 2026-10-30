package api.database;

import java.sql.ResultSet;
import java.sql.SQLException;

// интерфейс преобразования строки результата
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet resultSet) throws SQLException;
}

