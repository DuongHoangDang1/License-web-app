package hsf302.he180446.duonghd.controller;

import hsf302.he180446.duonghd.pojo.User;

import hsf302.he180446.duonghd.repository.UserRepository;
import hsf302.he180446.duonghd.service.EmailService;
import hsf302.he180446.duonghd.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String createUser(@ModelAttribute("user") User user, Model model) {

        User existingAppUser = userRepository.findByEmail(user.getEmail());
        if (existingAppUser != null) {
            if (existingAppUser.isVerified()) {
                model.addAttribute("error", "Tài khoản đã tồn tại và được xác thực.");
                return "signup";
            } else {
                String verificationToken = jwtUtil.generateToken(existingAppUser.getEmail());
                existingAppUser.setVerficationToken(verificationToken);
                userRepository.save(existingAppUser);
                emailService.sendVerificationEmail(existingAppUser.getEmail(), verificationToken);
                model.addAttribute("message", "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư.");
                return "signup";
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String verificationToken = jwtUtil.generateToken(user.getEmail());
        user.setVerficationToken(verificationToken);
        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        model.addAttribute("message", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực.");
        return "signup";
    }

    @ResponseBody
    @GetMapping("/signup/verify")
    public ResponseEntity verifyEmail(@RequestParam("token") String token) {
        String emailString = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(emailString);
        if (user == null || user.getVerficationToken() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired!");
        }

        if (!jwtUtil.validateToken(token) || !user.getVerficationToken().equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token Expired!");
        }
        user.setVerficationToken(null);
        user.setVerified(true);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Email successfully verified!");
    }

}
