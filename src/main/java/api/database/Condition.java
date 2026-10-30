package api.database;

import lombok.AllArgsConstructor;
import lombok.Data;

// описывает условие, например username = ?
@Data
@AllArgsConstructor
public class Condition {
    private final DbColumn column;
    private final String operator;
    private final Object value;

    public static Condition equalTo(DbColumn column, Object value) {
        return new Condition(column, "=", value);
    }

    public static Condition notEqualTo(DbColumn column, Object value) {
        return new Condition(column, "!=", value);
    }

    public static Condition like(DbColumn column, String value) {
        return new Condition(column, "LIKE", value);
    }
}
