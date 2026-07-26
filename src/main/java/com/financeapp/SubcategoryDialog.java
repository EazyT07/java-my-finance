package com.financeapp;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import java.util.List;

public class SubcategoryDialog extends Dialog<Subcategory> {

    private ComboBox<Category> categoryBox;
    private TextField name;
    private Subcategory subcategory;

    public SubcategoryDialog(Window ownerWindow, Subcategory subcategory) {
        this.subcategory = subcategory;

        if (this.subcategory != null) {
            setTitle("Unterkategorie bearbeiten");
            setHeaderText("Unterkategorie ändern");
        } else {
            setTitle("Neue Unterkategorie");
            setHeaderText("Unterkategorie anlegen");
        }

        initOwner(ownerWindow);

        // Layout Controls
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Kategorie wählen...");

        categoryBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return (category != null) ? category.getName() : "";
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });

        loadCategories();

        name = new TextField();
        name.setPromptText("Name der Unterkategorie");

        // Only pre-fill if we are actually editing an existing subcategory
        if (this.subcategory != null) {
            name.setText(this.subcategory.getName());
            for (Category cat : categoryBox.getItems()) {
                if (cat.getId() == this.subcategory.getCategoryId()) {
                    categoryBox.setValue(cat);
                    break;
                }
            }
        }

        grid.add(new Label("Kategorie:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(name, 1, 1);

        getDialogPane().setContent(grid);

        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        checkValidation(saveButton);

        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> checkValidation(saveButton));
        name.textProperty().addListener((obs, oldVal, newVal) -> checkValidation(saveButton));

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                int id = (this.subcategory != null) ? this.subcategory.getId() : 0;
                return new Subcategory(id, name.getText().trim(), categoryBox.getValue().getId(),
                        categoryBox.getValue().getName());
            }
            return null;
        });

    }

    /* 
    public SubcategoryDialog(Window ownerWindow) {
        this(ownerWindow, null);
    }*/

    private void loadCategories() {
        List<Category> categories = DatabaseManager.getAllCategories();
        categoryBox.setItems(FXCollections.observableArrayList(categories));
    }

    private void checkValidation(Node saveButton) {
        boolean isInvalid = categoryBox.getValue() == null || name.getText().trim().isEmpty();
        saveButton.setDisable(isInvalid);
    }

}
