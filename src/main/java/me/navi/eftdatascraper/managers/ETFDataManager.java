package me.navi.eftdatascraper.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.navi.eftdatascraper.config.Authorization;
import me.navi.eftdatascraper.sampleclasses.StockData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ETFDataManager {

    public String sendETFRequest() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(Authorization.URL))
                    .header("user-agent", Authorization.USER_AGENT)
                    .header("X-Limit", Authorization.X_LIMIT)
                    .headers("Authorization", Authorization.AUTHORIZATION)
                    .build();
            HttpResponse<String> input = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            return input.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public StockData sendPriceRequest(String name) {
        String input;
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI("https://finnhub.io/api/v1/quote?symbol="
                            + name +
                            "&token=chodrr9r01qmdnlqjq4gchodrr9r01qmdnlqjq50"))
                    .build();
            input = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        }

        var mapper = new ObjectMapper();
        StockData price = null;
        try {
            price = mapper.readValue(input, StockData.class);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        return price;

    }
}
