package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    // Hiển thị trang profile
    @GetMapping("/profile")
    public String showProfile(Model model) {
        // Lấy username của user đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Tìm user trong DB
        User user = userService.findByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy người dùng!");
            return "profile";
        }

        model.addAttribute("user", user);
        model.addAttribute("backUrl", "/home");
        return "profile";
    }

    // Quay về trang chủ
    @GetMapping("/back")
    public String backToHome() {
        return "redirect:/home";
    }
}
