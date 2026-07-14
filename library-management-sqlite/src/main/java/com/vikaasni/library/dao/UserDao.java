package com.vikaasni.library.dao;

import com.vikaasni.library.config.DatabaseManager;
import com.vikaasni.library.model.User;
import com.vikaasni.library.util.PasswordUtil;

import java.sql.*;
import java.util.Optional;

public class UserDao {
    public User register(String username, String password, String fullName, String role, String favoriteGenre) {
        String sql = "INSERT INTO users(username,password_hash,password_salt,full_name,role,favorite_genre) VALUES(?,?,?,?,?,?)";
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username.trim());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, fullName.trim());
            ps.setString(5, role);
            ps.setString(6, favoriteGenre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return new User(rs.getLong(1), username, fullName, role, favoriteGenre);
            }
            throw new SQLException("User ID was not returned");
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not register user. Username may already exist.", e);
        }
    }

    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT id,username,password_hash,password_salt,full_name,role,favorite_genre FROM users WHERE username=?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verifyPassword(password, rs.getString("password_salt"), rs.getString("password_hash"))) {
                    return Optional.of(new User(rs.getLong("id"), rs.getString("username"), rs.getString("full_name"),
                            rs.getString("role"), rs.getString("favorite_genre")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("Authentication failed", e);
        }
    }

    public boolean hasAnyUsers() {
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT EXISTS(SELECT 1 FROM users)");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
