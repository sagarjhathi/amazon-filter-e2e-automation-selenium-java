package main.java.amazonfilterapplicatione2e.fileReader;



import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

public class ExcelReader {

	
	  public static Object[][] readSingleColumn(String relativePath, String sheetName) {
	       
	       ExcelFileReader reader  = new ExcelFileReader();
	       reader.loadWorkbook(relativePath);
	       reader.loadSheet(sheetName);

	        try{
	            int totalRows = reader.getRowCount();

	            Object[][] data = new Object[totalRows - 1][1];  // one-column

	            DataFormatter formatter = new DataFormatter();

	            for (int i = 1; i < totalRows; i++) { // skip header
	                Row row = reader.getRow(i);
	                data[i - 1][0] = formatter.formatCellValue(row.getCell(0));
	            }


	            return data;
	            

	        } catch (Exception e) {
	            throw new RuntimeException("Failed to read Excel", e);
	        }
	        
	        
	        
	    }
}
	 


