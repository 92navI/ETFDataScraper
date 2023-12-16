package me.navi.etfdatascraper.sampleclasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import me.navi.etfdatascraper.utils.Utils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
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

    public Float getWeight() {
        return Float.parseFloat(StringUtils.chop(weight));
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return Utils.prettyToString(this);
    }
}