package main.java.amazonfilterapplicatione2e.captcha;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;

public class BlockerHandler extends BasePage {

	private final Logger log = LoggerUtility.getLogger(BlockerHandler.class);

	private final CaptchaHandler captchaHandler = new CaptchaHandler();
	private final GenericUtility genericUtility = new GenericUtility();

	private static final String CONTINUE_SHOPPING_XPATH =
			"//*[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='continue shopping']";
	private static final String UNAVAILABLE_XPATH =
			"//*[contains(text(),'Service Unavailable') or contains(text(),'rush hour')]";

	// Single entry point. blockers.light=false (default) -> original page-source checks,
	// blockers.light=true -> lightweight checks. Flip it with -Dblockers.light=true to compare.
	public void clearBlockers() {
		boolean light = ConfigManager.getBoolean("blockers.light", false);
		long start = System.currentTimeMillis();

		if (light) {
			clearBlockersLight();
		} else {
			clearBlockersFull();
		}

		log.info("[{}] clearBlockers ({}) took {} ms", ThreadContext.get("testName"),
				light ? "light" : "full", System.currentTimeMillis() - start);
	}

	// Original methods, unchanged: both read the full page source.
	public void clearBlockersFull() {
		captchaHandler.handleCaptcha();
		try {
			genericUtility.refreshIfServiceUnavailable();
		} catch (Exception e) {
			log.warn("[{}] Service-unavailable check failed: {}", ThreadContext.get("testName"), e.getMessage());
		}
	}

	// Same outcome, but detection only uses the URL, title and two small element lookups.
	// The original handlers are only called once a blocker is actually found.
	public void clearBlockersLight() {
		if (isCaptchaPageLight()) {
			captchaHandler.handleCaptcha();
		} else if (isServiceUnavailableLight()) {
			log.info("[{}] Service unavailable page detected. Refreshing...", ThreadContext.get("testName"));
			driver.navigate().refresh();
		}
	}

	private boolean isCaptchaPageLight() {
		try {
			return driver.getCurrentUrl().contains("/errors/validatecaptcha")
					|| !driver.findElements(By.xpath(CONTINUE_SHOPPING_XPATH)).isEmpty();
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isServiceUnavailableLight() {
		try {
			String title = driver.getTitle().toLowerCase();
			return title.contains("service unavailable") || title.contains("oops")
					|| !driver.findElements(By.xpath(UNAVAILABLE_XPATH)).isEmpty();
		} catch (Exception e) {
			return false;
		}
	}
}
