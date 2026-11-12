package com.example.homework.notification;

class EmailNotificationHandler implements NotificationHandler {
  @Override
  public void send(String target, String payload) {
    if (target != null && target.contains("error")) {
      throw new RuntimeException("SMTP Timeout");
    }
    System.out.println("[EMAIL] " + target + " -> " + payload);
  }
}
