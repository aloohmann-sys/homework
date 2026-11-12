package com.example.homework.notification;

import org.springframework.stereotype.Service;

@Service
class NotificationEnforcementService {

  private static final String RECIPIENT_REJECTION_MESSAGE =
      "Business Rule Violation: User is blacklisted or rate-limited by DB policy.";
  private static final String CIRCUIT_BREAKER_MESSAGE = "Circuit breaker OPEN via database state.";

  private final NotificationRepository repository;

  NotificationEnforcementService(NotificationRepository repository) {
    this.repository = repository;
  }

  void validateTarget(String target) {
    if (target == null || target.length() == 0) {
      throw new IllegalArgumentException("Target is mandatory.");
    }

    if (repository.isRecipientBlockedOrRateLimited(target)) {
      throw new RuntimeException(RECIPIENT_REJECTION_MESSAGE);
    }
  }

  void rejectIfCircuitBreakerIsOpen(long dispatchId) {
    if (repository.getFailureCount() >= 3) {
      repository.recordRejection(dispatchId, CIRCUIT_BREAKER_MESSAGE);
      throw new RuntimeException(CIRCUIT_BREAKER_MESSAGE);
    }
  }
}
