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
    private static final String API_KEY = "1234";
    private static final int MAX_THREADS = 5;
    private final OkHttpClient client;

    /**
     * Constructor
     */
    public NeutrinoAPI() {
        Duration connectTimeout = Duration.ofSeconds(5L);
        Duration readTimeout = Duration.ofSeconds(20L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Watermark one image with another image
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<String> imageWatermark(Map<String, String> params) {
        Optional<String> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/image-watermark").newBuilder();

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
            File targetFile = File.createTempFile("image-resize-", ".png");
            
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

        // Request data, see: https://www.neutrinoapi.com/api/image-watermark
        HashMap<String, String> params = new HashMap<>();
        params.put("format", "png"); // The output image format
        params.put("image-url", "https://www.neutrinoapi.com/img/LOGO.png"); // The URL or Base64
                                                                             // encoded Data URL for
                                                                             // the source image
                                                                             // (you can also upload
                                                                             // an image file
                                                                             // directly in which
                                                                             // case this field is
                                                                             // ignored)
        params.put("position", "center"); // The position of the watermark image
        params.put("watermark-url", "https://www.neutrinoapi.com/img/icons/security.png");
              // The URL or Base64 encoded Data URL for the watermark image (you can also
              // upload an image file directly in which case this field is ignored)
        params.put("opacity", "50"); // The opacity of the watermark (0 to 100)
        
        Optional<String> imageWatermark = neutrinoAPI.imageWatermark(params);
        if (imageWatermark.isPresent()) {
            System.out.printf("Success: %s\n", imageWatermark.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }
    }
}
