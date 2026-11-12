package com.example.homework.notification;

interface NotificationHandler {
  String type();

  void send(String target, String payload) throws Exception;
}
