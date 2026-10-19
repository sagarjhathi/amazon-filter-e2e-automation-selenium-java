package main.java.amazonfilterapplicatione2e.captcha;
import java.time.Duration;   
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;
import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.configManager.*;

public class CaptchaHandler extends BasePage{
	
	
	// Amazon's bot-check here is a "Continue shopping" interstitial, not an image/audio captcha,
	// so detection is just page-source/text matching rather than solving anything. Any exception
	// (e.g. driver mid-navigation) is treated as "not a captcha page" so callers aren't blocked by
	// a transient read failure.
	public boolean isCaptchaPage() {
	    try {
	        String src = driver.getPageSource().toLowerCase();

	        if (src.contains("click the button below to continue shopping") ||
	            src.contains("/errors/validatecaptcha")) {
	            return true;
	        }

	        return !driver.findElements(By.xpath(
	            "//*[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='continue shopping']"
	        )).isEmpty();

	    } catch (Exception e) {
	        return false;
	    }
	}

	
		
	public void handleCaptcha() {
	    int retries = ConfigManager.getInt("handleCaptcha.retries", 3);
	    long backoff = 1000;

	    for (int i = 1; i <= retries; i++) {

	        // Doubles as "did the previous attempt clear it?" for every loop pass after the first,
	        // since this runs right after the prior click + page-load wait + backoff sleep.
	        if (!isCaptchaPage()) return;

	        System.out.println("Captcha detected. Attempt " + i);

	        try {
	            List<WebElement> buttons = driver.findElements(By.xpath(
	                "//*[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='continue shopping']"
	            ));

	            if (!buttons.isEmpty()) {
	                WebElement button = buttons.get(0);

	                ((JavascriptExecutor) driver)
	                    .executeScript("arguments[0].scrollIntoView(true);", button);

	                new WebDriverWait(driver, Duration.ofSeconds(10))
	                        .until(d -> button.isDisplayed() && button.isEnabled());

	                button.click();
	            } else {
	                // Button not found (page may have changed) - a refresh is a cheap fallback
	                // that sometimes clears the interstitial on its own.
	                driver.navigate().refresh();
	            }

	            waitForPageLoad();
	            // Thread.sleep here (unlike the WebDriverWaits used elsewhere in this project) is
	            // intentional: this isn't waiting on a page condition/element, it's pacing how fast
	            // we retry the bot-check itself - a WebDriverWait has nothing to poll for that.
	            // Exponential backoff (1s, 2s, 4s, capped at 5s) between attempts, since Amazon's
	            // bot-check is more likely to persist if retried too aggressively.
	            Thread.sleep(backoff);
	            backoff = Math.min(backoff * 2, 5000);

	        } catch (Exception e) {
	            System.out.println("Captcha handling attempt failed: " + e.getMessage());
	        }
	    }

	    // The loop's own check only runs before each attempt, so the very last click/refresh is
	    // never re-verified before falling out of the loop - check once more here so a captcha
	    // that actually cleared on the final attempt doesn't get skipped anyway.
	    if (!isCaptchaPage()) return;

	    // SkipException (not a failure) - a persistent bot-check is an environment/rate-limit
	    // issue, not a real product/filter bug, so the test is marked skipped rather than failed.
	    throw new SkipException("Captcha persisted after retries.");
	}
	
	private void waitForPageLoad() {
	    int timeout = ConfigManager.getInt("pageLoad.timeout", 15);

	    new WebDriverWait(driver, Duration.ofSeconds(timeout))
	        .until(d -> ((JavascriptExecutor) d)
	        .executeScript("return document.readyState")
	        .equals("complete"));
	}
}
