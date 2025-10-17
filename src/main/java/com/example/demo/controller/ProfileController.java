package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy thông tin user từ DB
        User user = userService.findByUsername(username).orElse(null);

        // Thêm user vào model
        model.addAttribute("user", user);
        model.addAttribute("backUrl", "/home");

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(User updatedUser, Model model) {
        userService.findUserById(updatedUser.getId()).ifPresent(user -> {
            user.setEmail(updatedUser.getEmail());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setAddress(updatedUser.getAddress());
            userService.save(user);
        });

        model.addAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        model.addAttribute("user", updatedUser);
        model.addAttribute("backUrl", "/home");

        return "profile";
    }

    @GetMapping("/back")
    public String backToHome() {
        return "redirect:/home"; // đổi sang "/home" nếu bạn dùng route khác
    }
}
