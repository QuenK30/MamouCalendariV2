package fr.qmn.mamoucalendari.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.qmn.mamoucalendari.bdd.DBConfig;
import fr.qmn.mamoucalendari.config.AuthException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

public class AuthService {

    private final String     baseUrl;
    private final String     username;
    private final String     password;
    private final HttpClient http;
    private final ObjectMapper mapper;

    private volatile String  accessToken;
    private volatile String  refreshToken;
    private volatile Instant expiresAt;

    public AuthService(String baseUrl, String username, String password) {
        this.baseUrl  = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = username;
        this.password = password;
        this.http     = HttpClient.newHttpClient();
        this.mapper   = new ObjectMapper();
        loadFromDb();
    }

    public synchronized void login() {
        TokenResponse response = post("/api/login_check",
            Map.of("email", username, "password", password));
        storeTokens(response.token, response.refresh_token);
    }

    public synchronized void refresh() {
        if (refreshToken == null) {
            throw new AuthException("No refresh token; call login() first");
        }
        TokenResponse response = post("/api/token/refresh",
            Map.of("refresh_token", refreshToken));
        storeTokens(response.token, response.refresh_token);
    }

    public synchronized String getAccessToken() {
        if (accessToken == null) {
            loadFromDb();
        }
        if (accessToken == null) {
            login();
            return accessToken;
        }
        if (isExpired()) {
            if (refreshToken != null) {
                try {
                    refresh();
                } catch (AuthException e) {
                    System.out.println("[AuthService] Refresh failed, falling back to login: " + e.getMessage());
                    login();
                }
            } else {
                login();
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

    private void storeTokens(String access, String refresh) {
        this.accessToken  = access;
        this.refreshToken = refresh;
        this.expiresAt    = parseExpiry(access);
        saveToDb(access, refresh, expiresAt);
        System.out.println("[AuthService] Tokens stored, expires at " + expiresAt);
    }

    private void loadFromDb() {
        String sql = "SELECT ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT FROM AUTH_TOKENS WHERE ID = 1";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                accessToken  = rs.getString("ACCESS_TOKEN");
                refreshToken = rs.getString("REFRESH_TOKEN");
                String exp   = rs.getString("EXPIRES_AT");
                expiresAt    = exp != null ? Instant.parse(exp) : null;
            }
        } catch (Exception e) {
            System.out.println("[AuthService] loadFromDb error: " + e.getMessage());
        }
    }

    private void saveToDb(String access, String refresh, Instant expires) {
        String sql = "INSERT OR REPLACE INTO AUTH_TOKENS (ID, ACCESS_TOKEN, REFRESH_TOKEN, EXPIRES_AT)" +
                     " VALUES (1, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, access);
            pstmt.setString(2, refresh);
            pstmt.setString(3, expires != null ? expires.toString() : null);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("[AuthService] saveToDb error: " + e.getMessage());
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

    private TokenResponse post(String path, Object body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new AuthException("HTTP " + response.statusCode() + " from " + path + ": " + response.body());
            }
            return mapper.readValue(response.body(), TokenResponse.class);
        } catch (AuthException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthException("Auth request failed: " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenResponse {
        public String token;
        public String refresh_token;
    }
}
