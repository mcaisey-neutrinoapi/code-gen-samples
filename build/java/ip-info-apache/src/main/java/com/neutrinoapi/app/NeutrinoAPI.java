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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class NeutrinoAPI {

    private static final String USER_ID = "my-user-id";
    private static final String API_KEY = "my-api-key";

    /**
     * Get location information about an IP address and do reverse DNS (PTR) lookups
     *
     * @param params The API request parameters
     * @return Optional<JsonObject>
     */
    private Optional<JsonObject> ipInfo(Map<String, String> params) {
        Optional<JsonObject> result = Optional.empty();
        int readTimeout = (int) TimeUnit.SECONDS.toMillis(10);
        int connectTimeout = (int) TimeUnit.SECONDS.toMillis(5);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder("https://neutrinoapi.net/ip-info");
            params.entrySet().forEach(entity -> {
                uriBuilder.setParameter(entity.getKey(), entity.getValue());
            });

            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setConfig(config);
            httpGet.setHeader("User-ID", USER_ID);
            httpGet.setHeader("API-Key", API_KEY);

            try (CloseableHttpResponse response = client.execute(httpGet)) {
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
