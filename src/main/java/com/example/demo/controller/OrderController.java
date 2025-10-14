package com.example.demo.controller;

import com.example.demo.pojo.Order;
import com.example.demo.pojo.Product;
import com.example.demo.service.CheckoutService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/order")
public class OrderController{
    @Autowired
    private UserService userService;
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private ProductService productService;

    @GetMapping("/checkout")
    public String showCheckoutForm(@RequestParam Long productId, Model model) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "checkout-form";
    }



    @PostMapping("/checkout")
    public String checkout(@RequestParam Long productId,
                           @RequestParam int quantity,
                           Principal principal,
                           Model model) {

        Long userId = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        Order order = checkoutService.checkout(userId, productId, quantity);
        model.addAttribute("order", order);

        return "checkout-success";
    }
}
