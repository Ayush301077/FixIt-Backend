package com.fixit.FixIt.Backend.repository;

import com.fixit.FixIt.Backend.model.Favourite;
import com.fixit.FixIt.Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByCustomerId(Long customerId);
    Optional<Favourite> findByCustomerAndProvider(User customer, User provider);
    void deleteByCustomerAndProvider(User customer, User provider);
} 