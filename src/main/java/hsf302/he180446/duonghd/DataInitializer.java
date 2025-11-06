package hsf302.he180446.duonghd;

import hsf302.he180446.duonghd.pojo.User;
import hsf302.he180446.duonghd.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("testuser").isEmpty()) {
                User user = new User();
                user.setUsername("testuser");

                // Lưu password đã hash bằng BCrypt
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                user.setPassword(encoder.encode("123456"));

                user.setEmail("testuser@example.com");
                user.setRole("USER");
                user.setVerified(true);

                userRepository.save(user);
                System.out.println("Test user created: testuser / 123456");
            }
        };
    }
}

