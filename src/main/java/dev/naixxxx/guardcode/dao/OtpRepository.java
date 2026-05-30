package dev.naixxxx.guardcode.dao;

import dev.naixxxx.guardcode.domain.*;
import dev.naixxxx.guardcode.domain.DeliveryChannel;
import dev.naixxxx.guardcode.domain.OtpChallenge;
import dev.naixxxx.guardcode.domain.OtpState;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpRepository {
    private final DataSource ds;
    public OtpRepository(DataSource ds) { this.ds = ds; }

    public OtpChallenge create(long userId, String operationRef, String code, DeliveryChannel channel, String destination, LocalDateTime expiresAt) {
        String sql = """
                INSERT INTO otp_challenges(user_id, operation_ref, code_value, status, delivery_channel, destination, expires_at)
                VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?) RETURNING *
                """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId); ps.setString(2, operationRef); ps.setString(3, code);
            ps.setString(4, channel.name()); ps.setString(5, destination); ps.setTimestamp(6, Timestamp.valueOf(expiresAt));
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return map(rs); }
        } catch (SQLException e) { throw new DaoException("Cannot create OTP", e); }
    }

    public Optional<OtpChallenge> findActive(long userId, String operationRef, String code) {
        String sql = "SELECT * FROM otp_challenges WHERE user_id=? AND operation_ref=? AND code_value=? AND status='ACTIVE' ORDER BY id DESC LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId); ps.setString(2, operationRef); ps.setString(3, code);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new DaoException("Cannot find OTP", e); }
    }

    public void markUsed(long id) { updateStatus(id, OtpState.USED, true); }
    public void markExpired(long id) { updateStatus(id, OtpState.EXPIRED, false); }

    public int expireOverdue() {
        String sql = "UPDATE otp_challenges SET status='EXPIRED' WHERE status='ACTIVE' AND expires_at < CURRENT_TIMESTAMP";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { return ps.executeUpdate(); }
        catch (SQLException e) { throw new DaoException("Cannot expire OTPs", e); }
    }

    private void updateStatus(long id, OtpState status, boolean used) {
        String sql = used ? "UPDATE otp_challenges SET status=?, used_at=CURRENT_TIMESTAMP WHERE id=?" : "UPDATE otp_challenges SET status=? WHERE id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name()); ps.setLong(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DaoException("Cannot update OTP status", e); }
    }

    private OtpChallenge map(ResultSet rs) throws SQLException {
        Timestamp usedAt = rs.getTimestamp("used_at");
        return new OtpChallenge(
                rs.getLong("id"), rs.getLong("user_id"), rs.getString("operation_ref"), rs.getString("code_value"),
                OtpState.valueOf(rs.getString("status")), DeliveryChannel.valueOf(rs.getString("delivery_channel")),
                rs.getString("destination"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("expires_at").toLocalDateTime(),
                usedAt == null ? null : usedAt.toLocalDateTime()
        );
    }
}
