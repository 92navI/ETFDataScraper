package me.navi.eftdatascraper;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class Main {
    public static void main(String[] args) {
        try {
            HttpRequest.newBuilder(new URI("https://postman-echo.com/get"));
        } catch (URISyntaxException e) {
            e.toString();
        }
        System.out.println("Hello World!");
    }
}
