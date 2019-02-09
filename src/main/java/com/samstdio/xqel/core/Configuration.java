package com.samstdio.xqel.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class Configuration {
    final private Properties props;

    private enum PROPS {
        DBDRIVER
        , DBCONNECTION
        , DBSCHEME
        , DBUSER
        , DBPASS
        , TEMPLATE_DIR
        , OUTPUT_DIR
    }

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

    /**
     *
     * @param conf
     * @return true if valid
     */
    static boolean validate(Configuration conf) {
        for (PROPS p : PROPS.values()) {
            if (null == conf.props.getProperty(p.toString()))
                return false;
        }

        return true;
    }
}
