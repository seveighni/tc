package com.tc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;

@Entity
public class PassengerTransport extends Transport {
    private String numberOfPassengers;

    public PassengerTransport() {
        super();
    }

    public PassengerTransport(String startAddress, String endAddress, LocalDate startDate, LocalDate endDate,
            String numberOfPassengers, BigDecimal price, Boolean isPayed) {
        super(startAddress, endAddress, startDate, endDate, price, isPayed);
        this.numberOfPassengers = numberOfPassengers;
    }

    public String getNumberOfPassengers() {
        return this.numberOfPassengers;
    }

    public void setNumberOfPassengers(String numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }
}
