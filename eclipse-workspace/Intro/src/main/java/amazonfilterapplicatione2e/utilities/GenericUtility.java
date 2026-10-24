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
 

    