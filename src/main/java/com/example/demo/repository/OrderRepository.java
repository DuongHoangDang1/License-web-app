package com.example.demo.repository;

import com.example.demo.pojo.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByOrderDateDesc(Long userId);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}
