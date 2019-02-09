package com.samstdio.xqel.database;

import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class DBConnectionTest {

    @Test
    public void testDBConnection() throws XQELDBException {
        String driver = "org.mariadb.jdbc.Driver";
        String conn = "192.168.1.4:3307";
        String db = "test";
        String user = "test";
        String pass = "test";

        DBConnection dbh = DBConnection.createConnection(driver, conn, db, user, pass);
        ResultSet resultSet = dbh.query("select version()");
        String version = null;

        try {
            resultSet.next();
            version = resultSet.getString(1);
        } catch (SQLException qex) {
            ;
        }

        assertThat("Check DBMS Version", version, containsString("10.3.7-MariaDB"));
        dbh.close();
    }
}
