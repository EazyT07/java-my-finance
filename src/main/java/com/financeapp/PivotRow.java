package com.financeapp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PivotRow {

    private final String rowHeader;
    private final Map<String, BigDecimal> columnValues = new HashMap<>();
    private BigDecimal rowTotal = BigDecimal.ZERO;

    public PivotRow(String rowHeader) {
        this.rowHeader = rowHeader;
    }

    public void addValue(String columnKey, BigDecimal amount) {
        if (amount == null)
            return;
        BigDecimal current = columnValues.getOrDefault(columnKey, BigDecimal.ZERO);
        columnValues.put(columnKey, current.add(amount));
        this.rowTotal = this.rowTotal.add(amount);
    }

    public BigDecimal getValue(String columnKey) {
        return columnValues.getOrDefault(columnKey, BigDecimal.ZERO);
    }

    public String getRowHeader() {
        return rowHeader;
    }

    public BigDecimal getRowTotal() {
        return rowTotal;
    }
}