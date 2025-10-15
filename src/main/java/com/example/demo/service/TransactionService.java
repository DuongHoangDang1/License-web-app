package com.example.demo.service;

import com.example.demo.pojo.DepositTransaction;
import com.example.demo.repository.DepositTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransactionService {

    private final DepositTransactionRepository transactionRepository;
    private final UserWalletService walletService;

    public TransactionService(DepositTransactionRepository transactionRepository, UserWalletService walletService) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
    }

    @Transactional
    public String processVnpayTransaction(Long userId, String txnRef, long amount, boolean success) {
        Optional<DepositTransaction> existingOpt = transactionRepository.findByTxnRef(txnRef);

        if (existingOpt.isPresent()) {
            return "Giao dịch đã được xử lý trước đó!";
        }

        DepositTransaction transaction = new DepositTransaction();
        transaction.setUserId(userId);
        transaction.setTxnRef(txnRef);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());

        if (success) {
            transaction.setStatus(DepositTransaction.Status.SUCCESS);
            walletService.updateBalance(userId, amount);
            transactionRepository.save(transaction);
            return "Nạp tiền thành công!";
        } else {
            transaction.setStatus(DepositTransaction.Status.FAILED);
            transactionRepository.save(transaction);
            return "Giao dịch thất bại hoặc không hợp lệ!";
        }
    }
}
