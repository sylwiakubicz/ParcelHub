package com.parcelHub.shipment_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PersonDto {

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Postal code is required")
    @Pattern(regexp = "^\\d{2}-\\d{3}$", message = "Invalid postal code format")
    private String zip;

    public PersonDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
