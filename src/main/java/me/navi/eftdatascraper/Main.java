package me.navi.eftdatascraper;

import me.navi.eftdatascraper.utils.Utils;

import java.util.LinkedHashMap;


public class Main {
    public static void main(String[] args) {

        // Generate a test request for the lambda function
        generateTestData("queryStockData");
    }


    public static void generateTestData(String queryString) {
        var starter = new LambdaStarter();

        var map = new LinkedHashMap<String, String>();
        map.put("cash", "40000");
        map.put("dateFrom", "2023-11-01");
        map.put("dateTo", "2023-11-30");
        map.put("type", queryString);

        System.out.println(Utils.prettyToString(starter.process(map)));
    }
}
