package hsf302.he180446.duonghd.repository;

import hsf302.he180446.duonghd.pojo.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
