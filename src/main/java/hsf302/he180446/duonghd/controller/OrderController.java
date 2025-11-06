package hsf302.he180446.duonghd.controller;

import hsf302.he180446.duonghd.pojo.Order;
import hsf302.he180446.duonghd.pojo.Product;
import hsf302.he180446.duonghd.service.CheckoutService;
import hsf302.he180446.duonghd.service.OrderService;
import hsf302.he180446.duonghd.service.ProductService;
import hsf302.he180446.duonghd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController{
    @Autowired
    private UserService userService;
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String showCheckoutForm(@RequestParam Long productId, Model model) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", product);
        return "checkout-form";
    }



    @PostMapping("/checkout")
    public String checkout(@RequestParam Long productId,
                           @RequestParam int quantity,
                           Principal principal,
                           Model model) {

        Long userId = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        Order order = checkoutService.checkout(userId, productId, quantity);
        model.addAttribute("order", order);
        return "checkout-success";
    }


    @GetMapping("/history")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("orders", orders);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.homeInfor(auth, model);
        return "order-history";
    }

    @GetMapping("/detail/" +
            "{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("items", order.getOrderItems());
        return "order-item";
    }

}
