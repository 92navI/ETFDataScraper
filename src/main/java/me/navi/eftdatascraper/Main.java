package me.navi.eftdatascraper;

import me.navi.eftdatascraper.managers.ETFDataManager;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;


public class Main {
    public static void main(String[] args) {

        var starter = new LambdaStarter();

        var map = new LinkedHashMap<String, String>();
        map.put("cash", "400000000");

        System.out.println(starter.startLambdaFunction(map).get(0).getPrice());


    }
}
