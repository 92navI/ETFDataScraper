package me.navi.etfdatascraper;

import me.navi.etfdatascraper.utils.Utils;

import java.util.LinkedHashMap;


public class Main {
    public static void main(String[] args) {
        // Generate a test request for the lambda function
        generateTestData("updateHistDb");
    }


    public static void generateTestData(String queryString) {
        var starter = new LambdaFunction();

        var map = new LinkedHashMap<String, String>();
        map.put("cash", "40000");
        map.put("startDate", "2023-12-01");
        map.put("endDate", "2023-12-30");
        map.put("type", queryString);

        System.out.println(Utils.prettyToString(starter.handleRequest(map)));
    }
}
