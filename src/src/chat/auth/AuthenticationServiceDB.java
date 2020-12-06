package src.chat.auth;

import src.chat.db.DBService;
import src.chat.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationServiceDB implements AuthenticationService {


    @Override
    public Optional<User> doAuth(String login, String password) {
        Connection connection = DBService.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM user WHERE email = ? AND password = ?"
            );

            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(new User(resultSet.getString("nickname"),
                              resultSet.getString("email"),
                                resultSet.getString("password")
                        ));
            }
            return Optional.empty();
        } catch (SQLException throwables) {
            throw new RuntimeException("SWW", throwables);
        } finally {
            DBService.close(connection);
        }
    }
}
