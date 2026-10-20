package main.java.amazonfilterapplicatione2e.pathManager;




import java.io.File;


public class PathManager {

    private static String runFolderPath;

    // RUN LEVEL
    public static void setRunFolderPath(String path) {
        runFolderPath = path;
    }

    public static String getRunFolderPath() {
        return runFolderPath;
    }    
    
    public static String getRunFolder() {
        return runFolderPath;
    }

    public static String getReportPath() {
        return getRunFolder() + File.separator + "reports";
    }

    public static String getReportFilePath() {
        return getReportPath() + File.separator + "ExtentReport.html";
    }

    public static String getLogPath(String testName) {
    	if(testName!=null) {
            return getRunFolder() + File.separator + "logs" + File.separator + testName;

    	}else {
            return getRunFolder() + File.separator + "logs" + File.separator + "Default";
    	}
    }

    public static String getScreenshotPath(String testName) {
    	if(testName!=null) {
            return getRunFolder() + File.separator + "screenshots" + File.separator + testName;
    	}else {
            return getRunFolder() + File.separator + "screenshots" + File.separator + "Default";
    	}
    }
   
}
