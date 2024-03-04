package com.example.plproj;

import javafx.beans.property.*;

public class Expenses {
    private IntegerProperty id;
    private StringProperty date, category, description;
    private DoubleProperty amount;

    public Expenses () {}

    public Expenses(int id, String date, String category, double amount, String description) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleStringProperty(date);
        this.category = new SimpleStringProperty(category);
        this.amount = new SimpleDoubleProperty(amount);
        this.description = new SimpleStringProperty(description);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getDate() { return date.get(); }
    public void setDate(String date) { this.date.set(date); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty dateProperty() { return date; }
    public StringProperty categoryProperty() { return category; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }

}
