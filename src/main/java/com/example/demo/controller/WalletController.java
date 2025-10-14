package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.pojo.UserWallet;
import com.example.demo.service.UserService;
import com.example.demo.service.UserWalletService;
import com.example.demo.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserWalletService walletService;

    private final VnpayService vnpayService;

    public WalletController(VnpayService vnpayService) {
        this.vnpayService = vnpayService;
    }

    //nạp tiền admin
    @GetMapping("/topup")
    public String showTopUpForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserWallet wallet = walletService.getOrCreateWallet(user);

        model.addAttribute("wallet", wallet);
        return "wallet-topup";
    }


    // nạp tiền admin
    @PostMapping("/topup")
    public String topUpWallet(@RequestParam double amount, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (amount <= 0) {
            model.addAttribute("error", "Số tiền phải lớn hơn 0");
            return "wallet-topup";
        }

        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // (có transaction)
        UserWallet updatedWallet = walletService.topUpWallet(user.getId(), amount);

        model.addAttribute("wallet", updatedWallet);
        model.addAttribute("success", "Nạp tiền thành công!");
        return "wallet-topup";
    }


    // form nạp tiền vnp
    @GetMapping("/topupvnp")
    public String showTopupPage() {
        return "topup"; // trỏ tới file templates/wallet/topup.html
    }

    // redirect to trang thanh toán VNPAY
    @PostMapping("/topupvnp")
    public void createPayment(
            @RequestParam("amount") Long amount,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String paymentUrl = vnpayService.createPaymentUrl(amount, request);
        response.sendRedirect(paymentUrl);
    }


    //VNPAY redirect về đây sau khi thanh toán xong
    @GetMapping("/vnpay-return")
    public String handleVnpayReturn(
            HttpServletRequest request,
            Model model
    ) {
        boolean success = vnpayService.handleVnpayReturn(request);
        if (success) {
            model.addAttribute("message", "💰 Nạp tiền thành công!");
        } else {
            model.addAttribute("message", "❌ Giao dịch thất bại hoặc không hợp lệ!");
        }
        return "result";
    }

    @PostMapping("/api/vnpay/ipn")
    @ResponseBody
    public ResponseEntity<String> handleVnpayIPN(HttpServletRequest request) {
        boolean success = vnpayService.handleVnpayReturn(request);
        if (success) {
            //trả về “success” cho VNPAY
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.badRequest().body("Invalid signature or failed");
        }
    }




}
