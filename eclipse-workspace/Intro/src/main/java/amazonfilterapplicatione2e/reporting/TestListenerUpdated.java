package main.java.amazonfilterapplicatione2e.reporting;

import org.apache.logging.log4j.ThreadContext;  
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;


public class TestListenerUpdated implements ITestListener{

	
	@Override
    public void onTestStart(ITestResult result) {

        ReportManager.createTest(ThreadContext.get("testName"));
    }
    

	
    @Override
    public void onTestSuccess(ITestResult result) {

        ReportManager.getTest().pass("Test Passed");
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));
    }

    @Override
    public void onTestFailure(ITestResult result) {

        Object isRetry = result.getAttribute("retry");

        if (isRetry != null) {
            ReportManager.getTest().warning("Test Failed → Retrying...");
            Attachers.attachAllLogFolders(ThreadContext.get("testName"));
            Attachers.attachAllImageFolders(ThreadContext.get("testName"));
        } else {
            ReportManager.getTest().fail(result.getThrowable());
            Attachers.attachAllLogFolders(ThreadContext.get("testName"));
            Attachers.attachAllImageFolders(ThreadContext.get("testName"));
        }
        
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        Object isRetry = result.getAttribute("retry");

        if (isRetry != null) {
            ReportManager.getTest().warning("Retry Attempt");
            ReportManager.getTest().warning(result.getThrowable());
        } else {
            ReportManager.getTest().skip("Test Skipped");
            // OLD: ReportManager.getTest().fail(result.getThrowable());   (marked the skipped test as Failed in Extent)
            // Show the skip reason (the SkipException message) as info, so the test stays Skipped.
            if (result.getThrowable() != null) {
                ReportManager.getTest().info("Skip reason: " + result.getThrowable().getMessage());
            }
        }
        Attachers.attachAllLogFolders(ThreadContext.get("testName"));
        Attachers.attachAllImageFolders(ThreadContext.get("testName"));
    }

    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
    }
}