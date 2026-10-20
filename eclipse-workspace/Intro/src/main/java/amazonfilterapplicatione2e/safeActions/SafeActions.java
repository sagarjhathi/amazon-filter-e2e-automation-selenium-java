package main.java.amazonfilterapplicatione2e.safeActions;
import org.apache.logging.log4j.Logger;   
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtil;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class SafeActions extends BasePage{
   
	private  final Logger log = LoggerUtility.getLogger(SafeActions.class);

	
	private WebDriverWait safeWait;

	private WebDriverWait getSafeWait() {
	    if (safeWait == null) {
	        safeWait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getInt("safeActionsWaitTime", 10)));
	    }
	    return safeWait;
	}

	public void safeClick(By locator) {
		log.info("[{}] Within safeClick method", ThreadContext.get("testName"));

	    int attempts = 0;
	    while (attempts < 2) {
	        try {
	        	
	        	WebElement element = getSafeWait().until(ExpectedConditions.presenceOfElementLocated(locator));
	            getSafeWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
	            getSafeWait().until(ExpectedConditions.elementToBeClickable(locator));
	            element.click();
	    		log.info("[{}] Clicked the  "+element+"   using safeClick", ThreadContext.get("testName"));
	            // [3.1] System.out.println("Clicked using safeClick");
	            return;
	            
	        } catch (TimeoutException |ElementClickInterceptedException | StaleElementReferenceException  e) {
	            // [3.1] System.out.println("Retrying click for: " + locator + " - Attempt " + (attempts + 1));
	    		// [3.1] old: log.info("[{}] Cannot click the butto using safeClick, trying again", ThreadContext.get("testName"));
	    		log.info("[{}] Cannot click using safeClick, trying again. Locator: "+locator+" - Attempt "+(attempts + 1), ThreadContext.get("testName"));
	            attempts++;
	            
	                driver.navigate().refresh();
	            
	             
	        }
	    }
	    // After 2 attempts, skip the action without throwing exception
		// [3.1] old: log.info("[{}] Skipping click action: Element not clickable after- "+attempts, ThreadContext.get("testName"));
		log.info("[{}] Skipping click action: Element not clickable after- "+attempts+" attempts - "+locator, ThreadContext.get("testName"));
	    // [3.1] System.out.println("Skipping click action: Element not clickable after 2 attempts - " + locator);
	}

	
	public List<WebElement> safeFindElements(By locator) {
		log.info("[{}] Within safeFindElements ", ThreadContext.get("testName"));

	    int attempts = 0;
	    while (attempts < 2) {
	        try {
	            List<WebElement> elements = getSafeWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
	           
	           
	    		log.info("[{}] Returning the  "+elements+" from the safeFindElements method", ThreadContext.get("testName"));

	            // [3.1] System.out.println("Found the elements: " + locator);
	            return elements;
	        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
	    		// [3.1] old: log.info("[{}] Retrying findElements for:"+locator, ThreadContext.get("testName"));
	    		log.info("[{}] Retrying findElements for:"+locator+" - Attempt "+(attempts + 1), ThreadContext.get("testName"));
	            // [3.1] System.out.println("Retrying findElements for: " + locator + " - Attempt " + (attempts + 1));
	            attempts++;
	            
	                driver.navigate().refresh();
		    		log.info("[{}] Refreshing the page while trying to find :"+locator, ThreadContext.get("testName"));

	                // [3.1] System.out.println("Refreshing the page in safeFindElements Method");
	            
	             
	        
	    }
	    // After 2 attempts, return null instead of throwing exception
	    // [3.1] System.out.println("Skipping action: Elements not found after 2 attempts - " + locator);
		// [3.1] old: log.info("[{}] Skipping action Elements not found after"+attempts+"  Attempts", ThreadContext.get("testName"));
		log.info("[{}] Skipping action Elements not found after"+attempts+"  Attempts - "+locator, ThreadContext.get("testName"));

	    
	}
		return null;
	}

		
		
		public WebElement safeFindElement(By locator) {
			log.info("[{}] Within safeFindElement method", ThreadContext.get("testName"));

		    int attempts = 0;
		    while (attempts < 2) {
		        try {
		        	
		            WebElement element = getSafeWait().until(ExpectedConditions.presenceOfElementLocated(locator));
		            getSafeWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
		            getSafeWait().until(ExpectedConditions.elementToBeClickable(locator));
					log.info("[{}]Found the element returning it ,element ->"+element, ThreadContext.get("testName"));
		            // [3.1] System.out.println("Found the element: " + locator);
		            return element;
		        } catch (TimeoutException | StaleElementReferenceException e) {
					// [3.1] old: log.info("[{}]Retrying findElement for: ->"+locator, ThreadContext.get("testName"));
					log.info("[{}]Retrying findElement for: ->"+locator+" - Attempt "+(attempts + 1), ThreadContext.get("testName"));

		            // [3.1] System.out.println("Retrying findElement for: " + locator + " - Attempt " + (attempts + 1));
		            attempts++;
		            
		                driver.navigate().refresh();
						log.info("[{}]Refrehsing the page , while trying to safely find : ->"+locator, ThreadContext.get("testName"));
		                // [3.1] System.out.println("Refreshing the page in safeFindElement Method");
		             
		             
		        }
		    }
		


			// [3.1] old: log.info("[{}] Skipping action: Element not found after "+attempts+"   attemps", ThreadContext.get("testName"));
			log.info("[{}] Skipping action: Element not found after "+attempts+"   attempts - "+locator, ThreadContext.get("testName"));

		    // [3.1] System.out.println("Skipping action: Element not found after 2 attempts - " + locator);
		    return null;
		}




		public WebElement safeFindElement(WebElement locator) {
			log.info("[{}] Within safeFindElement method", ThreadContext.get("testName"));

		    int attempts = 0;
		    while (attempts < 2) {
		        try {
		        	
		            WebElement element = getSafeWait().until(ExpectedConditions.visibilityOf(locator));
		            getSafeWait().until(ExpectedConditions.elementToBeClickable(locator));
					log.info("[{}]Found the element returning it ,element ->"+element, ThreadContext.get("testName"));
		            // [3.1] System.out.println("Found the element: " + locator);
		            return element;
		        } catch (TimeoutException | StaleElementReferenceException e) {
					// [3.1] old: log.info("[{}]Retrying findElement for: ->"+locator, ThreadContext.get("testName"));
					log.info("[{}]Retrying findElement for: ->"+locator+" - Attempt "+(attempts + 1), ThreadContext.get("testName"));

		            // [3.1] System.out.println("Retrying findElement for: " + locator + " - Attempt " + (attempts + 1));
		            attempts++;
		            
		                driver.navigate().refresh();
						log.info("[{}]Refrehsing the page , while trying to safely find : ->"+locator, ThreadContext.get("testName"));
		                // [3.1] System.out.println("Refreshing the page in safeFindElement Method");
		             
		             
		        }
		    }
		


			// [3.1] old: log.info("[{}] Skipping action: Element not found after "+attempts+"   attemps", ThreadContext.get("testName"));
			log.info("[{}] Skipping action: Element not found after "+attempts+"   attempts - "+locator, ThreadContext.get("testName"));

		    // [3.1] System.out.println("Skipping action: Element not found after 2 attempts - " + locator);
		    return null;
		}







		public boolean safeClickBooleanWithScreenShot(By locator,String filterName,String filterOption) throws InterruptedException, java.util.concurrent.TimeoutException {
			    log.info("[{}] Within safeClickBooleanWithScreenShot method", ThreadContext.get("testName"));

				GenericUtility genericUtility=new GenericUtility();
				SafeActions safeAct=new SafeActions();
				ProductListingPage productPage=new ProductListingPage();
				String testName = ThreadContext.get("logFileName");
				String filterOptionToPass=filterOption;
				
		    int attempts = 0;
		    while (attempts < 3) {
		        try {

                	genericUtility.smoothScrollToElement(productPage.getFilterHeaderByTypeAndName(filterName));
                
                	try {
                    	genericUtility.clickMoreButtonIfPresent(safeAct, genericUtility, productPage.getMoreButtonByFilterTypeAndName(filterName));

                	}catch(Exception e) {
        			    log.info("[{}] could not find more button", ThreadContext.get("testName"));
                	}
                	

                	WebElement element = getSafeWait().until(ExpectedConditions.presenceOfElementLocated(locator));
		            getSafeWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
		            getSafeWait().until(ExpectedConditions.elementToBeClickable(locator));
		            // [3.1] System.out.println(element+"  printing the element address from the safeBooleanClick from safeActions");
		            log.info("[{}] {}", ThreadContext.get("testName"), element+"  printing the element address from the safeBooleanClick from safeActions");
		            element.click();
		         
		            
		            genericUtility.smoothScrollToElement(productPage.getFilterHeaderByTypeAndName(filterName));
		                   
		            
		        
		    
		            try {
                    	genericUtility.clickMoreButtonIfPresent(safeAct, genericUtility, productPage.getMoreButtonByFilterTypeAndName(filterName));
                	}catch(Exception e) {
        			    log.info("[{}] could not find more button", ThreadContext.get("testName"));
                	}
		            
		    
                	
		            genericUtility.smoothScrollToElement(productPage.getAppliedFilterByTypeAndName(filterName, filterOption));
		    
					log.info("[{}] Clicked the element using safe click ,element is "+element ,ThreadContext.get("testName"));
		            // [3.1] System.out.println("Clicking using safeClick");  
		            ScreenshotUtil.capture(testName,filterOptionToPass);
		            return true; // success
		        } catch (TimeoutException | ElementClickInterceptedException | StaleElementReferenceException e) {
		            // [3.1] System.out.println("Retrying click for: " + locator + " - Attempt " + (attempts + 1));
					// [3.1] old: log.info("[{}] Cannot click the button, element ->"+locator ,ThreadContext.get("testName"));
					log.info("[{}] Cannot click the button, element ->"+locator+" - Attempt "+(attempts + 1) ,ThreadContext.get("testName"));
		            attempts++;
		            
		            try {
		            	
		                   driver.navigate().refresh();
		                		
		                	log.info("[{}] Cannot apply filter taking failed screenshot for ->"+filterOption ,ThreadContext.get("testName"));
		                	genericUtility.smoothScrollToElement(productPage.getFilterHeaderByTypeAndName(filterName));
		               

		                	try {
		                    	genericUtility.clickMoreButtonIfPresent(safeAct, genericUtility, productPage.getMoreButtonByFilterTypeAndName(filterName));

		                	}catch(Exception E) {
		        			    log.info("[{}] could not find more button", ThreadContext.get("testName"));
		                	}
		                	
		       
				            ScreenshotUtil.capture(testName,filterOptionToPass,"failed filter apply screenshot-");
		                
		                
						log.info("[{}] Refrshing the page , while trying to click ->"+locator ,ThreadContext.get("testName"));



		     }
		            catch(Exception ee) {

		            }
		    }
		    }
			// [3.1] old: log.info("[{}]Skipping click action: Element not clickable after"+attempts+"   attempts" ,ThreadContext.get("testName"));
			log.info("[{}]Skipping click action: Element not clickable after"+attempts+"   attempts - "+locator ,ThreadContext.get("testName"));

		    // [3.1] System.out.println("Skipping click action: Element not clickable after 3 attempts - " + locator);
			return false;
		    }
		
		
		
		
		
		
		public String safeGetFilterOptionText(By locator, int index) {

		    for (int attempt = 1; attempt <= 3; attempt++) {
		        try {
		            log.info("[{}] Trying to read filter option {} (attempt {}/3)",
		                     ThreadContext.get("testName"), index, attempt);

		   
		            List<WebElement> list= getSafeWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));

		            if (index >= list.size()) {
		                log.warn("[{}] Index {} out of bounds (size={})",
		                         ThreadContext.get("testName"), index, list.size());
		                return "";
		            }

		            String text = list.get(index).getText().trim();
		            log.info("[{}] Successfully read filter option {} = '{}'",
		                     ThreadContext.get("testName"), index, text);
		            return text;   // SUCCESS

		        } catch (StaleElementReferenceException sere) {
		            log.warn("[{}] Stale element for index {} on attempt {}/3 — retrying...",
		                     ThreadContext.get("testName"), index, attempt);
		        } catch (Exception e) {
		            log.warn("[{}] Error reading filter option {} on attempt {}/3 — {}",
		                     ThreadContext.get("testName"), index, attempt, e.getMessage());
		        }

		    }

		    log.error("[{}] Failed to read filter option {} after 3 attempts",
		              ThreadContext.get("testName"), index);

		    return "";
		}


}