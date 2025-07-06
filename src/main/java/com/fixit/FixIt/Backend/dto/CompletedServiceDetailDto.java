package com.fixit.FixIt.Backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CompletedServiceDetailDto {
    private LocalDate date;
    private String customerName;
    private String customerContact;
    private String serviceName;
    private BigDecimal serviceCharge;

    public CompletedServiceDetailDto(LocalDate date, String customerName, String customerContact, String serviceName, BigDecimal serviceCharge) {
        this.date = date;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.serviceName = serviceName;
        this.serviceCharge = serviceCharge;
    }

    // Explicit getters for JSON serialization (keep these)
    public LocalDate getDate() {
        return date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getServiceCharge() {
        return serviceCharge;
    }
}