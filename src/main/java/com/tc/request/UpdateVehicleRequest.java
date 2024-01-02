package com.tc.request;

public record UpdateVehicleRequest(String registration, String type, Integer capacity) {
}