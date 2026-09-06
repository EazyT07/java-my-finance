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

    public record AggregationResult(List<PivotRow> rows, List<String> columnKeys) {
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
            BigDecimal amountValue = "INC".equalsIgnoreCase(t.getType()) ? t.getAmount() : t.getAmount().negate();
            row.addAmount(colKey, amountValue);

        }

        PivotRow grandTotalRow = new PivotRow("Gesamt", null);

        // Sum up column values across all rows
        for (PivotRow row : rowMap.values()) {
            for (String colKey : dynamicColumnKeys) {
                BigDecimal val = row.getValue(colKey);
                grandTotalRow.addAmount(colKey, val);
            }
        }

        List<PivotRow> finalRows = new ArrayList<>(rowMap.values());
        finalRows.add(grandTotalRow);

        return new AggregationResult(finalRows, new ArrayList<>(dynamicColumnKeys));
    }

    private static String extractColumnKey(Transaction t, ColumnDimension dim) {
        return switch (dim) {
            case YEAR -> String.valueOf(t.getDate().getYear());
            case MONTH -> t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());
        };
    }

}