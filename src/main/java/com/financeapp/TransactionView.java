package com.financeapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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

        // Header
        Label headerLabel = new Label("Transaktionen");
        headerLabel.getStyleClass().add("label");

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
        Button btnImport = new Button("Importieren");
        btnImport.setOnAction(e -> handleImportCsv());
        HBox inputArea = new HBox(10, btnAdd, btnEdit, btnDelete, btnImport);

        // Add everything to the VBox
        getChildren().addAll(headerLabel, inputArea, transactionTableView);

        // Intial Data Load
        refreshAccountDropdown();
        refreshSubcategoryDropdown();

    }

    private void handleImportCsv() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Transaktionen importieren");
        // Limit only to CSV Files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Dateien (*.csv)", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile == null)
            return;

        int importedCount = 0;
        int errorCount = 0;

        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");

        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                String[] parts = line.split(";");
                // Skip first line or header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                // Check number of columns
                if (parts.length < 6) {
                    errorCount++;
                    continue;
                }

                try {
                    // Basic Fields
                    String accountName = parts[0].trim();
                    LocalDate date = LocalDate.parse(parts[1].trim(), germanFormatter);
                    String type = parts[2].trim();
                    String description = parts[3].trim();
                    String subcategoryName = parts[4].trim();

                    // Convert Amount
                    String amountStr = parts[5].trim().replace(".", "").replace(",", ".");
                    BigDecimal amount = new BigDecimal(amountStr);

                    // Resolve Account ID from Account Name (Dropdown Box)
                    int accountId = -1;
                    for (Account acc : accountComboBox.getItems()) {
                        if (acc.getName().equalsIgnoreCase(accountName)) {
                            accountId = acc.getId();
                            break;
                        }
                    }

                    // Resolve Subcategory ID from Subcategory Name (Dropdown Box)
                    int subcategoryId = 0;
                    for (Subcategory sub : subcatComboBox.getItems()) {
                        if (sub.getName().equalsIgnoreCase(subcategoryName)) {
                            subcategoryId = sub.getId();
                            break;
                        }
                    }

                    // If account wasn't found, skip or handle error
                    if (accountId == -1) {
                        errorCount++;
                        continue;
                    }

                    // Save to database
                    boolean success = DatabaseManager.addTransaction(
                            accountId, date, type, description, subcategoryId, amount);

                    if (success) {
                        importedCount++;
                    } else {
                        errorCount++;
                    }

                } catch (Exception e) {
                    errorCount++;
                }

            }
            // Refresh table to show newly imported records
            refreshTransactionList();

            // Show summary alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import abgeschlossen");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Erfolgreich importiert: " + importedCount + "\nFehlerhaft / Übersprungen: " + errorCount);
            alert.showAndWait();
        } catch (IOException e) {
            showAlert("Fehler beim Lesen der Datei");
        }
    }

    private void handleDeleteTransaction() {

    }

    private void handleEditTransaction() {

    }

    private void handleAddTransaction() {

        TransactionDialog dialog = new TransactionDialog(getScene().getWindow(), null);
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
                showAlert("Fehler: Transaktion konnte nicht gespeichert werden.");
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public final void refreshAccountDropdown() {
        List<Account> accounts = DatabaseManager.getAllAccounts();
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
    }

    public final void refreshSubcategoryDropdown() {
        List<Subcategory> subcategories = DatabaseManager.getAllSubcategories();
        subcatComboBox.setItems(FXCollections.observableArrayList(subcategories));
    }

    public void refreshTransactionList() {
        transactionData.setAll(DatabaseManager.getAllTransactions());
    }

    private void initTableView() {

        // Set the TableView to grow vertically
        VBox.setVgrow(transactionTableView, Priority.ALWAYS);
        transactionTableView.setMaxHeight(Double.MAX_VALUE);
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
