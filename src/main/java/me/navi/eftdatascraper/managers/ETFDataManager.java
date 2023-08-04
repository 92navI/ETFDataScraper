package me.navi.eftdatascraper.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.navi.eftdatascraper.sampleclasses.StockData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ETFDataManager {
    public static String URL = "https://apiprod.etf.com/private/fund/PP/holdings?type=securities&formatValues=true";

    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36";

    public static String X_LIMIT = "1000";

    public static String AUTHORIZATION = "Bearer 0QE2aa6trhK3hOmkf5zXwz6Riy7UWdk4V6HYw3UdZcRZV3myoV9MOfwNLL6FKHrpTN7IF7g12GSZ6r44jAfjte0B3APAaQdWRWZtW2qhYJrAXXwkpYJDFdkCng97prr7N4JAXkCI1zB7EiXrFEY8CIQclMLgQk2XHBZJiqJSIEgtWckHK3UPLfm12X9rhME9ac7gvcF3fWDo8A66X6RHXr3g9jzKeC62th75S1t6juvWjQYDCz65i7UlRfTVWDVV";
    public String sendETFRequest() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(URL))
                    .header("user-agent", USER_AGENT)
                    .header("X-Limit", X_LIMIT)
                    .headers("Authorization", AUTHORIZATION)
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
