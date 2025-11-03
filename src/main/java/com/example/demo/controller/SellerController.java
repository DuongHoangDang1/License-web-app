package com.example.demo.controller;

import com.example.demo.pojo.Product;
import com.example.demo.pojo.Feedback;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.User;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
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

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";


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
        for (int i = 1; i <= 12; i++) monthlyRevenue.put(i, 0.0);

        for (Order order : orders) {
            if (order.getCreatedAt() != null && order.getTotalAmount() != null) {
                int month = order.getCreatedAt().getMonthValue();
                monthlyRevenue.put(month, monthlyRevenue.getOrDefault(month, 0.0) + order.getTotalAmount());
            }
        }

        double totalRevenueThisMonth = monthlyRevenue.getOrDefault(currentMonth, 0.0);
        long orderCountThisMonth = orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().getMonthValue() == currentMonth)
                .count();

        List<Double> revenueData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) revenueData.add(monthlyRevenue.get(i));

        model.addAttribute("revenue", totalRevenueThisMonth);
        model.addAttribute("orderCount", orderCountThisMonth);
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("currentYear", currentYear);

        return "seller-revenue";
    }

    @GetMapping("/feedback/list")
    public String feedback(Model model) {
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        List<String> positiveKeywords = Arrays.asList(
                "tốt", "hài lòng", "tuyệt vời", "ổn", "ok", "5 sao",
                 "good", "great", "nice", "amazing", "love"
        );
        List<String> negativeKeywords = Arrays.asList(
                "tệ", "xấu", "không tốt", "kém", "chán", "thất vọng", "dở", "bad", "poor", "terrible"
        );
        long positiveCount = 0;
        long negativeCount = 0;
        long neutralCount = 0;
        for (Feedback f : feedbackList) {
            String msg = f.getMessage();
            if (msg == null || msg.trim().isEmpty()) continue;
            String lower = msg.toLowerCase(Locale.ROOT);
            boolean isPositive = positiveKeywords.stream().anyMatch(lower::contains);
            boolean isNegative = negativeKeywords.stream().anyMatch(lower::contains);
            if (isPositive && !isNegative) {
                positiveCount++;
            } else if (isNegative && !isPositive) {
                negativeCount++;
            } else {
                neutralCount++;
            }
        }
        long totalCount = feedbackList.size();
        model.addAttribute("pageTitle", "Phản hồi khách hàng");
        model.addAttribute("feedbacks", feedbackList);
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("neutralCount", neutralCount);
        model.addAttribute("totalFeedback", totalCount);
        return "feedback3";
    }



    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("pageTitle", "Khách hàng tiềm năng");
        return "seller-customer";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        return "addproduct";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") Product product,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, imageFile.getBytes());
                product.setImageUrl("/uploads/" + fileName);
            }
            productService.save(product);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Nếu người dùng nhập link, giữ nguyên
        } else if (!imageFile.isEmpty()) {
            // Nếu upload file, xử lý như hiện tại
        }

        return "redirect:/sellers/home";
    }


    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có id: " + id));
        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
        return "editproduct";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute("product") Product updatedProduct,
                                @RequestParam("imageFile") MultipartFile imageFile) {
        Product existingProduct = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có id: " + id));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());

        try {
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, imageFile.getBytes());
                existingProduct.setImageUrl("/uploads/" + fileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (updatedProduct.getImageUrl() != null && !updatedProduct.getImageUrl().isEmpty()) {
            existingProduct.setImageUrl(updatedProduct.getImageUrl());
        } else if (!imageFile.isEmpty()) {
            // Xử lý upload file như cũ
        }

        productService.save(existingProduct);
        return "redirect:/sellers/home";
    }


    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/sellers/home";
    }
}
