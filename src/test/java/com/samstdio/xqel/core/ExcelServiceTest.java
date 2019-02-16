package com.samstdio.xqel.core;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Calendar;

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

        row++;
        testee.writeValueAt("boolean", row, col_key);
        testee.writeValueAt(Boolean.TRUE, row, col_value);
        cell = testee.getCell(row, col_value);
        assertTrue("read boolean", cell.getBooleanCellValue());

        Calendar today = Calendar.getInstance();

        row++;
        testee.writeValueAt("date", row, col_key);
        testee.writeValueAt(today, row, col_value);
        cell = testee.getCell(row, col_value);
        Calendar read_date = Calendar.getInstance();
        read_date.setTime(cell.getDateCellValue());
        assertEquals("read date", today, read_date);

        BigDecimal bd = new BigDecimal("1234567890");
        row++;
        testee.writeValueAt("decimal", row, col_key);
        testee.writeValueAt(bd, row, col_value);
        cell = testee.getCell(row, col_value);
        assertEquals("read decimal", bd, new BigDecimal(cell.getNumericCellValue()));

        testee.save();
    }

    @Before
    public void init() {
        File output_file = new File(test_file);
        output_file.delete();
    }

}