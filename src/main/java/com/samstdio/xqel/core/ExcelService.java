package com.samstdio.xqel.core;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//import com.samstdio.xqel.database.DBConnection;

class ExcelService {
    private Workbook wb = null;    // 작업 대상 엑셀 파일
    private Sheet workingsheet = null;    // 작업 대상 엑셀 시트
    private static final String PLACEHOLDERSTRIP = "[{}]";    // 쓰기 장소 표시(placeholder)
    private final File TEMPLATE_PATH;
    private final File OUTPUT_PATH;
    private final String output_filename;

    ExcelService(File template_path, File output_path, String output_filename) {
        this.TEMPLATE_PATH = template_path;
        this.OUTPUT_PATH = output_path;
        this.output_filename = output_filename;
    }

    static ExcelService createExcel(String filename) {
        Workbook wb = new XSSFWorkbook();
        return new ExcelService(wb, filename);
    }

    private ExcelService(Workbook wb, String output_filename) {
        this.wb = wb;
        this.output_filename = output_filename;
        this.TEMPLATE_PATH = new File(".");
        this.OUTPUT_PATH = new File(".");
    }

    /**
     * 파일 열기
     *
     * @param filename 엑셀 템플릿 파일명
     * @throws InvalidFormatException
     * @throws IOException
     */
    void open(String filename) throws IOException, ExcelException {
        filename = TEMPLATE_PATH + "/" + filename;
        try {
            wb = new XSSFWorkbook(new File(filename));
        } catch (InvalidFormatException ex) {
            throw new ExcelException(ex);
        }
    }

    /**
     * 이름있는 영역(Named Range)의 이름값 가져오기
     *
     * @param namedRange
     * @return
     */
    Name getNamedRange(String namedRange) {
        // retrieve the named range
        int namedCellIdx = wb.getNameIndex(namedRange);
        return wb.getNameAt(namedCellIdx);
    }

    /**
     * 작업 대상 워크시트 선택
     *
     * @param worksheetName
     */
    boolean selectWorksheet(String worksheetName) {
        workingsheet = wb.getSheet(worksheetName);
        if (null == workingsheet)
            return false;
        return true;
    }

    boolean createSheet(String sheetName) {
        workingsheet = wb.getSheet(sheetName);
        if (null == workingsheet) {
            workingsheet = wb.createSheet(sheetName);
            return true;
        }

        return false;
    }

    /**
     * 쿼리 결과를 지정된 이름 영역에 쓰기. 워크시트가 선택되어 있어야 한다.
     *
     * @param result
     * @param regionName
     */
    void writeResultToRegion(ResultSet result, String regionName) {
        // FIXME 컬럼/타입
//        Map<String, Integer> column_types = DBConnection.getResultSetMetaData(result);
        Map<String, Integer> column_types = null;
        // 이름영역에서 쓰기장소표시(Placeholder) 이름/셀위치(row, column)
        Map<String, int[]> placeholder = null;

        try {
            placeholder = getPlaceholders(regionName);
        } catch (ExcelException e) {
            e.printStackTrace();
        }
        // first row
        int rowStart = ((int[]) (placeholder.values().toArray()[0]))[0];
        int rowWrite = rowStart;    // 쓰기 선택 줄

        // Placeholder 이름을 기준으로 Query 결과를 참조한다.
        Set<String> columnNames = placeholder.keySet();

        try {
            // by row
            while (result.next()) {
                for (String colName : columnNames) { // by placeholder
                    int colWrite = placeholder.get(colName)[1];    // column position

                    // 스타일 복사
                    copyStyle(rowStart, colWrite, rowWrite, colWrite);

                    // null 예외 처리
                    if (null == column_types.get(colName)) {
                        writeValueAt(null, rowWrite, colWrite);
                        continue;    // 꼭꼭!
                    }

                    // FIXME ResultSet에서 값 읽어오기 함수가 다양해서..이렇게 해야하나 ㅠㅠ
                    if (column_types.get(colName) == Types.VARCHAR ||
                            column_types.get(colName) == Types.CHAR ||
                            column_types.get(colName) == Types.LONGVARCHAR) {
                        String val = result.getString(colName);    // get value

                        writeValueAt(val, rowWrite, colWrite);
                    } else if (column_types.get(colName) == Types.NUMERIC ||
                            column_types.get(colName) == Types.DECIMAL) {
                        BigDecimal val = result.getBigDecimal(colName);    // get value

                        writeValueAt(val, rowWrite, colWrite);
                    } else if (column_types.get(colName) == Types.DATE) {
                        Date val = result.getDate(colName);    // get value

                        writeValueAt(val, rowWrite, colWrite);
                    }
                }
                rowWrite++;
            }

//            logger.debug("WRITING CNT: " + (rowWrite - rowStart) + " FOR : " + regionName);

            // 한 줄도 쓸게 없었다면..(ResultSet row.count == 0)
            if (rowStart == rowWrite) {
                // Placeholder 영역 청소. 굳이 보여줄 필요 없으니까..
                for (int[] rowcol : placeholder.values()) {
                    writeValueAt(null, rowcol[0], rowcol[1]);
                }
            }
        } catch (SQLException qex) {
            qex.printStackTrace();
        } finally {
            try {
                result.close();    // 꼭꼭!
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 이름영역에서 Placeholder 가져오기
     *
     * @param rangeName 이름영역
     * @return Placeholder값과 위치(row, col)
     * @throws ExcelException
     */
    private Map<String, int[]> getPlaceholders(String rangeName) throws ExcelException {
        Map<String, int[]> placeholders = new HashMap<String, int[]>();

        Name name = getNamedRange(rangeName);

        // 이름영역 값 참조 준비..
        // test codes
        // retrieve the cell at the named range and test its contents
        AreaReference aref = new AreaReference(name.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
        CellReference[] crefs = aref.getAllReferencedCells();
        if (null == workingsheet) {
            throw new ExcelException("Worksheet is not selected. selectWorksheet first.");
        }
        // 값과 위치 가져오기
        for (int i = 0; i < crefs.length; i++) {
            Row r = workingsheet.getRow(crefs[i].getRow());
            Cell c = r.getCell(crefs[i].getCol());
            // 불필요한 {} 표시는 없애기
            // 이름/위치(row,col)
            String placeholder_name = c.getStringCellValue().replaceAll(PLACEHOLDERSTRIP, "");
            placeholders.put(placeholder_name, new int[]{c.getRowIndex(), c.getColumnIndex()});
//            logger.debug("GET PLACEHOLER: " + placeholder_name + " AT(Row,Col):" + c.getRowIndex() + "," + c.getColumnIndex());
        }
        return placeholders;
    }

    /**
     * 작업 중인 엑셀을 파일로 저장
     */
    void save() {
        try {
            String output_full_path = OUTPUT_PATH.getCanonicalPath() + File.separator + output_filename;
            createPathIfNotExist();    // 경로가 없으면 만들어.
            FileOutputStream out = new FileOutputStream(new File(output_full_path));
            wb.write(out);
//            logger.debug("WRITING OUTPUT: " + output_filename);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPathIfNotExist() {
        File path = OUTPUT_PATH;
        if (!path.exists()) {
            new File(path.getPath().substring(0, path.getPath().lastIndexOf(File.separator))).mkdirs();
        }
    }

    /**
     * ResultSet 이외의 값을 쓰는 기능
     *
     * @param regionName 영역이름
     * @param nameValue  Placeholder와 쓰려는 값
     */
    void writeValuesToRegion(String regionName, Map<String, String> nameValue) {
        Map<String, int[]> placeholder = null;
        try {
            placeholder = getPlaceholders(regionName);
        } catch (ExcelException e) {
            e.printStackTrace();
        }

        // Nothing to do..
        if (null == placeholder || null == nameValue)
            return;

        Set<String> keys = nameValue.keySet();

        int rowWrite;
        int colWrite;
        for (String key : keys) {
            if (placeholder.containsKey(key)) {
                rowWrite = placeholder.get(key)[0];
                colWrite = placeholder.get(key)[1];
                writeValueAt(nameValue.get(key), rowWrite, colWrite);
//                logger.debug("WRITING VALUE: (" + nameValue.get(key) + ") AT(Row,Col): " + rowWrite + "," + colWrite);
            }
        }
    }

    /**
     * 셀 위치에 값 쓰기
     *
     * @param value        쓰려는 값
     * @param row_to_write 행
     * @param col_to_write 열
     */
    void writeValueAt(Object value, int row_to_write, int col_to_write) {
        Row row = workingsheet.getRow(row_to_write);
        if (null == row)
            row = workingsheet.createRow(row_to_write);

        Cell cell = row.getCell(col_to_write);
        if (null == cell)
            cell = row.createCell(col_to_write);

        if (null == value)
            cell.setCellValue("");
        // FIXME 형(Type) 쫌....문제야 문제... writeResultToRegion 이 메서드도..
        if (value instanceof String)
            cell.setCellValue((String) value);
        else if (value instanceof Date)
            cell.setCellValue((Date) value);
        else if (value instanceof BigDecimal)
            cell.setCellValue(((BigDecimal) value).doubleValue());
    }

    void fomularUpdate() {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (Row r : workingsheet) {
            for (Cell c : r) {
                if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    evaluator.evaluateFormulaCell(c);
                }
            }
        }
    }

    private void copyStyle(int rowSrc, int colSrc, int rowDest, int colDest) {
        Cell srcCell = getCell(rowSrc, colSrc);
        Cell destCell = getCell(rowDest, colDest);

        destCell.setCellStyle(srcCell.getCellStyle());
    }

    //private
    Cell getCell(int row_i, int col_i) {
        Row row = workingsheet.getRow(row_i);
        if (null == row)
            row = workingsheet.createRow(row_i);

        Cell cell = row.getCell(col_i);
        if (null == cell)
            cell = row.createCell(col_i);
        return cell;
    }

    int getSheetSeq(String sheetName) throws ExcelException {
        Sheet sheet = wb.getSheet(sheetName);
        if (null == sheet)
            throw new ExcelException("Sheet not found : " + sheetName);
        return wb.getSheetIndex(sheet);
    }

    int countSheet() {
        return wb.getNumberOfSheets();
    }

    Sheet cloneSheet(int sheet_seq) {
        return wb.cloneSheet(sheet_seq);
    }

    void renameSheet(Sheet sheet, String new_name) {
        int sheet_index = wb.getSheetIndex(sheet);
        wb.setSheetName(sheet_index, new_name);
    }

    void reorder(String sheet_name, int new_order) {
        wb.setSheetOrder(sheet_name, new_order);
    }
}
