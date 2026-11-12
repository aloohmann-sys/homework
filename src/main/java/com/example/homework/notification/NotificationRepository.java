package com.example.homework.notification;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
class NotificationRepository {

  private final NotificationDispatchJpaRepository dispatches;

  NotificationRepository(NotificationDispatchJpaRepository dispatches) {
    this.dispatches = dispatches;
  }

  boolean isRecipientBlockedOrRateLimited(String recipient) {
    if (recipient != null && recipient.toLowerCase().contains("blocked")) {
      return true;
    }

    Timestamp since = Timestamp.from(Instant.now().minus(1, ChronoUnit.MINUTES));
    return dispatches.countByRecipientAndCreatedAtGreaterThanEqual(recipient, since) >= 3;
  }

  long openDispatch(String type, String recipient, String payload) {
    NotificationDispatchEntity latest = latestEntity();
    long id = latest == null ? 1L : latest.getId().longValue() + 1L;
    int dispatchCount = latest == null ? 1 : latest.getDispatchCount() + 1;
    int failureCount = latest == null ? 0 : latest.getFailureCount();
    String blocked = recipient != null && recipient.toLowerCase().contains("blocked") ? "Y" : "N";
    Timestamp now = new Timestamp(System.currentTimeMillis());

    NotificationDispatchEntity dispatch = new NotificationDispatchEntity(
        Long.valueOf(id),
        type,
        recipient,
        payload,
        "N",
        failureCount,
        dispatchCount,
        blocked,
        "START",
        now,
        now);
    dispatches.saveAndFlush(dispatch);
    return id;
  }

  int getFailureCount() {
    NotificationDispatchEntity latest = latestEntity();
    return latest == null ? 0 : latest.getFailureCount();
  }

  void recordFailure(long id, String recipient, String error) {
    NotificationDispatchEntity dispatch = dispatch(id);
    NotificationDispatchEntity latest = latestEntity();
    int nextFailureCount = (latest == null ? 0 : latest.getFailureCount()) + 1;
    dispatch.setStatus("E");
    dispatch.setFailureCount(nextFailureCount);
    dispatch.setMessage(shorten(error));
    dispatch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dispatches.saveAndFlush(dispatch);
    System.out.println("[DB AUDIT] Inserted failure record for " + recipient + ": " + error);
  }

  void recordSuccess(long id) {
    NotificationDispatchEntity dispatch = dispatch(id);
    dispatch.setStatus("S");
    dispatch.setFailureCount(0);
    dispatch.setMessage("SENT");
    dispatch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dispatches.saveAndFlush(dispatch);
  }

  void recordUnsupportedType(long id, String type) {
    NotificationDispatchEntity dispatch = dispatch(id);
    dispatch.setStatus("U");
    dispatch.setMessage(shorten("Unsupported type: " + type));
    dispatch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dispatches.saveAndFlush(dispatch);
  }

  void recordRejection(long id, String message) {
    NotificationDispatchEntity dispatch = dispatch(id);
    dispatch.setStatus("R");
    dispatch.setMessage(shorten(message));
    dispatch.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    dispatches.saveAndFlush(dispatch);
  }

  LegacyNotificationResponse getLatestState() {
    NotificationDispatchEntity latest = latestEntity();
    return latest == null ? null : toResponse(latest);
  }

  LegacyNotificationResponse getDispatchById(long id) {
    return toResponse(dispatch(id));
  }

  List<LegacyNotificationResponse> getHistory() {
    return dispatches.findTop20ByOrderByIdDesc().stream()
        .map(this::toResponse)
        .toList();
  }

  private NotificationDispatchEntity dispatch(long id) {
    return dispatches.findById(Long.valueOf(id))
        .orElseThrow(() -> new IllegalArgumentException("Notification dispatch not found: " + id));
  }

  private NotificationDispatchEntity latestEntity() {
    return dispatches.findFirstByOrderByIdDesc().orElse(null);
  }

  private LegacyNotificationResponse toResponse(NotificationDispatchEntity dispatch) {
    LegacyNotificationResponse response = new LegacyNotificationResponse();
    response.setId(dispatch.getId().longValue());
    response.setType(dispatch.getType());
    response.setTarget(dispatch.getRecipient());
    response.setPayload(dispatch.getPayload());
    response.setStatus(dispatch.getStatus());
    response.setFailureCount(dispatch.getFailureCount());
    response.setDispatchCount(dispatch.getDispatchCount());
    response.setBlocked(dispatch.getBlocked());
    response.setMessage(dispatch.getMessage());
    response.setCreatedAt(dispatch.getCreatedAt());
    response.setUpdatedAt(dispatch.getUpdatedAt());
    return response;
  }

  private String shorten(String value) {
    if (value == null || value.length() <= 255) {
      return value;
    }
    return value.substring(0, 255);
  }
}
