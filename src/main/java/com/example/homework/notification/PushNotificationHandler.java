package com.example.homework.notification;

import org.springframework.stereotype.Component;

@Component
class PushNotificationHandler implements NotificationHandler {
  @Override
  public String type() {
    return "PUSH";
  }

  @Override
  public void send(String target, String payload) {
    System.out.println("[PUSH] " + target + " -> " + payload);
  }
}
