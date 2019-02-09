package com.samstdio.xqel.database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class DBConnection {
    private final Connection conn;

    static final Map<String, String> DRIVERS_URL = new HashMap<String, String>() {{
        put("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver");
        put("com.mysql.jdbc.Driver", "mysql");
        put("oracle.jdbc.OracleDriver", "oracle:thin"); // thin/thick/oci?
        put("org.mariadb.jdbc.Driver", "mariadb");
    }};

    static DBConnection createConnection(String driver, String conn, String db, String user, String pass) throws XQELDBException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new XQELDBException(ex);
        }

        String driver_url_prefix = DRIVERS_URL.get(driver);
        if (null == driver_url_prefix) {
            throw new XQELDBException("Not Supported Drivers." + driver + "\nSupported Drivers are : " + String.join(", ", DRIVERS_URL.keySet()));
        }

        Connection con;

        try {
            con = DriverManager.getConnection(
                    "jdbc:" + DRIVERS_URL.get(driver) + "://" + conn + "/" + db,
                    user,
                    pass);
        } catch (SQLException ex) {
            throw new XQELDBException(ex);
        }

        return new DBConnection(con);
    }

    private DBConnection(Connection conn) {
        this.conn = conn;
    }

    ResultSet query(String query) throws XQELDBException {
        Statement statement;
        ResultSet resultSet;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(query);
//            logger.debug("[RUN_QUERY]\n" + query);
        } catch (SQLException qex) {
//            logger.error("Query Error" + qex);
            throw new XQELDBException(qex);
        }
        return resultSet;
    }

    void close() throws XQELDBException {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new XQELDBException(ex);
        }
    }
}
