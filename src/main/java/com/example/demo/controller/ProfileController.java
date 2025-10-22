package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

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


        @Autowired
        private UserService userService;

        @GetMapping
        public String showProfile (Model model){
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
        public String updateProfile (User updateUser, Model model){
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

            public String updateProfile (@ModelAttribute("user") User updatedUser, Model model){
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
                return "profile";
            }

            @GetMapping("/back")
                public String backToHome () {
                return "redirect:/home"; // đổi sang "/home" nếu bạn dùng route khác
        }
    }
