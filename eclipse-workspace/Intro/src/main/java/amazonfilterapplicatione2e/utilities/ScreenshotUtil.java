package main.java.amazonfilterapplicatione2e.utilities;

import java.io.File;  

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.driverManager.DriverManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pathManager.PathManager;

public class ScreenshotUtil {

	
	private  final static Logger log = LoggerUtility.getLogger(ScreenshotUtil.class);
	
	WebDriver driver;
	
	 public static String capture(String... names) {
	        WebDriver driver = DriverManager.getDriver(); // get from ThreadLocal
	        String inputNames=String.join("_", names);
	        
	        try {
	        	//get the screenshot path
	        	String screenShotPath = PathManager.getScreenshotPath(ThreadContext.get("testName"));

	        	// create folder
	        	File folder = new File(screenShotPath);
	        	folder.mkdirs();

	        	
	        	//creating a final uniqe image file name
	        	String finalPath= screenShotPath + File.separator + ThreadContext.get("testNameShort")+System.nanoTime()+".png";
	        	// [3.1] System.out.println(finalPath+"   -->> final path if param exist");
	        	log.info("[{}] {}", ThreadContext.get("testName"), finalPath+"   -->> final path if param exist");
	        		        	
	       
	        	// capture
	        		File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	        		
	        		  if(ConfigManager.getBoolean("compressImage", false)) {
	                  	try{
	                  		String imageQuality=ConfigManager.get("imageCompressionQuality");
	                  		// [3.1] System.out.println(imageQuality);
	                  		log.info("[{}] Image compression quality (from config): {}", ThreadContext.get("testName"), imageQuality);
	                  		
	                  		double parseImageQuality=Double.parseDouble(imageQuality);
	                  		// [3.1] System.out.println(parseImageQuality);
	                  		log.info("[{}] Image compression quality (as number): {}", ThreadContext.get("testName"), parseImageQuality);
	                  		ImageCompressor.compressImage(src, finalPath,parseImageQuality);
	                  	  }catch(Exception e) {
	                  		  log.warn("[{}] compression quality / compressImage method failed, handling it the default way[No compression].", ThreadContext.get("testName"));
	                  		  FileUtils.copyFile(src, new File(finalPath));
	                  	  }
	                	  
	                  }else {
	                	  log.warn("[{}] handling it the default way[No compression].", ThreadContext.get("testName"));
	            		  FileUtils.copyFile(src, new File(finalPath));
	                  }

	        	return finalPath;

	        } catch (Exception e) {
	            // [3.1] System.out.println("Screenshot failed: " + e.getMessage()+ThreadContext.get("testName"));
	            log.error("[{}] {}", ThreadContext.get("testName"), "Screenshot failed: " + e.getMessage());
	            return null;
	        }
	        
	    }
}