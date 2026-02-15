package com.futbol.proyectoacd.model;

public enum Rol {
    ADMIN("admin"),
    USER("user");

    private final String value;

    Rol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
