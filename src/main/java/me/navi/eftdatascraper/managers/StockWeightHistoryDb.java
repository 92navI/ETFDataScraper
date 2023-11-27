package me.navi.eftdatascraper.managers;

import me.navi.eftdatascraper.utils.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StockWeightHistoryDb {

    private final String user;
    private final String password;

    public StockWeightHistoryDb(String user, String password) {
        this.user = user;
        this.password = password;
    }

    private Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:postgresql://etf-db3.cimx9kb6higa.us-west-2.rds.amazonaws.com/";
            conn = DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean insert(HashMap<String, Float> map) {
        String json;
        json = Utils.prettyToString(map);

        String sql = """
                INSERT INTO stockWeightHistory(datetime, info)
                VALUES(NOW(), ?::json)""";

        try (Connection conn = this.connect();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, json);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    public HashMap<String, List<String>> select(String dateFrom, String dateTo) {
        // Format YYYY-DD-MM HH:MM:SS
        String sql = "SELECT * FROM stockWeightHistory WHERE datetime >= ?::TIMESTAMP AND ?::TIMESTAMP >= datetime";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dateFrom);
            pstmt.setString(2, dateTo);


            ResultSet rs = pstmt.executeQuery();

            var output = new HashMap<String, List<String>>();
            List<String> infoList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();

            while (rs.next()) {
                dateList.add(rs.getString("datetime"));
                infoList.add(rs.getString("info"));
            }

            System.out.println("Request successful: " + dateFrom + " - " + dateTo);


            output.put("dateList", dateList);
            output.put("jsonList", infoList);
            return output;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}
