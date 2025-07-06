package com.fixit.FixIt.Backend.service;

import com.fixit.FixIt.Backend.model.Favourite;
import com.fixit.FixIt.Backend.model.User;

import java.util.List;

public interface FavouriteService {
    Favourite addFavourite(User customer, User provider);
    void removeFavourite(User customer, User provider);
    List<Favourite> getCustomerFavourites(User customer);
    boolean isFavourite(User customer, User provider);
} 