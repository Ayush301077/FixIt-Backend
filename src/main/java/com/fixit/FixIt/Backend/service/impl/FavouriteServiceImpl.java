package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.exception.ResourceNotFoundException;
import com.fixit.FixIt.Backend.model.Favourite;
import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.FavouriteRepository;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.FavouriteService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouriteServiceImpl implements FavouriteService {

    @Autowired
    private FavouriteRepository favouriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Favourite addFavourite(User customer, User provider) {
        // Check if favourite already exists
        if (favouriteRepository.findByCustomerAndProvider(customer, provider).isPresent()) {
            // Optionally, throw an exception or return existing favourite
            return favouriteRepository.findByCustomerAndProvider(customer, provider).get();
        }
        Favourite favourite = new Favourite();
        favourite.setCustomer(customer);
        favourite.setProvider(provider);
        return favouriteRepository.save(favourite);
    }

    @Override
    @Transactional
    public void removeFavourite(User customer, User provider) {
        favouriteRepository.deleteByCustomerAndProvider(customer, provider);
    }

    @Override
    public List<Favourite> getCustomerFavourites(User customer) {
        return favouriteRepository.findByCustomerId(customer.getId());
    }

    @Override
    public boolean isFavourite(User customer, User provider) {
        return favouriteRepository.findByCustomerAndProvider(customer, provider).isPresent();
    }
} 