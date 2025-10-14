package com.example.demo.repository;

import com.example.demo.pojo.License;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseRepository extends JpaRepository<License, Long> {
}
