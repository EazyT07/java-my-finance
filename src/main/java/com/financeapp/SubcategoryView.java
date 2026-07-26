package com.financeapp;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
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

    public void refreshCategoryDropdown() {
        List<Category> categories = DatabaseManager.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    public void refreshSubcategoryList() {
        subcategoryData.setAll(DatabaseManager.getAllSubcategories());
    }

    private void handleAddSubcategory() {

        // Create the dialog
        Dialog<Subcategory> dialog = new Dialog<>();
        dialog.setTitle("Neue Unterkategorie");
        dialog.setHeaderText("Unterkategorie erstellen");
        dialog.initOwner(getScene().getWindow());

        // 2. Set the button types (Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 3. Create the layout and inputs for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Category> dialogCategoryBox = new ComboBox<>();
        dialogCategoryBox.setPromptText("Kategorie wählen...");
        dialogCategoryBox.setItems(categoryComboBox.getItems());

        TextField dialogNameField = new TextField();
        dialogNameField.setPromptText("Name der Unterkategorie");

        grid.add(new Label("Kategorie:"), 0, 0);
        grid.add(dialogCategoryBox, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(dialogNameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 4. Optional: Disable save button until fields are filled
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        dialogCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> saveButton
                .setDisable(newVal == null || dialogNameField.getText().trim().isEmpty()));
        dialogNameField.textProperty().addListener((obs, oldVal, newVal) -> saveButton
                .setDisable(dialogCategoryBox.getValue() == null || newVal.trim().isEmpty()));

        // 5. Convert the result when "Save" is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Subcategory(0, dialogNameField.getText().trim(), dialogCategoryBox.getValue().getId(), "");
            }
            return null;
        });

        // 6. Show the dialog and handle the database save
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

        // Open Dialog
        SubcategoryDialog dialog = new SubcategoryDialog(getScene().getWindow(), selectedSub);
        Optional<Subcategory> result = dialog.showAndWait();
        result.ifPresent(updateSub -> {
            if (DatabaseManager.updateSubcategory(updateSub.getId(), updateSub.getName(), updateSub.getCategoryId()))
                refreshSubcategoryList();
            else
                showAlert("Fehler beim Ändern");
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