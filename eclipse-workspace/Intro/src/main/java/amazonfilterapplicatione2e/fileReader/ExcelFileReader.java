package main.java.amazonfilterapplicatione2e.fileReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelFileReader {

	
    private Workbook workbook;
    private Sheet sheet;
    /**
     * Load Excel workbook by file name (relative path)
     */
    public void loadWorkbook(String fileName) {
        try {
            File file = find(fileName);
            if (file == null || !file.exists()) {
                throw new RuntimeException("Excel file not found: " + fileName);
            }
            workbook = WorkbookFactory.create(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Excel file: " + fileName, e);
        }
    }

    /**
     * Select the sheet by name
     */
    public void loadSheet(String sheetName) {
        if (workbook == null)
            throw new IllegalStateException("Workbook not loaded. Call loadWorkbook() first.");
        sheet = workbook.getSheet(sheetName);
        if (sheet == null)
            throw new RuntimeException("Sheet not found: " + sheetName);
    }

    /**
     * Get a Row by index (0-based)
     */
    public Row getRow(int rowIndex) {
        if (sheet == null)
            throw new IllegalStateException("Sheet not loaded. Call loadSheet() first.");
        return sheet.getRow(rowIndex);
    }

    /**
     * Get a Cell by row and column index (0-based)
     */
    public Cell getCell(int rowIndex, int colIndex) {
        Row row = getRow(rowIndex);
        return (row != null) ? row.getCell(colIndex) : null;
    }

    /**
     * Get cell value as String (auto-handles numbers, booleans, etc.)
     */
    public String getCellValue(int rowIndex, int colIndex) {
        Cell cell = getCell(rowIndex, colIndex);
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Get total row count in current sheet
     */
    public int getRowCount() {
        if (sheet == null)
            throw new IllegalStateException("Sheet not loaded. Call loadSheet() first.");
        return sheet.getPhysicalNumberOfRows();
    }

    /**
     * Get total column count for a given row
     */
    public int getColumnCount(int rowIndex) {
        Row row = getRow(rowIndex);
        return (row != null) ? row.getPhysicalNumberOfCells() : 0;
    }

    /**
     * Close the workbook safely
     */
    public void closeWorkbook() {
        try {
            if (workbook != null) workbook.close();
        } catch (IOException e) {
            System.err.println("Failed to close workbook: " + e.getMessage());
        }
    }

        
    public static File find(String fileName) {
        // 1. Try as a direct/absolute/relative-to-cwd path
        File f = new File(fileName);
        if (f.exists()) {
            return f.getAbsoluteFile();
        }

        // 2. Fall back to classpath (covers files bundled in resources/)
        URL resource = ExcelFileReader.class.getClassLoader().getResource(fileName);
        if (resource != null) {
            return new File(resource.getPath());
        }

        System.err.println("File not found: " + fileName);
        return null;
    }
	 

}
