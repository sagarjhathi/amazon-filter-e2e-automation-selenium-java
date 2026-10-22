package test.testDataProvider;

import java.lang.reflect.Method;
import java.util.List;

import org.testng.annotations.DataProvider;

import main.java.amazonfilterapplicatione2e.fileReader.ExcelReader;
import main.java.amazonfilterapplicatione2e.fileReader.TestInputs;

public class TestDataProvider {

	 @DataProvider(name = "ExcelData")
	    public Object[][] excelData() {
	        return ExcelReader.readSingleColumn("data/Products.xlsx", "Products");
	    }

	// Every test uses this one provider. TestNG tells it which test is asking, and it returns
	// the search terms written for that test in data/testInputs.json (one row per term).
	@DataProvider(name = "TestRuns")
	public Object[][] testRuns(Method testMethod) {
		List<String> terms = TestInputs.termsFor(testMethod.getName());

		Object[][] rows = new Object[terms.size()][1];
		for (int i = 0; i < terms.size(); i++) {
			rows[i][0] = terms.get(i);
		}
		return rows;
	}

}
