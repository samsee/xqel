package com.samstdio.xqel.core;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

class ExcelException extends Exception {

    public ExcelException(String arg0) {
        super(arg0);
    }

    public ExcelException(Throwable ex) {
        super(ex);
    }
}
