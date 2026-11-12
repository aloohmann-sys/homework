package com.example.homework.notification;

interface NotificationHandler {
  void send(String target, String payload) throws Exception;
}
