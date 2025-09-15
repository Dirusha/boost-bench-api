package com.boostbench.api.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public class CreateUserDto {
    @NotNull
    @Size(min = 1, max = 255)
    private String username;

    @NotNull
    @Size(min = 8, max = 255)
    private String password;

    @NotNull
    @Size(min = 1, max = 255)
    private String fullName;

    @NotNull
    @Email
    private String workEmail;

    @NotNull
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull
    @Size(min = 1, max = 255)
    private String address;

    @NotNull
    @Size(min = 1, max = 100)
    private String city;

    @NotNull
    @Size(min = 1, max = 100)
    private String country;

    @NotNull
    @Pattern(regexp = "^\\d{5,10}$", message = "Invalid zip/postal code")
    private String zipCode;

    private Set<Long> roleIds;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getWorkEmail() { return workEmail; }
    public void setWorkEmail(String workEmail) { this.workEmail = workEmail; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public Set<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds; }
}