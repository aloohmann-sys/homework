package com.example.homework.notification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
class NotificationRepository {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<LegacyNotificationResponse> rowMapper = new RowMapper<LegacyNotificationResponse>() {
    @Override
    public LegacyNotificationResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
      LegacyNotificationResponse response = new LegacyNotificationResponse();
      response.setId(rs.getLong("id"));
      response.setType(rs.getString("tp"));
      response.setTarget(rs.getString("tgt"));
      response.setPayload(rs.getString("pld"));
      response.setStatus(rs.getString("st"));
      response.setFailureCount(rs.getInt("fc"));
      response.setDispatchCount(rs.getInt("dc"));
      response.setBlocked("Y".equals(rs.getString("bl")) ? "Y" : "N");
      response.setMessage(rs.getString("lm"));
      response.setCreatedAt(rs.getTimestamp("crt"));
      response.setUpdatedAt(rs.getTimestamp("upd"));
      return response;
    }
  };

  NotificationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  boolean isRecipientBlockedOrRateLimited(String target) {
    if (target != null && target.toLowerCase().contains("blocked")) {
      return true;
    }

    Integer recentCount = jdbcTemplate.queryForObject(
        "select count(*) "
            + "from ntf_tx "
            + "where tgt = ? "
            + "  and crt >= dateadd('minute', -1, current_timestamp)",
        Integer.class,
        target);
    return recentCount != null && recentCount.intValue() >= 3;
  }

  long openDispatch(String type, String target, String payload) {
    long id = getNextId();
    int nextDispatchCount = getTotalDispatches() + 1;
    int currentFailureCount = getFailureCount();
    String blocked = target != null && target.toLowerCase().contains("blocked") ? "Y" : "N";
    Timestamp now = new Timestamp(System.currentTimeMillis());

    jdbcTemplate.update(
        "insert into ntf_tx(id, tp, tgt, pld, st, fc, dc, bl, lm, crt, upd) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        Long.valueOf(id),
        type,
        target,
        payload,
        "N",
        Integer.valueOf(currentFailureCount),
        Integer.valueOf(nextDispatchCount),
        blocked,
        "START",
        now,
        now);
    return id;
  }

  int getFailureCount() {
    List<Integer> results = jdbcTemplate.query(
        "select fc from ntf_tx order by id desc limit 1",
        (resultSet, rowNum) -> Integer.valueOf(resultSet.getInt(1)));
    return results.isEmpty() ? 0 : results.get(0).intValue();
  }

  void recordFailure(long id, String target, String error) {
    int nextFailureCount = getFailureCount() + 1;
    jdbcTemplate.update(
        "update ntf_tx set st = ?, fc = ?, lm = ?, upd = ? where id = ?",
        "E",
        Integer.valueOf(nextFailureCount),
        shorten(error),
        new Timestamp(System.currentTimeMillis()),
        Long.valueOf(id));
    System.out.println("[DB AUDIT] Inserted failure record for " + target + ": " + error);
  }

  void recordSuccess(long id) {
    jdbcTemplate.update(
        "update ntf_tx set st = ?, fc = ?, lm = ?, upd = ? where id = ?",
        "S",
        Integer.valueOf(0),
        "SENT",
        new Timestamp(System.currentTimeMillis()),
        Long.valueOf(id));
  }

  void recordUnsupportedType(long id, String type) {
    jdbcTemplate.update(
        "update ntf_tx set st = ?, lm = ?, upd = ? where id = ?",
        "U",
        shorten("Unsupported type: " + type),
        new Timestamp(System.currentTimeMillis()),
        Long.valueOf(id));
  }

  void recordRejection(long id, String message) {
    jdbcTemplate.update(
        "update ntf_tx set st = ?, lm = ?, upd = ? where id = ?",
        "R",
        shorten(message),
        new Timestamp(System.currentTimeMillis()),
        Long.valueOf(id));
  }

  LegacyNotificationResponse getLatestState() {
    List<LegacyNotificationResponse> items = jdbcTemplate.query(
        "select * from ntf_tx order by id desc limit 1",
        rowMapper);
    return items.isEmpty() ? null : items.get(0);
  }

  LegacyNotificationResponse getDispatchById(long id) {
    return jdbcTemplate.queryForObject(
        "select * from ntf_tx where id = ?",
        rowMapper,
        Long.valueOf(id));
  }

  List<LegacyNotificationResponse> getHistory() {
    return jdbcTemplate.query(
        "select * from ntf_tx order by id desc limit 20",
        rowMapper);
  }

  private long getNextId() {
    Long maxId = jdbcTemplate.queryForObject("select coalesce(max(id), 0) from ntf_tx", Long.class);
    return (maxId == null ? 0L : maxId.longValue()) + 1L;
  }

  private int getTotalDispatches() {
    Integer latestDispatchCount = jdbcTemplate.queryForObject(
        "select coalesce(max(dc), 0) from ntf_tx",
        Integer.class);
    return latestDispatchCount == null ? 0 : latestDispatchCount.intValue();
  }

  private String shorten(String value) {
    if (value == null || value.length() <= 255) {
      return value;
    }
    return value.substring(0, 255);
  }
}
