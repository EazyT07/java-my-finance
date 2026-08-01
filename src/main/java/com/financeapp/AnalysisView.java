package com.financeapp;

import java.math.BigDecimal;
import java.util.List;

// --- JavaFX Imports ---
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
            new Label("Spalten:"), columnDimensionBox
        );

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
        if (selectedRowDim == null || selectedColDim == null) return;

        // Calculate Aggregated Data
        AggregationResult result = AnalysisEngine.aggregate(transactions, selectedRowDim, selectedColDim);

        // Clear Existing Columns
        analysisTableView.getColumns().clear();

        // 3. Column #1: Row Header (e.g., Subcategory Name)
        TableColumn<PivotRow, String> rowHeaderCol = new TableColumn<>(selectedRowDim.toString());
        rowHeaderCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getRowHeader()));
        analysisTableView.getColumns().add(rowHeaderCol);

        // 4. Dynamic Columns (e.g., 2024, 2025, 2026)
        for (String colKey : result.columnKeys()) {
            TableColumn<PivotRow, String> dynamicCol = new TableColumn<>(colKey);
            dynamicCol.setCellValueFactory(cell -> {
                BigDecimal amount = cell.getValue().getValue(colKey);
                return new ReadOnlyStringWrapper(String.format("%.2f €", amount));
            });
            analysisTableView.getColumns().add(dynamicCol);
        }

        // 5. Final Column: Row Total Sum
        TableColumn<PivotRow, String> totalCol = new TableColumn<>("Gesamt");
        totalCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.format("%.2f €", cell.getValue().getRowTotal())));
        analysisTableView.getColumns().add(totalCol);

        // 6. Set Items
        analysisTableView.setItems(FXCollections.observableArrayList(result.rows()));
    }
}