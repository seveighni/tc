package com.tc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;

@Entity
public class CargoTransport extends Transport {
    private String cargoType;
    private Integer cargoWeight;

    public CargoTransport() {
        super();
    }

    public CargoTransport(String startAddress, String endAddress, LocalDate startDate, LocalDate endDate,
            String cargoType, Integer cargoWeight, BigDecimal price, Boolean isPayed) {
        super(startAddress, endAddress, startDate, endDate, price, isPayed);
        this.cargoType = cargoType;
        this.cargoWeight = cargoWeight;
    }

    public String getCargoType() {
        return this.cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public Integer getCargoWeight() {
        return this.cargoWeight;
    }

    public void setCargoWeight(Integer cargoWeight) {
        this.cargoWeight = cargoWeight;
    }
}
