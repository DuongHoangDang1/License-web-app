package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/settings")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
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

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Model model) {
        User existingUser = userService.findUserById(updatedUser.getId()).orElse(null);

        if (existingUser == null) {
            model.addAttribute("error", "Không tìm thấy người dùng!");
            return "profile";
        }

        existingUser.setFullname(updatedUser.getFullname());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAddress(updatedUser.getAddress());

        userService.updateUser(existingUser);

        model.addAttribute("success", "Cập nhật hồ sơ thành công!");
        model.addAttribute("user", existingUser);
        return "profile";
    }

    @GetMapping("/back")
    public String backToHome() {
        return "redirect:/home"; // đổi sang "/home" nếu bạn dùng route khác
    }
}
