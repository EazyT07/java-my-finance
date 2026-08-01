package com.financeapp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

    public AnalysisView() {
        setSpacing(15);
        setPadding(new Insets(20));

        rowDimensionBox = new ComboBox<>(FXCollections.observableArrayList(RowDimension.values()));
        rowDimensionBox.setValue(RowDimension.CATEGORY);

        columnDimensionBox = new ComboBox<>(FXCollections.observableArrayList(ColumnDimension.values()));
        columnDimensionBox.setValue(ColumnDimension.YEAR);

        analysisTableView = new TableView<>();

        rowDimensionBox.setOnAction(e -> refreshAnalysisList());
        columnDimensionBox.setOnAction(e -> refreshAnalysisList());

        HBox controlBar = new HBox(10,
                new Label("Zeilen:"), rowDimensionBox,
                new Label("Spalten:"), columnDimensionBox);

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

        if (selectedRowDim == null || selectedColDim == null)
            return;

        AggregationResult result = AnalysisEngine.aggregate(transactions, selectedRowDim, selectedColDim);

        analysisTableView.getColumns().clear();

        // Dynamically add row dimension columns based on selection
        switch (selectedRowDim) {
            case CATEGORY -> {
                TableColumn<PivotRow, String> catCol = new TableColumn<>("Kategorie");
                catCol.setCellValueFactory(cell -> 
                    new ReadOnlyStringWrapper(cell.getValue().getCategoryName() != null 
                        ? cell.getValue().getCategoryName() 
                        : cell.getValue().getRowHeader()));
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
                subcatCol.setCellValueFactory(cell -> {
                    String label = cell.getValue().getSubcategoryName() != null
                            ? cell.getValue().getSubcategoryName()
                            : cell.getValue().getRowHeader();
                    return new ReadOnlyStringWrapper(label);
                });
                analysisTableView.getColumns().add(subcatCol);
            }
        }

        // Add dynamic period columns (Years / Months)
        for (String colKey : result.columnKeys()) {
            TableColumn<PivotRow, BigDecimal> dynamicCol = new TableColumn<>(colKey);
            dynamicCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getValue(colKey)));
            dynamicCol.setCellFactory(createGermanCurrencyCellFactory());

            analysisTableView.getColumns().add(dynamicCol);
        }

        // Add overall Total column
        TableColumn<PivotRow, BigDecimal> totalCol = new TableColumn<>("Gesamt");
        totalCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getRowTotal()));
        totalCol.setCellFactory(createGermanCurrencyCellFactory());

        analysisTableView.getColumns().add(totalCol);

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