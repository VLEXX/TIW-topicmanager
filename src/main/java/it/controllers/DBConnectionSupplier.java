package it.controllers;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionSupplier {

    private static BasicDataSource ds = new BasicDataSource();

    static {
        ds.setUrl("jdbc:mysql://localhost:3306/dbimagecat");
        ds.setUsername("root");
        ds.setPassword("lolled97mysql");
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxActive(25);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    }

    static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    DBConnectionSupplier(){ }
}
