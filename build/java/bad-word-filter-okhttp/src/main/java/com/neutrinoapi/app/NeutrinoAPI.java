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
        Duration readTimeout = Duration.ofSeconds(30L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Detect bad words, swear words and profanity in a given text
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<JsonObject> badWordFilter(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/bad-word-filter").newBuilder();

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
    }
}
