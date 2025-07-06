package com.fixit.FixIt.Backend.repository;

import com.fixit.FixIt.Backend.model.Earning;
import com.fixit.FixIt.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EarningRepository extends JpaRepository<Earning, Long> {
    
    List<Earning> findByProviderAndDate(User provider, LocalDate date);
    
    @Query("SELECT e FROM Earning e WHERE e.provider = :provider AND e.date BETWEEN :startDate AND :endDate")
    List<Earning> findByProviderAndDateBetween(
            @Param("provider") User provider,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.provider = :provider AND e.date = :date")
    Double getDailyEarnings(@Param("provider") User provider, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.provider = :provider AND e.date BETWEEN :startDate AND :endDate")
    Double getEarningsBetweenDates(
            @Param("provider") User provider,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.provider = :provider")
    Double getTotalEarnings(@Param("provider") User provider);
} 