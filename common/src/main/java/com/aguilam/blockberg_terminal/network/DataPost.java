package com.aguilam.blockberg_terminal.network;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.aguilam.blockberg_terminal.config.ConfigManager;

public class DataPost {
    public static CompletableFuture<HttpResponse<String>> postBarrelData(JsonArray barrelDataList) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ConfigManager.apiUrl + "/barrels"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + ConfigManager.apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(barrelDataList.toString()))
            .build();
    
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> sendBarrelItemsToServer(JsonObject json) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ConfigManager.apiUrl + "/barrels/items"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
            .build();
                
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
    
    public static CompletableFuture<HttpResponse<String>> getBarrelInfo(int barrelid) {
        HttpClient httpClient = HttpClient.newHttpClient();
        String url = ConfigManager.apiUrl + "/barrels/" + barrelid;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<String>> searchBarrels(String searchTerm, int page) {
        HttpClient httpClient = HttpClient.newHttpClient();
        String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String requestUrl = ConfigManager.apiUrl + "/barrels" + "?query=" + encodedSearchTerm + "&page=" + page + "&page_size=10";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .GET()
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}