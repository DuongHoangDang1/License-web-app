package hsf302.he180446.duonghd.repository;

import hsf302.he180446.duonghd.pojo.DepositTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DepositTransactionRepository  extends JpaRepository<DepositTransaction, Long> {
    Optional<DepositTransaction> findByTxnRef(String txnRef);
    List<DepositTransaction> findByUserId(Long userId);
    DepositTransaction save(DepositTransaction transaction);

    @Query("""
    SELECT new map(u.username as username, t.amount as amount, t.createdAt as createdAt)
    FROM DepositTransaction t
    JOIN User u ON t.userId = u.id
    ORDER BY t.createdAt DESC
""")
    List<Map<String, Object>> findRecentTransactionsWithUsername();


    @Query("""
        SELECT u.username, SUM(t.amount) AS total
        FROM DepositTransaction t
        JOIN User u ON t.userId = u.id
        GROUP BY u.username
        ORDER BY total DESC
    """)
    List<Object[]> findTop3Depositors();
}
