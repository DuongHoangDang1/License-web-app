package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model, HttpSession ses) {
        if (error != null) {
            model.addAttribute("errorMsg", "Tên đăng nhập hoặc mật khẩu không đúng");
        }
        if (logout != null) {
            model.addAttribute("msg", "Bạn đã đăng xuất thành công");
        }

        ses.setAttribute("successMessage", "Đăng nhập thành công");

        return "login";
    }

}
