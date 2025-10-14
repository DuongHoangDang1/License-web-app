package com.example.demo.service;

import com.example.demo.pojo.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order save(Order order){
        orderRepository.save(order);
        return order;
    }
}
