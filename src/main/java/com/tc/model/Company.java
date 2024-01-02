package com.tc.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    Set<Driver> drivers;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    Set<Vehicle> vehicles;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "company_customer", joinColumns = { @JoinColumn(name = "company_id") }, inverseJoinColumns = {
            @JoinColumn(name = "customer_id") })
    private Set<Customer> customers = new HashSet<>();

    public Company() {
    }

    public Company(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Driver> getDrivers() {
        return this.drivers;
    }

    public void setDrivers(Set<Driver> drivers) {
        this.drivers = drivers;
    }

    public Set<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public void setVehicles(Set<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public Set<Customer> getCustomers() {
        return this.customers;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
        customer.getCompanies().add(this);
    }

    public void removeCustomer(long customerId) {
        var customer = this.customers.stream().filter(c -> c.getId() == customerId).findFirst().orElse(null);
        if (customer != null) {
            this.customers.remove(customer);
            customer.getCompanies().remove(this);
        }
    }
}
