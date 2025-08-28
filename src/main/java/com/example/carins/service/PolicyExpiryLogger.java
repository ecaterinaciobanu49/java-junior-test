package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class PolicyExpiryLogger {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyExpiryLogger.class);
    private final InsurancePolicyRepository insuranceRepository;
    private final ZoneId zoneId = ZoneId.systemDefault();

    public PolicyExpiryLogger(InsurancePolicyRepository insuranceRepository) {
        this.insuranceRepository = insuranceRepository;
    }

    //@Scheduled(cron = "*/30 * * * * *") //test only
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void logRecentlyExpiredPolicies() {
        LocalDate today = LocalDate.now(zoneId);

        List<InsurancePolicy> candidates = insuranceRepository.findExpiredPolicies(today);

        if (candidates.isEmpty()) return;

        for (InsurancePolicy p : candidates) {

            log.info("Policy {} for car {} expired on {}",
                    p.getId(),
                    p.getCar() != null ? p.getCar().getId() : null,
                    p.getEndDate());
        }
    }
}
