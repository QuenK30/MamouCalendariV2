package fr.qmn.mamoucalendari.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RemoteApiClient {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public RemoteApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey  = apiKey;
        this.http    = HttpClient.newHttpClient();
        this.mapper  = new ObjectMapper();
    }

    public <T> T get(String path, Class<T> type) {
        HttpRequest request = builder(path).GET().build();
        String body = send(request);
        return deserialize(body, type);
    }

    public <T> T get(String path, TypeReference<T> typeRef) {
        HttpRequest request = builder(path).GET().build();
        String body = send(request);
        return deserialize(body, typeRef);
    }

    public void post(String path, Object payload) {
        HttpRequest request = builder(path)
            .POST(jsonBody(payload))
            .build();
        send(request);
    }

    public <T> T postAndGet(String path, Object payload, Class<T> type) {
        HttpRequest request = builder(path)
            .POST(jsonBody(payload))
            .build();
        return deserialize(send(request), type);
    }

    public void delete(String path) {
        HttpRequest request = builder(path).DELETE().build();
        send(request);
    }

    public void patch(String path, Object payload) {
        HttpRequest request = builder(path)
            .method("PATCH", jsonBody(payload))
            .build();
        send(request);
    }

    private HttpRequest.Builder builder(String path) {
        String url = baseUrl + (path.startsWith("/") ? path : "/" + path);
        return HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-API-Key", apiKey)
            .header("Content-Type", "application/ld+json")
            .header("Accept", "application/ld+json");
    }

    private HttpRequest.BodyPublisher jsonBody(Object payload) {
        try {
            return HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload));
        } catch (IOException e) {
            throw new RemoteApiException(0, "Erreur de sérialisation JSON : " + e.getMessage());
        }
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new RemoteApiException(response.statusCode(), response.body());
            }
            return response.body();
        } catch (RemoteApiException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteApiException(0, "Erreur réseau : " + e.getMessage());
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RemoteApiException(0, "Erreur de désérialisation JSON : " + e.getMessage());
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(json, typeRef);
        } catch (IOException e) {
            throw new RemoteApiException(0, "Erreur de désérialisation JSON : " + e.getMessage());
        }
    }
}
