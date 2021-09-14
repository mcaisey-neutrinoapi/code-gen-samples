package com.neutrinoapi.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
     * Clean and sanitize untrusted HTML
     */
    public Optional<String> htmlClean(Map<String, String> params) {
        Optional<String> result = Optional.empty();
        String url = "https://neutrinoapi.net/html-clean";
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

            HttpResponse<InputStream> response
                    = client.send(request, BodyHandlers.ofInputStream());

            File targetFile = File.createTempFile("image-resize-", ".txt");
            
            Files.copy(
                    response.body(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            
            String filename = targetFile.getAbsolutePath();

            if (response.statusCode() == 200) {
                result = Optional.of(filename);
            } else {
                System.err.printf("Response: %d %s",
                        response.statusCode(),
                        new String(Files.readAllBytes(targetFile.toPath()), "UTF-8")
                );
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println(e);
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

        // Request data, see: https://www.neutrinoapi.com/api/html-clean
        HashMap<String, String> params = new HashMap<>();
        params.put("output-type", "plain-text"); // The level of sanitization
        params.put("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars..."); // The HTML content
        
        Optional<String> htmlClean = neutrinoAPI.htmlClean(params);
        if (htmlClean.isPresent()) {
            System.out.printf("Success: %s\n", htmlClean.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }

        neutrinoAPI.shutdown();
    }
}
