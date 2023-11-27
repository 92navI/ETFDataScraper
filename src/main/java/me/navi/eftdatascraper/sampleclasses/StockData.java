package me.navi.eftdatascraper.sampleclasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockData {
    @JsonProperty("name")
    private String name;
    @JsonProperty("weight")
    private String weight;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("c")
    private Float price = 0.0f;

    private float value;

    private int amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getWeight() {
        return Float.parseFloat(StringUtils.chop(weight));
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "\n{\n" +
                "   name= '" + name + "',\n" +
                "   weight= '" + weight + "',\n" +
                "   url= '" + symbol + "',\n" +
                "   price= " + price + ",\n" +
                "   value= " + value + ",\n" +
                "   amount= " + amount + ",\n" +
                '}';
    }
}