package me.navi.etfdatascraper.managers;

import lombok.extern.slf4j.Slf4j;
import me.navi.etfdatascraper.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class StockWeightHistoryDb {

    public static final String ENTRY_DATE = "entry_date";
    public static final String INFO = "info";
    public static final String DATE_LIST = "dateList";
    public static final String JSON_LIST = "jsonList";

    public static boolean put(HashMap<String, Float> map) {
        String json;
        json = Utils.prettyToString(map);

        String sql = """
                INSERT INTO stockWeightHistory(entry_date, info)
                VALUES(NOW(), ?::json)""";

        try (Connection conn = DbConnection.connect();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, json);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    public static HashMap<String, List<String>> get(String dateFrom, String dateTo) {
        // Format YYYY-DD-MM HH:MM:SS
        String sql = "SELECT * FROM stockWeightHistory WHERE entry_date >= ?::DATE AND ?::DATE >= entry_date";

        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dateFrom);
            pstmt.setString(2, dateTo);


            ResultSet rs = pstmt.executeQuery();

            var output = new HashMap<String, List<String>>();
            List<String> infoList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();

            while (rs.next()) {
                dateList.add(rs.getString(ENTRY_DATE));
                infoList.add(rs.getString(INFO));
            }

            log.info("Request successful: " + dateFrom + " - " + dateTo);


            output.put(DATE_LIST, dateList);
            output.put(JSON_LIST, infoList);
            return output;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
