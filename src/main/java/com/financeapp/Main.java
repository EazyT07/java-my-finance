package com.financeapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class Main extends Application{

    private Stage primaryStage;
    private Label currentDbLabel;
    private StackPane contentArea;
    private CategoryView categoryView;
    private VBox dashboardView;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        // Init the default DB
        DatabaseManager.initializeDatabase();

        // Build Sub-Views
        categoryView = new CategoryView();
        dashboardView = createDashboardView();

        // Setup Content Area Container
        contentArea = new StackPane();
        contentArea.getChildren().add(dashboardView);

        // Build Layout
        VBox sidebar = createSidebar();
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // Set scene
        Scene scene = new Scene(root, 1000, 750);
        primaryStage.setTitle("Meine Finanzen");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private VBox createDashboardView() {
        // Welcome Label and DB Label
        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        this.currentDbLabel = new Label();
        updateDBLabel();
        // Content Area
        VBox contentArea = new VBox(15, welcomeLabel, currentDbLabel);
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setPadding(new Insets(30));
        return contentArea;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: #f4f4f6; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        // App Title/Logo Area
        Label appTitle = new Label("Meine Finanzen");
        appTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        // Navigation Buttons
        Button btnDashboard = createNavButton("📊 Übersicht");
        btnDashboard.setOnAction(e -> setMainContent(dashboardView));
        Button btnTransactions = createNavButton("💳 Transaktionen");
        Button btnCategories = createNavButton("🏷️ Kategorien");
        btnCategories.setOnAction(e -> {
            categoryView.refreshCategoryList();
            setMainContent(categoryView);
        });
        // Spacer to push database to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        // Switch DB Action Button at the bottom
        Button btnSwitchDB = new Button("📁 DB wechseln");
        btnSwitchDB.setMaxWidth(Double.MAX_VALUE);
        btnSwitchDB.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 4; -fx-background-radius: 4;");
        btnSwitchDB.setOnAction(e -> handleSwitchDatabase());

        sidebar.getChildren().addAll(
                appTitle,
                new Separator(),
                btnDashboard,
                btnTransactions,
                btnCategories,
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
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 13px; -fx-padding: 8 10;");
        return btn;
    }

    private void updateDBLabel() {
        String dbName = DatabaseManager.getCurrentDatabaseName();
        currentDbLabel.setText("Aktuelle DB: " + dbName);
        currentDbLabel.setStyle("-fx-text-fill: #555555; -fx-font-style: italic;");
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
            DatabaseManager.switchDatabase(selectedFile);
            updateDBLabel();
            categoryView.refreshCategoryList();
        }
    }

    private void setMainContent(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    public static void main(String[] args) {
    launch(args);
    }
}
