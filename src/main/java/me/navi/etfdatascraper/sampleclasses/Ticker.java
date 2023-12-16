package me.navi.etfdatascraper.sampleclasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Setter
@Getter
public class Ticker {
    @JsonProperty("ticker")
    private String ticker;
    @JsonProperty("type")
    private String type;
    @JsonProperty("data")
    private ArrayList<StockData> data;

}
