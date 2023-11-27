package me.navi.eftdatascraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.navi.eftdatascraper.managers.StockPriceCacheDb;
import me.navi.eftdatascraper.managers.ETFDataManager;
import me.navi.eftdatascraper.managers.StockWeightHistoryDb;
import me.navi.eftdatascraper.sampleclasses.StockData;
import me.navi.eftdatascraper.sampleclasses.Ticker;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LambdaStarter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ETFDataManager etfDataManager = new ETFDataManager();
    private final StockPriceCacheDb stockPriceCacheDb = new StockPriceCacheDb("postgres", "12345678");
    private final StockWeightHistoryDb stockWeightHistoryDb = new StockWeightHistoryDb("postgres", "12345678");


    private List<StockData> sendTickerRequest() {
        
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

        return ticker.getData();
    }

    private void setStockPrice(List<StockData> stockDataList) {
        stockDataList.parallelStream().forEach(
                stockData -> {
                    String id = stockData.getSymbol();
                    Float price = stockPriceCacheDb.selectFromYesterday(id);
                    if (!Objects.isNull(price)) {
                        stockData.setPrice(price);
                    } else {
                        DecimalFormat df = new DecimalFormat("#.##");
                        Float newPrice = Float.parseFloat(df.format(
                                etfDataManager.sendPriceRequest(id).getPrice()));


                        stockData.setPrice(newPrice);
                        if (!newPrice.equals(0.0f)) {
                            stockPriceCacheDb.replace(id, newPrice);
                        }
                    }
                });
    }

    private void manageSymbols(List<StockData> stockDataList) {
        stockDataList.parallelStream().forEach(stockData -> {
            if (Objects.equals(stockData.getName(),
                    "Taiwan Semiconductor Manufacturing Co., Ltd. Sponsored ADR")) {
                stockData.setSymbol("TSM");
            }
            if (Objects.equals(stockData.getName(),
                    "U.S. Dollar")) {
                stockData.setSymbol("USD");
            }
        });
    }

    private List<StockData> excludeUnneeded(List<StockData> stockDataList) {
        return stockDataList.stream()
                .filter(
                        stockData -> !(stockData.getWeight() < 0.0f)
                ).collect(Collectors.toList());
    }

    private void processStockValues(List<StockData> stockDataList, int cash) {
        stockDataList.forEach(
                stockData -> {
                    DecimalFormat df = new DecimalFormat("#.##");
                    stockData.setValue(Float.parseFloat(df.format(stockData.getWeight() * .01 * cash)));
                });
    }

    public Object process(LinkedHashMap<String, String> input) {
        String type = input.get("type");

        // Check for the "type" value
        return switch (type) {
            case "queryStockData" -> queryStockData(input);
            case "queryHistDb" -> queryHistDb(input);
            case "updateHistDb" -> updateHistDb();
            default -> null;
        };
    }

    public boolean updateHistDb() {

        var stockDataList = sendTickerRequest();
        manageSymbols(stockDataList);

        // Exclude USD
        stockDataList = stockDataList.stream().filter(stockData -> !stockData.getSymbol().equals("USD")).toList();

        // Create Output
        var priceMap = new HashMap<String, Float>();
        stockDataList.forEach(stockData -> priceMap.put(stockData.getSymbol(), stockData.getWeight()));

        System.out.println(priceMap);

        return stockWeightHistoryDb.insert(priceMap);
    }

    public Map<String, Object> queryHistDb(LinkedHashMap<String, String> input) {

        // Import data
        HashMap<String, List<String>> result = stockWeightHistoryDb.select(input.get("dateFrom"), input.get("dateTo"));
        List<String> dateList = result.get("dateList").stream().map(string -> {
            var date = ZonedDateTime.parse(
                    string.substring(0, 23),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.of("GMT")));
            var formatter = DateTimeFormatter.ofPattern("MMM. dd, yyyy");
            return date.format(formatter);

        }).toList();
        List<HashMap<String, Float>> jsonList = result.get("jsonList").stream().map(json -> {
            var map = new HashMap<String, Float>();
            try {
                map = mapper.readValue(json, new TypeReference<>() {
                });
            } catch (JsonProcessingException ignored) {
            }

            return map;
        }).toList();

        // Create data sets for both graphs - large and small
        List<List<String>> valuesLarge = new ArrayList<>();
        List<List<String>> valuesSmall = new ArrayList<>();

        Set<String> namesLarge = new HashSet<>();
        Set<String> namesSmall = new HashSet<>();
        jsonList.forEach(map -> namesLarge.addAll(map.keySet()));

        jsonList.forEach(map -> map.forEach((name, price) -> {
            if (price <= 10.0f) {
                namesLarge.remove(name);
                namesSmall.add(name);
            }
        }));

        // Convert the data into proper format
        IntStream.range(0, dateList.size()).forEach(index -> {

            var dataMapLarge = new ArrayList<String>();
            var dataMapSmall = new ArrayList<String>();

            dataMapLarge.add(dateList.get(index));
            dataMapSmall.add(dateList.get(index));

            namesLarge.forEach(name -> {
                var json = jsonList.get(index);
                dataMapLarge.add(String.valueOf(json.get(name)));
            });

            namesSmall.forEach(name -> {
                var json = jsonList.get(index);
                dataMapSmall.add(String.valueOf(json.get(name)));
            });

            valuesLarge.add(dataMapLarge);
            valuesSmall.add(dataMapSmall);
        });

        return Map.of("namesLarge", namesLarge, "valuesLarge", valuesLarge, "namesSmall", namesSmall, "valuesSmall", valuesSmall);
    }

    public List<StockData> queryStockData(LinkedHashMap<String, String> input) {
        int cash = Integer.parseInt(input.get("cash"));

        var stockDataList = sendTickerRequest();

        // Manage the stock symbols (Ex. "TSLA")
        manageSymbols(stockDataList);
        stockDataList = excludeUnneeded(stockDataList);

        // Calculate values
        setStockPrice(stockDataList);
        processStockValues(stockDataList, cash);
        stockDataList.forEach(
                stockData -> stockData.setAmount((int) Math.floor(stockData.getValue() / stockData.getPrice())));

        return stockDataList;
    }
}
