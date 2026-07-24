package com.financeapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class CategoryView extends VBox {
    private final ListView<Category> categoryListView;
    private final ObservableList<Category> categoryData;
    private final TextField inputField;

    public CategoryView() {
        setSpacing(15);
        setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Kategorien");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Input Controls
        inputField = new TextField();
        inputField.setPromptText("Neue Kategorie:");
        inputField.setPrefWidth(250);

        Button btnAdd = new Button("Hinzufügen");
        btnAdd.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> handleAddCategory());

        HBox inputLayout = new HBox(10, inputField, btnAdd);

        // Category List
        categoryData = FXCollections.observableArrayList();
        categoryListView = new ListView<>(categoryData);
        categoryListView.setPrefHeight(250);

        // Action Buttons
        Button btnEdit = new Button("Ändern");
        btnEdit.setOnAction(e -> handleEditCategory());
        Button btnDelete = new Button("Löschen");
        btnDelete.setStyle("-fx-text-fill: #FF3B30;");
        btnDelete.setOnAction(e -> handleDeleteCategory());

        // Layout
        HBox actionLayout = new HBox(10, btnEdit, btnDelete);
        actionLayout.setAlignment(Pos.CENTER_RIGHT);

        // Add everything to Parent
        getChildren().addAll(headerLabel, inputLayout, categoryListView, actionLayout);

        // Load data on load
        refreshCategoryList();
    }

    public void refreshCategoryList() {
        categoryData.setAll(DatabaseManager.getAllCategories());
    }

    private void handleDeleteCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Bitte eine Kategorie auswählen");
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kategorie Löschen");
        alert.setHeaderText("Löschen " + selected.getName() + "'?");
        alert.setContentText("Sind Sie sicher?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseManager.deleteCategory(selected.getId());
            refreshCategoryList();
        }
    }

    private void handleEditCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Bitte eine Kategorie auswählen");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Kategorie bearbeiten");
        dialog.setHeaderText("Kategorie umbennen");
        dialog.setContentText("Neuer Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                DatabaseManager.updateCategory(selected.getId(), newName.trim());
                refreshCategoryList();
            }
        });

    }

    private void handleAddCategory() {
        String name = inputField.getText().trim();
        if (name.isEmpty()) return;

        if (DatabaseManager.addCategory(name)) {
            inputField.clear();
            refreshCategoryList();
        }
        else {
            showAlert("Fehler: Kategorie konnte nicht hinzugefügt werden");
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warnung");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
