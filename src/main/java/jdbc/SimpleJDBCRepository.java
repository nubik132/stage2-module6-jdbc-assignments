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
    private static final String deleteUser = "DELETE FROM myusers WHERE id = _id";
    private static final String findUserByIdSQL = "SELECT * FROM myusers WHERE id = _id";
    private static final String findUserByNameSQL = "SELECT * FROM myusers WHERE firstname like '_firstname'";
    private static final String findAllUserSQL = "SELECT * FROM myusers";
    private final String[] regexes = {"_id", "_firstname", "_lastname", "_age"};

    public Long createUser(User user) {
        try {
            connection = CustomDataSource.getInstance().getConnection();

            String sql = applyUserDataToSql(createUserSQL, user);

            connection.createStatement().execute(sql);
            connection.close();

            return user.getId();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

    }

    public User findUserById(Long userId) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
            ResultSet rs = connection.createStatement().executeQuery(findUserByIdSQL.replaceFirst("_id", userId + ""));
            rs.next();
            User user = buildUser(rs);
            connection.close();
            rs.close();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public User findUserByName(String userName) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
            ResultSet rs = connection.createStatement().executeQuery(findUserByNameSQL.replaceFirst("_firstname", userName));
            rs.next();
            User user = buildUser(rs);
            connection.close();
            rs.close();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public List<User> findAllUser() {
        List<User> users = new ArrayList<>();
        try (Connection con = CustomDataSource.getInstance().getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(findAllUserSQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User updateUser(User user) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
            String sql = updateUserSQL;

            sql = applyUserDataToSql(sql, user);

            connection.createStatement().executeUpdate(sql);
            connection.close();
            return findUserById(user.getId());
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public void deleteUser(Long userId) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
            connection.createStatement().execute(deleteUser.replaceFirst("_id", String.valueOf(userId)));
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private User buildUser(ResultSet rs) {
        try {
            return new User(
                    rs.getLong("id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getInt("age"));
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private String[] userToStrings(User user) {
        String[] strings = new String[4];
        strings[0] = String.valueOf(user.getId());
        strings[1] = String.valueOf(user.getFirstName());
        strings[2] = String.valueOf(user.getLastName());
        strings[3] = String.valueOf(user.getAge());
        return strings;
    }

    private String replaceAll(String string, String[] regexes, String[] params) {
        for (int i = 0; i < regexes.length; i++) {
            string = string.replaceFirst(regexes[i], params[i]);
        }
        return string;
    }

    private String applyUserDataToSql(String sql, User user) {
        String[] userData = userToStrings(user);
        return replaceAll(sql, regexes, userData);
    }
}
