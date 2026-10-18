package main.java.amazonfilterapplicatione2e.reporting;

import java.io.File; 

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import main.java.amazonfilterapplicatione2e.pathManager.PathManager;

public class ReportManager {

	
    	private static ExtentReports extent;
	    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

	    // 🔹 Initialize report (run-level)
	    public static void initReport(String runPath) {    	
	    	
	    	 String extentPath = PathManager.getReportFilePath();

	    	    new File(PathManager.getReportPath()).mkdirs(); // ensure folder

	    	    ExtentSparkReporter reporter = new ExtentSparkReporter(extentPath);
	    	    reporter.config().setTheme(Theme.DARK);
	    	    reporter.config().setReportName("Amazon Filter E2E - Selenium + Java Automation Report");
	    	    reporter.config().setDocumentTitle("Selenium + Java | Amazon Filter E2E");

	    	    extent = new ExtentReports();
	    	    extent.attachReporter(reporter);
	    }

	    // 🔹 Create test (per test method)
	    public static void createTest(String testName) {
	        ExtentTest extentTest = extent.createTest(testName);
	        test.set(extentTest);
	    }

	    // 🔹 Get current test (used everywhere)
	    public static ExtentTest getTest() {
	        return test.get();
	    } 

	    // 🔹 Flush report (write to file)
	    public static void flush() {
	        extent.flush();
	    }
}