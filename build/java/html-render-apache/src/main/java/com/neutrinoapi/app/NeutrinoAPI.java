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
     * Render HTML content to PDF, JPG or PNG
     *
     * @param params The API request parameters
     * @return Optional<String>
     */
    private Optional<String> htmlRender(Map<String, String> params) {
        Optional<String> result = Optional.empty();
        int readTimeout = (int) TimeUnit.SECONDS.toMillis(300);
        int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://neutrinoapi.net/html-render");
            params.entrySet().forEach(entity -> {
                uriBuilder.setParameter(entity.getKey(), entity.getValue());
            });

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setConfig(config);
            httpPost.setHeader("User-ID", USER_ID);
            httpPost.setHeader("API-Key", API_KEY);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                File targetFile = File.createTempFile("image-resize-", ".pdf");
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
