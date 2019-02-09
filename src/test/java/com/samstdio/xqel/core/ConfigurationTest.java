package com.samstdio.xqel.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class ConfigurationTest {
    static final String FALSY_TEST_PROPERTIES = "falsy_test.properties";
    static final String VALID_TEST_PROPERTIES = "valid_test.properties";

    private Configuration falsy_config;
    private Configuration valid_config;

    @Test
    public void getProperty() {
        assertEquals("sample_driver", falsy_config.getProperty("xqel_driver"));
        assertEquals("sample_user", falsy_config.getProperty("xqel_db_user"));
        assertEquals("sample_connection", falsy_config.getProperty("xqel_connections"));
    }

    @Test
    public void validate() {
        assertFalse(Configuration.validate(falsy_config));
        assertTrue(Configuration.validate(valid_config));
    }

    @Before
    public void setUp() throws Exception {

        try (FileWriter out = new FileWriter(FALSY_TEST_PROPERTIES)) {
            out.write("xqel_driver=sample_driver\n");
            out.write("xqel_connections=sample_connection\n");
            out.write("xqel_db_user=sample_user\n");
            out.flush();
        }

        try (FileWriter out = new FileWriter(VALID_TEST_PROPERTIES)) {
            out.write("DBDRIVER=org.mariadb.jdbc.Driver\n");
            out.write("DBCONNECTION=s192.168.1.4:3307\n");
            out.write("DBSCHEME=test\n");
            out.write("DBUSER=test\n");
            out.write("DBPASS=test\n");
            out.write("TEMPLATE_DIR=template_dir\n");
            out.write("OUTPUT_DIR=output_dir\n");
            out.flush();
        }

        try {
            falsy_config = Configuration.loadFromConfigFile(FALSY_TEST_PROPERTIES);
            valid_config = Configuration.loadFromConfigFile(VALID_TEST_PROPERTIES);
        } catch (IOException e) {
            fail("Fail to load configuration file");
        }
    }

    @After
    public void tearDown() {
        File falsy_config_file = new File(FALSY_TEST_PROPERTIES);
        File valid_config_file = new File(VALID_TEST_PROPERTIES);

        if (falsy_config_file.exists()) {
            falsy_config_file.delete();
        }
        if (valid_config_file.exists()) {
            valid_config_file.delete();
        }
    }

}