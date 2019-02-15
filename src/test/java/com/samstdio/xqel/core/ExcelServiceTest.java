package com.samstdio.xqel.core;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ExcelServiceTest {
    final static String test_file = "sample.xlsx";
    @Test
    public void testCreate() {
        System.out.println(File.separator);
        final String test_file = "sample.xlsx";
        ExcelService testee = ExcelService.createExcel(test_file );
        testee.save();

        testee.writeValueAt("World", 1, 2);
        Cell cell = testee.getCell(1, 2);
        assertEquals("World", cell.getStringCellValue());


        testee.save();

//        cell.getBooleanCellValue();
//        cell.getDateCellValue();
//        cell.getNumericCellValue();
//        cell.getRichStringCellValue();
    }

}