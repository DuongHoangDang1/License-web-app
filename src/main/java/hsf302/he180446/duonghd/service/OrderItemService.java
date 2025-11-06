package hsf302.he180446.duonghd.service;

import hsf302.he180446.duonghd.pojo.OrderItem;
import hsf302.he180446.duonghd.repository.OrderItemRepository;
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
