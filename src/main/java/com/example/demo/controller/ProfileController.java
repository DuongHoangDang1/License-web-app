package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
<<<<<<< HEAD
=======
import org.springframework.beans.factory.annotation.Autowired;
>>>>>>> 5ea6ed9c6b1aa51da501d16c08c7b7030e6ae30f
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
=======
import org.springframework.web.bind.annotation.*;
>>>>>>> 5ea6ed9c6b1aa51da501d16c08c7b7030e6ae30f

@Controller
@RequestMapping("/settings")
public class ProfileController {

<<<<<<< HEAD
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

=======
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
>>>>>>> 5ea6ed9c6b1aa51da501d16c08c7b7030e6ae30f
        return "profile";
    }

    @PostMapping("/update")
<<<<<<< HEAD
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

=======
    public String updateProfile(@ModelAttribute("user") User updatedUser, Model model) {
        User existingUser = userService.findUserById(updatedUser.getId()).orElse(null);

        if (existingUser == null) {
            model.addAttribute("error", "Không tìm thấy người dùng!");
            return "profile";
        }

        existingUser.setFullname(updatedUser.getFullname());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setAddress(updatedUser.getAddress());

        userService.updateUser(updatedUser.getId(), existingUser);

        model.addAttribute("success", "Cập nhật hồ sơ thành công!");
        model.addAttribute("user", existingUser);
        model.addAttribute("backUrl", "/home");
>>>>>>> 5ea6ed9c6b1aa51da501d16c08c7b7030e6ae30f
        return "profile";
    }

    @GetMapping("/back")
    public String backToHome() {
        return "redirect:/home"; // đổi sang "/home" nếu bạn dùng route khác
    }
}
