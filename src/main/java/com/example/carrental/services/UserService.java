package com.example.carrental.services;

import com.example.carrental.model.UserModel;
import com.example.carrental.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserModel> getallUsers() {
        return userRepository.findAll();
    }
    public UserModel saveUser(UserModel user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
       userRepository.deleteById(id);
    }

}
