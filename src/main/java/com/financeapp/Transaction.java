package com.financeapp;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Transaction {
    
    private int id;
    private int accountId;
    private String accountName;
    private LocalDate date;
    private String type;
    private String description;
    private int subcategoryId;
    private String subcategoryName;
    private String categoryName;
    private BigDecimal amount;


    public Transaction(
        int id, int accountId, String accountName, LocalDate date,
        String type, String description, int subcategoryId,
        String subcategoryName, String categoryName, BigDecimal amount) {
        this.id = id;
        this.accountId = accountId;
        this.accountName = accountName;
        this.date = date;
        this.type = type;
        this.description = description;
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.categoryName = categoryName;
        this.amount = amount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(int subcategoryId) { this.subcategoryId = subcategoryId; }

    public String getSubcategoryName() { return subcategoryName; }
    public void setSubcategoryName(String subcategoryName) { this.subcategoryName = subcategoryName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
