package com.financeapp;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class DatabaseManager {
    
    private static final String PREF_KEY_DB_PATH = "last_selected_db_path";
    private static final Preferences prefs = Preferences.userNodeForPackage(DatabaseManager.class);
    private static Connection activeConnection = null;
    
    public static String getDatabasePath() {
        String defaultPath = System.getProperty("user.home")
                + File.separator + ".myfinance"
                + File.separator + "finance.db";
        return prefs.get(PREF_KEY_DB_PATH, defaultPath);
    }

    public static void setDatabasePath(String newPath) {
        prefs.put(PREF_KEY_DB_PATH, newPath);
    }

    public static synchronized Connection getConnection() throws  SQLException {
        if (activeConnection == null || activeConnection.isClosed()) {
            activeConnection = createNewConnection(getDatabasePath());
        }
        return activeConnection;
    }

    private static Connection createNewConnection(String databasePath) throws SQLException {
        File dbFile = new File(databasePath);

        if(dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
            dbFile.getParentFile().mkdirs();
        }
        String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        return DriverManager.getConnection(dbUrl);

    }

    public static synchronized void switchDatabase(String newAbsolutePath) throws SQLException {
        closeConnection();
        setDatabasePath(newAbsolutePath);
        activeConnection = createNewConnection(newAbsolutePath);
        initializeDatabase();
    }

    private static void closeConnection() {
        if (activeConnection != null) {
            try {
                if (!activeConnection.isClosed()) {
                    activeConnection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                activeConnection = null;
            }
        }
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

    /*
    ------------------
    CRUD Operations
    ------------------
    */
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
