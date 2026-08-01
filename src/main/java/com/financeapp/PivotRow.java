package com.financeapp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PivotRow {
    private String rowHeader;
    private String categoryName;
    private String subcategoryName;
    private final Map<String, BigDecimal> columnValues = new HashMap<>();
    private BigDecimal rowTotal = BigDecimal.ZERO;

    public PivotRow(String rowHeader) {
        this.rowHeader = rowHeader;
    }

    public PivotRow(String categoryName, String subcategoryName) {
        this.categoryName = categoryName != null ? categoryName : "Ohne Kategorie";
        this.subcategoryName = subcategoryName != null ? subcategoryName : "Ohne Subkategorie";
        this.rowHeader = this.categoryName + " - " + this.subcategoryName;
    }

    public void addAmount(String colKey, BigDecimal amount) {
        if (amount == null)
            return;
        BigDecimal current = columnValues.getOrDefault(colKey, BigDecimal.ZERO);
        columnValues.put(colKey, current.add(amount));
        rowTotal = rowTotal.add(amount);
    }

    public BigDecimal getValue(String colKey) {
        return columnValues.getOrDefault(colKey, BigDecimal.ZERO);
    }

    public String getRowHeader() {
        return rowHeader;
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