package com.tc.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String type;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, mappedBy = "qualifications")
    private Set<Driver> drivers = new HashSet<>();

    public Qualification() {
    }

    public Qualification(String type) {
        this.type = type;
    }

    public Long getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Driver> getDrivers() {
        return this.drivers;
    }

    public void setDrivers(Set<Driver> drivers) {
        this.drivers = drivers;
    }
}
