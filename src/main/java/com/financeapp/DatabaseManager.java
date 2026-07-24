package com.financeapp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    
    private static File currentDbFile = new File("my_finance_1.db");
    
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + currentDbFile.getAbsolutePath();
        return DriverManager.getConnection(url);
    }

    public static void switchDatabase(File newDbFile) {
        if (newDbFile == null) return;
        currentDbFile = newDbFile;
        System.out.println("Switched to DB: " + currentDbFile.getName());

        initializeDatabase();
    }

    public static String getCurrentDatabaseName() {
        return currentDbFile.getName();
    }

    public static void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS Category (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
                """;
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    
}
