package com.example.serverProject;

import com.example.serverProject.Exceptions.UserAlreadyExistsException;
import com.example.serverProject.Objects.Post;
import com.example.serverProject.Objects.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DbUtils {
    @Autowired
    private CustomConnectionPool connectionPool;

    public boolean registerUser(String login, String password, String token) throws UserAlreadyExistsException, SQLException {
        String insertQuery = "INSERT INTO users (login, password,token) VALUES (?, ?, ?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, token);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            if (connection != null) {
                throw new UserAlreadyExistsException(e);
            }
            throw new SQLException(e);
        } finally {
            closeResources(connection, preparedStatement, null);
        }
    }

    public User getUserToken(String login, String password, BCryptPasswordEncoder bCryptPasswordEncoder) {
        String selectQuery = "SELECT * FROM users WHERE login=?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, login);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (bCryptPasswordEncoder.matches((password), resultSet.getString("password"))) {
                    return new User(resultSet.getInt("id"), resultSet.getString("login"),
                            resultSet.getString("token"), resultSet.getString("image_url"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return null;
    }

    public Integer getUserIdByName(String name) {
        String query = "SELECT id FROM users WHERE login=?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return null;
    }

    public boolean addFollower(int followerId, int followeId) throws SQLException {
        String query = "INSERT INTO followers_followe VALUES(?,?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, followerId);
            preparedStatement.setInt(2, followeId);
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, null);
        }
    }

    public List<String> searchByName(String name) {
        String query = "SELECT login FROM users WHERE login LIKE ?";
        Connection connection = null;
        List<String> resultList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name + '%');
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String userName = resultSet.getString("login");
                resultList.add(userName);
            }
            Collections.sort(resultList);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return resultList;
    }

    public boolean checkToken(String token) {
        String query = "SELECT login FROM users WHERE token=?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, token);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (Exception e) {
            return false;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
    }

    public boolean publishPost(String text, int userId) {
        String query = "INSERT INTO user_posts(publish_date,text,user_id) VALUES (?,?,?)";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            preparedStatement.setTimestamp(1, currentTimestamp);
            preparedStatement.setString(2, text);
            preparedStatement.setInt(3, userId);
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, null);
        }
    }

    public User getUserByName(String name) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = "SELECT id, login, image_url FROM users WHERE login=?";
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(resultSet.getInt("id"), resultSet.getString("login"),
                        null, resultSet.getString("image_url"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    public List<Post> getFeed(List<String> userNames) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Post> result = new ArrayList<>();
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement("SELECT user_posts.*, users.login " +
                    "FROM user_posts JOIN users ON user_posts.user_id = users.id " +
                    "WHERE users.login IN (" + preparePlaceholders(userNames.size()) + ") ORDER BY user_posts.publish_date desc LIMIT 20");
            for (int i = 0; i < userNames.size(); i++) {
                preparedStatement.setString(i + 1, userNames.get(i));
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Post post = new Post(resultSet.getString("text"), resultSet.getTimestamp("publish_date"),
                        resultSet.getString("login"));
                result.add(post);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return result;
    }

    private String preparePlaceholders(int length) {
        if (length < 1) {
            throw new RuntimeException("No placeholders");
        } else {
            return "?" + ", ?".repeat(length - 1);
        }
    }

    private void closeResources(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getUserRelations(int id, boolean followers) {
        String query = "SELECT u.login FROM users u JOIN followers_followe ff ON u.id = ff.follower_id WHERE ff.followee_id=?";
        if (!followers) {
            query = "SELECT u.login FROM users u JOIN followers_followe ff ON u.id = ff.followee_id WHERE ff.follower_id=?";
        }
        Connection connection = null;
        List<String> resultList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String userName = resultSet.getString("login");
                resultList.add(userName);
            }
            Collections.sort(resultList);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        return resultList;
    }

    public boolean updateImageUrl(String url, int userId) {
        String query = "UPDATE users SET image_url = ? WHERE id = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, url);
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
