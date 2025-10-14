package com.example.demo.repository;

import com.example.demo.pojo.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
    <T> Optional<T> findByUserId(Long userId);
}
