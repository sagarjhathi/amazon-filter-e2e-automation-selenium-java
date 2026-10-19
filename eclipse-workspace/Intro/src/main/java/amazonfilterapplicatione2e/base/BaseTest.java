package main.java.amazonfilterapplicatione2e.base;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import main.java.amazonfilterapplicatione2e.driverManager.DriverManager;
import main.java.amazonfilterapplicatione2e.pathManager.PathManager;
import main.java.amazonfilterapplicatione2e.reporting.ReportManager;

public class BaseTest { 	
	
	
	public WebDriver driver;
	  
	  @BeforeSuite(alwaysRun = true)
	  public void createRunFolder() {
		  
	      String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
	      
	      String runPath= System.getProperty("user.dir")
	              + File.separator + "run_" + timestamp;
	      
	      System.out.println(runPath+"    RUN PATH HERE FOR TESTING");
	      
	      
		  PathManager.setRunFolderPath(runPath);
	      ReportManager.initReport(runPath);
	 
	      File runFolder = new File(runPath);
	      runFolder.mkdirs();
	  }

	    	    	  
	  
	  @BeforeMethod(alwaysRun = true)
	  public void beforeTest(ITestResult result) {

	      String baseName = result.getMethod().getMethodName();
	      
	      System.out.println(baseName+"  =======  BASE NAME HERE ");
	      String params = Arrays.toString(result.getParameters())
	              .replaceAll("[\\[\\] ]", "")
	              .replace(",", "_");

	      String timestamp = LocalDateTime.now()
	              .format(DateTimeFormatter.ofPattern("HH-mm-ss"));

	      String testName = baseName 
	              + (params.isEmpty() ? "" : "_" + params)
	              + "_" + timestamp;

	      
	      String logPathName = PathManager.getLogPath(testName);

	      // SET CONTEXT FIRST (VERY IMPORTANT)
	      ThreadContext.put("testNameShort", params);
	      ThreadContext.put("logFileName", testName);
	      ThreadContext.put("logPath", logPathName);
	      ThreadContext.put("testName", testName);
	      ThreadContext.put("baseTestName",baseName);

	      // Ensure folder exists BEFORE logging
	      new File(logPathName).mkdirs();

	      // Now safe to do anything else
	      DriverManager.initDriver();
	      driver = DriverManager.getDriver();
	  }
	    
	  
	  
	  
	  
	  
	  
	  @AfterMethod(alwaysRun = true)
	  public void afterTest(ITestResult result) {
		  
	      String testName = ThreadContext.get("testName");
	      System.out.println(testName+"    checking the test name being null");
	      	      
	      System.out.println("Looking logs in: " + PathManager.getLogPath(testName));
	      System.out.println("Looking screenshots in: " + PathManager.getScreenshotPath(testName));
	     
	      
	      ThreadContext.clearAll();
	      DriverManager.quitDriver();
	      
	      
	  }
}
