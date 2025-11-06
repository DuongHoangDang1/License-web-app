package hsf302.he180446.duonghd.repository;

import hsf302.he180446.duonghd.pojo.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByOrderDateDesc(Long userId);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
