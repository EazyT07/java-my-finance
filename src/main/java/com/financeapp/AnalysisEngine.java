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
        CATEGORY, SUBCATEGORY, ACCOUNT
    }

    public enum ColumnDimension {
        NONE, YEAR, MONTH
    }

    public static AggregationResult aggregate(
            List<Transaction> transactions,
            RowDimension rowDim,
            ColumnDimension colDim) {

        Map<String, PivotRow> rowMap = new LinkedHashMap<>();
        Set<String> dynamicColumnKeys = new TreeSet<>();

        for (Transaction t : transactions) {
            // 1. Determine Row Key (e.g. Subcategory Name)
            String rowKey = extractRowKey(t, rowDim);

            // 2. Determine Column Key (e.g. "2024" or "Jan 2025" or "Total")
            String colKey = extractColumnKey(t, colDim);

            // Track unique columns
            if (!colDim.equals(ColumnDimension.NONE)) {
                dynamicColumnKeys.add(colKey);
            }

            // 3. Populate PivotRow
            PivotRow row = rowMap.computeIfAbsent(rowKey, PivotRow::new);
            row.addValue(colKey, t.getAmount());
        }

        return new AggregationResult(new ArrayList<>(rowMap.values()), new ArrayList<>(dynamicColumnKeys));
    }

    
    private static String extractRowKey(Transaction t, RowDimension dim) {
        return switch (dim) {
            case CATEGORY -> "1"; //TODO: DatabaseManager.getCategoryNameById("1");
            case SUBCATEGORY -> "2"; //TODO: DatabaseManager.getSubcategoryNameById(t.getSubcategoryId());
            case ACCOUNT -> "3"; //TODO: DatabaseManager.getAccountNameById(t.getAccountId());
        };
    }

    private static String extractColumnKey(Transaction t, ColumnDimension dim) {
        return switch (dim) {
            case YEAR -> String.valueOf(t.getDate().getYear());
            case MONTH -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());
            case NONE -> "Gesamt";
        };
    }

    // Helper Record to hold both rows and detected column keys
    public record AggregationResult(List<PivotRow> rows, List<String> columnKeys) {
    }
}