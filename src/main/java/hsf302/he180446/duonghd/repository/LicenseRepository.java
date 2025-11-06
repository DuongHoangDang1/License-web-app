package hsf302.he180446.duonghd.repository;

import hsf302.he180446.duonghd.pojo.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {
    Optional<License> findByLicenseKey(String licenseKey);
    List<License> findByUser_Id(Long userId);
}
