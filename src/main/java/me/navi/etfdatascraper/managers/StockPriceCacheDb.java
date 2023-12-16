package me.navi.etfdatascraper.managers;

import lombok.extern.slf4j.Slf4j;
import me.navi.etfdatascraper.utils.Utils;
import java.sql.Connection;

import java.sql.*;

@Slf4j
public class StockPriceCacheDb {

    public static final String PRICE = "price";

    public static void upsert(String name, Float price) {
        String sql = """
                INSERT INTO stockPriceCache(name, datetime, price)
                VALUES(?, NOW(), ?)\s
                ON CONFLICT ON CONSTRAINT unique_name\s
                DO\s
                UPDATE SET datetime = NOW(), price = ? WHERE stockPriceCache.name = ?;""";

        try (Connection conn = DbConnection.connect();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setFloat(2, price);
            statement.setFloat(3, price);
            statement.setString(4, name);
            statement.executeUpdate();

            log.info(
                    "name: {}, datetime: {}, price: {} successfully uploaded to table \"stockPriceCache\"",
                    name, Utils.epochTimeNow(), price);
        } catch (SQLException e) {
            log.error("SQL Error while trying to upsert data into caching.", e);
        }
    }

    public static Float selectFromYesterday(String name) {
        String sql = "SELECT name, datetime, price FROM stockPriceCache WHERE datetime >= 'yesterday'::TIMESTAMP  AND name = ?";

        try (Connection conn = DbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);


            ResultSet rs = pstmt.executeQuery();

            Float[] output = new Float[1];
            while (rs.next()) {
                output[0] = rs.getFloat(PRICE);
            }

            return output[0];

        } catch (SQLException e) {
            log.error("SQL Error while trying to select caching.", e);
        }
        return null;
    }
}
