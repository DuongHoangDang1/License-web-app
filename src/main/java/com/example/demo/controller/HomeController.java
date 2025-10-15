package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.pojo.UserWallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;


@Controller
public class HomeController {
    private final ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWalletService walletService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("products", productService.findAll());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);

                walletService.findByWalletId(user.getId()).ifPresent(wallet ->
                        model.addAttribute("wallet", wallet)
                );
            });
        }
        return "home";
    }

    @GetMapping("/home2")
    public String homePage2(Model model) {
        model.addAttribute("products", productService.findAll());
        return "home2";
    }
}
