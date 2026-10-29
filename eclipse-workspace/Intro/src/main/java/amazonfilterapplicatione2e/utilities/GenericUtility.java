package main.java.amazonfilterapplicatione2e.utilities;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;


public class GenericUtility extends ProductListingPage{
	
	private  final Logger log = LoggerUtility.getLogger(GenericUtility.class);
	

	public boolean filterCheckUnderList(String filterName) {
		log.info("[{}] Within filterCheckUnderList method", ThreadContext.get("testName"));
		
	    String target = filterName.trim().toLowerCase();

	    // The left navigation fills in gradually, so wait until it is complete before looking for the filter.
	    waitForCompleteList(leftNavHeadingsBy);

	    for (WebElement el : listOfFilterNameInLeftNav) {
	    
	        if (el.getText().trim().toLowerCase().equals(target)) {
	            System.out.println(filterName + " matches with assert text here");
	    		log.info("[{}] Found the Filter within the filter check List", ThreadContext.get("testName"));
	            return true;
	        }
	    }


	    System.out.println("Filter option '" + filterName + "' does not exist in the list. Skipping the test.");
		log.info("[{}] Filter option does not exist within the filter check List", ThreadContext.get("testName"));
	    return false;
	}
	


	
	
	// APPLY ONE FILTER OPTION SAFELY. Returns null when the option shows as selected, otherwise the reason it did not.
	//   1. click, then wait up to 10 s for the option to show as selected
	//   2. reload once (the click may have worked while the page did not update) and look again
	//   3. clear, then click a second time
	//   4. give up: clean up, so the next option is not applied on top of this one, and say why
	// listingUrl is the address of the results page before any option was applied (used by clearOption).
	public String applyOption(SafeActions safeAct, String filterName, String filterValue, String listingUrl) throws InterruptedException, TimeoutException {
		By clickBy = getfilterByTypeAndName(filterName, filterValue);
		By selectedBy = getAppliedfilterByTypeAndName(filterName, filterValue);

		if (safeAct.safeClickBooleanWithScreenShot(clickBy, filterName, filterValue) && isElementVisible(selectedBy)) {
			return null;
		}

		log.warn("[{}] '{}' does not show as selected. Reloading once.", ThreadContext.get("testName"), filterValue);
		driver.navigate().refresh();
		if (isElementVisible(selectedBy)) {
			return null;
		}

		log.warn("[{}] '{}' still does not show as selected. Clearing and clicking again.", ThreadContext.get("testName"), filterValue);
		clearOption(safeAct, selectedBy, listingUrl);
		if (safeAct.safeClickBooleanWithScreenShot(clickBy, filterName, filterValue) && isElementVisible(selectedBy)) {
			return null;
		}

		clearOption(safeAct, selectedBy, listingUrl);
		return "'" + filterValue + "' did not show as selected after the click, a reload and a second click";
	}

	// Clears the applied filter and confirms it is gone. If the Clear button does not do it (or throws), go back to the
	// stored address of the results page, so the next option starts from a clean page.
	public void clearOption(SafeActions safeAct, By selectedBy, String listingUrl) {
		try {
			if (!driver.findElements(clearButtonBy).isEmpty()) {
				safeAct.safeClick(clearButtonBy);
			}
			new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.invisibilityOfElementLocated(selectedBy));
		} catch (Exception e) {
			log.warn("[{}] Clear did not remove the option. Going back to the results page address.", ThreadContext.get("testName"));
			driver.get(listingUrl);
		}
	}

	// Waits until a list on the page is complete before we count it: the first item is visible, then the number of
	// items is the same twice in a row, 0.5 s apart. Gives up after 10 s and uses what is there.
	// Without it a list that is still filling in is counted too early (a filter looks missing, an option is skipped).
	public void waitForCompleteList(By locator) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(locator));
		} catch (Exception e) {
			return; // nothing appeared, so there is nothing to wait for: the caller handles an empty list
		}

		int previous = -1;
		long deadline = System.currentTimeMillis() + 10_000;
		while (System.currentTimeMillis() < deadline) {
			int now = driver.findElements(locator).size();
			if (now == previous) {
				return; // same count twice in a row: the list is complete
			}
			previous = now;
			pauseHalfSecond();
		}
	}

	// Waits for the parts of a product page (they load in pieces, and not every product has every part).
	// Returns which parts are there: [0] title, [1] key features, [2] about, [3] technical details (needs at least one row).
	// Once a second, up to 10 s, it checks the parts and scrolls one screen (some parts load only when scrolled to).
	// It stops early when all four are there, or when no new part has appeared for 5 s, so a page that simply has no
	// technical details table does not wait the full 10 s.
	public boolean[] waitForProductSections() {
		By[] sections = { productNameIndividualPage, productKeyFeatureBlock, aboutThisItemBulletPoint, technicalDetailsRowsBy };
		boolean[] present = new boolean[sections.length];
		int mostFound = 0;
		int lastNewPartSecond = 0;

		for (int second = 0; second <= 10; second++) {
			int found = 0;
			for (int i = 0; i < sections.length; i++) {
				present[i] = !driver.findElements(sections[i]).isEmpty();
				if (present[i]) {
					found++;
				}
			}

			if (found == sections.length) {
				break; // everything is there
			}
			if (found > mostFound) {
				mostFound = found;
				lastNewPartSecond = second;
			} else if (second - lastNewPartSecond >= 5) {
				break; // nothing new for 5 s: read what exists
			}

			((JavascriptExecutor) driver).executeScript("window.scrollBy(0, window.innerHeight);");
			pauseHalfSecond();
			pauseHalfSecond();
		}
		log.info("[{}] Product page parts present: title={}, keyFeatures={}, about={}, techDetails={}", ThreadContext.get("testName"),
				present[0], present[1], present[2], present[3]);
		return present;
	}

	private void pauseHalfSecond() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// The text for a test that is skipped because its filter is not in the left navigation. It says what the page
	// showed (headings, URL, title), so the report explains the skip. When the left navigation is empty or the page
	// is a 503 / captcha page, the real cause is Amazon blocking us, so the reason says that instead.
	public String filterMissingReason(String filterName, String searchTerm) {
		List<String> headings = new ArrayList<>();
		try {
			for (WebElement el : listOfFilterNameInLeftNav) {
				headings.add(el.getText().trim());
			}
		} catch (Exception e) {
			log.warn("[{}] Could not read the left navigation headings: {}", ThreadContext.get("testName"), e.getMessage());
		}

		String url = driver.getCurrentUrl();
		String title = driver.getTitle();
		String lowerTitle = title.toLowerCase();
		boolean blocked = lowerTitle.contains("service unavailable") || lowerTitle.contains("oops") || url.contains("validatecaptcha");

		if (blocked) {
			return "Blocked by Amazon: the page did not load properly, so filter '" + filterName + "' could not be checked (search term '"
					+ searchTerm + "'). URL: " + url + ". Title: '" + title + "'.";
		}
		// A normal results page with an empty left navigation: the filters had not loaded when we looked (or Amazon hid them).
		if (headings.isEmpty()) {
			return "The left navigation was empty, so filter '" + filterName + "' could not be checked (search term '" + searchTerm
					+ "'). The filters had not loaded, or Amazon did not show them. URL: " + url + ". Title: '" + title + "'.";
		}
		return "Filter '" + filterName + "' is not in the left navigation for '" + searchTerm + "' in this session. The page showed: "
				+ String.join(", ", headings) + ". URL: " + url + ". Title: '" + title + "'.";
	}

	// For "there was nothing to check" (no product was read, no product could be compared).
	// It throws a normal exception, not an assertion, on purpose: a blocked page also returns nothing, and the retry
	// rule retries normal exceptions but not assertion failures (see RetryFailedTest).
	public void requireSome(int count, String message) {
		if (count == 0) {
			throw new IllegalStateException(message);
		}
	}

	public boolean isElementInViewport(By locator) throws TimeoutException {
	    log.info("[{}] Waiting until element is visible in viewport", ThreadContext.get("testName"));

	    try {
	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

	        JavascriptExecutor js = (JavascriptExecutor) driver;
	        return (Boolean) js.executeScript(
	            "var elem = arguments[0],                 " +
	            "  box = elem.getBoundingClientRect(),    " +
	            "  cx = box.left + box.width / 2,         " +
	            "  cy = box.top + box.height / 2,         " +
	            "  e = document.elementFromPoint(cx, cy); " +
	            "for (; e; e = e.parentElement) {         " +
	            "  if (e === elem)                        " +
	            "    return true;                         " +
	            "}                                        " +
	            "return false;", element);

	    } catch (Exception e) {
	        return false;
	    }
	}

	// True when the element becomes visible within 10 seconds.
	// Used to decide if a product page has loaded. Unlike isElementInViewport it does not
	// require the element to be the top element at its centre, which a wrapped title or a
	// parent <h1> breaks even on a fully loaded page.
	public boolean isElementVisible(By locator) {
	    try {
	        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(locator));
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}


	

    public boolean filterCheckUnderList(String filterName1,String filterName2) {
	
		log.info("[{}] Within filterCheckUnderList method", ThreadContext.get("testName"));

    List<String> filterNames = new ArrayList<>();
    String target1 = filterName1.trim().toLowerCase(); // convert input to lowercase
    String target2 = filterName2.trim().toLowerCase();


    for (WebElement el : listOfFilterNameInLeftNav) {
        String text = el.getText().trim().toLowerCase(); // convert element text to lowercase
        filterNames.add(text);
    }
    
    for (String filterName : filterNames) {
        System.out.println(filterName+"  Filter list printing here from filter checklist function in generic utility function");
    }

    if (filterNames.contains(target1) ||filterNames.contains(target2) ) {
        System.out.println(filterName1+ "  and " +filterName2 + " matches with assert text here");
        return true;
    } else {
        System.out.println("Filter option '" + filterName1 +"  " +filterName2+ "' does not exist in the list. Skipping the test.");
        return false;
    }
}
    

    public void refreshIfServiceUnavailable() {
	    String pageSource = driver.getPageSource().toLowerCase();
	    String title = driver.getTitle().toLowerCase();

	    if (pageSource.contains("service unavailable") || 
	        pageSource.contains("it's rush hour") || 
	        title.contains("service unavailable") || 
	        title.contains("oops")) {
	        
	        System.out.println("Detected error page. Refreshing...");
	        driver.navigate().refresh();
	    }
	}
    
    
   
    
	
   

    
    public void scrollByPixel(int x, int y) {
    	
		log.info("[{}] Within scrollByPixel method", ThreadContext.get("testName"));
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
      
       
        
    }
    
    
    public void smoothScrollToElement(By locator) {
        log.info("[{}] Within smoothScrollToElement method", ThreadContext.get("testName"));

        try {
            SafeActions safeAct = new SafeActions();
            WebElement element = safeAct.safeFindElement(locator);

            if (element != null) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", 
                    element
                );
                log.info("[{}] Successfully scrolled to element: {}", ThreadContext.get("testName"), locator);
            } else {
                log.warn("[{}] Element not found for locator: {}", ThreadContext.get("testName"), locator);
            }

        } catch (Exception e) {
            log.error("[{}] Error while scrolling to element: {} - {}", ThreadContext.get("testName"), locator, e.getMessage());
        }
    }    
    
    
    
    public void closeCurrentWindowAndSwitchBack(String originalWindow) throws InterruptedException {
        log.info("[{}] Within closeCurrentWindowAndSwitchBack method", ThreadContext.get("testName"));
        
        try {
            Set<String> allWindowHandles = driver.getWindowHandles();
            log.info("[{}] Total open windows/tabs count -> {}", ThreadContext.get("testName"), allWindowHandles.size());
            log.info("[{}] Original window handle -> {}", ThreadContext.get("testName"), originalWindow);

            for (String handle : allWindowHandles) {
                if (!handle.equals(originalWindow)) {
                    log.info("[{}] Found new tab/window handle -> {} (will be closed)", ThreadContext.get("testName"), handle);
                    try {
                        driver.switchTo().window(handle);
                        log.info("[{}] Successfully switched to new tab/window -> {}", ThreadContext.get("testName"), handle);
                        driver.close();
                        log.info("[{}] Closed the newly opened tab/window -> {}", ThreadContext.get("testName"), handle);
                    } catch (Exception e) {
                        log.warn("[{}] Failed to close or switch to window handle -> {} due to {}", 
                                 ThreadContext.get("testName"), handle, e.getMessage());
                    }
                }
            }

    
            driver.switchTo().window(originalWindow);
            log.info("[{}] Switched back to the original window -> {}", ThreadContext.get("testName"), originalWindow);

        } catch (Exception e) {
            log.error("[{}] Exception while closing current window and switching back: {}", 
                      ThreadContext.get("testName"), e.getMessage(), e);
        }
    }

    


 public String fetchTextWithRetries(By locator, SafeActions safeAct) {
	    String text = "";
	    int retryCount = 0;

	    while (text.isEmpty() && retryCount < 2) {
	        try {
	            WebElement element = safeAct.safeFindElement(locator);
	            if (element != null) {
	                text = element.getText().trim();
	            } else {
	           
	            }
	        } catch (Exception e) {
	            System.out.println("Exception while fetching element text: " + e.getMessage());
	        }

	        retryCount++;
	    }

	    return text;
	}

 
 
 public void setSliderValue(WebElement slider, int value) {

	 JavascriptExecutor js= (JavascriptExecutor)driver;

	 js.executeScript(
			 "arguments[0].value = arguments[1];" +
					 "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));" +
					 "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
					 slider, String.valueOf(value)
			 );
	 log.info("[{}] in the setSliderValue method with value "+value, ThreadContext.get("testName"));
 }

 
 
 // Strips currency symbols, thousands-separator commas and whitespace, but keeps the decimal
 // point - a blanket "\D" strip would treat "1,299.50" the same as "1299" and "129950",
 // silently corrupting any price that carries a fractional part.
 public static String extractPriceOrFail(String input) {
	    if (input == null) {
	        throw new IllegalArgumentException("Cannot extract price: input is null");
	    }

	    String cleaned = input.replaceAll("[^0-9.]", "");

	    // Keep only the first decimal point in case stray "." characters slipped through.
	    int firstDot = cleaned.indexOf('.');
	    if (firstDot != -1) {
	        cleaned = cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "");
	    }

	    if (cleaned.isEmpty() || cleaned.equals(".")) {
	        throw new IllegalArgumentException("Cannot extract price: no digits found in input '" + input + "'");
	    }

	    return cleaned;
	}
 
 
 
 public void clickMoreButtonIfPresent( SafeActions safeAct, GenericUtility genericUtility, By moreButton ) throws InterruptedException {
	 
	try {
		 if(safeAct.safeFindElement(moreButton).isDisplayed()) {
				genericUtility.smoothScrollToElement(moreButton);
				safeAct.safeClick(moreButton);
			
			}
	}catch(Exception e) {
	
		 log.info("[{}] checkMoreButtonScrollClick failed: {}",
	             ThreadContext.get("testName"), e.getMessage());
	}
	 
 }
 
 
 
 
 
 
 
 public boolean openClickOnNewPage(WebElement element, int beforeCount) {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

	    try {
	        
	        new Actions(driver)
	            .keyDown(Keys.CONTROL)
	            .click(element)
	            .keyUp(Keys.CONTROL)
	            .perform();

	        wait.until(ExpectedConditions.numberOfWindowsToBe(beforeCount + 1));
	        return true; // success
	    } catch (Exception e) {
	        
	        try {
	            element.click();
	            wait.until(ExpectedConditions.numberOfWindowsToBe(beforeCount + 1));
	            return true;
	        } catch (Exception ex) {
	            return false; 
	        }
	    }
	}

 
 
 public void addFieldIfPresent(String key,String value,String filterValue,int productIndex,  Map<String, Object> result) {

	 String testName = ThreadContext.get("testName");

	 if (value != null && !value.isEmpty()) {
		 result.put(key, value);
	 } else {
		 result.put(key, null);  // 👈 ensures consistent keys
		 log.warn("[{}] Failed to fetch {} after retries for filter value -> {} and index -> {}",
		         testName, key, filterValue, productIndex);
	 }

}

 
 
 
 public String safeLower(Object value) {
	    if (value == null) return "";
	    return value.toString().toLowerCase();
	}

 // Keeps only the products whose details were really read (the product page can fail to load,
 // leaving title/features/about/tech all empty). Those would show up as false "filter not found" failures.
 public List<Map<String, Object>> onlyCapturedProducts(List<Map<String, Object>> results, String testName) {
	    List<Map<String, Object>> captured = new ArrayList<>();
	    if (results == null) return captured;

	    for (Map<String, Object> product : results) {
	        String details = safeLower(product.get("title")) + safeLower(product.get("keyFeatures"))
	                + safeLower(product.get("about")) + safeLower(product.get("techDetails"));

	        if (details.trim().isEmpty()) {
	            log.warn("[{}] Product details were not captured for filter '{}'. Not counting it as a mismatch.",
	                    testName, product.get("filter"));
	        } else {
	            captured.add(product);
	        }
	    }
	    return captured;
	}

 
 
 
 public void switchToNewWindow(String currentWindowHandle) {
		log.info("[{}] Within switchToNewWindow method", ThreadContext.get("testName"));

		Set<String> allWindowHandles = driver.getWindowHandles();

		for (String handle : allWindowHandles) {
			if (!handle.equals(currentWindowHandle)) {
				System.out.println("Found the new window. Switching now...");
				driver.switchTo().window(handle);
				return;
			}
		}

		System.out.println("No new window found to switch to.");
	}
 
 
 
 
	public void waitForNewWindowAndSwitch(String originalWindow, int beforeCount) {
		log.info("[{}] Waiting for new window to open...", ThreadContext.get("testName"));


		  wait.until(ExpectedConditions.numberOfWindowsToBe(beforeCount + 1));

		for (String handle : driver.getWindowHandles()) {
			if (!handle.equals(originalWindow)) {
				driver.switchTo().window(handle);
				log.info("[{}] Switched to new window -> {}", ThreadContext.get("testName"), handle);
				return;
			}
		}

		log.error("[{}] Timeout after {}s: no new window detected", ThreadContext.get("testName"));
		throw new RuntimeException("Timeout waiting for new window");
	}
	

	
	public boolean containsCheck(String toBeChecked,String toBeCheckedIn) {
		
		String[] list=toBeChecked.split("\\s+");
		int trueCount=0;
		int falseCount=0;
		boolean bool=true;
		for(int i=0;i<list.length;i++) {
			String str=list[i];
			
			if(toBeCheckedIn.contains(str)) {
				
				trueCount++;
				log.error("[{}] Text present within Under containsCheck Method, text checked is  "+list[i], ThreadContext.get("testName"));
			}else {
				bool=false;
				falseCount++;
				log.error("[{}] Text Not present within Under containsCheck Method, text checked is  "+list[i], ThreadContext.get("testName"));
			}
		}
		
		
		return bool;
		
		
	}

 }
 

    