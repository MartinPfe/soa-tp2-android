package com.example.tp2_grupo4.data.model;

public class Country {
    String name;
    String slug;

    public Country(String name, String slug) {
        this.name = name;
        this.slug = slug;
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

}
