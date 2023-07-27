package me.navi.eftdatascraper.managers;

import me.navi.eftdatascraper.utils.Utils;

import java.sql.*;

public class SQLDBManager {

    private final String url;
    private final String user;
    private final String password;

    public SQLDBManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public void replace(String name, String price) {
        String sql = """
INSERT INTO stockPriceCash(name, datetime, price)
VALUES(?, NOW(), ?)\s
ON CONFLICT ON CONSTRAINT unique_name\s
DO\s
UPDATE SET datetime = NOW(), price = ? WHERE stockPriceCash.name = ?;""";

        try (Connection conn = this.connect();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, price);
            statement.setString(3, price);
            statement.setString(4, name);
            statement.executeUpdate();

            System.out.printf(
                    "name: %s, datetime: %s, price: %s successfully uploaded to table \"stockPriceCash\"%n",
                    name, Utils.epochTimeNow(), price);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Float selectFromYesterday(String name) {
        String sql = "SELECT name, datetime, price FROM stockPriceCash WHERE datetime >= 'yesterday'::TIMESTAMP  AND name = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);


            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                return rs.getFloat("price");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
