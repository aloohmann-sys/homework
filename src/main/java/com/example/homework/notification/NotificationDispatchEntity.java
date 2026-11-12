package com.example.homework.notification;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_dispatches")
class NotificationDispatchEntity {

  @Id
  private Long id;

  @Column(name = "notification_type", nullable = false, length = 32)
  private String type;

  @Column(name = "recipient", nullable = false, length = 255)
  private String recipient;

  @Lob
  @Column(name = "payload")
  private String payload;

  @Column(name = "status", nullable = false, length = 1)
  private String status;

  @Column(name = "failure_count", nullable = false)
  private int failureCount;

  @Column(name = "dispatch_count", nullable = false)
  private int dispatchCount;

  @Column(name = "is_blocked", nullable = false, length = 1)
  private String blocked;

  @Column(name = "message", length = 255)
  private String message;

  @Column(name = "created_at", nullable = false)
  private Timestamp createdAt;

  @Column(name = "updated_at", nullable = false)
  private Timestamp updatedAt;

  protected NotificationDispatchEntity() {
  }

  NotificationDispatchEntity(
      Long id,
      String type,
      String recipient,
      String payload,
      String status,
      int failureCount,
      int dispatchCount,
      String blocked,
      String message,
      Timestamp createdAt,
      Timestamp updatedAt) {
    this.id = id;
    this.type = type;
    this.recipient = recipient;
    this.payload = payload;
    this.status = status;
    this.failureCount = failureCount;
    this.dispatchCount = dispatchCount;
    this.blocked = blocked;
    this.message = message;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  Long getId() {
    return id;
  }

  String getType() {
    return type;
  }

  String getRecipient() {
    return recipient;
  }

  String getPayload() {
    return payload;
  }

  String getStatus() {
    return status;
  }

  int getFailureCount() {
    return failureCount;
  }

  int getDispatchCount() {
    return dispatchCount;
  }

  String getBlocked() {
    return blocked;
  }

  String getMessage() {
    return message;
  }

  Timestamp getCreatedAt() {
    return createdAt;
  }

  Timestamp getUpdatedAt() {
    return updatedAt;
  }

  void setStatus(String status) {
    this.status = status;
  }

  void setFailureCount(int failureCount) {
    this.failureCount = failureCount;
  }

  void setMessage(String message) {
    this.message = message;
  }

  void setUpdatedAt(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }
}
