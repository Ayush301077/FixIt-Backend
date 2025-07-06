 package com.fixit.FixIt.Backend.service;

import com.fixit.FixIt.Backend.model.Earning;
import com.fixit.FixIt.Backend.model.User;

import java.time.LocalDate;
import java.util.List;

public interface EarningService {
    Earning createEarning(Earning earning);
    List<Earning> getDailyEarnings(User provider, LocalDate date);
    List<Earning> getWeeklyEarnings(User provider, LocalDate startDate, LocalDate endDate);
    Double getDailyEarningsTotal(User provider, LocalDate date);
    Double getWeeklyEarningsTotal(User provider, LocalDate startDate, LocalDate endDate);
    Double getTotalEarnings(User provider);
}