package hsf302.he180446.duonghd.repository;

import hsf302.he180446.duonghd.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    User findByEmail(String email);

    List<User> findByRole(String role);

}
