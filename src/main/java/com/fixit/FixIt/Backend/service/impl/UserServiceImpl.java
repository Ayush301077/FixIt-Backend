 package com.fixit.FixIt.Backend.service.impl;

import com.fixit.FixIt.Backend.model.User;
import com.fixit.FixIt.Backend.repository.UserRepository;
import com.fixit.FixIt.Backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}