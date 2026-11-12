package com.example.homework.notification;

import org.springframework.stereotype.Component;

@Component
class SmsNotificationHandler implements NotificationHandler {
  @Override
  public String type() {
    return "SMS";
  }

  @Override
  public void send(String target, String payload) {
    System.out.println("[SMS] " + target + " -> " + payload);
  }
}
