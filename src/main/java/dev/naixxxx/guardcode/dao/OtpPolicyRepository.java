package dev.naixxxx.guardcode.dao;

import dev.naixxxx.guardcode.domain.OtpPolicy;

import javax.sql.DataSource;
import java.sql.*;

public class OtpPolicyRepository {
    private final DataSource ds;
    public OtpPolicyRepository(DataSource ds) { this.ds = ds; }

    public OtpPolicy get() {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT code_length, lifetime_seconds FROM otp_settings WHERE id=1"); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new DaoException("OTP settings row not found", null);
            return new OtpPolicy(rs.getInt("code_length"), rs.getInt("lifetime_seconds"));
        } catch (SQLException e) { throw new DaoException("Cannot read OTP policy", e); }
    }

    public OtpPolicy update(int codeLength, int lifetimeSeconds) {
        String sql = "UPDATE otp_settings SET code_length=?, lifetime_seconds=?, updated_at=CURRENT_TIMESTAMP WHERE id=1 RETURNING code_length, lifetime_seconds";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, codeLength); ps.setInt(2, lifetimeSeconds);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("OTP settings row not found", null);
                return new OtpPolicy(rs.getInt("code_length"), rs.getInt("lifetime_seconds"));
            }
        } catch (SQLException e) { throw new DaoException("Cannot update OTP policy", e); }
    }
}
