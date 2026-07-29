package com.financeapp;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SubcategoryView extends VBox {

    private final ComboBox<Category> categoryComboBox;
    private final TableView<Subcategory> subcategoryTableView;
    private final ObservableList<Subcategory> subcategoryData;

    public SubcategoryView() {
        setSpacing(15);
        setPadding(new Insets(20));

        // 1. Parent Category Selector
        Label lblSelectCategory = new Label("Unterkategorien");
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Kategorie auswählen...");
        categoryComboBox.setOnAction(e -> refreshSubcategoryList());

        // 2. Subcategory Table
        // ----------------------
        subcategoryData = FXCollections.observableArrayList();
        subcategoryTableView = new TableView<>(subcategoryData);
        subcategoryTableView.setPrefHeight(250);
        // Name
        TableColumn<Subcategory, String> nameColumn = new TableColumn<>("Unterkategorie");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        subcategoryTableView.getColumns().add(nameColumn);
        // Category Name
        TableColumn<Subcategory, String> categoryColumn = new TableColumn<>("Kategorie");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        subcategoryTableView.getColumns().add(categoryColumn);

        // Buttons
        // ---------
        Button btnAdd = new Button("Hinzufügen");
        btnAdd.setOnAction(e -> handleAddSubcategory());
        Button btnEdit = new Button("Ändern");
        btnEdit.setOnAction(e -> handleEditSubcategory());
        Button btnDelete = new Button("Löschen");
        btnDelete.setOnAction(e -> handleDeleteSubcategory());

        HBox inputArea = new HBox(10, btnAdd, btnEdit, btnDelete);

        // Add everything to the VBox
        getChildren().addAll(lblSelectCategory, categoryComboBox, inputArea, subcategoryTableView);

        // Initial Data Load
        refreshCategoryDropdown();
        refreshSubcategoryList();
    }

    public final void refreshCategoryDropdown() {
        List<Category> categories = DatabaseManager.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    public final void refreshSubcategoryList() {
        subcategoryData.setAll(DatabaseManager.getAllSubcategories());
    }

    private void handleAddSubcategory() {
        // Use the SubcategoryDialog, passing null for a new entry
        SubcategoryDialog dialog = new SubcategoryDialog(getScene().getWindow(), null);
        Optional<Subcategory> result = dialog.showAndWait();

        result.ifPresent(newSub -> {
            if (DatabaseManager.addSubcategory(newSub.getName(), newSub.getCategoryId())) {
                refreshSubcategoryList();
            } else {
                showAlert("Fehler: Unterkategorie konnte nicht hinzugefügt werden.");
            }
        });
    }

    private void handleEditSubcategory() {
        Subcategory selectedSub = subcategoryTableView.getSelectionModel().getSelectedItem();

        if (selectedSub == null) {
            showAlert("Bitte Unterkategorie auswählen");
            return;
        }

        // Open Dialog, passing the selected subcategory to edit
        SubcategoryDialog dialog = new SubcategoryDialog(getScene().getWindow(), selectedSub);
        Optional<Subcategory> result = dialog.showAndWait();
        result.ifPresent(updateSub -> {
            if (DatabaseManager.updateSubcategory(updateSub.getId(), updateSub.getName(), updateSub.getCategoryId())) {
                refreshSubcategoryList();
            } else {
                showAlert("Fehler beim Ändern");
            }
        });
    }

    private void handleDeleteSubcategory() {
        Subcategory selectedSub = subcategoryTableView.getSelectionModel().getSelectedItem();

        if (selectedSub != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Unterkategorie wirklich löschen?", ButtonType.YES,
                    ButtonType.NO);
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                if (DatabaseManager.deleteSubcategory(selectedSub.getId())) {
                    refreshSubcategoryList();
                } else {
                    showAlert("Fehler: Unterkategorie konnte nicht gelöscht werden.");
                }
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
