package api.database;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

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
