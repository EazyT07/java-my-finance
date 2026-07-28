package com.financeapp;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDialog extends Dialog<Transaction> {

    private ComboBox<Account> accountBox;
    private DatePicker datePicker;
    private ComboBox<String> type;
    private TextField description;
    private ComboBox<Subcategory> subcategoryBox;
    private TextField amount;
    private Transaction transaction;
   
    public TransactionDialog(Window ownerWindow, Transaction transaction) {
        
        this.transaction = transaction;

        if (this.transaction != null) {
            setTitle("Transaktion bearbeiten");
            setHeaderText("Transaktion ändern");
        } else {
            setTitle("Neue Transaktion");
            setHeaderText("Transaktion anlegen");
        }

        initOwner(ownerWindow);

        // Layout Controls
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Combobox: Account
        //------------------
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
        //-------------
        datePicker = new DatePicker();
        datePicker.setPromptText("Datum wählen");
        datePicker.setValue(LocalDate.now());


        // Build the Dialog with all Components
        grid.add(new Label("Konto:"), 0, 0);
        grid.add(accountBox, 1, 0);
        grid.add(new Label("Datum:"), 0, 1);
        grid.add(datePicker, 1, 1);

        getDialogPane().setContent(grid);

    }

    private void loadAccounts() {
        List<Account> accounts = DatabaseManager.getAllAccounts();
        accountBox.setItems(FXCollections.observableArrayList(accounts));
    }

    
}