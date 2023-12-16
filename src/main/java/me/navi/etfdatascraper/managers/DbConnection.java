package me.navi.etfdatascraper.managers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@Slf4j
@Getter
@Setter
public class DbConnection {

    private static String login = System.getenv("LOGIN");

    private static String password = System.getenv("PASSWORD");

    private static String url = System.getenv("URL");

    static {
        if (!(System.getenv().containsKey("LOGIN")
                && System.getenv().containsKey("PASSWORD")
                && System.getenv().containsKey("URL"))) {
            log.error("Not enough environment variables to establish the database connection!");
        }
    }

    public static Connection connect() {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, login, password);

        } catch (SQLException e) {
            log.error("SQL Error while trying to connect to database: " + url, e);
        }
        return conn;
    }
}
