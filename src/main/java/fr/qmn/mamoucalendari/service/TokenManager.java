package fr.qmn.mamoucalendari.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.qmn.mamoucalendari.bdd.DBConfig;
import fr.qmn.mamoucalendari.config.AuthException;
import fr.qmn.mamoucalendari.service.AuthService.TokenPair;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class TokenManager {

    private final AuthService authService;

    private volatile String  accessToken;
    private volatile String  refreshToken;
    private volatile Instant expiresAt;

    public TokenManager(AuthService authService) {
        this.authService = authService;
        loadFromDb();
    }

    public void ensureAuthenticated() {
        try {
            getAccessToken();
        } catch (AuthException e) {
            System.out.println("[TokenManager] Auth warning: " + e.getMessage());
        }
    }

    public synchronized String getAccessToken() {
        if (accessToken == null) {
            loadFromDb();
        }
        if (accessToken == null) {
            storeTokens(authService.login());
            return accessToken;
        }
        if (isExpired()) {
            if (refreshToken != null) {
                try {
                    storeTokens(authService.refresh(refreshToken));
                } catch (AuthException e) {
                    System.out.println("[TokenManager] Refresh failed, falling back to login: " + e.getMessage());
                    storeTokens(authService.login());
                }
            } else {
                storeTokens(authService.login());
            }
        }
        return accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    private boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt.minus(30, ChronoUnit.SECONDS));
    }

    public boolean hasValidToken() {
        return accessToken != null && !isExpired();
    }

    public synchronized void storeTokens(TokenPair pair) {
        this.accessToken  = pair.accessToken();
        this.refreshToken = pair.refreshToken();
        this.expiresAt    = parseExpiry(pair.accessToken());
        saveToDb(pair.accessToken(), pair.refreshToken(), expiresAt);
        System.out.println("[TokenManager] Tokens stored, expires at " + expiresAt);
    }

    private void loadFromDb() {
        String sql = "SELECT ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT FROM AUTH_TOKENS WHERE ID = 1";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            if (rs.next()) {
                accessToken  = rs.getString("ACCESS_TOKEN");
                refreshToken = rs.getString("REFRESH_TOKEN");
                String exp   = rs.getString("EXPIRES_AT");
                expiresAt    = exp != null ? Instant.parse(exp) : null;
            }
        } catch (Exception e) {
            System.out.println("[TokenManager] loadFromDb error: " + e.getMessage());
        }
    }

    private void saveToDb(String access, String refresh, Instant expires) {
        String sql = "INSERT OR REPLACE INTO AUTH_TOKENS (ID, ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT)" +
                     " VALUES (1, ?, ?, ?)";
        try (Connection conn  = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, access);
            pstmt.setString(2, refresh);
            pstmt.setString(3, expires != null ? expires.toString() : null);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("[TokenManager] saveToDb error: " + e.getMessage());
        }
    }

    private static Instant parseExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("Not a JWT");
            String payload = parts[1];
            int mod4 = payload.length() % 4;
            if (mod4 > 0) payload += "=".repeat(4 - mod4);
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            long exp = new ObjectMapper().readTree(decoded).get("exp").asLong();
            return Instant.ofEpochSecond(exp);
        } catch (Exception e) {
            return Instant.now().plus(1, ChronoUnit.HOURS);
        }
    }
}
