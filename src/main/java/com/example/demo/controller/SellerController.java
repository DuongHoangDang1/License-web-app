package com.example.demo.controller;

import com.example.demo.pojo.Feedback;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.Product;
import com.example.demo.pojo.User;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        //Giả sử bạn có danh sách sản phẩm
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);

        // Giả lập user (sau này sẽ lấy từ session / authentication)
        User user = new User();
        user.setId(1);
        user.setUsername("demo_seller");
        model.addAttribute("user", user);

        return "seller-home";
    }
    @GetMapping("/revenue")
    public String revenue(Model model) {
        model.addAttribute("pageTitle", "Doanh thu tháng này");

        // Lấy tháng và năm hiện tại
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();

        // Lấy danh sách đơn hàng trong năm hiện tại
        List<Order> orders = orderService.getOrdersByYear(currentYear);

        //  Tính tổng doanh thu theo tháng
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

        // Lấy doanh thu và số đơn trong tháng hiện tại
        double totalRevenueThisMonth = monthlyRevenue.getOrDefault(currentMonth, 0.0);
        long orderCountThisMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().getMonthValue() == currentMonth)
                .count();

        //Chuyển danh sách doanh thu ra mảng để Chart.js hiển thị
        List<Double> revenueData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            revenueData.add(monthlyRevenue.get(i));
        }

        // Truyền dữ liệu ra view
        model.addAttribute("revenue", totalRevenueThisMonth);
        model.addAttribute("orderCount", orderCountThisMonth);
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("currentYear", currentYear);

        return "seller-revenue";
    }
    @GetMapping("/feedback/list")
    public String feedback(Model model) {
        // Gọi qua đối tượng feedbackService, không phải class
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

        // Gửi dữ liệu ra template
        model.addAttribute("pageTitle", "Phản hồi khách hàng");
        model.addAttribute("feedbacks", feedbackList);
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("totalFeedback", totalCount);
        return "feedback3"; // Tên file feedback2.html
    }
    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("pageTitle", "Khách hàng tiềm năng");
        return "seller-customer";
    }
}
