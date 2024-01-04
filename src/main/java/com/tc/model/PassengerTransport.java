package com.tc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;

@Entity
public class PassengerTransport extends Transport {
    private Integer numberOfPassengers;

    public PassengerTransport() {
        super();
    }

    public PassengerTransport(String startAddress, String endAddress, LocalDate startDate, LocalDate endDate,
            Integer numberOfPassengers, BigDecimal price, Boolean isPaid) {
        super(startAddress, endAddress, startDate, endDate, price, isPaid);
        this.numberOfPassengers = numberOfPassengers;
    }

    public Integer getNumberOfPassengers() {
        return this.numberOfPassengers;
    }

    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }
}
