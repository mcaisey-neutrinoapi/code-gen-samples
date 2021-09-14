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
    private static final String API_KEY = "1234";

    /**
     * Watermark one image with another image
     *
     * @param params The API request parameters
     * @return Optional<String>
     */
    private Optional<String> imageWatermark(Map<String, String> params) {
        Optional<String> result = Optional.empty();
        int readTimeout = (int) TimeUnit.SECONDS.toMillis(20);
        int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://neutrinoapi.net/image-watermark");
            params.entrySet().forEach(entity -> {
                uriBuilder.setParameter(entity.getKey(), entity.getValue());
            });

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setConfig(config);
            httpPost.setHeader("User-ID", USER_ID);
            httpPost.setHeader("API-Key", API_KEY);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                File targetFile = File.createTempFile("image-resize-", ".png");
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
