package com.example.demo.service;

import com.example.demo.pojo.OrderItem;
import com.example.demo.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public void save(OrderItem orderItem){
        orderItemRepository.save(orderItem);
    }

}
