package me.navi.eftdatascraper.utils;

import java.util.Date;

public class Utils {

    public static java.sql.Timestamp epochTimeNow() {
        return new java.sql.Timestamp(new Date().getTime());
    }
}
