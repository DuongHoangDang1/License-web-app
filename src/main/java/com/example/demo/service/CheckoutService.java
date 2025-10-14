package com.example.demo.service;

import com.example.demo.pojo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CheckoutService {

    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserWalletService walletService;
    @Autowired
    private LicenseService licenseService;

    @Transactional
    public Order checkout(Long userId, Long productId, int quantity) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        double totalAmount = product.getPrice() * quantity;

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(totalAmount);
        order = orderService.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        orderItemService.save(item);

        UserWallet wallet = walletService.findByWalletId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (wallet.getBalance() < totalAmount) {
            throw new RuntimeException("Số dư không đủ");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOrder(order);
        payment.setMethod("WALLET");
        payment.setAmount(totalAmount);
        payment.setStatus("SUCCESS");
        payment.setTransactionCode(UUID.randomUUID().toString());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now());
        paymentService.savePayment(payment);

        wallet.setBalance(wallet.getBalance() - totalAmount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletService.save(wallet);

        order.setStatus("PAID");
        orderService.save(order);

        for (int i = 0; i < quantity; i++) {
            License license = new License();
            license.setUser(user);
            license.setProduct(product);
            license.setLicenseKey(UUID.randomUUID().toString());
            license.setIssueDate(LocalDateTime.now());
            license.setExpiryDate(
                    product.getLicenseDurationDays() != null
                            ? LocalDateTime.now().plusDays(product.getLicenseDurationDays())
                            : null
            );
            license.setStatus("ACTIVE");
            licenseService.save(license);
        }

        return order;
    }

}
