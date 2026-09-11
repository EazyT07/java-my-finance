package com.financeapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import com.financeapp.AnalysisEngine.AggregationResult;
import com.financeapp.AnalysisEngine.ColumnDimension;
import com.financeapp.AnalysisEngine.RowDimension;

public class AnalysisView extends VBox {

    private final ComboBox<RowDimension> rowDimensionBox;
    private final ComboBox<ColumnDimension> columnDimensionBox;
    private final TableView<PivotRow> analysisTableView;
    private List<Transaction> transactionData;

    // Filter Elements
    private final ComboBox<String> yearFromBox;
    private final ComboBox<String> yearToBox;

    public AnalysisView() {
        setSpacing(15);
        setPadding(new Insets(20));

        rowDimensionBox = new ComboBox<>(FXCollections.observableArrayList(RowDimension.values()));
        rowDimensionBox.setValue(RowDimension.CATEGORY);

        columnDimensionBox = new ComboBox<>(FXCollections.observableArrayList(ColumnDimension.values()));
        columnDimensionBox.setValue(ColumnDimension.YEAR);

        analysisTableView = new TableView<>();
        // Set the TableView to grow vertically
        VBox.setVgrow(analysisTableView, Priority.ALWAYS);
        analysisTableView.setMaxHeight(Double.MAX_VALUE);

        // Filter Elements Years
        int currentYear = LocalDate.now().getYear();
        List<String> yearOptions = new ArrayList<>();
        yearOptions.add("Alle");
        for (int y = currentYear - 15; y <= currentYear; y++) {
            yearOptions.add(String.valueOf(y));
        }
        yearFromBox = new ComboBox<>(FXCollections.observableArrayList(yearOptions));
        yearFromBox.setValue("Alle");
        yearToBox = new ComboBox<>(FXCollections.observableArrayList(yearOptions));
        yearToBox.setValue("Alle");

        // Event Handler
        rowDimensionBox.setOnAction(e -> refreshAnalysisList());
        columnDimensionBox.setOnAction(e -> refreshAnalysisList());
        yearFromBox.setOnAction(e -> refreshAnalysisList());
        yearToBox.setOnAction(e -> refreshAnalysisList());

        // Set Up Layout
        HBox controlBar = new HBox(10,
                new Label("Zeilen:"), rowDimensionBox,
                new Label("Spalten:"), columnDimensionBox);
        HBox filterBar = new HBox(10,
                new Label("Jahr von:"), yearFromBox,
                new Label("Jahr bis:"), yearToBox);

        this.getChildren().addAll(controlBar, filterBar, analysisTableView);
    }

    public void refreshAnalysisList() {
        transactionData = DatabaseManager.getAllTransactions();

        if (transactionData == null || transactionData.isEmpty()) {
            analysisTableView.getItems().clear();
            analysisTableView.getColumns().clear();
            return;
        }

        // Apply Filter
        String selectedFrom = yearFromBox.getValue();
        String selectedTo = yearToBox.getValue();
        Integer yearFrom = (selectedFrom == null || "Alle".equalsIgnoreCase(selectedFrom))
                ? null
                : Integer.parseInt(selectedFrom);
        Integer yearTo = (selectedTo == null || "Alle".equalsIgnoreCase(selectedTo))
                ? null
                : Integer.parseInt(selectedTo);
        List<Transaction> filteredTransactions = transactionData.stream().filter(t -> {
            if (t.getDate() == null) return false;
            int txYear = t.getDate().getYear();
            if (yearFrom != null && txYear < yearFrom) return false;
            if (yearTo != null && txYear > yearTo) return false;
            return true;

        }).collect(Collectors.toList());

        updateAnalysisTable(filteredTransactions);
    }

    public void updateAnalysisTable(List<Transaction> transactions) {
        RowDimension selectedRowDim = rowDimensionBox.getValue();
        ColumnDimension selectedColDim = columnDimensionBox.getValue();

        if (selectedRowDim == null || selectedColDim == null)
            return;

        AggregationResult result = AnalysisEngine.aggregate(transactions, selectedRowDim, selectedColDim);

        analysisTableView.getColumns().clear();

        // Dynamically add row dimension columns based on selection
        switch (selectedRowDim) {
            case CATEGORY -> {
                TableColumn<PivotRow, String> catCol = new TableColumn<>("Kategorie");
                catCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCategoryName()));
                analysisTableView.getColumns().add(catCol);
            }
            case CATEGORY_AND_SUBCATEGORY -> {
                TableColumn<PivotRow, String> catCol = new TableColumn<>("Kategorie");
                catCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCategoryName()));
                analysisTableView.getColumns().add(catCol);

                TableColumn<PivotRow, String> subcatCol = new TableColumn<>("Subkategorie");
                subcatCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSubcategoryName()));
                analysisTableView.getColumns().add(subcatCol);
            }
            case SUBCATEGORY -> {
                TableColumn<PivotRow, String> subcatCol = new TableColumn<>("Subkategorie");
                subcatCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSubcategoryName()));
                analysisTableView.getColumns().add(subcatCol);
            }
        }

        // Add dynamic period columns (Years / Months)
        for (String colKey : result.columnKeys()) {
            TableColumn<PivotRow, BigDecimal> dynamicCol = new TableColumn<>(colKey);
            dynamicCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue(colKey)));
            dynamicCol.setCellFactory(createGermanCurrencyCellFactory(false));

            analysisTableView.getColumns().add(dynamicCol);
        }

        // Add overall Total column
        TableColumn<PivotRow, BigDecimal> totalCol = new TableColumn<>("Gesamt");
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRowTotal()));
        totalCol.setCellFactory(createGermanCurrencyCellFactory(true));

        analysisTableView.getColumns().add(totalCol);

        analysisTableView.setItems(FXCollections.observableArrayList(result.rows()));
    }

    private Callback<TableColumn<PivotRow, BigDecimal>, TableCell<PivotRow, BigDecimal>> createGermanCurrencyCellFactory(
            boolean isTotalColumn) {
        return column -> new TableCell<PivotRow, BigDecimal>() {
            private final NumberFormat germanFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(germanFormat.format(item));

                    // Check if the current row is the "Gesamt" summary row
                    PivotRow row = getTableRow() != null ? getTableRow().getItem() : null;
                    boolean isTotalRow = row != null && ("Gesamt".equalsIgnoreCase(row.getCategoryName()) ||
                            "Gesamt".equalsIgnoreCase(row.getSubcategoryName()));

                    // Bold if it's the Total Column OR the Total Row
                    if (isTotalColumn || isTotalRow) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            };
        };
    }

}