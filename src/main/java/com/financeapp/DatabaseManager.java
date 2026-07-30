package com.financeapp;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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
    public static synchronized Connection getConnection() throws SQLException {
        if (activeConnection == null || activeConnection.isClosed()) {
            activeConnection = createNewConnection(getDatabasePath());
        }
        return activeConnection;
    }

    private static Connection createNewConnection(String databasePath) throws SQLException {
        File dbFile = new File(databasePath);

        if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
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
                System.err.println("Error closing connection: " + e.getMessage());
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
        String createTransactionSQL = """
                CREATE TABLE IF NOT EXISTS "AppTransaction" (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_id INTEGER NOT NULL,
                    date DATE NOT NULL,
                    type TEXT,
                    description TEXT,
                    subcategory_id INTEGER NOT NULL,
                    amount REAL,
                    FOREIGN KEY (account_id) REFERENCES Account(id) ON DELETE CASCADE,
                    FOREIGN KEY (subcategory_id) REFERENCES Subcategory(id) ON DELETE CASCADE,
                    CHECK (type IN ('INC', 'EXP'))
                );
                """;
        try (Statement stmt = getConnection().createStatement()) {
            // Create all tables
            stmt.execute(createAccountSQL);
            stmt.execute(createCategorySQL);
            stmt.execute(createSubCategorySQL);
            stmt.execute(createTransactionSQL);
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
                Statement stmt = getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()) {
                accounts.add(new Account(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving accounts: " + e.getMessage());
        }
        return accounts;
    }

    public static boolean addAccount(String name) {
        String sql = "INSERT INTO Account(name) VALUES(?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding Account: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAccount(int id, String name) {
        String sql = "UPDATE Account SET NAME = ? WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating account: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteAccount(int id) {
        String sql = "DELETE FROM Account WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
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
                Statement stmt = getConnection().createStatement(); ResultSet rs = stmt.executeQuery(sql);) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
        }
        return categories;
    }

    public static boolean addCategory(String name) {
        String sql = "INSERT INTO Category(name) VALUES(?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateCategory(int id, String name) {
        String sql = "UPDATE Category SET NAME = ? WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteCategory(int id) {
        String sql = "DELETE FROM Category WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
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

    /*
    ------------------------------
    CRUD Operations - Transaction
    ------------------------------
     */
    public static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT 
            t.id, t.account_id, a.name AS acc_name,
            t.date, t.type, t.description, t.subcategory_id,
            s.name AS subcat_name, c.name AS cat_name, t.amount
            FROM AppTransaction t
            JOIN Account a ON t.account_id = a.id
            JOIN Subcategory s ON t.subcategory_id = s.id 
            JOIN Category c ON s.category_id = c.id
            ORDER BY t.date ASC
        """;

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("account_id"),
                            rs.getString("acc_name"),
                            rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getInt("subcategory_id"),
                            rs.getString("subcat_name"),
                            rs.getString("cat_name"),
                            rs.getBigDecimal("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subcategories: " + e.getMessage());
        }
        return transactions;
    }

    public static boolean addTransaction(int accountId, LocalDate date, String type,
            String description, int subcategoryId, BigDecimal amount) {
        String sql = """
                INSERT INTO AppTransaction(account_id, date, type, description, subcategory_id, amount)
                VALUES(?, ?, ?, ?, ?, ?)
                
                """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setString(3, type);
            pstmt.setString(4, description);
            pstmt.setInt(5, subcategoryId);
            pstmt.setBigDecimal(6, amount);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateTransaction(int id, int accountId, LocalDate date, String type,
            String description, int subcategoryId, BigDecimal amount) {
        String sql = """
                     UPDATE AppTransaction
                     SET account_id = ?, date = ?, type = ?, description = ?, subcategory_id = ?, amount = ?
                     WHERE id = ?
                     """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setString(3, type);
            pstmt.setString(4, description);
            pstmt.setInt(5, subcategoryId);
            pstmt.setBigDecimal(6, amount);
            pstmt.setInt(7, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteTransaction(int id) {
        String sql = "DELETE FROM AppTransaction WHERE id =?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }

    public static void insertDummyTestData() {
        try {
            // 1. Add Test Accounts
            addAccount("Girokonto");
            addAccount("Tagesgeld");
            addAccount("Credit Card");

            // 2. Add Test Categories
            addCategory("Fixkosten");
            addCategory("Einnahmen");
            addCategory("Freizeit");

            // 3. Add Test Subcategories (Assuming Category IDs 1, 2, and 3 exist)
            addSubcategory("Miete", 1);
            addSubcategory("Strom", 1);
            addSubcategory("Gehalt", 2);
            addSubcategory("Restaurant", 3);

            // 4. Add Test Transactions (Using LocalDate)
            addTransaction(1, java.time.LocalDate.now().minusDays(5), "EXP", "Monatliche Miete", 1, new BigDecimal("123.40"));
            addTransaction(1, java.time.LocalDate.now().minusDays(2), "INC", "Gehaltseingang", 3, new BigDecimal("1456.78"));
            addTransaction(3, java.time.LocalDate.now().minusDays(1), "EXP", "Abendessen", 4, new BigDecimal("3456.89"));

            System.out.println("Dummy test data inserted successfully!");
        } catch (Exception e) {
            System.err.println("Error inserting test data: " + e.getMessage());
        }
    }

}
