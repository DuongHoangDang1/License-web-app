package com.example.demo.controller;

import com.example.demo.pojo.Feedback;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.Product;
import com.example.demo.pojo.User;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/sellers")
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private OrderService orderService;


    @GetMapping("/home")
    public String sellerHome(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        User user = new User();
        user.setId(1);
        user.setUsername("demo_seller");
        model.addAttribute("user", user);

        return "seller-home";
    }
    @GetMapping("/revenue")
    public String revenue(Model model) {
        model.addAttribute("pageTitle", "Doanh thu tháng này");
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        List<Order> orders = orderService.getOrdersByYear(currentYear);
        Map<Integer, Double> monthlyRevenue = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, 0.0);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null && order.getTotalAmount() != null) {
                int month = order.getCreatedAt().getMonthValue();
                monthlyRevenue.put(month,
                        monthlyRevenue.getOrDefault(month, 0.0) + order.getTotalAmount());
            }
        }

        double totalRevenueThisMonth = monthlyRevenue.getOrDefault(currentMonth, 0.0);
        long orderCountThisMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().getMonthValue() == currentMonth)
                .count();

        List<Double> revenueData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            revenueData.add(monthlyRevenue.get(i));
        }

        model.addAttribute("revenue", totalRevenueThisMonth);
        model.addAttribute("orderCount", orderCountThisMonth);
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("currentYear", currentYear);

        return "seller-revenue";
    }
    @GetMapping("/feedback/list")
    public String feedback(Model model) {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();


        long positiveCount = feedbackList.stream()
                .filter(f -> f.getMessage() != null && (
                        f.getMessage().toLowerCase(Locale.ROOT).contains("tốt") ||
                                f.getMessage().toLowerCase(Locale.ROOT).contains("hài lòng") ||
                                f.getMessage().toLowerCase(Locale.ROOT).contains("tuyệt vời") ||
                                f.getMessage().toLowerCase(Locale.ROOT).contains("ổn") ||
                                f.getMessage().toLowerCase(Locale.ROOT).contains("ok") ||
                                f.getMessage().toLowerCase(Locale.ROOT).contains("5 sao")
                ))
                .count();

        long totalCount = feedbackList.size();
        long negativeCount = totalCount - positiveCount;
        model.addAttribute("pageTitle", "Phản hồi khách hàng");
        model.addAttribute("feedbacks", feedbackList);
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("totalFeedback", totalCount);
        return "feedback3";
    }
    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("pageTitle", "Khách hàng tiềm năng");
        return "seller-customer";
    }


    @GetMapping("/register")
    public String showSellerRegisterForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        Optional<User> currentUserOpt = userService.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy tài khoản người dùng!");
            return "seller-register";
        }

        model.addAttribute("user", currentUserOpt.get());
        return "seller-register";
    }

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerSeller(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String sellerDescription,
                                 @RequestParam String sellerPhone,
                                 Model model) {

        String username = userDetails.getUsername();
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy tài khoản người dùng!");
            return "seller-register";
        }

        User user = userOpt.get();
        user.setSellerDescription(sellerDescription);
        user.setSellerPhone(sellerPhone);
        user.setSeller(true);
        userService.save(user);

        model.addAttribute("success", "Nâng cấp tài khoản thành Seller thành công!");
        model.addAttribute("user", user);
        return "seller-register";
    }


}
