package me.navi.eftdatascraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.navi.eftdatascraper.managers.ETFDataManager;
import me.navi.eftdatascraper.managers.SQLDBManager;
import me.navi.eftdatascraper.managers.PriceCacheService;
import me.navi.eftdatascraper.sampleclasses.StockData;
import me.navi.eftdatascraper.sampleclasses.Ticker;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class LambdaStarter {

    private static void setStockValues(ArrayList<StockData> stockDataList, int cash) {
        stockDataList.forEach(
                stockData -> {
                    DecimalFormat df = new DecimalFormat("#.##");
                    float percentWeight = Float.parseFloat(StringUtils.chop(stockData.getWeight()));
                    stockData.setValue(Float.parseFloat(df.format(percentWeight * .01 * cash)));
                });
    }

    private static void checkForUrls(StockData stockData) {
        if (Objects.equals(stockData.getName(),
                "Taiwan Semiconductor Manufacturing Co., Ltd. Sponsored ADR")) {
            stockData.setUrl("/stock/TSM");
        }
    }

    public ArrayList<StockData> startLambdaFunction(LinkedHashMap<String, String> input) {
        int cash = Integer.parseInt(input.get("cash"));

        var mapper = new ObjectMapper();

        String path = "jdbc:postgresql://localhost:5432/etf";
        var dbManager = new SQLDBManager(path, "postgres", "root");

        var etfDataManager = new ETFDataManager();

        var priceCacheService = new PriceCacheService(dbManager, etfDataManager);


        String inputString = etfDataManager.sendETFRequest();
        Ticker ticker;
        try {
            ticker = mapper.readValue(inputString, new TypeReference<ArrayList<Ticker>>() {
                    })
                    .stream()
                    .findFirst()
                    .orElseThrow();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ArrayList<StockData> stockDataList = ticker.getData();


        stockDataList.parallelStream().forEach(
                stockData -> {
                    checkForUrls(stockData);
                    if (!Objects.isNull(stockData.getUrl())) {
                        String id = stockData.getUrl().replace("/stock/", "");

                        priceCacheService.process(stockData, id);
                    }
                });


        setStockValues(stockDataList, cash);


        stockDataList = stockDataList.stream()
                .filter(
                        stockData -> !(stockData.getPrice().equals(0.0f))
                ).collect(Collectors.toCollection(ArrayList::new));


        stockDataList.forEach(
                stockData -> stockData.setAmount((int) Math.floor(stockData.getValue() / stockData.getPrice())));


        return stockDataList;
    }
}
