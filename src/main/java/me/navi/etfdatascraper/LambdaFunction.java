package me.navi.etfdatascraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.navi.etfdatascraper.managers.ETFDataManager;
import me.navi.etfdatascraper.managers.StockPriceCacheDb;
import me.navi.etfdatascraper.managers.StockWeightHistoryDb;
import me.navi.etfdatascraper.sampleclasses.StockData;
import me.navi.etfdatascraper.sampleclasses.Ticker;
import me.navi.etfdatascraper.utils.Utils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
public class LambdaFunction {

    public static final String QUERY_STOCK_DATA = "queryStockData";
    public static final String QUERY_HIST_DB = "queryHistDb";
    public static final String UPDATE_HIST_DB = "updateHistDb";
    public static final String TYPE = "type";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String NAMES_LARGE = "namesLarge";
    public static final String VALUES_LARGE = "valuesLarge";
    public static final String NAMES_SMALL = "namesSmall";
    public static final String VALUES_SMALL = "valuesSmall";
    public static final String DATE_LIST = "dateList";
    public static final String JSON_LIST = "jsonList";
    public static final String CASH_AMOUNT = "cash";
    private final ObjectMapper mapper = new ObjectMapper();
    private final ETFDataManager etfDataManager = new ETFDataManager();


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

        log.info("Ticker respond: " + ticker.getData());
        return ticker.getData();
    }

    private void setStockPrice(List<StockData> stockDataList) {
        stockDataList.parallelStream().forEach(
                stockData -> {
                    String id = stockData.getSymbol();
                    Float price = StockPriceCacheDb.selectFromYesterday(id);
                    if (!Objects.isNull(price)) {
                        stockData.setPrice(price);
                    } else {
                        DecimalFormat df = new DecimalFormat("#.##");
                        Float newPrice = Float.parseFloat(df.format(
                                etfDataManager.sendPriceRequest(id).getPrice()));


                        stockData.setPrice(newPrice);
                        if (!newPrice.equals(0.0f)) {
                            StockPriceCacheDb.upsert(id, newPrice);
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

    public Object handleRequest(LinkedHashMap<String, String> input) {
        final String type = input.get(TYPE);

        log.info("Lambda started; Type: " + type);

        // Check for the "type" value
        return switch (type) {
            case QUERY_STOCK_DATA -> queryStockData(input);
            case QUERY_HIST_DB -> queryHistDb(input);
            case UPDATE_HIST_DB -> updateHistDb();
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

        log.info("Db update: " + Utils.prettyToString(priceMap));

        return StockWeightHistoryDb.put(priceMap);
    }

    public Map<String, Object> queryHistDb(LinkedHashMap<String, String> input) {

        // Import data
        final HashMap<String, List<String>> result = StockWeightHistoryDb.get(input.get(START_DATE), input.get(END_DATE));
        if (Objects.isNull(result)) {
            return null;
        }

        final List<String> dateList = result.get(DATE_LIST).stream().map(string -> {

            var date = LocalDate.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            var formatter = DateTimeFormatter.ofPattern("MMM. dd, yyyy");
            return date.format(formatter);

        }).toList();
        final List<HashMap<String, Float>> jsonList = result.get(JSON_LIST).stream().map(json -> {
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

        log.info("Data converted!");
        log.info("Data sent.");

        return Map.of(NAMES_LARGE, namesLarge, VALUES_LARGE, valuesLarge, NAMES_SMALL, namesSmall, VALUES_SMALL, valuesSmall);
    }

    public List<StockData> queryStockData(LinkedHashMap<String, String> input) {
        int cash = Integer.parseInt(input.get(CASH_AMOUNT));

        log.info("Ticker request sent.");
        var stockDataList = sendTickerRequest();

        // Manage the stock symbols (Ex. "TSLA")
        manageSymbols(stockDataList);
        stockDataList = excludeUnneeded(stockDataList);

        // Calculate values
        setStockPrice(stockDataList);
        processStockValues(stockDataList, cash);
        stockDataList.forEach(
                stockData -> {
                    if (!(stockData.getPrice() <= 0)) {
                        stockData.setAmount((int) Math.floor(stockData.getValue() / stockData.getPrice()));
                    }
                });

        log.info("Data calculated.");
        return stockDataList;
    }
}
