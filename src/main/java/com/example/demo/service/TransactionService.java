package com.example.demo.service;

import com.example.demo.pojo.DepositTransaction;
import com.example.demo.repository.DepositTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Map<String, Object>> getRecentTransactionsWithUsernames() {
        return transactionRepository.findRecentTransactionsWithUsername();
    }


    public List<TopDepositor> getTopDepositors() {
        return transactionRepository.findTop3Depositors().stream()
                .map(obj -> new TopDepositor((String) obj[0], ((Number) obj[1]).doubleValue()))
                .collect(Collectors.toList());
    }

    public static class TopDepositor {
        private String username;
        private double total;

        public TopDepositor(String username, double total) {
            this.username = username;
            this.total = total;
        }

        public String getUsername() {
            return username;
        }

        public double getTotal() {
            return total;
        }
    }
}
