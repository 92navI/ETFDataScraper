package me.navi.eftdatascraper.managers;

import me.navi.eftdatascraper.utils.Utils;

import java.sql.*;

public class stockPriceCacheDb {

    private final String user;
    private final String password;

    public stockPriceCacheDb(String user, String password) {
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


    public void replace(String name, Float price) {
        String sql = """
                INSERT INTO stockPriceCache(name, datetime, price)
                VALUES(?, NOW(), ?)\s
                ON CONFLICT ON CONSTRAINT unique_name\s
                DO\s
                UPDATE SET datetime = NOW(), price = ? WHERE stockPriceCache.name = ?;""";

        try (Connection conn = this.connect();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setFloat(2, price);
            statement.setFloat(3, price);
            statement.setString(4, name);
            statement.executeUpdate();

            System.out.printf(
                    "name: %s, datetime: %s, price: %s successfully uploaded to table \"stockPriceCache\"%n",
                    name, Utils.epochTimeNow(), price);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Float selectFromYesterday(String name) {
        String sql = "SELECT name, datetime, price FROM stockPriceCache WHERE datetime >= 'yesterday'::TIMESTAMP  AND name = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);


            ResultSet rs = pstmt.executeQuery();

            Float[] output = new Float[1];
            while (rs.next()) {
                output[0] = rs.getFloat("price");
            }

            return output[0];

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
