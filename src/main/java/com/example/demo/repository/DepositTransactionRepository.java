package com.example.demo.repository;

import com.example.demo.pojo.DepositTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositTransactionRepository  extends JpaRepository<DepositTransaction, Long> {
    Optional<DepositTransaction> findByTxnRef(String txnRef);
}
