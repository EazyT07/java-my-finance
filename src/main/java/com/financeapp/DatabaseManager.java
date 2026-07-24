package com.financeapp;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    
    private static File currentDbFile = new File("my_finance_tomek.db");
    
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

    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM CATEGORY ORDER BY name ASC";
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
            ) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  categories;
    }

    public static boolean addCategory(String name){
        String sql = "INSERT INTO Category(name) VALUES(?)";
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
            )
        {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateCategory(int id, String name){
        String sql = "UPDATE Category SET NAME = ? WHERE id =?";
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        )
        {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteCategory(int id){
        String sql = "DELETE FROM Category WHERE id =?";
        try (
                Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        )
        {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }





    
}
