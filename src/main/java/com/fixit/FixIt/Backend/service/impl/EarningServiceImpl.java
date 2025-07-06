package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.model.Earning;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.EarningRepository;
import com.fixit.FixIt.Backend.service.EarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EarningServiceImpl implements EarningService {

    @Autowired
    private EarningRepository earningRepository;

    @Override
    public Earning createEarning(Earning earning) {
        return earningRepository.save(earning);
    }

    @Override
    public List<Earning> getDailyEarnings(User provider, LocalDate date) {
        return earningRepository.findByProviderAndDate(provider, date);
    }

    @Override
    public List<Earning> getWeeklyEarnings(User provider, LocalDate startDate, LocalDate endDate) {
        return earningRepository.findByProviderAndDateBetween(provider, startDate, endDate);
    }

    @Override
    public Double getDailyEarningsTotal(User provider, LocalDate date) {
        Double total = earningRepository.getDailyEarnings(provider, date);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getWeeklyEarningsTotal(User provider, LocalDate startDate, LocalDate endDate) {
        Double total = earningRepository.getEarningsBetweenDates(provider, startDate, endDate);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalEarnings(User provider) {
        Double total = earningRepository.getTotalEarnings(provider);
        return total != null ? total : 0.0;
    }
} 