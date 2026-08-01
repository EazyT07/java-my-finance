package com.financeapp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AnalysisEngine {

    public enum RowDimension {
        CATEGORY, SUBCATEGORY
    }

    public enum ColumnDimension {
        YEAR, MONTH
    }

    public static AggregationResult aggregate(
            List<Transaction> transactions,
            RowDimension rowDim,
            ColumnDimension colDim) {

        Map<String, PivotRow> rowMap = new LinkedHashMap<>();
        Set<String> dynamicColumnKeys = new TreeSet<>();

        for (Transaction t : transactions) {

            String catName = t.getCategoryName();
            String subcatName = t.getSubcategoryName();

            // Determine Unique Row Key (e.g. Subcategory Name)
            String rowKey = (rowDim == RowDimension.CATEGORY)
                    ? catName + " - " + subcatName
                    : (subcatName != null ? subcatName : "Ohne Subkategorie");

            // Determine and add Column Key
            String colKey = extractColumnKey(t, colDim);
            if (colDim.equals(ColumnDimension.YEAR) || colDim.equals(ColumnDimension.MONTH)) {
                dynamicColumnKeys.add(colKey);
            }

            // Construct PivotRow with BOTH category & subcategory names explicitly
            PivotRow row = rowMap.computeIfAbsent(rowKey, k -> new PivotRow(catName, subcatName));
            row.addAmount(colKey, t.getAmount());
        }

        return new AggregationResult(new ArrayList<>(rowMap.values()), new ArrayList<>(dynamicColumnKeys));
    }

    private static String extractRowKey(Transaction t, RowDimension dim) {
        return switch (dim) {
            case CATEGORY -> t.getCategoryName();
            case SUBCATEGORY -> t.getSubcategoryName();
        };
    }

    private static String extractColumnKey(Transaction t, ColumnDimension dim) {
        return switch (dim) {
            case YEAR -> String.valueOf(t.getDate().getYear());
            case MONTH -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());
        };
    }

    // Helper Record to hold both rows and detected column keys
    public record AggregationResult(List<PivotRow> rows, List<String> columnKeys) {
    }
}