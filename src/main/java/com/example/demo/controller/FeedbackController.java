package com.example.demo.controller;

import com.example.demo.pojo.Feedback;
import com.example.demo.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // Hiển thị form phản hồi
    @GetMapping("/form")
    public String feedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "feedback";
    }

    // Xử lý form gửi phản hồi
    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute Feedback feedback) {
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
        return "redirect:/feedback/list";
    }

    @GetMapping("/feedback2")
    public String feedback2(Model model) {
        model.addAttribute("feedbacks", feedbackService.getAllFeedback());
        return "feedback2";
    }
}
