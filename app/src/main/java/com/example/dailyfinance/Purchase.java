package com.example.dailyfinance;

public class Purchase {

    private String name;
    private double price;
    private String title;
    private String description;
    private String image;
    private String location;
    private String dateCreated;
    private String timeCreated;

    public Purchase(String name, double price, String title, String description, String image, String location, String dateCreated, String timeCreated){

        this.name = name;
        this.price = price;
        this.title = title;
        this.description = description;
        this.image = image;
        this.location = location;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }
}
