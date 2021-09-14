package com.neutrinoapi.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

/**
 * NeutrinoAPI
 *
 */
public class NeutrinoAPI {

    private static final String USER_ID = "my-user-id";
    private static final String API_KEY = "my-api-key";
    private static final int MAX_THREADS = 5;
    private final OkHttpClient client;

    /**
     * Constructor
     */
    public NeutrinoAPI() {
        Duration connectTimeout = Duration.ofSeconds(5L);
        Duration readTimeout = Duration.ofSeconds(30L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Clean and sanitize untrusted HTML
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<String> htmlClean(Map<String, String> params) {
        Optional<String> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/html-clean").newBuilder();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        params.entrySet().forEach(entry -> {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        });
        
        Request request = new Request.Builder()
                .header("User-ID", USER_ID)
                .header("API-Key", API_KEY)
                .url(urlBuilder.build())
                .post(formBodyBuilder.build())
                .build();   

        try (Response response = client.newCall(request).execute()) {
            File targetFile = File.createTempFile("image-resize-", ".txt");
            
            if (response.isSuccessful()) {
                Files.copy(
                        response.body().byteStream(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                result = Optional.of(targetFile.getAbsolutePath());
            } else {
                System.err.printf("Response: %d %s", response.code(), response.body().string());
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        return result;
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
    }
}
