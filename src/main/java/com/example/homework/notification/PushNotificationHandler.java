package com.example.homework.notification;

class PushNotificationHandler implements NotificationHandler {
  @Override
  public void send(String target, String payload) {
    System.out.println("[PUSH] " + target + " -> " + payload);
  }
}
