package com.neutrinoapi.app;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
     * Browser bot can extract content, interact with keyboard and mouse events, and execute JavaScript on a website
     *
     * @param params The API request parameters
     * @return Optional<JsonObject>
     */
    private Optional<JsonObject> browserBot(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();
        int readTimeout = (int) TimeUnit.SECONDS.toMillis(300);
        int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://neutrinoapi.net/browser-bot");
            params.entrySet().forEach(entity -> {
                uriBuilder.setParameter(entity.getKey(), entity.getValue());
            });

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setConfig(config);
            httpPost.setHeader("User-ID", USER_ID);
            httpPost.setHeader("API-Key", API_KEY);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String jsonStr = (entity != null) ? EntityUtils.toString(entity) : "";
                if (response.getStatusLine().getStatusCode() == 200) {
                    result = Optional.of(JsonParser.parseString(jsonStr).getAsJsonObject());
                } else {
                    System.err.printf("Response: %d %s",
                            response.getStatusLine().getStatusCode(),
                            jsonStr);
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
