package com.example.demo.service;

import com.example.demo.pojo.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserWalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String name){
        return userRepository.findByUsername(name);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(Long.valueOf(id)).orElse(null);
    }

    public User updateUser(long id, User user) {
        return userRepository.save(user);
    }

    @Autowired
    private UserWalletRepository userWalletRepository;

    public void save(User user) {
        userRepository.save(user);
    }
//    public User updateUser(Long id, User user) {
//        return userRepository.save(user);
//    }

    public Object findAll() {
        return userRepository.findAll();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    @Transactional
    public void deleteById(Long id) {
        userWalletRepository.deleteByUserId(id);
        userRepository.deleteById(id);    }

}
