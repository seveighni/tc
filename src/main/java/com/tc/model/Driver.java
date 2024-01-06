package com.tc.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String firstName;
    private String lastName;

    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "driver_qualification", joinColumns = { @JoinColumn(name = "driver_id") }, inverseJoinColumns = {
            @JoinColumn(name = "qualification_id") })
    private Set<Qualification> qualifications = new HashSet<>();

    public Driver() {
    }

    public Driver(String firstName, String lastName, BigDecimal salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
    }

    public Long getId() {
        return this.id;
    }

    public String getFistName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BigDecimal getSalary() {
        return this.salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Company getCompany() {
        return this.company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Qualification> getQualifications() {
        return this.qualifications;
    }

    public void setQualifications(Set<Qualification> qualifications) {
        this.qualifications = qualifications;
    }

    public void addQualification(Qualification qualification) {
        this.qualifications.add(qualification);
        qualification.getDrivers().add(this);
    }

    public void removeQualification(Long qualificationId) {
        var qualification = this.qualifications.stream().filter(c -> c.getId() == qualificationId).findFirst()
                .orElse(null);
        if (qualification != null) {
            this.qualifications.remove(qualification);
            qualification.getDrivers().remove(this);
        }
    }
}
