package me.navi.eftdatascraper.managers;

import me.navi.eftdatascraper.sampleclasses.StockData;

import java.text.DecimalFormat;
import java.util.Objects;

public class PriceCacheService {

    public SQLDBManager dbManager;
    public ETFDataManager etfDataManager;

    public PriceCacheService(SQLDBManager dbManager, ETFDataManager etfDataManager) {
        this.dbManager = dbManager;
        this.etfDataManager = etfDataManager;
    }

    public void process(StockData stockData, String id) {
        Float price = dbManager.selectFromYesterday(id);
        if (!Objects.isNull(price)) {
            stockData.setPrice(price);
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            String newPrice = df.format(
                    etfDataManager.sendPriceRequest(id).getPrice());
            stockData.setPrice(Float.parseFloat(newPrice));
            dbManager.replace(id, newPrice);
        }
    }
}
