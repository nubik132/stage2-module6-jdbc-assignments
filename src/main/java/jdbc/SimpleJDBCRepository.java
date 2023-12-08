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
    private final String[] regexes = {"_id", "_firstName", "_lastName", "_age"};
    public Long createUser(User user) throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();

        String sql = applyUserDataToSql(createUserSQL, user);

        connection.createStatement().execute(sql);
        return user.getId();
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

    public User updateUser(User user) throws SQLException {
        connection = CustomDataSource.getInstance().getConnection();
        String sql = updateUserSQL;

        sql = applyUserDataToSql(sql, user);

        ResultSet rs = connection.createStatement().executeQuery(sql);
        return buildUser(rs);
    }

    public void deleteUser(Long userId) throws SQLException {
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

    private String[] userToStrings(User user){
        String[] strings = new String[4];
        strings[0] = String.valueOf(user.getId());
        strings[1] = String.valueOf(user.getFirstName());
        strings[2] = String.valueOf(user.getLastName());
        strings[3] = String.valueOf(user.getAge());
        return strings;
    }

    private String replaceAll(String string, String[] regexes, String[] params){
        for (int i = 0; i < regexes.length; i++) {
            string = string.replaceFirst(regexes[i], params[i]);
        }
        return string;
    }

    private String applyUserDataToSql(String sql, User user){
        String[] userData = userToStrings(user);
        return replaceAll(sql, regexes, userData);
    }
}
