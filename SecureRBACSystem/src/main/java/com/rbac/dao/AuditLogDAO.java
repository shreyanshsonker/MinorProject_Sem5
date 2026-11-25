package com.rbac.dao;

import com.rbac.model.AuditLog;
import com.rbac.util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO — Data Access Object for the 'audit_logs' table.
 *
 * Feature 2: Audit Logging.
 * All queries use PreparedStatement exclusively (AI Rules §A3).
 */
public class AuditLogDAO {

    /**
     * Inserts a new audit log entry.
     *
     * @param log the AuditLog object to insert
     * @return true if insertion succeeded
     */
    public boolean insertLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, action_type, target_entity, target_id, ip_address, details) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (log.getUserId() != null) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getActionType());
            stmt.setString(3, log.getTargetEntity());
            if (log.getTargetId() != null) {
                stmt.setInt(4, log.getTargetId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, log.getIpAddress());
            stmt.setString(6, log.getDetails());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves audit logs with pagination (most recent first).
     * Includes a LEFT JOIN on users for the username column.
     *
     * @param limit  max number of records to return
     * @param offset the offset for pagination
     * @return list of AuditLog objects
     */
    public List<AuditLog> getAllLogs(int limit, int offset) {
        String sql = "SELECT a.log_id, a.user_id, a.action_type, a.target_entity, "
                + "a.target_id, a.ip_address, a.details, a.created_at, u.username "
                + "FROM audit_logs a "
                + "LEFT JOIN users u ON a.user_id = u.user_id "
                + "ORDER BY a.created_at DESC "
                + "LIMIT ? OFFSET ?";

        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = mapResultSetToAuditLog(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Retrieves audit logs for a specific user.
     *
     * @param userId the user's ID
     * @return list of AuditLog objects for that user
     */
    public List<AuditLog> getLogsByUser(int userId) {
        String sql = "SELECT a.log_id, a.user_id, a.action_type, a.target_entity, "
                + "a.target_id, a.ip_address, a.details, a.created_at, u.username "
                + "FROM audit_logs a "
                + "LEFT JOIN users u ON a.user_id = u.user_id "
                + "WHERE a.user_id = ? "
                + "ORDER BY a.created_at DESC";

        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = mapResultSetToAuditLog(rs);
                    log.setUsername(rs.getString("username"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Gets the total count of audit log entries (for pagination).
     *
     * @return total number of log entries
     */
    public int getTotalLogCount() {
        String sql = "SELECT COUNT(*) FROM audit_logs";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Maps a ResultSet row to an AuditLog object.
     */
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getInt("log_id"));

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }

        log.setActionType(rs.getString("action_type"));
        log.setTargetEntity(rs.getString("target_entity"));

        int targetId = rs.getInt("target_id");
        if (!rs.wasNull()) {
            log.setTargetId(targetId);
        }

        log.setIpAddress(rs.getString("ip_address"));
        log.setDetails(rs.getString("details"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}
