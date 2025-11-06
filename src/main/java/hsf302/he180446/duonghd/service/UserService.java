package hsf302.he180446.duonghd.service;

import hsf302.he180446.duonghd.pojo.User;
import hsf302.he180446.duonghd.repository.UserRepository;
import hsf302.he180446.duonghd.repository.UserWalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserWalletService walletService;
    public UserService(UserRepository userRepository, UserWalletService walletService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
    }

    public void homeInfor(Authentication auth, Model model) {
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);

                walletService.findByWalletId(user.getId()).ifPresent(wallet ->
                        model.addAttribute("wallet", wallet)
                );
            });
        }
    }

    public Optional<User> findUserById(Long id){
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String name){
        return userRepository.findByUsername(name);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(Long.valueOf(id)).orElse(null);
    }

    public User updateUser(long id, User user) {
        return userRepository.save(user);
    }

    @Autowired
    private UserWalletRepository userWalletRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public Object findAll() {
        return userRepository.findAll();
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    @Transactional
    public void deleteById(Long id) {
        userWalletRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    public List<User> findPendingSellers(String role) {
        return userRepository.findByRole(role);
    }

}
