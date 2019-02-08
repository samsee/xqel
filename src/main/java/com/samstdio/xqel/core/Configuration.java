package com.samstdio.xqel.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class Configuration {
    final private Properties props;

    static Configuration loadFromConfigFile(String filename) throws IOException {
        return new Configuration(filename);
    }

    Configuration(String filename) throws IOException {
        try (FileInputStream input = new FileInputStream(filename)) {
            props = new Properties();
            props.load(input);
        }
    }

    String getProperty(String key) {
        return props.getProperty(key);
    }
}
