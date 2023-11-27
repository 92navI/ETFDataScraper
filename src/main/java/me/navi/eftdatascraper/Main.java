package me.navi.eftdatascraper;

import java.util.LinkedHashMap;


public class Main {
    public static void main(String[] args) {
        generateTestData();
    }

    private static void generateTestData() {
        var starter = new LambdaStarter();

        var map = new LinkedHashMap<String, String>();
        map.put("cash", "400000000");

        System.out.println(starter.startLambdaFunction(map).get(0).getPrice());
    }
}
