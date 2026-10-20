package test.java.retry;

import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import java.util.Arrays;
import java.util.List;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;

public class RetryFailedTest implements IRetryAnalyzer {

    private static final Logger log = LoggerUtility.getLogger(RetryFailedTest.class);

    private int retryCount = 0;
    private static final int maxRetryCount = 1; // Retry only once

    // List of exceptions to retry on (excluding assertion errors)
    private static final List<Class<? extends Throwable>> nonRetryableExceptions =
            Arrays.asList(AssertionError.class);

    @Override
    public boolean retry(ITestResult result) {
        Throwable cause = result.getThrowable();

        if (retryCount < maxRetryCount && (cause == null || isRetryable(cause))) {
            retryCount++;
            // [3.1] System.out.println("Retrying test: " + result.getName() + " due to " + cause);
            log.warn("Retrying test: {} due to {}", result.getName(), cause);
            return true;
        }
        return false;
    }

    private boolean isRetryable(Throwable cause) {
        for (Class<? extends Throwable> nonRetryable : nonRetryableExceptions) {
            if (nonRetryable.isInstance(cause)) {
                return false;
            }
        }
        return true;
    }
}

