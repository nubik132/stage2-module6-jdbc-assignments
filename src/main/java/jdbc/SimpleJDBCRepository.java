package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String createUserSQL = "INSERT INTO myusers (firstname, lastname, age) VALUES ('_firstname', '_lastname', _age)";
    private static final String updateUserSQL = "UPDATE myusers SET firstname = '_firstname', lastname = '_lastname', age = _age WHERE id = _id";
    private static final String deleteUser = "DELETE FROM myusers WHERE";
    private static final String findUserByIdSQL = "SELECT * FROM myusers WHERE id = _id";
    private static final String findUserByNameSQL = "SELECT * FROM myusers WHERE name like '_name'";
    private static final String findAllUserSQL = "SELECT * FROM myusers";

    public Long createUser() throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        connection.createStatement().execute(createUserSQL);
        return null;
    }

    public User findUserById(Long userId) throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        ResultSet rs = connection.createStatement().executeQuery(findUserByIdSQL.replaceFirst("_id", userId + ""));
        return buildUser(rs);
    }

    public User findUserByName(String userName) throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        ResultSet rs = connection.createStatement().executeQuery(findUserByNameSQL.replaceFirst("_name", userName));
        return buildUser(rs);
    }

    public List<User> findAllUser() throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        ResultSet rs = connection.createStatement().executeQuery(findAllUserSQL);
        List<User> users = new ArrayList<>();
        while(rs.next()){
            users.add(buildUser(rs));
        }
        return users;
    }

    public User updateUser() throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        String sql = updateUserSQL;
        sql = sql.replaceFirst("_id", "299");
        sql = sql.replaceFirst("_firstname", "");
        sql = sql.replaceFirst("_lastname", "");
        sql = sql.replaceFirst("_age", "22");
        ResultSet rs = connection.createStatement().executeQuery(sql);
        return buildUser(rs);
    }

    private void deleteUser(Long userId) throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        connection.createStatement().execute(deleteUser.replaceFirst("_id", userId + ""));
    }

    private User buildUser (ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("firstname"))
                .lastName(rs.getString("lastname"))
                .age(rs.getInt("age"))
                .build();
    }
}
