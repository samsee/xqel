package com.samstdio.xqel.database;

import java.sql.*;

class DBConnection {
    private final Connection conn;

    public static DBConnection createConnection(String driver, String conn, String db, String user, String pass) throws XQELDBException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new XQELDBException(ex);
        }

        Connection con;

        try {
            con = DriverManager.getConnection(
                    "jdbc:mariadb://" + conn + "/" + db,
                    user,
                    pass);
        } catch (SQLException ex) {
            throw new XQELDBException(ex);
        }

        DBConnection dbc = new DBConnection(con);

        return dbc;
    }

    private DBConnection(Connection conn) {
        this.conn = conn;
    }

    ResultSet query(String query) throws XQELDBException {
        Statement statement;
        ResultSet resultSet = null;
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

    public void close() throws XQELDBException {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new XQELDBException(ex);
        }
    }
}
