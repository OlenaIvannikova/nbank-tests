package api.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/*
SQL вместе с параметрами
Например,
   "SELECT * FROM customers WHERE username = ?",
   List.of("alex123")
*/
@AllArgsConstructor
@Data
@Getter
public class SqlQuery {
    String sql;
    List<Object> parameters;
}
