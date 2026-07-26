package com.financeapp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @SuppressWarnings("exports")
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
        Connection conn = DriverManager.getConnection(dbUrl);
        // Force Foreign Keys ON immediately
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
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
        String createAccountSQL = """
                CREATE TABLE IF NOT EXISTS Account (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
                """;
        
        String createCategorySQL = """
                CREATE TABLE IF NOT EXISTS Category (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
                """;
        
        String createSubCategorySQL = """
                CREATE TABLE IF NOT EXISTS Subcategory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category_id INTEGER NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES Category(id) ON DELETE CASCADE    
                )
                """;
        try ( Statement stmt = getConnection().createStatement() ) {
            // Create all tables
            stmt.execute(createAccountSQL);
            stmt.execute(createCategorySQL);
            stmt.execute(createSubCategorySQL);
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /*
    ---------------------------
    CRUD Operations - Account
    ---------------------------
    */
    public static List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT id, name FROM ACCOUNT ORDER BY name ASC";
        try (
                Statement stmt = getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql);
            ) {
            while (rs.next()) {
                accounts.add(new Account(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  accounts;
    }

    public static boolean addAccount(String name){
        String sql = "INSERT INTO Account(name) VALUES(?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
        {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding Account: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAccount(int id, String name){
        String sql = "UPDATE Account SET NAME = ? WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
        {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteAccount(int id){
        String sql = "DELETE FROM Account WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
        {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            return false;
        }
    }
    
    /*
    ---------------------------
    CRUD Operations - Category
    ---------------------------
    */
    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM CATEGORY ORDER BY name ASC";
        try (
                Statement stmt = getConnection().createStatement();
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
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
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
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
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
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql))
        {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    /*
    ------------------------------
    CRUD Operations - Subcategory
    ------------------------------
    */
    public static List<Subcategory> getAllSubcategories() {
        List<Subcategory> subcategories = new ArrayList<>();
        String sql = """
            SELECT s.id, s.name, s.category_id, c.name AS cat_name
            FROM Subcategory s
            JOIN Category c ON s.category_id = c.id
            ORDER BY s.name ASC
        """;
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subcategories.add(new Subcategory(
                            rs.getInt("id"), 
                            rs.getString("name"), 
                            rs.getInt("category_id"),
                            rs.getString("cat_name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subcategories: " + e.getMessage());
        }
        return subcategories;
    }

    public static List<Subcategory> getSubcategoriesByCategory(int categoryId) {
        List<Subcategory> subcategories = new ArrayList<>();
        String sql = "SELECT id, name, category_id FROM Subcategory WHERE category_id = ? ORDER BY name ASC";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subcategories.add(new Subcategory(
                            rs.getInt("id"), 
                            rs.getString("name"), 
                            rs.getInt("category_id"),
                            rs.getString("c.name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subcategories: " + e.getMessage());
        }
        return subcategories;
    }

    public static boolean addSubcategory(String name, int categoryId) {
        String sql = "INSERT INTO Subcategory(name, category_id) VALUES(?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, categoryId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding subcategory: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSubcategory(int id, String name, int categoryId) {
        String sql = "UPDATE Subcategory SET name = ?, category_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating subcategory: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteSubcategory(int id) {
        String sql = "DELETE FROM Subcategory WHERE id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting subcategory: " + e.getMessage());
            return false;
        }
    }





    
}
