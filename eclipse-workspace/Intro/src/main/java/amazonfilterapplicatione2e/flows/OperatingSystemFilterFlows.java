package main.java.amazonfilterapplicatione2e.flows;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtil;

public class OperatingSystemFilterFlows extends BasePage{

	
	
	private  final Logger log = LoggerUtility.getLogger(OperatingSystemFilterFlows.class);
	
	
	
	
	
    private ProductListingPage productPage;
    private GenericUtility genericUtility;

public OperatingSystemFilterFlows() {
    this.productPage = new ProductListingPage();
    this.genericUtility = new GenericUtility();
}




	
	// No Assert here on purpose — this is a flow method, not a test. It returns one
	// result Map per product (filter/title/keyFeatures/about/techDetails keys) so the
	// actual test in AmazonTests decides what to assert.
	public List<Map<String, Object>> applyOperatingSystemFilterAndValidateProductsWithResults(By filterOptionsBy, String filterName) throws InterruptedException, TimeoutException {
		log.info("[{}] Within applyOperatingSystemFilterAndValidateProductsWithResults method", ThreadContext.get("testName"));


		SafeActions safeAct = new SafeActions();
		ProductListingPage productPage = new ProductListingPage();
		GenericUtility genericUtility = new GenericUtility();

		List<WebElement> filterOptions = safeAct.safeFindElements(filterOptionsBy);


		List<Map<String, Object>> allResults = new ArrayList<>();

		// safeFindElements returns null (not an empty list) once it exhausts its own retries.
		if (filterOptions == null) {
			log.warn("[{}] No filter options found for locator -> {}", ThreadContext.get("testName"), filterOptionsBy);
			return allResults;
		}

		log.info("[{}] Within OS fucntion , this is the filterOptions size ->"+filterOptions.size(), ThreadContext.get("testName"));


		// Nightly/CRON runs use separate *CI config keys so they can scale up independently
		// of push runs (matches SharedFilterFlows) — without this, OS filter tests always ran
		// at push-run depth even on the nightly full regression.
		boolean isCron = Boolean.parseBoolean(System.getenv("IS_CRON"));

		int filterOptionSize=filterOptions.size();
		if(isCron) {
			boolean runAll= ConfigManager.getBoolean("runForAllFilterOptionsCI", false);
			if(runAll==false) {
				int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
				filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
			}
		} else {
			boolean runAll= ConfigManager.getBoolean("runForAllFilterOptions", false);
			if(runAll==false) {
				int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
				filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
			}
		}


		for (int i = 0; i <filterOptionSize; i++) {
			log.info("[{}] Within the FilterOptions loop", ThreadContext.get("testName"));

			List<WebElement> inloopParent = safeAct.safeFindElements(filterOptionsBy);
			// Amazon's filter list is re-rendered dynamically as options get clicked/expanded,
			// so the live element count can shift between fetches. Once the index we need no
			// longer exists in that live list, the list state can't be trusted for the rest of
			// this run, so we return the results gathered so far rather than continue on
			// unreliable data — same defensive choice SharedFilterFlows makes for this case.
			// safeFindElements returns null (not an empty list) once it exhausts its own retries,
			// so that counts the same as "index no longer present" here.
			if (inloopParent == null || i > inloopParent.size() - 1) {
				return allResults;
			}


			String str = safeAct.safeGetFilterOptionText(filterOptionsBy, i);


			// safeClickBooleanWithScreenShot also clicks Amazon's "See more" toggle for this
			// filter (before and after clicking the option) — expansion isn't done explicitly
			if (!safeAct.safeClickBooleanWithScreenShot(productPage.getFilterByTypeAndName(filterName, str),filterName,str)) {
				System.out.println("Filter click failed for: " + str);
				log.info("[{}] Checking if The Filter is being applied else continuing to next filter , filter option ->"+str+"  ", ThreadContext.get("testName"));
				continue;
			}

			
			String currentWindow = driver.getWindowHandle();
			List<WebElement> productNameListingPage = safeAct.safeFindElements(productPage.productNameListingPageBy());


			// Default to the real fetched list size 
			int productNameListingPageSize=productNameListingPage.size();
			if(isCron) {
				boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListingCI", false);
				if(runAllProducts==false) {
					int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");
					productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
				}
			} else {
				boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListing", false);
				if(runAllProducts==false) {
					int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");
					productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
				}
			}

			// One result per product, added inside the loop. A product that fails to open/scrape
			// still comes back as a (partial) Map, not null — see the helper's catch block — so
			// it surfaces as a visible, blank-fields assertion failure downstream in AmazonTests
			// instead of silently vanishing from the results.
			for (int productListIndex = 1; productListIndex <=productNameListingPageSize-1; productListIndex++) {
				Map<String, Object> result = applyFilterOptionsAndFetchProductDetailsForOS(productListIndex, str, currentWindow, safeAct);
				allResults.add(result);
			}

			safeAct.safeClick(productPage.clearButtonBy());
			if (i % 10 == 0 && i != 0) {
				driver.navigate().refresh();
				log.info("[{}] Refreshing the page to avoid storage issue ->"+str, ThreadContext.get("testName"));
			}
		}

		log.info("[{}] Returning the Master data 'allResults'  ->", ThreadContext.get("testName"));
		return allResults;
	}
	
	
	
	
	
	
	
	
	
	
	
	 public Map<String, Object>applyFilterOptionsAndFetchProductDetailsForOS(
			 
			 
		        int productListIndex,
		        String filterValue,
		        String currentWindow,
		        SafeActions safeAct
		        ){

		    String testName = ThreadContext.get("logFileName");
		    // productPage.getProductByIndex(int) builds a 1-based XPath position, so the caller's
		    // productListIndex is kept for element lookups; this 0-based productIndex exists only
		    // for the field-map/logging/screenshot labels below.
		    int productIndex = productListIndex - 1;

		    // filter/title/keyFeatures/about/techDetails keys — one Map per product, read by
		    // AmazonTests.verifyingOperatingSystemVersionFilterFunctionality to check whether the
		    // filter value appears in any of the 4 text fields.
		    Map<String, Object> result = new HashMap<>();
	        result.put("filter", filterValue);
	        int before = driver.getWindowHandles().size();
		    try {
				

		        WebElement productElement = driver.findElement(productPage.getProductByIndex(productListIndex));
		        genericUtility.smoothScrollToElement(productPage.getProductByIndex(productListIndex));
		       
		  
   
		        genericUtility.openClickOnNewPage(productElement,before);
		        log.info("[{}] Clicking product index={} for filter='{}'",
		                 ThreadContext.get("testName"), productListIndex, filterValue);

		        
		    	int after = driver.getWindowHandles().size();

				if (after == before) {
					System.out.println("Before click and AFTER CLICK count is same , trying again");
					safeAct.safeClick(productPage.getProductByIndex(productListIndex));
		         }
				
		     
		        genericUtility.switchToNewWindow(currentWindow);

		        String name = genericUtility.fetchTextWithRetries(productPage.productNameIndividualPageBy(), safeAct);
		        String keyFeatures = genericUtility.fetchTextWithRetries(productPage.productKeyFeatureBlockBy(), safeAct);
		        String about = genericUtility.fetchTextWithRetries(productPage.aboutThisItemBulletPointBy(), safeAct);
		        String techDetails = genericUtility.fetchTextWithRetries(productPage.technicalDetailsBlockIndividualPageBy(), safeAct);

		        genericUtility.addFieldIfPresent("title",name ,filterValue,productIndex,result);
		        genericUtility.addFieldIfPresent("keyFeatures",keyFeatures ,filterValue,productIndex,result);
		        genericUtility.addFieldIfPresent("about",about ,filterValue,productIndex,result);
		        genericUtility.addFieldIfPresent("techDetails",techDetails ,filterValue,productIndex,result);

		
		        try {
		            if (genericUtility.isElementInViewport(productPage.showMoreOnlyIndividualPageBy()) && techDetails.isEmpty()) {
		                String productNamePlusIndex = "Product Index=" + productIndex;
		                genericUtility.smoothScrollToElement(productPage.reportAnIssueBy());
		              
		                ScreenshotUtil.capture(testName, filterValue, productNamePlusIndex);
		                log.info("[{}] Took screenshot for missing tech details; show-more visible",
		                         ThreadContext.get("testName"));
		            }
		        } catch (Exception screenshotEx) {
		            log.info("[{}] Failed screenshot flow for empty tech details",
		                     ThreadContext.get("testName"));
		        }
		        return result;

		    } catch (Exception e) {
		        System.out.println("Failed to validate product at index " + productListIndex + " for filter: " + filterValue);
		        log.warn("[{}] Exception while processing product index={} filter='{}': {}",
		                 ThreadContext.get("testName"), productListIndex, filterValue, e.getMessage());
		        // Returns the partial result (matches SharedFilterFlows) instead of null — it
		        // already has "filter" set, and safeLower(null) resolves to "" downstream in
		        // AmazonTests, so this surfaces as a visible, blank-fields assertion failure
		        // rather than the product silently disappearing from the results entirely.
		        return result;
		    } finally {
		        try {
		        	genericUtility.closeCurrentWindowAndSwitchBack(currentWindow);
		        } catch (Exception ignored) {
		            log.warn("[{}] Failed to close product window or switch back", ThreadContext.get("testName"));
		        }
		    }
		    
		}
	
	
	
	
	
}