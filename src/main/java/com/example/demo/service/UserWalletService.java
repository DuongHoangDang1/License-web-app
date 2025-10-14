package com.example.demo.service;

import com.example.demo.pojo.User;
import com.example.demo.pojo.UserWallet;
import com.example.demo.repository.UserWalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserWalletService {

    private final UserWalletRepository userWalletRepository;
    public UserWalletService(UserWalletRepository userWalletRepository) {
        this.userWalletRepository = userWalletRepository;
    }

    public Optional<UserWallet> findByWalletId(Long userId) {
        return userWalletRepository.findById(userId);
    }

    public void save(UserWallet userWallet) {
        userWalletRepository.save(userWallet);
    }

    public UserWallet saveAndReturn(UserWallet userWallet) {
         return userWalletRepository.save(userWallet);
    }

    @Transactional
    public UserWallet topUpWallet(Long userId, double amount) {
        UserWallet wallet = userWalletRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        return userWalletRepository.save(wallet);
    }

    @Transactional
    public UserWallet getOrCreateWallet(User user) {
        return userWalletRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserWallet newWallet = new UserWallet();
                    newWallet.setUser(user);
                    newWallet.setUserId(user.getId());
                    newWallet.setBalance(0.0);
                    newWallet.setUpdatedAt(LocalDateTime.now());
                    return userWalletRepository.save(newWallet);
                });
    }

    @Transactional
    public void updateBalance(Long userId, double amount) {
        UserWallet wallet = userWalletRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        userWalletRepository.save(wallet);
    }


//    @Transactional
//    public boolean payWithWallet(User user, Order order, double amount) {
//        UserWallet wallet = walletRepo.findById(user.getId())
//                .orElseThrow(() -> new RuntimeException("Wallet not found"));
//
//        if (wallet.getBalance() < amount) {
//            return false; // không đủ tiền
//        }
//
//        // Trừ tiền
//        wallet.setBalance(wallet.getBalance() - amount);
//        wallet.setUpdatedAt(LocalDateTime.now());
//        walletRepo.save(wallet);
//
//        // Lưu payment
//        Payment payment = new Payment();
//        payment.setUser(user);
//        payment.setOrder(order);
//        payment.setMethod("WALLET");
//        payment.setAmount(amount);
//        payment.setStatus("SUCCESS");
//        payment.setPaidAt(LocalDateTime.now());
//        paymentRepo.save(payment);
//        return true;
//    }
}

