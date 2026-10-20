package main.java.amazonfilterapplicatione2e.captcha;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.testng.SkipException;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;

public class BlockerHandler extends BasePage {

	private final Logger log = LoggerUtility.getLogger(BlockerHandler.class);

	private final CaptchaHandler captchaHandler = new CaptchaHandler();

	// A normal results or product page has thousands of elements (measured on amazon.in: about 5,500 and 7,000).
	// A bot-check or error page is tiny. The slower lookups below only run on pages smaller than this.
	private static final int BIG_PAGE_ELEMENTS = 1500;

	// The 503 page says "Please try again in a short while", so we reload it a few times, waiting a little between tries.
	private static final int UNAVAILABLE_RETRIES = 3;
	private static final int UNAVAILABLE_WAIT_MS = 3000;

	private static final String CONTINUE_SHOPPING_XPATH =
			"//*[translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='continue shopping']";
	// [3.8] OLD: "//*[contains(text(),'Service Unavailable') or contains(text(),'rush hour')]" (it also matched the text inside <script> tags)
	// NEW: script and style elements are left out, so only text the visitor can read counts.
	private static final String UNAVAILABLE_XPATH =
			"//*[not(self::script or self::style)][contains(text(),'Service Unavailable') or contains(text(),'rush hour')]";

	// Clears a bot-check ("Continue shopping") page or a "service unavailable" page, if one is showing.
	// Kept light: on a normal page this costs only reading the address and counting the page elements.
	public void clearBlockers() {
		long start = System.currentTimeMillis();

		// 1. Cheap checks first: the address, then the size of the page.
		boolean captchaUrl = isCaptchaUrl();
		boolean smallPage = !isBigPage();

		// 2. Only a small page can be a bot-check or error page, so only then look inside it.
		if (captchaUrl || (smallPage && hasContinueShoppingButton())) {
			captchaHandler.handleCaptcha();
		} else if (smallPage && isServiceUnavailablePage()) {
			// [3.8] OLD (one refresh, and nothing checked afterwards):
			//   log.info("[{}] Service unavailable page detected. Refreshing...", ThreadContext.get("testName"));
			//   driver.navigate().refresh();
			reloadUntilAvailable();
		}

		log.info("[{}] clearBlockers took {} ms", ThreadContext.get("testName"), System.currentTimeMillis() - start);
	}

	private boolean isCaptchaUrl() {
		try {
			return driver.getCurrentUrl().toLowerCase().contains("validatecaptcha");
		} catch (WebDriverException e) {
			return false;
		}
	}

	// One small script call that counts the elements on the page. If it fails we cannot tell, so we look inside.
	private boolean isBigPage() {
		try {
			Object count = ((JavascriptExecutor) driver).executeScript("return document.getElementsByTagName('*').length");
			return ((Number) count).intValue() > BIG_PAGE_ELEMENTS;
		} catch (WebDriverException e) {
			return false;
		}
	}

	private boolean hasContinueShoppingButton() {
		try {
			return !driver.findElements(By.xpath(CONTINUE_SHOPPING_XPATH)).isEmpty();
		} catch (WebDriverException e) {
			return false;
		}
	}

	// Reloads the 503 page up to UNAVAILABLE_RETRIES times. The "Go to the home page" link on that page is NOT used,
	// because it would leave the search results and lose the filters we are working on.
	// If the page is still unavailable after the last try, the test is skipped (an Amazon-side problem, not a product bug).
	private void reloadUntilAvailable() {
		for (int attempt = 1; attempt <= UNAVAILABLE_RETRIES; attempt++) {
			log.info("[{}] Service unavailable page detected. Reloading (try {} of {})...", ThreadContext.get("testName"), attempt, UNAVAILABLE_RETRIES);
			driver.navigate().refresh();
			pause(UNAVAILABLE_WAIT_MS);

			if (isBigPage() || !isServiceUnavailablePage()) {
				return; // the page is back
			}
		}
		throw new SkipException("Service unavailable (503) page persisted after " + UNAVAILABLE_RETRIES + " reloads. URL: " + driver.getCurrentUrl());
	}

	// A short wait that is not tied to any page element (we are waiting for Amazon, not for the page), so Thread.sleep is used.
	private void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean isServiceUnavailablePage() {
		try {
			String title = driver.getTitle().toLowerCase();
			return title.contains("service unavailable") || title.contains("oops")
					|| !driver.findElements(By.xpath(UNAVAILABLE_XPATH)).isEmpty();
		} catch (WebDriverException e) {
			return false;
		}
	}

	// ====================================================================================================
	// [3.6] OLD CODE, kept as comments. Two modes were compared; the light mode won and is now the only one.
	//  - "full" read the whole page source twice per call (about 2.3 s on average over 210 calls).
	//  - the first "light" version still ran two XPath searches over every element of every page.
	// ====================================================================================================
	//
	// // Single entry point. blockers.light=false (default) -> original page-source checks,
	// // blockers.light=true -> lightweight checks. Flip it with -Dblockers.light=true to compare.
	// public void clearBlockers() {
	// 	boolean light = ConfigManager.getBoolean("blockers.light", false);
	// 	long start = System.currentTimeMillis();
	// 	if (light) { clearBlockersLight(); } else { clearBlockersFull(); }
	// 	log.info("[{}] clearBlockers ({}) took {} ms", ThreadContext.get("testName"), light ? "light" : "full", System.currentTimeMillis() - start);
	// }
	//
	// // Original methods, unchanged: both read the full page source.
	// public void clearBlockersFull() {
	// 	captchaHandler.handleCaptcha();
	// 	try {
	// 		genericUtility.refreshIfServiceUnavailable();
	// 	} catch (WebDriverException e) {
	// 		log.warn("[{}] Service-unavailable check failed: {}", ThreadContext.get("testName"), e.getMessage());
	// 	}
	// }
	//
	// // First light version: detection by URL, title and two element lookups, on every page.
	// public void clearBlockersLight() {
	// 	if (isCaptchaPageLight()) { captchaHandler.handleCaptcha(); }
	// 	else if (isServiceUnavailableLight()) { driver.navigate().refresh(); }
	// }
}
