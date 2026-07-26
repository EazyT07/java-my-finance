package com.financeapp;

public class Account {
    
    private int id;
    private String name;

    public Account(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return  id; }

    public String getName() { return name; }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return  this.name;
    }
    
}
