package main.java.amazonfilterapplicatione2e.reporting;

import org.apache.logging.log4j.ThreadContext;  
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;


public class TestListener implements ITestListener{

	
	@Override
    public void onTestStart(ITestResult result) {

        ReportManager.createTest(ThreadContext.get("testName"));
    }
    

	
    @Override
    public void onTestSuccess(ITestResult result) {

        ReportManager.getTest().pass("Test Passed");
        ReportAttachments.attachAllLogFolders(ThreadContext.get("testName"));
        ReportAttachments.attachAllImageFolders(ThreadContext.get("testName"));
    }

    @Override
    public void onTestFailure(ITestResult result) {

        Object isRetry = result.getAttribute("retry");

        if (isRetry != null) {
            ReportManager.getTest().warning("Test Failed → Retrying...");
            ReportAttachments.attachAllLogFolders(ThreadContext.get("testName"));
            ReportAttachments.attachAllImageFolders(ThreadContext.get("testName"));
        } else {
            ReportManager.getTest().fail(result.getThrowable());
            ReportAttachments.attachAllLogFolders(ThreadContext.get("testName"));
            ReportAttachments.attachAllImageFolders(ThreadContext.get("testName"));
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
            // OLD: the skip reason was logged as a FAIL, so every skip looked red in the Extent report:
            // ReportManager.getTest().fail(result.getThrowable());
            // NEW: show the skip reason (for example the missing-filter message) as a SKIP.
            ReportManager.getTest().skip(result.getThrowable());
        }
        ReportAttachments.attachAllLogFolders(ThreadContext.get("testName"));
        ReportAttachments.attachAllImageFolders(ThreadContext.get("testName"));
    }
    
    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
    }
}