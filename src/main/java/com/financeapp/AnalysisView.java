package com.financeapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// --- JavaFX Imports ---
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell; // <--- WAS MISSING
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback; // <--- WAS MISSING

import com.financeapp.AnalysisEngine.AggregationResult;
import com.financeapp.AnalysisEngine.ColumnDimension;
import com.financeapp.AnalysisEngine.RowDimension;

public class AnalysisView extends VBox {

    private final ComboBox<RowDimension> rowDimensionBox;
    private final ComboBox<ColumnDimension> columnDimensionBox;
    private final TableView<PivotRow> analysisTableView;
    private List<Transaction> transactionData;

    public AnalysisView() {

        setSpacing(15);
        setPadding(new Insets(20));

        // Init the UI Controls
        rowDimensionBox = new ComboBox<>(FXCollections.observableArrayList(RowDimension.values()));
        rowDimensionBox.setValue(RowDimension.CATEGORY);
        columnDimensionBox = new ComboBox<>(FXCollections.observableArrayList(ColumnDimension.values()));
        columnDimensionBox.setValue(ColumnDimension.YEAR);

        analysisTableView = new TableView<>();

        // Setup Event Listeners to recalculate when controls change
        rowDimensionBox.setOnAction(e -> refreshAnalysisList());
        columnDimensionBox.setOnAction(e -> refreshAnalysisList());

        // Build Control Bar
        HBox controlBar = new HBox(10,
                new Label("Zeilen:"), rowDimensionBox,
                new Label("Spalten:"), columnDimensionBox);

        // Add components to this VBox container
        this.getChildren().addAll(controlBar, analysisTableView);
    }

    public void refreshAnalysisList() {
        transactionData = DatabaseManager.getAllTransactions();

        if (transactionData == null || transactionData.isEmpty()) {
            analysisTableView.getItems().clear();
            analysisTableView.getColumns().clear();
            return;
        }

        updateAnalysisTable(transactionData);
    }

    public void updateAnalysisTable(List<Transaction> transactions) {

        RowDimension selectedRowDim = rowDimensionBox.getValue();
        ColumnDimension selectedColDim = columnDimensionBox.getValue();

        // Safety fallback if no dimension selected yet
        if (selectedRowDim == null || selectedColDim == null)
            return;

        // Calculate Aggregated Data
        AggregationResult result = AnalysisEngine.aggregate(transactions, selectedRowDim, selectedColDim);

        // Clear Existing Columns
        analysisTableView.getColumns().clear();

        // 3. Row Header Columns (1 or 2 depending on selected dimension)
        if (selectedRowDim == RowDimension.CATEGORY) {
            // Category Column
            TableColumn<PivotRow, String> catCol = new TableColumn<>("Kategorie");
            catCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCategoryName()));
            analysisTableView.getColumns().add(catCol);

            // Subcategory Column
            TableColumn<PivotRow, String> subcatCol = new TableColumn<>("Subkategorie");
            subcatCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getSubcategoryName()));
            analysisTableView.getColumns().add(subcatCol);

        } else if (selectedRowDim == RowDimension.SUBCATEGORY) {
            // Single Subcategory Column
            TableColumn<PivotRow, String> subcatCol = new TableColumn<>("Subkategorie");
            subcatCol.setCellValueFactory(cell -> {
                String label = cell.getValue().getSubcategoryName() != null
                        ? cell.getValue().getSubcategoryName()
                        : cell.getValue().getRowHeader();
                return new ReadOnlyStringWrapper(label);
            });
            analysisTableView.getColumns().add(subcatCol);
        }

        // 4. Dynamic Period Columns (Years / Months)
        for (String colKey : result.columnKeys()) {
            TableColumn<PivotRow, BigDecimal> dynamicCol = new TableColumn<>(colKey);
            dynamicCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue(colKey)));
            dynamicCol.setCellFactory(createGermanCurrencyCellFactory());

            analysisTableView.getColumns().add(dynamicCol);
        }

        // 5. Total Column
        TableColumn<PivotRow, BigDecimal> totalCol = new TableColumn<>("Gesamt");
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRowTotal()));
        totalCol.setCellFactory(createGermanCurrencyCellFactory());

        analysisTableView.getColumns().add(totalCol);

        // 6. Populate Table
        analysisTableView.setItems(FXCollections.observableArrayList(result.rows()));
    }

    private Callback<TableColumn<PivotRow, BigDecimal>, TableCell<PivotRow, BigDecimal>> createGermanCurrencyCellFactory() {
        return column -> new TableCell<PivotRow, BigDecimal>() {
            private final NumberFormat germanFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(germanFormat.format(item));
                }
            }
        };
    }
}