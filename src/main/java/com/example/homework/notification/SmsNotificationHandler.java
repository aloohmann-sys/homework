package com.example.homework.notification;

class SmsNotificationHandler implements NotificationHandler {
  @Override
  public void send(String target, String payload) {
    System.out.println("[SMS] " + target + " -> " + payload);
  }
}
