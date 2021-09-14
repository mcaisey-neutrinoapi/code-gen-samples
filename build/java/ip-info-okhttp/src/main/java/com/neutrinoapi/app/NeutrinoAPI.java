package com.neutrinoapi.app;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
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
        Duration readTimeout = Duration.ofSeconds(10L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Get location information about an IP address and do reverse DNS (PTR) lookups
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<JsonObject> ipInfo(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/ip-info").newBuilder();

        params.entrySet().forEach(entry -> {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        });

        Request request = new Request.Builder()
                .header("User-ID", USER_ID)
                .header("API-Key", API_KEY)
                .url(urlBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonStr = response.body().string();
            if (response.isSuccessful()) {
                result = Optional.of(JsonParser.parseString(jsonStr).getAsJsonObject());
            } else {
                System.err.printf("Response: %d %s", response.code(), jsonStr);
            }
        } catch (JsonSyntaxException | IOException e) {
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

        // Request data, see: https://www.neutrinoapi.com/api/ip-info
        HashMap<String, String> params = new HashMap<>();
        params.put("request-delay", "11000");
        params.put("ip", "1.1.1.1"); // IPv4 or IPv6 address
        params.put("reverse-lookup", "false"); // Do a reverse DNS (PTR) lookup
        
        Optional<JsonObject> ipInfo = neutrinoAPI.ipInfo(params);
        if (ipInfo.isPresent()) {
            System.out.printf("Success: %s\n", ipInfo.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }
    }
}
