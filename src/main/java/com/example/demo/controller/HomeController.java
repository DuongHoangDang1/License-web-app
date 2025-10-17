package com.example.demo.controller;

import com.example.demo.pojo.Product;
import com.example.demo.pojo.User;
import com.example.demo.pojo.UserWallet;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserWalletService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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
    public String homePage(Model model, HttpSession ses) {
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

        String msg = (String) ses.getAttribute("successMessage");
        if(msg != null) {
            model.addAttribute("successMessage", msg);
            ses.removeAttribute("successMessage");
        }
        return "home";
    }

    @GetMapping("/search")
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword, Model model,
                               HttpSession ses) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);

                walletService.findByWalletId(user.getId()).ifPresent(wallet ->
                        model.addAttribute("wallet", wallet)
                );
            });
        }
        String msg = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Product> searchList = productService.searchByName(keyword);
            if(searchList.isEmpty()) {
                msg = "Từ khóa không hợp lệ";
            }
            model.addAttribute("products", searchList);
        } else {
            model.addAttribute("products", productService.findAll());
            msg = "Không tìm thấy sản phẩm";
        }


        if(msg != null) {
            model.addAttribute("successMessage", msg);
            ses.removeAttribute("successMessage");
        }

        return "home";
    }

    @GetMapping("/home2")
    public String homePage2(Model model) {
        model.addAttribute("products", productService.findAll());
        return "home2";
    }
}
