package com.example.demo.service;

import com.example.demo.pojo.License;
import com.example.demo.repository.LicenseRepository;
import org.springframework.stereotype.Service;

@Service
public class LicenseService {
    private final LicenseRepository licenseRepository;
    public LicenseService(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public void save(License license) {
        licenseRepository.save(license);
    }
}
