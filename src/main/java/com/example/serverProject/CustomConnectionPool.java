package com.example.serverProject;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.example.serverProject.Configuration.*;

@Component
public class CustomConnectionPool {

    private final List<Connection> connections;

    public CustomConnectionPool() {
        connections = new ArrayList<>(CONNECTION_POOL_SIZE);

        for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {
            try {
                Connection connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USERNAME, MYSQL_PASSWORD);
                connections.add(connection);
            } catch (SQLException e) {
                System.out.println("Failed to create connection: " + e.getMessage());
            }
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!connections.isEmpty()) {
            return connections.remove(connections.size() - 1);
        }
        return DriverManager.getConnection(MYSQL_URL, MYSQL_USERNAME, MYSQL_PASSWORD);
    }

    public synchronized void releaseConnection(Connection connection) {
        try {
            if (!connection.isClosed()) {
                connections.add(connection);
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve connection: " + e.getMessage());
        }
    }

    public synchronized void close() throws SQLException {
        for (Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }
}
