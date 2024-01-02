package com.tc.request;

public record CreateVehicleRequest(String registration, String type, Integer capacity) {
}
