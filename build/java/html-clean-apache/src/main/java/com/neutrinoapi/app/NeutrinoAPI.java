package com.neutrinoapi.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class NeutrinoAPI {

    private static final String USER_ID = "my-user-id";
    private static final String API_KEY = "my-api-key";

    /**
     * Clean and sanitize untrusted HTML
     *
     * @param params The API request parameters
     * @return Optional<String>
     */
    private Optional<String> htmlClean(Map<String, String> params) {
        Optional<String> result = Optional.empty();
        int readTimeout = (int) TimeUnit.SECONDS.toMillis(30);
        int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://neutrinoapi.net/html-clean");
            params.entrySet().forEach(entity -> {
                uriBuilder.setParameter(entity.getKey(), entity.getValue());
            });

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setConfig(config);
            httpPost.setHeader("User-ID", USER_ID);
            httpPost.setHeader("API-Key", API_KEY);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                File targetFile = File.createTempFile("image-resize-", ".txt");
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try (FileOutputStream outstream = new FileOutputStream(targetFile)) {
                            entity.writeTo(outstream);
                        }
                    }
                    result = Optional.of(targetFile.getAbsolutePath());
                } else {
                    System.err.printf("Response: %d %s",
                            response.getStatusLine().getStatusCode(),
                            EntityUtils.toString(response.getEntity()));
                }
            } catch (ClientProtocolException e) {
                System.err.println(e);
            }
        } catch (URISyntaxException | IOException e) {
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
