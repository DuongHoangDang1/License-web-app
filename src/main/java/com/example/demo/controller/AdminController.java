package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/m")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userForm", new User());
        return "admin";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("editUser") User user,
                           @RequestParam(value = "newPassword", required = false) String newPassword) {

        User existingUser = userService.findUserById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId()));

        user.setFullname(existingUser.getFullname());

        if (newPassword == null || newPassword.trim().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else {
            String encoded = passwordEncoder.encode(newPassword);
            user.setPassword(encoded);
        }

        userService.saveUser(user);
        return "redirect:/m/users";
    }



    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("users", userService.findAll());
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        model.addAttribute("editUser", user);
        model.addAttribute("userEdit", true);
        return "editaccount";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/m/users";
    }

}
