package com.example.homework.notification;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationDispatchJpaRepository extends JpaRepository<NotificationDispatchEntity, Long> {

  long countByRecipientAndCreatedAtGreaterThanEqual(String recipient, Timestamp since);

  Optional<NotificationDispatchEntity> findFirstByOrderByIdDesc();

  List<NotificationDispatchEntity> findTop20ByOrderByIdDesc();
}
