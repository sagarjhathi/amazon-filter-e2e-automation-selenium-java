package main.java.amazonfilterapplicatione2e.reporting;

import java.io.File;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import com.aventstack.extentreports.MediaEntityBuilder;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.Logger;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pathManager.PathManager;

public class ReportAttachments {

	// Called "logger" because the methods below already use a variable named "log" (a log file).
	private static final Logger logger = LoggerUtility.getLogger(ReportAttachments.class);

	
	
	public static void attachAllLogFolders(String testName) {
		File logFolder = new File(PathManager.getLogPath(testName)!=null ? PathManager.getLogPath(testName):"Default");
	      
	      File[] allFilesLogs = logFolder.listFiles();

	      if (allFilesLogs != null && allFilesLogs.length > 0) {

	          ReportManager.getTest().info("📂 Logs:");


	          for (File log : allFilesLogs) {

	              if (log.getName().endsWith(".log")) {

	                  String relativePath =
	                          "../logs/" + testName + "/" + log.getName();
	                  
	                  ReportManager.getTest().info(
	                		    "📄 <a href='" + relativePath + "' target='_blank'>" + log.getName() + "</a>"
	                		);

							// Allure: copies the log content into allure-results
                    try {
                        Allure.addAttachment(log.getName(), "text/plain", Files.readString(log.toPath()));
                    } catch (Exception e) {
                        // [3.1] System.err.println("Allure log attach failed for " + log.getName() + ": " + e.getMessage());
                        logger.error("Allure log attach failed for {}: {}", log.getName(), e.getMessage());
                    }
					
	              }
	          }
	      }

	}
	
	
	
	
	public static void attachAllImageFolders(String testName) {
		 File screenshotFolder = new File(PathManager.getScreenshotPath(testName)!=null ?PathManager.getScreenshotPath(testName): "Default");

	      File[] allFilesImages = screenshotFolder.listFiles();

	      if (allFilesImages != null && allFilesImages.length > 0) {

	          ReportManager.getTest().info("📸 Screenshots:");

	          for (File img : allFilesImages) {

	              if (img.getName().endsWith(".png")) {

	                  String relativeImgPath =
	                          "../screenshots/" + testName + "/" + img.getName();

	                  ReportManager.getTest().info(
	                      MediaEntityBuilder
	                          .createScreenCaptureFromPath(relativeImgPath)
	                          .build()
	                  );

					      // Allure: copies the image into allure-results
                    try (InputStream in = Files.newInputStream(img.toPath())) {
                        Allure.addAttachment(img.getName(), "image/png", in, "png");
                    } catch (Exception e) {
                        // [3.1] System.err.println("Allure image attach failed for " + img.getName() + ": " + e.getMessage());
                        logger.error("Allure image attach failed for {}: {}", img.getName(), e.getMessage());
                    }
	              }
	          }
	      }	
	}
	
	
	}