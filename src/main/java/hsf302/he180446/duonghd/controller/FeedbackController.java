package hsf302.he180446.duonghd.controller;

import hsf302.he180446.duonghd.pojo.Feedback;
import hsf302.he180446.duonghd.pojo.User;
import hsf302.he180446.duonghd.service.FeedbackService;
import hsf302.he180446.duonghd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService UserService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.UserService = userService;
    }
    @GetMapping("/form")
    public String feedbackForm(Model model, @AuthenticationPrincipal UserDetails user) {
        Feedback feedback = new Feedback();
        Optional<User> currentUser = UserService.findByUsername(user.getUsername());
        if (currentUser.isPresent()) {
            feedback.setName(currentUser.get().getUsername() != null ? currentUser.get().getUsername() : currentUser.get().getUsername());
            feedback.setEmail(currentUser.get().getEmail());
        } else {
            feedback.setName(user.getUsername());
            feedback.setEmail("unknown@example.com");
        }

        model.addAttribute("feedback", feedback);
        return "feedback"; // file feedback.html
    }

    // Xử lý form gửi phản hồi
    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute Feedback feedback,
                                 @AuthenticationPrincipal UserDetails user) {

        Optional<User> currentUserOpt = UserService.findByUsername(user.getUsername());

        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            String displayName = currentUser.getUsername() != null && !currentUser.getUsername().isBlank()
                    ? currentUser.getUsername()
                    : currentUser.getUsername();
            feedback.setName(displayName);
            feedback.setEmail(currentUser.getEmail());
        } else {
            feedback.setName(user.getUsername());
            feedback.setEmail("unknown@example.com");
        }

        feedbackService.saveFeedback(feedback);
        return "redirect:/home";
    }

    @GetMapping("/list")
    public String viewFeedbackList(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "feedback2";
    }

    @GetMapping("/list2")
    public String viewFeedbackList2(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "adminfeedback";
    }

    // Xóa phản hồi
    @GetMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/feedback/list2";
    }

    @GetMapping("/feedback2")
    public String feedback2(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "feedback2";
    }

    // Hiển thị form trả lời
    @GetMapping("/reply/{id}")
    public String replyForm(@PathVariable Long id, Model model) {
        Optional<Feedback> feedbackOpt = feedbackService.getFeedbackById(id);
        if (feedbackOpt.isPresent()) {
            model.addAttribute("feedback", feedbackOpt.get());
            return "adminreply";
        } else {
            return "redirect:/feedback/list2";
        }
    }

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostMapping("/sendReply")
    public String sendReply(@RequestParam("toEmail") String toEmail,
                            @RequestParam("replyMessage") String replyMessage,
                            Model model) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Phản hồi từ Admin");
            message.setText(replyMessage);
            message.setFrom(fromEmail);
            mailSender.send(message);
            model.addAttribute("success", "Phản hồi đã được gửi đến " + toEmail);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể gửi phản hồi: " + e.getMessage());
        }
        return "redirect:/feedback/list2";
    }
}
