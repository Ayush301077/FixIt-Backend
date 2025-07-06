package com.fixit.FixIt.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor

public class EarningsResponseDto {
    private BigDecimal totalEarnings;
    private Integer completedServicesCount;
    private List<CompletedServiceDetailDto> completedServices;

    public EarningsResponseDto(BigDecimal totalEarnings, Integer completedServicesCount, List<CompletedServiceDetailDto> completedServices) {
        this.totalEarnings = totalEarnings;
        this.completedServicesCount = completedServicesCount;
        this.completedServices = completedServices;
    }

    // Explicit Getters to ensure serialization (keep these)
    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public Integer getCompletedServicesCount() {
        return completedServicesCount;
    }

    public List<CompletedServiceDetailDto> getCompletedServices() {
        return completedServices;
    }
}