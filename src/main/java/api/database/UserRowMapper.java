package api.database;

import api.dao.UserDao;

import java.sql.ResultSet;
import java.sql.SQLException;

// Читает строки ResultSet и создаёт объект UserDao
public class UserRowMapper implements RowMapper<UserDao> {
    @Override
    public UserDao map(ResultSet resultSet) throws SQLException {
        return UserDao.builder()
                .id(resultSet.getLong(DbColumn.ID.getColumnName()))
                .username(resultSet.getString(DbColumn.USERNAME.getColumnName()))
                .password(resultSet.getString(DbColumn.PASSWORD.getColumnName()))
                .role(resultSet.getString(DbColumn.ROLE.getColumnName()))
                .name(resultSet.getString(DbColumn.NAME.getColumnName()))
                .build();
    }
}
