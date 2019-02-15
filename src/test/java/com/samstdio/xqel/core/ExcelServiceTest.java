package com.samstdio.xqel.core;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ExcelServiceTest {

    @Test
    public void testCreate() {
        System.out.println(File.separator);
        final String test_file = "sample.xlsx";
        ExcelService testee = ExcelService.createExcel(test_file );
        testee.save();

        File output_file = new File(test_file);
        assertTrue("File exists", output_file.exists());
        output_file.delete();
    }

}