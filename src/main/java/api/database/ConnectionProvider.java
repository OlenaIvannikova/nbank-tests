package api.database;

import common.configs.Config;
import common.configs.Property;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Подключается к БД
public final class ConnectionProvider {

  private ConnectionProvider() {}

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        Config.getProperty(Property.DB_URL),
        Config.getProperty(Property.DB_LOGIN),
        Config.getProperty(Property.DB_PASSWORD));
  }
}
