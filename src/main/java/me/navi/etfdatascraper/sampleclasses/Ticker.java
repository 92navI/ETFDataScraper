package me.navi.etfdatascraper.sampleclasses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;


public class Ticker {
    @JsonProperty("ticker")
    private String ticker;
    @JsonProperty("type")
    private String type;
    @JsonProperty("data")
    private ArrayList<StockData> data;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<StockData> getData() {
        return data;
    }

    public void setData(ArrayList<StockData> data) {
        this.data = data;
    }
}
