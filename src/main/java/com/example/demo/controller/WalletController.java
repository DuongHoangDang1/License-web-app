package com.example.demo.controller;

import com.example.demo.pojo.DepositTransaction;
import com.example.demo.pojo.User;
import com.example.demo.pojo.UserWallet;
import com.example.demo.repository.DepositTransactionRepository;
import com.example.demo.service.TransactionService;
import com.example.demo.service.UserService;
import com.example.demo.service.UserWalletService;
import com.example.demo.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserWalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DepositTransactionRepository transactionRepository;

    private final VNPayService vnpayService;

    public WalletController(VNPayService vnpayService) {
        this.vnpayService = vnpayService;
    }


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

        //có transaction
        UserWallet updatedWallet = walletService.topUpWallet(user.getId(), amount);
        DepositTransaction txn = new DepositTransaction();
        txn.setUserId(user.getId());
        txn.setAmount((long) amount);
        txn.setTxnRef("ADMIN-" + System.currentTimeMillis());
        txn.setStatus(DepositTransaction.Status.SUCCESS);
        transactionRepository.save(txn);

        model.addAttribute("wallet", updatedWallet);
        model.addAttribute("success", "Nạp tiền thành công!");
        return "wallet-topup";
    }

    @GetMapping("/history")
    public String showHistory(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() ->new RuntimeException("User not found"));
        Long currentUserId = currentUser.getId();
        List<DepositTransaction> transactions = transactionRepository.findByUserId(currentUserId);
        model.addAttribute("transactions", transactions);
        return "history";
    }




    //vn pay top up
    @GetMapping("/topupvnp")
    public String showTopupPage() {
        return "topup-vnp";
    }

    @PostMapping("/topupvnp")
    public void createPayment(
            @RequestParam("amount") int amount,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (amount <= 0) {
            response.sendRedirect("/wallet/topupvnp?error=amount");
            return;
        }

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String paymentUrl = vnpayService.createOrder(
                amount,
                "Nap tien vao vi",
                baseUrl
        );

        response.sendRedirect(paymentUrl);
    }

    //return
    @GetMapping("/vnpay-payment")
    public String handleVnpayReturn(HttpServletRequest request, Model model, Principal principal,
                                    HttpSession ses) {
        if (principal == null) {
            model.addAttribute("message", "Vui lòng đăng nhập để hoàn tất giao dịch.");
            return "result";
        }

        String txnRef = request.getParameter("vnp_TxnRef");
        String amountParam = request.getParameter("vnp_Amount");
        long amount = Long.parseLong(amountParam) / 100;
        int paymentStatus = vnpayService.orderReturn(request);

        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isSuccess = paymentStatus == 1;
        String message = transactionService.processVnpayTransaction(
                user.getId(),
                txnRef,
                amount,
                isSuccess
        );

        ses.setAttribute("successMessage", message);
        return "redirect:/home";
    }

}
