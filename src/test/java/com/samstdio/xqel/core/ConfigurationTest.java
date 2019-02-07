package com.samstdio.xqel.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class ConfigurationTest {
    static final String test_configuration_file = "test.properties";
    Configuration config;

    @Test
    public void getProperty() {
        assertEquals("sample_driver", config.getProperty("xqel_driver"));
        assertEquals("sample_user", config.getProperty("xqel_db_user"));
        assertEquals("sample_connection", config.getProperty("xqel_connections"));
    }

    @Before
    public void setUp() throws Exception {
        try (FileWriter out = new FileWriter(test_configuration_file)) {
            out.write("xqel_driver=sample_driver\n");
            out.write("xqel_connections=sample_connection\n");
            out.write("xqel_db_user=sample_user\n");
            out.flush();
        }

        try {
            config = Configuration.loadFromConfigFile(test_configuration_file);
        } catch (IOException e) {
            fail("Fail to load configuration file");
        }
    }

    @After
    public void tearDown() {
        File sample_property = new File(test_configuration_file);
        if (sample_property.exists()) {
            sample_property.delete();
        }
    }

}