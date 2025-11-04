package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TransactionService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/m")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/topupadmin")
    public String topupAdmin(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userForm", new User());
        model.addAttribute("recentTransactions", transactionService.getRecentTransactionsWithUsernames());
        model.addAttribute("topUsers", transactionService.getTopDepositors());
        return "admin-payment";
    }

    @PostMapping("/topupadmin")
    public String topupAdminAction(@RequestParam("username") String username,
                                   @RequestParam("amount") Double amount,
                                   Model model) {
        try {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

            transactionService.topupForAdmin(user.getId(), amount);

            model.addAttribute("successMessage", "Đã nạp " + amount + "₫ cho " + username);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi nạp tiền: " + e.getMessage());
        }

        model.addAttribute("users", userService.findAll());
        model.addAttribute("recentTransactions", transactionService.getRecentTransactionsWithUsernames());
        model.addAttribute("topUsers", transactionService.getTopDepositors());
        return "admin-payment";
    }




    @GetMapping("/viewsellers")
    public String viewSellers(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userForm", new User());
        return "seller-list";
    }

    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng có ID: " + id));
        user.setVerified(false);
        userService.saveUser(user);
        return "redirect:/m/users";
    }

    @PostMapping("/enable/{id}")
    public String enableUser(@PathVariable Long id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng có ID: " + id));
        user.setVerified(true);
        userService.saveUser(user);
        return "redirect:/m/users";
    }

    @GetMapping("/upgrade")
    public String showUpgradeForm(Model model, Principal principal) {
        Optional<User> optionalUser = userService.findByUsername(principal.getName());
        if (optionalUser.isPresent()) {
            model.addAttribute("user", optionalUser.get());
        }
        return "seller-register";
    }

    @PostMapping("/upgrade")
    public String upgradeSeller(Model model, Principal principal) {
        Optional<User> optionalUser = userService.findByUsername(principal.getName());

        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy tài khoản người dùng.");
            return "seller-register";
        }

        User user = optionalUser.get();

        if ("SELLER".equals(user.getRole())) {
            model.addAttribute("success", "Bạn đã là Seller rồi!");
        }
        else if ("SELLER_PENDING".equals(user.getRole())) {
            model.addAttribute("pending", "Yêu cầu của bạn đang chờ phê duyệt từ quản trị viên.");
        }
        else if ("USER".equals(user.getRole())) {
            user.setRole("SELLER_PENDING");
            userService.save(user);
            model.addAttribute("pending", "Yêu cầu của bạn đang chờ phê duyệt từ quản trị viên.");
        }

        model.addAttribute("user", user);
        return "seller-register";
    }



    @GetMapping("/pending")
    public String viewPending(Model model) {
        List<User> pendingUsers = userService.findPendingSellers("SELLER_PENDING");
        model.addAttribute("pendingRequests", pendingUsers);
        return "seller-pending";
    }



    @PostMapping("/pending")
    public String requestUpgrade(Model model, Principal principal) {
        Optional<User> optionalUser = userService.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy người dùng!");
            return "seller-register";
        }

        User user = optionalUser.get();

        if ("SELLER".equals(user.getRole())) {
            model.addAttribute("success", "Bạn đã là Seller rồi!");
        }
        else if ("SELLER_PENDING".equals(user.getRole())) {
            model.addAttribute("pending", "Yêu cầu của bạn đang chờ phê duyệt.");
        }
        else if ("USER".equals(user.getRole())) {
            user.setRole("SELLER_PENDING");
            userService.save(user);
            model.addAttribute("pending", "Yêu cầu nâng cấp đã được gửi.");
        }

        model.addAttribute("user", user);
        return "seller-register";
    }


    @PostMapping("/approve/{id}")
    public String approveSeller(@PathVariable Long id) {
        Optional<User> optionalUser = userService.findUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole("SELLER");
            userService.saveUser(user);
        }
        return "redirect:/m/pending";
    }

    @PostMapping("/reject/{id}")
    public String rejectSeller(@PathVariable Long id) {
        Optional<User> optionalUser = userService.findUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setRole("USER");
            userService.saveUser(user);
        }
        return "redirect:/m/pending";
    }

}
