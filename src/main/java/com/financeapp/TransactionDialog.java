package com.financeapp;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDialog extends Dialog<Transaction> {

    private final ComboBox<Account> accountBox;
    private final DatePicker datePicker;
    private final ComboBox<String> typeBox;
    private final TextField descriptionField;
    private final ComboBox<Subcategory> subcategoryBox;
    private final TextField amountField;

    @SuppressWarnings("exports")
    public TransactionDialog(Window ownerWindow, Transaction transaction) {
        if (transaction != null) {
            setTitle("Transaktion bearbeiten");
            setHeaderText("Transaktion ändern");
        } else {
            setTitle("Neue Transaktion");
            setHeaderText("Transaktion erstellen");
        }

        initOwner(ownerWindow);

        // 1. Set the button types (Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 2. Create the layout and inputs for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Combobox: Account
        accountBox = new ComboBox<>();
        accountBox.setPromptText("Konto wählen...");
        accountBox.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                return (account != null) ? account.getName() : "";
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });
        loadAccounts();

        // Date Picker
        datePicker = new DatePicker();
        datePicker.setPromptText("Datum wählen");
        datePicker.setValue(LocalDate.now());

        // Combobox: Type (INC / EXP)
        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("INC", "EXP");
        typeBox.getSelectionModel().select("EXP");
        typeBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String type) {
                if ("INC".equals(type)) {
                    return "Einnahme (INC)";
                }
                if ("EXP".equals(type)) {
                    return "Ausgabe (EXP)";
                }
                return "";
            }

            @Override
            public String fromString(String string) {
                return string.contains("INC") ? "INC" : "EXP";
            }
        });

        // Description
        descriptionField = new TextField();
        descriptionField.setPromptText("Beschreibung");

        // Combobox: Subcategory
        subcategoryBox = new ComboBox<>();
        subcategoryBox.setPromptText("Unterkategorie wählen...");
        subcategoryBox.setConverter(new StringConverter<Subcategory>() {
            @Override
            public String toString(Subcategory subcat) {
                return (subcat != null) ? subcat.getName() : "";
            }

            @Override
            public Subcategory fromString(String string) {
                return null;
            }
        });
        loadSubcategories();

        // Amount
        amountField = new TextField();
        amountField.setPromptText("0,00");

        // Add components to Grid
        grid.add(new Label("Konto:"), 0, 0);
        grid.add(accountBox, 1, 0);
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Typ:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Beschreibung:"), 0, 3);
        grid.add(descriptionField, 1, 3);
        grid.add(new Label("Unterkategorie:"), 0, 4);
        grid.add(subcategoryBox, 1, 4);
        grid.add(new Label("Betrag:"), 0, 5);
        grid.add(amountField, 1, 5);

        getDialogPane().setContent(grid);

        // 3. Validation: Disable save button until mandatory fields are filled
        javafx.scene.Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        Runnable validateInputs = () -> {
            boolean accountValid = accountBox.getValue() != null;
            boolean dateValid = datePicker.getValue() != null;
            boolean amountValid = !amountField.getText().trim().isEmpty();
            saveButton.setDisable(!(accountValid && dateValid && amountValid));
        };

        accountBox.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        amountField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());

        // 4. Pre-fill data if we are editing an existing transaction
        if (transaction != null) {
            // Find and select matching account object in dropdown
            for (Account acc : accountBox.getItems()) {
                if (acc.getId() == transaction.getAccountId()) {
                    accountBox.getSelectionModel().select(acc);
                    break;
                }
            }
            datePicker.setValue(transaction.getDate());
            typeBox.getSelectionModel().select(transaction.getType());
            descriptionField.setText(transaction.getDescription());

            // Find and select matching subcategory object in dropdown
            for (Subcategory sub : subcategoryBox.getItems()) {
                if (sub.getId() == transaction.getSubcategoryId()) {
                    subcategoryBox.getSelectionModel().select(sub);
                    break;
                }
            }
            if (transaction.getAmount() != null) {
                amountField.setText(transaction.getAmount().toString());
            }
        }

        // 5. Convert the result when "Save" is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String amountText = amountField.getText().trim().replace(",", ".");
                    BigDecimal amount = new BigDecimal(amountText);

                    int accountId = accountBox.getValue().getId();
                    LocalDate date = datePicker.getValue();
                    String type = typeBox.getValue();
                    String description = descriptionField.getText().trim();
                    int subcatId = subcategoryBox.getValue() != null ? subcategoryBox.getValue().getId() : 0;

                    int idToUse = (transaction != null) ? transaction.getId() : 0;
                    return new Transaction(idToUse, accountId, "", date, type, description, subcatId, "", "", amount);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });
    }

    private void loadSubcategories() {
        List<Subcategory> subcategories = DatabaseManager.getAllSubcategories();
        subcategoryBox.setItems(javafx.collections.FXCollections.observableArrayList(subcategories));
    }

    private void loadAccounts() {
        List<Account> accounts = DatabaseManager.getAllAccounts();
        accountBox.setItems(FXCollections.observableArrayList(accounts));
    }

}
