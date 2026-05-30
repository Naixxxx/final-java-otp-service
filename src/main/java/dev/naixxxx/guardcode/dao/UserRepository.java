package dev.naixxxx.guardcode.dao;

import dev.naixxxx.guardcode.domain.AppUser;
import dev.naixxxx.guardcode.domain.UserRole;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final DataSource ds;
    public UserRepository(DataSource ds) { this.ds = ds; }

    public AppUser create(String login, String passwordHash, UserRole role) {
        String sql = "INSERT INTO app_users(login, password_hash, role) VALUES (?, ?, ?) RETURNING id, login, password_hash, role, created_at";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login); ps.setString(2, passwordHash); ps.setString(3, role.name());
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return map(rs); }
        } catch (SQLException e) { throw new DaoException("Cannot create user", e); }
    }

    public Optional<AppUser> findByLogin(String login) {
        String sql = "SELECT id, login, password_hash, role, created_at FROM app_users WHERE login = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new DaoException("Cannot find user", e); }
    }

    public Optional<AppUser> findById(long id) {
        String sql = "SELECT id, login, password_hash, role, created_at FROM app_users WHERE id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new DaoException("Cannot find user", e); }
    }

    public boolean existsAdmin() {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT 1 FROM app_users WHERE role='ADMIN' LIMIT 1"); ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException e) { throw new DaoException("Cannot check admin", e); }
    }

    public List<AppUser> listNonAdmins() {
        String sql = "SELECT id, login, password_hash, role, created_at FROM app_users WHERE role <> 'ADMIN' ORDER BY id";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<AppUser> users = new ArrayList<>();
            while (rs.next()) users.add(map(rs));
            return users;
        } catch (SQLException e) { throw new DaoException("Cannot list users", e); }
    }

    public boolean deleteRegularUser(long id) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM app_users WHERE id = ? AND role <> 'ADMIN'")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DaoException("Cannot delete user", e); }
    }

    private AppUser map(ResultSet rs) throws SQLException {
        return new AppUser(
                rs.getLong("id"), rs.getString("login"), rs.getString("password_hash"),
                UserRole.valueOf(rs.getString("role")), rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
