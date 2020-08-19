package com.example.dailyfinance;


/*
    This is where we handle data for retrieving profiles
 */
public class Profile {

    private String name;
    private double dailyLimit;
    private String currency;
    private String selected;

    public Profile(String name, double dailyLimit, String currency, String selected) {

        this.name = name;
        this.dailyLimit = dailyLimit;
        this.currency = currency;
        this.selected = selected;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
