package com.example.homework.notification;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class LegacyNotificationEngine {

  private final NotificationRepository repository;
  private final NotificationDispatcher dispatcher;
  private final NotificationEnforcementService enforcementService;

  public LegacyNotificationEngine(
      NotificationRepository repository,
      NotificationDispatcher dispatcher,
      NotificationEnforcementService enforcementService) {
    this.repository = repository;
    this.dispatcher = dispatcher;
    this.enforcementService = enforcementService;
  }

  public synchronized LegacyNotificationResponse processNotification(String type, String target, String payload)
      throws Exception {

    String safeType = type != null ? type.trim().toUpperCase() : "";
    String safeTarget = target != null ? target.trim() : null;
    String safePayload = payload != null ? payload : "";

    enforcementService.validateTarget(safeTarget);

    long id = repository.openDispatch(safeType, safeTarget, safePayload);
    enforcementService.rejectIfCircuitBreakerIsOpen(id);

    try {
      dispatcher.dispatch(safeType, safeTarget, safePayload);
      repository.recordSuccess(id);
      return repository.getDispatchById(id);
    } catch (NotificationDispatcher.UnsupportedNotificationTypeException e) {
      repository.recordUnsupportedType(id, safeType);
      throw new Exception("Unsupported type: " + type, e);
    } catch (Exception e) {
      repository.recordFailure(id, safeTarget, e.getMessage());
      throw e;
    }
  }

  public List<LegacyNotificationResponse> getHistory() {
    return repository.getHistory();
  }

  public LegacyNotificationResponse getLatestState() {
    return repository.getLatestState();
  }
}
