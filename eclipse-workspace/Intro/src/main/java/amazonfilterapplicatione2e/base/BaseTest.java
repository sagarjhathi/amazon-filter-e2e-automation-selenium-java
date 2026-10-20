package main.java.amazonfilterapplicatione2e.base;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import main.java.amazonfilterapplicatione2e.driverManager.DriverManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pathManager.PathManager;
import main.java.amazonfilterapplicatione2e.reporting.ReportManager;

public class BaseTest { 	
	
	
	public WebDriver driver;

	private final Logger log = LoggerUtility.getLogger(BaseTest.class);
	  
	  @BeforeSuite(alwaysRun = true)
	  public void createRunFolder() {
		  
	      String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
	      
	      String runPath= System.getProperty("user.dir")
	              + File.separator + "run_" + timestamp;
	      
	      // [3.1] System.out.println(runPath+"    RUN PATH HERE FOR TESTING");
	      // [3.9] OLD (no test name yet, so this line only reached the console): log.info("Run folder for this execution: {}", runPath);
	      
	      
		  PathManager.setRunFolderPath(runPath);
	      ReportManager.initReport(runPath);
	 
	      File runFolder = new File(runPath);
	      runFolder.mkdirs();

	      // [3.9] Suite setup has no test yet, so its log lines go to their own file: <run folder>/logs/suite/suite.log
	      String suiteLogPath = PathManager.getLogPath("suite");
	      new File(suiteLogPath).mkdirs();
	      ThreadContext.put("logFileName", "suite");
	      ThreadContext.put("logPath", suiteLogPath);
	      ThreadContext.put("testName", "suite");
	      log.info("Run folder for this execution: {}", runPath);
	      ThreadContext.clearAll();
	  }

	    	    	  
	  
	  @BeforeMethod(alwaysRun = true)
	  public void beforeTest(ITestResult result) {

	      String baseName = result.getMethod().getMethodName();
	      
	      // [3.1] System.out.println(baseName+"  =======  BASE NAME HERE ");
	      // [3.9] OLD (the test name was not set yet, so this line only reached the console): log.info("Starting test method: {}", baseName);
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
	      log.info("Starting test method: {}", baseName);

	      // Now safe to do anything else
	      DriverManager.initDriver();
	      driver = DriverManager.getDriver();
	  }
	    
	  
	  
	  
	  
	  
	  
	  @AfterMethod(alwaysRun = true)
	  public void afterTest(ITestResult result) {
		  
	      String testName = ThreadContext.get("testName");
	      // [3.1] System.out.println(testName+"    checking the test name being null");
	      log.info("[{}] Test finished, cleaning up", testName);
	      	      
	      // [3.1] System.out.println("Looking logs in: " + PathManager.getLogPath(testName));
	      log.info("[{}] Logs folder: {}", testName, PathManager.getLogPath(testName));
	      // [3.1] System.out.println("Looking screenshots in: " + PathManager.getScreenshotPath(testName));
	      log.info("[{}] Screenshots folder: {}", testName, PathManager.getScreenshotPath(testName));
	     
	      
	      ThreadContext.clearAll();
	      DriverManager.quitDriver();
	      
	      
	  }
}
