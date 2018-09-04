package com.deadtech.deadtech.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Manufacturer {

    @Id
    @GeneratedValue
    private int id;

    @NotNull
    @Size(min=3, max=24)
    private String name;

    private String description;

    @OneToMany
    @JoinColumn(name = "manufacturer_id")
    private List<Product> products = new ArrayList<>();

    public Manufacturer() {
    }

    public Manufacturer(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Product> getProducts() {
        return products;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
