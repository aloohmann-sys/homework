package com.example.homework.notification;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
class NotificationDispatcher {

  private final Map<String, NotificationHandler> handlers;

  NotificationDispatcher(List<NotificationHandler> handlers) {
    this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(
        NotificationHandler::type,
        Function.identity()));
  }

  void dispatch(String type, String target, String payload) throws Exception {
    NotificationHandler handler = handlers.get(type);
    if (handler == null) {
      throw new UnsupportedNotificationTypeException(type);
    }
    handler.send(target, payload);
  }

  static class UnsupportedNotificationTypeException extends Exception {
    UnsupportedNotificationTypeException(String type) {
      super("Unsupported type: " + type);
    }
  }
}
