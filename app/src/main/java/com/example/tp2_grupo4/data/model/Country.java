package com.example.tp2_grupo4.data.model;

public class Country {
    String name;
    String slug;


    int infectedQty;

    public Country(String name, String slu, int infectedQty) {
        this.name = name;
        this.slug = slug;
        this.infectedQty = infectedQty;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getName() {
        return name;
    }
    public String getSlug() {
        return slug;
    }

    public int getInfectedQty() { return infectedQty; }
    public void setInfectedQty(int infectedQty) { this.infectedQty = infectedQty; }
}
