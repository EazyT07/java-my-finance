package com.financeapp;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AccountView extends VBox {

    private final ListView<Account> accountListView;
    private final ObservableList<Account> accountData;
    private final TextField inputField;

    public AccountView() {

        setSpacing(15);
        setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Konten");
        headerLabel.getStyleClass().add("label");

        // Input Controls
        inputField = new TextField();
        inputField.setPromptText("Neues Sachkonto");
        inputField.setPrefWidth(250);

        Button btnAdd = new Button("Hinzufügen");
        btnAdd.setOnAction(e -> handleAddAccount());

        HBox inputLayout = new HBox(10, inputField, btnAdd);

        // Account List
        accountData = FXCollections.observableArrayList();
        accountListView = new ListView<>(accountData);
        accountListView.setPrefHeight(250);

        // Action Buttons
        Button btnEdit = new Button("Ändern");
        btnEdit.setOnAction(e -> handleEditAccount());
        Button btnDelete = new Button("Löschen");
        btnDelete.setOnAction(e -> handleDeleteAccount());

        // Layour
        HBox actionLayout = new HBox(10, btnEdit, btnDelete);
        actionLayout.setAlignment(Pos.CENTER_RIGHT);

        // Add everything to Parent
        getChildren().addAll(headerLabel, inputLayout, accountListView, actionLayout);

        // Load data
        refreshAccountList();

    }

    public final void refreshAccountList() {
        accountData.setAll(DatabaseManager.getAllAccounts());
    }

    private void handleDeleteAccount() {
        Account select = accountListView.getSelectionModel().getSelectedItem();
        if (select == null) {
            showAlert("Bitte ein Konto auswählen");
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konto löschen");
        alert.setHeaderText("Löschen" + select.getName() + "'?");
        alert.setContentText("Sind Sie sicher?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DatabaseManager.deleteAccount(select.getId())) {
                refreshAccountList();
            } else {
                showAlert("Konto konnte nicht gelöscht werden");
            }
        }
    }

    private void handleEditAccount() {
        Account selected = accountListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Bitte ein Konto auswählen");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Konto bearbeiten");
        dialog.setHeaderText("Konto ändern");
        dialog.setContentText("Neuer Name");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                if (DatabaseManager.updateAccount(selected.getId(), newName.trim())) {
                    refreshAccountList();
                } else {
                    showAlert("Fehler beim Ändern");
                }
            }
        });
    }

    private void handleAddAccount() {
        String name = inputField.getText().trim();
        if (name.isEmpty()) {
            return;
        }

        if (DatabaseManager.addAccount(name)) {
            inputField.clear();
            refreshAccountList();
        } else {
            showAlert("Fehler beim Löschen");
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
