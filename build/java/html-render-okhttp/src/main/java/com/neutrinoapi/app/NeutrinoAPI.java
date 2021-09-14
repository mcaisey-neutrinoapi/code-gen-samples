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
        Duration readTimeout = Duration.ofSeconds(300L);

        Builder httpBuilder = new Builder();
        httpBuilder.connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .connectionPool(new ConnectionPool(MAX_THREADS, 5L, TimeUnit.MINUTES))
                ;

        client = httpBuilder.build();
    }

    /**
     * Render HTML content to PDF, JPG or PNG
     *
     * @param params The API request parameters
     * @return boolean
     */
    public Optional<String> htmlRender(Map<String, String> params) {
        Optional<String> result = Optional.empty();

        HttpUrl.Builder urlBuilder = 
                HttpUrl.parse("https://neutrinoapi.net/html-render").newBuilder();

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
            File targetFile = File.createTempFile("image-resize-", ".pdf");
            
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

        // Request data, see: https://www.neutrinoapi.com/api/html-render
        HashMap<String, String> params = new HashMap<>();
        params.put("margin", "0"); // The document margin (in mm)
        params.put("image-width", "1024"); // If rendering to an image format (PNG or JPG) use this
                                           // image width (in pixels)
        params.put("format", "PDF"); // Which format to output
        params.put("zoom", "1"); // Set the zoom factor when rendering the page (2.0 for double size
        params.put("content", "[Ф][Ö][©][ش𐹧][的] Some HTML with UTF chars..."); // The HTML content
        params.put("timeout", "300"); // Timeout in seconds
        params.put("margin-right", "0"); // The document right margin (in mm)
        params.put("grayscale", "false"); // Render the final document in grayscale
        params.put("margin-left", "0"); // The document left margin (in mm)
        params.put("page-size", "A4"); // Set the document page size
        params.put("delay", "0"); // Number of seconds to wait before rendering the page (can be
                                  // useful for pages with animations etc)
        params.put("ignore-certificate-errors", "false"); // Ignore any TLS/SSL certificate errors
        params.put("margin-top", "0"); // The document top margin (in mm)
        params.put("margin-bottom", "0"); // The document bottom margin (in mm)
        params.put("landscape", "false"); // Set the document to landscape orientation
        
        Optional<String> htmlRender = neutrinoAPI.htmlRender(params);
        if (htmlRender.isPresent()) {
            System.out.printf("Success: %s\n", htmlRender.get());
        } else {
            System.err.println("\nAPI request failed!"); // you should handle this gracefully!
        }
    }
}
