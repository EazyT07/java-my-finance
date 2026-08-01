package com.financeapp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PivotRow {

    private final String categoryName;
    private final String subcategoryName;
    private final Map<String, BigDecimal> columnValues = new HashMap<>();
    private BigDecimal rowTotal = BigDecimal.ZERO;

    public PivotRow(String categoryName, String subcategoryName) {
        this.categoryName = categoryName != null ? categoryName : "Ohne Kategorie";
        this.subcategoryName = subcategoryName != null ? subcategoryName : "Ohne Subkategorie";
    }

    public void addAmount(String columnKey, BigDecimal amount) {
        if (amount == null)
            return;
        BigDecimal current = columnValues.getOrDefault(columnKey, BigDecimal.ZERO);
        columnValues.put(columnKey, current.add(amount));
        this.rowTotal = this.rowTotal.add(amount);
    }

    public BigDecimal getValue(String columnKey) {
        return columnValues.getOrDefault(columnKey, BigDecimal.ZERO);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public BigDecimal getRowTotal() {
        return rowTotal;
    }
}