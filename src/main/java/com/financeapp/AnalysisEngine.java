package com.financeapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AnalysisEngine {

    public enum RowDimension {
        CATEGORY("Kategorie"),
        CATEGORY_AND_SUBCATEGORY("Kategorie & Subkategorie"),
        SUBCATEGORY("Subkategorie");

        private final String label;

        RowDimension(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label; // Displays user-friendly labels in the ComboBox dropdown
        }
    }

    public enum ColumnDimension {
        YEAR("Jahr"),
        MONTH("Monat");

        private final String label;

        ColumnDimension(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static AggregationResult aggregate(
            List<Transaction> transactions,
            RowDimension rowDim,
            ColumnDimension colDim) {

        Map<String, PivotRow> rowMap = new LinkedHashMap<>();
        Set<String> dynamicColumnKeys = new TreeSet<>();

        for (Transaction t : transactions) {
            String catName = t.getCategoryName() != null ? t.getCategoryName() : "Ohne Kategorie";
            String subcatName = t.getSubcategoryName();

            String rowKey;
            PivotRow row;

            // Grouping logic based on selected row dimension
            switch (rowDim) {
                case CATEGORY -> {
                    rowKey = catName;
                    row = rowMap.computeIfAbsent(rowKey, k -> new PivotRow(catName, null));
                }
                case CATEGORY_AND_SUBCATEGORY -> {
                    rowKey = catName + " - " + (subcatName != null ? subcatName : "Ohne Subkategorie");
                    row = rowMap.computeIfAbsent(rowKey, k -> new PivotRow(catName, subcatName));
                }
                case SUBCATEGORY -> {
                    rowKey = subcatName != null ? subcatName : "Ohne Subkategorie";
                    row = rowMap.computeIfAbsent(rowKey, k -> new PivotRow(null, rowKey));
                }
                default -> throw new IllegalStateException("Unexpected value: " + rowDim);
            }

            // Determine dynamic column key (Year or Month)
            String colKey = extractColumnKey(t, colDim);
            dynamicColumnKeys.add(colKey);

            // Accumulate transaction amount into the row
            row.addAmount(colKey, t.getAmount());
        }

        return new AggregationResult(new ArrayList<>(rowMap.values()), new ArrayList<>(dynamicColumnKeys));
    }

    private static String extractColumnKey(Transaction t, ColumnDimension dim) {
        return switch (dim) {
            case YEAR -> String.valueOf(t.getDate().getYear());
            case MONTH -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());
        };
    }

    public record AggregationResult(List<PivotRow> rows, List<String> columnKeys) {
    }
}