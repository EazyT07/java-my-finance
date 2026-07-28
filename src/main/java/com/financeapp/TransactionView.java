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

    private final TableView<Transaction> transactionTableView;
    private final ObservableList<Transaction> transactionData;

    public TransactionView() {

        setSpacing(15);
        setPadding(new Insets(20));

        // Transaction Table
        // -------------------
        transactionData = FXCollections.observableArrayList();
        transactionTableView = new TableView<>(transactionData);
        initTableView();

        // Add everything to the VBox
        getChildren().addAll(transactionTableView);
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
