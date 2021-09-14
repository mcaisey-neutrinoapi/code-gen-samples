package com.neutrinoapi.app;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
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
        Duration readTimeout = Duration.ofSeconds(300L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<JsonObject> browserBot(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/browser-bot").newBuilder();

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
            String jsonStr = response.body().string();
            if (response.isSuccessful()) {
                result = Optional.of(JsonParser.parseString(jsonStr).getAsJsonObject());
            } else {
                System.err.printf("Response: %d %s", response.code(), jsonStr);
            }
        } catch (JsonParseException | IOException e) {
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

        // Request data, see: https://www.neutrinoapi.com/api/browser-bot
        HashMap<String, String> params = new HashMap<>();
        params.put("delay", "3"); // Delay in seconds to wait before capturing any page data
        params.put("ignore-certificate-errors", "false"); // Ignore any TLS/SSL certificate errors
                                                          // and load the page anyway
        params.put("selector", ".header-link"); // Extract content from the page DOM using this
                                                // selector
        params.put("exec[1]", "\"Hello\".toUpperCase()");
        params.put("url", "https://www.neutrinoapi.com/"); // The URL to load
        params.put("timeout", "30"); // Timeout in seconds
        params.put("exec[0]", "document.getElementsByTagName('title')[0].innerText");
                                                   // Execute JavaScript on the page
        
        Optional<JsonObject> browserBot = neutrinoAPI.browserBot(params);
        if (browserBot.isPresent()) {
            System.out.printf("Success: %s\n", browserBot.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }
    }
}
