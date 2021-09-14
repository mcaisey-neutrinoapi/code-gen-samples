package com.neutrinoapi.app;

import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class NeutrinoAPI {

    /**
     * API credentials
     */
    private static final String USER_ID = "my-user-id";
    private static final String API_KEY = "my-api-key";
    private static final int MAX_THREADS = 5;
    private final ExecutorService executorService;
    private final HttpClient client;

    private final Function<Map<String, String>, String> urlEncodeParams = params
            -> params.entrySet().stream()
                    .map(p -> URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8)
                      + "=" + URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8))
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");

    /**
     * Constructor
     *
     */
    public NeutrinoAPI() {
        executorService = Executors.newFixedThreadPool(MAX_THREADS);

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .executor(executorService)
                .build();
    }

    /**
     * Detect bad words, swear words and profanity in a given text
     */
    public Optional<JsonObject> badWordFilter(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();
        String url = "https://neutrinoapi.net/bad-word-filter";
        long readTimeout = 30L;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(readTimeout))
                    .header("User-ID", USER_ID)
                    .header("API-Key", API_KEY)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(urlEncodeParams.apply(params)))
                    .build();

            HttpResponse<String> response
                    = client.send(request, BodyHandlers.ofString());

            String jsonStr = response.body();
            if (response.statusCode() == 200) {
                result = Optional.of(JsonParser.parseString(jsonStr).getAsJsonObject());
            } else {
                System.err.printf("Response: %d %s", response.statusCode(), jsonStr);
            }
        } catch (InterruptedException | IOException e) {
            System.err.println(e);
        }
        return result;
    }

    /**
     * Avoid IllegalStateThreadException following blocking timeout
     *
     */
    public void shutdown() {
        executorService.shutdownNow();
    }

    /**
     * Main
     *
     * @param args
     */
    public static void main(String[] args) {
        NeutrinoAPI neutrinoAPI = new NeutrinoAPI();

        // Request data, see: https://www.neutrinoapi.com/api/bad-word-filter
        HashMap<String, String> params = new HashMap<>();
        params.put("censor-character", "λ"); // The character to use to censor out the bad words
                                             // found
        params.put("catalog", "strict"); // Which catalog of bad words to use
        params.put("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars..."); // The content to
                                                                                // scan
        
        Optional<JsonObject> badWordFilter = neutrinoAPI.badWordFilter(params);
        if (badWordFilter.isPresent()) {
            System.out.printf("Success: %s\n", badWordFilter.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }

        neutrinoAPI.shutdown();
    }
}
