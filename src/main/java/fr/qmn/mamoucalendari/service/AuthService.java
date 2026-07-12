package fr.qmn.mamoucalendari.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.qmn.mamoucalendari.config.AuthException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    private final String       baseUrl;
    private final String       username;
    private final String       password;
    private final HttpClient   http;
    private final ObjectMapper mapper;

    public record TokenPair(String accessToken, String refreshToken) {}

    public AuthService(String baseUrl, String username, String password) {
        this.baseUrl  = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = username;
        this.password = password;
        this.http     = HttpClient.newHttpClient();
        this.mapper   = new ObjectMapper();
    }

    public TokenPair login() {
        TokenResponse r = post("/api/login_check",
            Map.of("email", username, "password", password));
        return new TokenPair(r.token, r.refresh_token);
    }

    public TokenPair refresh(String refreshToken) {
        TokenResponse r = post("/api/token/refresh",
            Map.of("refresh_token", refreshToken));
        return new TokenPair(r.token, r.refresh_token);
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
