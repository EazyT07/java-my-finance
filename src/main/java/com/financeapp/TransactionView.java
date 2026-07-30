package com.financeapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class TransactionView extends VBox {

    // Basic Controls
    private final TableView<Transaction> transactionTableView;
    private final ObservableList<Transaction> transactionData;
    private final ObservableList<Transaction> allTransactionsMasterList;
    private final ComboBox<Account> accountComboBox;
    private final ComboBox<Category> categoryComboBox;
    private final ComboBox<Subcategory> subcatComboBox;

    // Filter Controls
    private final ComboBox<Account> filterAccountBox;
    private final DatePicker filterDateFrom;
    private final DatePicker filterDateTo;
    private final ComboBox<Category> filterCategoryBox;
    private final ComboBox<Subcategory> filterSubcatBox;
    private final TextField filterDescriptionField;

    // Balance Info Labels
    private final Label labelOpenBalance;
    private final Label labelCloseBalance;

    public TransactionView() {

        setSpacing(15);
        setPadding(new Insets(20));

        accountComboBox = new ComboBox<>();
        categoryComboBox = new ComboBox<>();
        subcatComboBox = new ComboBox<>();

        // Header
        Label headerLabel = new Label("Transaktionen");
        headerLabel.getStyleClass().add("label");

        // Filter Panel Setup
        // --------------------
        // Account
        filterAccountBox = new ComboBox<>();
        filterAccountBox.setPromptText("Konto...");
        filterAccountBox.setOnAction(e -> applyFilters());

        // Dates
        filterDateFrom = new DatePicker();
        filterDateFrom.setPromptText("Datum von");
        filterDateFrom.setOnAction(e -> applyFilters());
        filterDateTo = new DatePicker();
        filterDateTo.setPromptText("Datum bis");
        filterDateTo.setOnAction(e -> applyFilters());

        // Category
        filterCategoryBox = new ComboBox<>();
        filterCategoryBox.setPromptText("Kategorie...");
        filterCategoryBox.setOnAction(e -> {
            updateSubcategoryFilterDropdown();
            applyFilters();
        });
        // Subcategory Dropdown
        filterSubcatBox = new ComboBox<>();
        filterSubcatBox.setPromptText("Unterkategorie...");
        filterSubcatBox.setOnAction(e -> applyFilters());

        // Description
        filterDescriptionField = new TextField();
        filterDescriptionField.setPromptText("Beschreibung...");
        filterDescriptionField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Filter Reset Button
        Button btnResetFilter = new Button("Filter zurücksetzen");
        btnResetFilter.setOnAction(e -> resetFilters());

        // Box Layouts
        HBox filterBarTop = new HBox(10, filterAccountBox, filterDateFrom, filterDateTo);
        HBox filterBarBottom = new HBox(10, filterSubcatBox, filterDescriptionField, btnResetFilter);
        VBox filterPanel = new VBox(10, new Label("Filter"), filterBarTop, filterBarBottom);
        filterPanel.setPadding(new Insets(10));
        filterPanel.getStyleClass().add("filter-panel");

        // Balance Info Panel
        labelOpenBalance = new Label("0,00 €");
        labelCloseBalance = new Label("0,00 €");
        HBox balanceBox = new HBox(20,
                new Label("Anfangssaldo:"), labelOpenBalance,
                new Label("Endsaldo:"), labelCloseBalance);

        // Transaction Table
        // -------------------
        allTransactionsMasterList = FXCollections.observableArrayList();
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
        getChildren().addAll(headerLabel, filterPanel, balanceBox, inputArea, transactionTableView);

    }

    private final void resetFilters() {
        filterAccountBox.setValue(null);
        filterDateFrom.setValue(null);
        filterDateTo.setValue(null);
        filterCategoryBox.setValue(null);
        filterSubcatBox.setValue(null);
        filterDescriptionField.clear();
        applyFilters();
    }

    private final void updateSubcategoryFilterDropdown() {
        // TODO Auto-generated method stub
    }

    private final void applyFilters() {
        // Read Filter Values
        Account selectedAccount = filterAccountBox.getValue();
        LocalDate dateFrom = filterDateFrom.getValue();
        LocalDate dateTo = filterDateTo.getValue();
        Category selectedCategory = filterCategoryBox.getValue();
        Subcategory selectedSubcat = filterSubcatBox.getValue();
        String descQuery = filterDescriptionField.getText().toLowerCase().trim();

        // Filter Table Data
        List<Transaction> filteredList = allTransactionsMasterList.stream().filter(t -> {
            if (selectedAccount != null && t.getAccountId() != selectedAccount.getId())
                return false;
            if (dateFrom != null && t.getDate().isBefore(dateFrom))
                return false;
            if (dateTo != null && t.getDate().isAfter(dateTo))
                return false;
            if (selectedCategory != null) {
                // Find subcategory to check its category, or check if transaction matches
                // category
                Subcategory sub = DatabaseManager.getAllSubcategories().stream()
                        .filter(s -> s.getId() == t.getSubcategoryId()).findFirst().orElse(null);
                if (sub == null || sub.getCategoryId() != selectedCategory.getId())
                    return false;
            }
            if (selectedSubcat != null && t.getSubcategoryId() != selectedSubcat.getId())
                return false;
            if (!descQuery.isEmpty() && !t.getDescription().toLowerCase().contains(descQuery))
                return false;
            return true;
        }).collect(Collectors.toList());

        transactionData.setAll(filteredList);

        // Calculate Balances (Account & Date From dependent)
        calculateBalances(selectedAccount, dateFrom, dateTo);
    }

    private void calculateBalances(Account account, LocalDate dateFrom, LocalDate dateTo) {
        if (account == null) {
            labelOpenBalance.setText("-");
            labelCloseBalance.setText("-");
            return;
        }

        BigDecimal openingBalance = new BigDecimal("0.00");

        // 2. Determine the threshold date. If no "Date From" is selected, opening
        // balance is just the initial balance.
        LocalDate thresholdDateFrom = dateFrom;

        // Loop through ALL master transactions to calculate what happened BEFORE the
        // dateFrom filter
        for (Transaction t : allTransactionsMasterList) {
            if (t.getAccountId() == account.getId()) {
                BigDecimal amountVal = "INC".equalsIgnoreCase(t.getType()) ? t.getAmount() : t.getAmount().negate();

                // If a "Date From" filter is set, any transaction strictly BEFORE that date
                // builds the opening balance
                if (thresholdDateFrom != null && t.getDate().isBefore(thresholdDateFrom)) {
                    openingBalance = openingBalance.add(amountVal);
                }
            }
        }

        // 3. Closing balance is the opening balance PLUS all transactions that fall
        // WITHIN the active filter period (from transactionData)
        BigDecimal periodChange = BigDecimal.ZERO;
        for (Transaction t : transactionData) {
            if (t.getAccountId() == account.getId()) {
                BigDecimal amountVal = "INC".equalsIgnoreCase(t.getType()) ? t.getAmount() : t.getAmount().negate();
                periodChange = periodChange.add(amountVal);
            }
        }

        BigDecimal closingBalance = openingBalance.add(periodChange);

        // Format and display
        NumberFormat germanFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        labelOpenBalance.setText(germanFormat.format(openingBalance));
        labelCloseBalance.setText(germanFormat.format(closingBalance));
    }

    private void handleImportCsv() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Transaktionen importieren");
        // Limit only to CSV Files
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Dateien (*.csv)", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        int importedCount = 0;
        int errorCount = 0;

        DateTimeFormatter germanFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");

        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

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
        Transaction selectedTransatcion = transactionTableView.getSelectionModel().getSelectedItem();

        if (selectedTransatcion == null) {
            showAlert("Bitte eine Transation selektieren");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Transaktion wirklich löschen?",
                ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (DatabaseManager.deleteTransaction(selectedTransatcion.getId())) {
                refreshTransactionList();
            } else {
                showAlert("Fehler: Transaktion konnte nicht gelöscht werden.");
            }
        }

    }

    private void handleEditTransaction() {
        Transaction selectedTransaction = transactionTableView.getSelectionModel().getSelectedItem();

        if (selectedTransaction == null) {
            showAlert("Bitte Transaktion auswählen");
            return;
        }

        // Open Dialog, passing the selected transaction to edit
        TransactionDialog dialog = new TransactionDialog(getScene().getWindow(), selectedTransaction);
        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(updateTransaction -> {
            boolean success = DatabaseManager.updateTransaction(
                    updateTransaction.getId(),
                    updateTransaction.getAccountId(),
                    updateTransaction.getDate(),
                    updateTransaction.getType(),
                    updateTransaction.getDescription(),
                    updateTransaction.getSubcategoryId(),
                    updateTransaction.getAmount());
            if (success) {
                refreshTransactionList();
            } else {
                showAlert("Fehler: Transaktion konnte nicht aktualisiert werden.");
            }
        });
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

    public final void refreshDropdowns() {
        // Accounts
        List<Account> accounts = DatabaseManager.getAllAccounts();
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
        filterAccountBox.setItems(FXCollections.observableArrayList(accounts));
        // Category
        List<Category> categories = DatabaseManager.getAllCategories();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        filterCategoryBox.setItems(FXCollections.observableArrayList(categories));
        // Subcategory
        List<Subcategory> subcategories = DatabaseManager.getAllSubcategories();
        subcatComboBox.setItems(FXCollections.observableArrayList(subcategories));
        filterSubcatBox.setItems(FXCollections.observableArrayList(subcategories));
    }

    public void refreshTransactionList() {
        allTransactionsMasterList.setAll(DatabaseManager.getAllTransactions());
        refreshDropdowns();
        applyFilters();
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
