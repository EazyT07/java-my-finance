package com.financeapp;

import java.io.File;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;
    private Label currentDbLabel;
    private StackPane contentArea;
    private CategoryView categoryView;
    private SubcategoryView subcategoryView;
    private AccountView accountView;
    private TransactionView transactionView;
    private VBox dashboardView;

    @SuppressWarnings("exports")
    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        // Init the default DB
        DatabaseManager.initializeDatabase();
        //DatabaseManager.insertDummyTestData();

        // Build Sub-Views
        dashboardView = createDashboardView();
        categoryView = new CategoryView();
        subcategoryView = new SubcategoryView();
        accountView = new AccountView();
        transactionView = new TransactionView();

        // Setup Content Area Container
        contentArea = new StackPane();
        contentArea.getChildren().add(dashboardView);

        // Build Layout
        VBox sidebar = createSidebar();
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // Set scene and style
        Scene scene = new Scene(root, 1000, 750);
        String css = getClass().getResource("/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Meine Finanzen");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private VBox createDashboardView() {
        // Welcome Label and DB Label
        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.getStyleClass().add("label");
        this.currentDbLabel = new Label();
        updateDBLabel();
        // Content Area
        VBox dashboardContent = new VBox(15, welcomeLabel, currentDbLabel);
        dashboardContent.setAlignment(Pos.CENTER);
        dashboardContent.setPadding(new Insets(30));
        return dashboardContent;
    }

    private VBox createSidebar() {

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(180);
        sidebar.getStyleClass().add("sidebar");

        // App Title/Logo Area
        Label appTitle = new Label("Meine Finanzen");
        appTitle.getStyleClass().add("app-title");

        // Navigation Buttons
        Button btnDashboard = createNavButton("📊 Übersicht");
        btnDashboard.setOnAction(e -> setMainContent(dashboardView));
        // Transactions
        Button btnTransactions = createNavButton("💳 Transaktionen");
        btnTransactions.setOnAction(e -> {
            transactionView.refreshTransactionList();
            setMainContent(transactionView);
        });

        // Master Data Section
        Separator mdSeparator = new Separator();
        Label mdLabel = new Label("Stammdaten");
        mdLabel.getStyleClass().add("sidebar-section-label");
        Button btnCategories = createNavButton("🏷️ Kategorien");
        btnCategories.setOnAction(e -> {
            categoryView.refreshCategoryList();
            setMainContent(categoryView);
        });
        // Subcategory
        Button btnSubCategories = createNavButton("🏷️ Unterkategorien");
        btnSubCategories.setOnAction(e -> {
            subcategoryView.refreshSubcategoryList();
            setMainContent(subcategoryView);
        });
        // Account
        Button btnAccounts = createNavButton("🏷️ Konto");
        btnAccounts.setOnAction(e -> {
            accountView.refreshAccountList();
            setMainContent(accountView);
        });

        // Spacer to push database to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        // Switch DB Action Button at the bottom
        Button btnSwitchDB = new Button("📁 DB wechseln");
        btnSwitchDB.setMaxWidth(Double.MAX_VALUE);
        btnSwitchDB.getStyleClass().add("switch-db-button");
        btnSwitchDB.setOnAction(e -> handleSwitchDatabase());

        sidebar.getChildren().addAll(
                appTitle,
                new Separator(),
                btnDashboard,
                btnTransactions,
                mdSeparator,
                mdLabel,
                btnAccounts,
                btnCategories,
                btnSubCategories,
                spacer,
                new Separator(),
                btnSwitchDB
        );
        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    private void updateDBLabel() {
        String dbName = DatabaseManager.getDatabasePath();
        currentDbLabel.setText("Aktuelle DB: " + dbName);
    }

    private void handleSwitchDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("DB auswählen");

        // Only SQLite files allowed
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQLite Database (*.db)", "*.db")
        );

        // Show file window relative to main
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Switch DB and update Labels
        if (selectedFile != null) {
            try {
                DatabaseManager.switchDatabase(selectedFile.getAbsolutePath());
                reloadDatabaseConnection();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Datenbank Fehler");
                alert.setHeaderText("Fehler beim DB Wechsel");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void reloadDatabaseConnection() {
        updateDBLabel();
        categoryView.refreshCategoryList();
        subcategoryView.refreshSubcategoryList();
    }

    private void setMainContent(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
