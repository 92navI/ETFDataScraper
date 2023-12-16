package me.navi.etfdatascraper.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

public class Utils {

    public static java.sql.Timestamp epochTimeNow() {
        return new java.sql.Timestamp(new Date().getTime());
    }

    public static String prettyToString(Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

}
