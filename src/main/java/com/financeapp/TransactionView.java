package com.financeapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TransactionView extends VBox {

    private final ComboBox<Account> accountComboBox;
    private final ComboBox<Subcategory> subcatComboBox;
    private final TableView<Transaction> transactionTableView;
    private final ObservableList<Transaction> transactionData;

    public TransactionView() {

        setSpacing(15);
        setPadding(new Insets(20));

        accountComboBox = new ComboBox<>();
        subcatComboBox = new ComboBox<>();

        // Transaction Table
        // -------------------
        transactionData = FXCollections.observableArrayList();
        transactionTableView = new TableView<>(transactionData);
        initTableView();

        // Buttons
        // ---------
        Button btnAdd = new Button("Hinzufügen");
        btnAdd.setOnAction(e -> handleAddTransaction());
        Button btnEdit = new Button("Ändern");
        btnEdit.setOnAction(e -> handleEditTransaction());
        Button btnDelete = new Button("Löschen");
        btnDelete.setOnAction(e -> handleDeleteTransaction());
        HBox inputArea = new HBox(10, btnAdd, btnEdit, btnDelete);

        // Add everything to the VBox
        getChildren().addAll(inputArea, transactionTableView);

        // Intial Data Load
        refreshAccountDropdown();
        refreshSubcategoryDropdown();

    }

    private void handleDeleteTransaction() {

    }

    private void handleEditTransaction() {

    }

    private void handleAddTransaction() {

        // Create the dialog and set settings
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Neue Transaktion");
        dialog.setHeaderText("Transaktion erstellen");
        dialog.initOwner(getScene().getWindow());

        // 2. Set the button types (Save and Cancel)
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the layout and inputs for the dialog
        // ------------------------------------------------
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Combobox Account
        ComboBox<Account> dialogAccountBox = new ComboBox<>();
        dialogAccountBox.setPromptText("Konto wählen...");
        dialogAccountBox.setItems(accountComboBox.getItems());

        // Date Picker
        DatePicker dialogDateField = new DatePicker();
        dialogDateField.setValue(LocalDate.now());

        // Combobox Type
        ComboBox<String> dialogTypeBox = new ComboBox<>();
        dialogTypeBox.getItems().addAll("INC", "EXP");
        dialogTypeBox.getSelectionModel().select("EXP");
        dialogTypeBox.setConverter(new javafx.util.StringConverter<String>() {
            @Override
            public String toString(String type) {
                if ("INC".equals(type))
                    return "Einnahme (INC)";
                if ("EXP".equals(type))
                    return "Ausgabe (EXP)";
                return "";
            }

            @Override
            public String fromString(String string) {
                return string.contains("INC") ? "INC" : "EXP";
            }
        });

        // Description
        TextField dialogDescrField = new TextField();
        dialogDescrField.setPromptText("Beschreibung");

        // Combobox Subcategory
        ComboBox<Subcategory> dialogSubcatBox = new ComboBox<>();
        dialogSubcatBox.setPromptText("Unterkategorie wählen...");
        dialogSubcatBox.setItems(subcatComboBox.getItems());

        // Amount
        TextField amountField = new TextField();
        amountField.setPromptText("0,00");

        // Add the components
        grid.add(new Label("Konto:"), 0, 0);
        grid.add(dialogAccountBox, 1, 0);
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(dialogDateField, 1, 1);
        grid.add(new Label("Typ:"), 0, 2);
        grid.add(dialogTypeBox, 1, 2);
        grid.add(new Label("Beschreibung:"), 0, 3);
        grid.add(dialogDescrField, 1, 3);
        grid.add(new Label("Unterkategorie:"), 0, 4);
        grid.add(dialogSubcatBox, 1, 4);
        grid.add(new Label("Betrag:"), 0, 5);
        grid.add(amountField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // 4. Optional: Disable save button until fields are filled
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Enable save only when mandatory fields are selected/filled
        Runnable validateInputs = () -> {
            boolean accountValid = dialogAccountBox.getValue() != null;
            boolean dateValid = dialogDateField.getValue() != null;
            boolean amountValid = !amountField.getText().trim().isEmpty();
            saveButton.setDisable(!(accountValid && dateValid && amountValid));
        };

        dialogAccountBox.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        dialogDateField.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        amountField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());

        // 5. Convert the result when "Save" is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Clean and parse amount safely (handling German commas)
                    String amountText = amountField.getText().trim().replace(",", ".");
                    BigDecimal amount = new BigDecimal(amountText);

                    int accountId = dialogAccountBox.getValue().getId();
                    LocalDate date = dialogDateField.getValue();
                    String type = dialogTypeBox.getValue();
                    String description = dialogDescrField.getText().trim();

                    // Subcategory can be optional depending on your DB design, handle null safely
                    // if needed
                    int subcatId = dialogSubcatBox.getValue() != null ? dialogSubcatBox.getValue().getId() : 0;

                    return new Transaction(0, accountId, "", date, type, description, subcatId, "", "", amount);
                } catch (NumberFormatException e) {
                    showAlert("Ungültiges Betrag-Format. Bitte Zahlen eingeben.");
                    return null;
                }
            }
            return null;
        });

        // 6. Show the dialog and handle the database save
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(newTransaction -> {
            boolean success = DatabaseManager.addTransaction(
                    newTransaction.getAccountId(),
                    newTransaction.getDate(),
                    newTransaction.getType(),
                    newTransaction.getDescription(),
                    newTransaction.getSubcategoryId(),
                    newTransaction.getAmount());

            if (success) {
                refreshTransactionList();
            } else {
                showAlert("Fehler: Transaktion konnte nicht in der Datenbank gespeichert werden.");
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshAccountDropdown() {
        List<Account> accounts = DatabaseManager.getAllAccounts();
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
    }

    public void refreshSubcategoryDropdown() {
        List<Subcategory> subcategories = DatabaseManager.getAllSubcategories();
        subcatComboBox.setItems(FXCollections.observableArrayList(subcategories));
    }

    public void refreshTransactionList() {
        transactionData.setAll(DatabaseManager.getAllTransactions());
    }

    private void initTableView() {

        transactionTableView.setPrefHeight(250);
        // Column: Account Name
        TableColumn<Transaction, String> accountNameColumn = new TableColumn<>("Konto");
        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        transactionTableView.getColumns().add(accountNameColumn);
        // Column: Date
        TableColumn<Transaction, LocalDate> dateColumn = new TableColumn<>("Datum");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTableView.getColumns().add(dateColumn);
        // Column: Type
        TableColumn<Transaction, String> typeColumn = new TableColumn<>("Typ");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionTableView.getColumns().add(typeColumn);
        // Column: Description
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("Beschreibung");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionTableView.getColumns().add(descriptionColumn);
        // Column: Subcategory Name
        TableColumn<Transaction, String> subcatNameColumn = new TableColumn<>("Unterkategorie");
        subcatNameColumn.setCellValueFactory(new PropertyValueFactory<>("subcategoryName"));
        transactionTableView.getColumns().add(subcatNameColumn);
        // Column: Category Name
        TableColumn<Transaction, String> catNameColumn = new TableColumn<>("Kategorie");
        catNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        transactionTableView.getColumns().add(catNameColumn);
        // Column: Amount
        TableColumn<Transaction, BigDecimal> amountColumn = new TableColumn<>("Betrag");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, BigDecimal>() {
            private final NumberFormat germanFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(germanFormat.format(amount));
                }
            }
        });

        transactionTableView.getColumns().add(amountColumn);
    }

}
