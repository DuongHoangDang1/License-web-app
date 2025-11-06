package hsf302.he180446.duonghd.controller;

import hsf302.he180446.duonghd.pojo.License;
import hsf302.he180446.duonghd.repository.LicenseRepository;
import hsf302.he180446.duonghd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@RequestMapping("/licenses")
public class LicenseController {

    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listLicenses(Model model) {
        AtomicReference<Long> userId = new AtomicReference<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                userId.set(user.getId());
            });
        }
        List<License> licenses = licenseRepository.findByUser_Id(userId.get());
        model.addAttribute("licenses", licenses);
        return "license-list";
    }
}

