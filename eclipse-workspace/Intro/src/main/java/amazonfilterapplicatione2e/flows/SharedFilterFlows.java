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
import main.java.amazonfilterapplicatione2e.captcha.BlockerHandler;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtilUpdated;

public class SharedFilterFlows extends BasePage {

	
	
	private  final Logger log = LoggerUtility.getLogger(SharedFilterFlows.class);

	SafeActions safeAct;
	GenericUtility genericUtility;
	BlockerHandler blockerHandler;
	ProductListingPage productPage;

	// Filter options that did not show as applied after the click. The tests report them (a soft assertion).
	public List<String> notAppliedOptions = new ArrayList<>();
	
	public SharedFilterFlows() {
		
		this.productPage= new ProductListingPage();
		this.safeAct = new SafeActions();
		this.genericUtility = new GenericUtility();
		this.blockerHandler = new BlockerHandler();
	}
	
	
	// The usual call: opens every product page and reads its details.
	public List<Map<String, Object>> applyFilterAndValidateProductsWithResult(By filterOptionsBy, String filterName) throws InterruptedException, TimeoutException {
		return applyFilterAndValidateProductsWithResult(filterOptionsBy, filterName, false);
	}

	// readListingOnly = true reads only the card in the results list (used by the Discount test) and opens no product page.
	// OLD signature: applyFilterAndValidateProductsWithResult(By filterOptionsBy, String filterName)
	public List<Map<String, Object>> applyFilterAndValidateProductsWithResult(By filterOptionsBy, String filterName, boolean readListingOnly) throws InterruptedException, TimeoutException {

		log.info("[{}] Within applyFilterAndValidateProductsWithResult method", ThreadContext.get("testName"));

		notAppliedOptions = new ArrayList<>();



		genericUtility.waitForCompleteList(filterOptionsBy);   // count the options only once the list is complete
		List<WebElement> filterOptions = safeAct.safeFindElements(filterOptionsBy);
		List<Map<String, Object>> results = new ArrayList<>();

		// safeFindElements returns null (not an empty list) once it exhausts its own retries.
		if (filterOptions == null) {
			log.warn("[{}] No filter options found for locator -> {}", ThreadContext.get("testName"), filterOptionsBy);
			return results;
		}

		log.info("[{}] Within applyFilterAndValidateProductsWithResult filterOptions size is -> "+filterOptions.size(), ThreadContext.get("testName"));

		
		boolean isCron = Boolean.parseBoolean(System.getenv("IS_CRON"));
		
		
		int filterOptionSize=filterOptions.size();
		
		// if(isCron) {

			// log.info("[{}] Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData", ThreadContext.get("testName"));

			// System.out.println("Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData");

			// boolean runAll= ConfigManager.getBoolean("runForAllFilterOptionsCI", false);
			// System.out.println("runAll is"+runAll);
			// if(runAll==false) {
				// int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
				// filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
				// System.out.println(filterOptionSize +" is the  filterOptionSize");
			// }


		// }else {
		
			// log.info("[{}] Execution is Not CRON Job Hence refering to normal keys from UtilData", ThreadContext.get("testName"));

	    	// System.out.println("Execution is Not CRON Job Hence refering to normal keys from UtilData");
			
		    // boolean runAll= ConfigManager.getBoolean("runForAllFilterOptions", false);
	     	// if(runAll==false) {
	     	// int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
			// filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
			// System.out.println(filterOptionSize +" is the  filterOptionSize");
		   // }
		
		
		// }
		// OLD: the commented if/else above read the *CI or normal keys and did not know "all" or the page size.
		// How many filter options to run: a number or "all", never more than the page shows (see ConfigManager.getLimit).
		// Discount (readListingOnly) has its own keys.
		if (readListingOnly) {
			filterOptionSize = ConfigManager.getLimit("discountFilterOptions", filterOptions.size());
		} else if (isCron) {
			filterOptionSize = ConfigManager.getLimit("runForAllFilterOptionsCI", "overideFilteOptionsCount", filterOptions.size());
		} else {
			filterOptionSize = ConfigManager.getLimit("runForAllFilterOptions", "overideFilteOptionsCount", filterOptions.size());
		}



		// The address of the results page before any option is applied. clearOption goes back to it if Clear fails.
		String listingUrl = driver.getCurrentUrl();

		for (int filterOption = 0; filterOption <filterOptionSize; filterOption++) {

			log.info("[{}] Within filterOptions loop in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));

			List<WebElement> inloopParent = safeAct.safeFindElements(filterOptionsBy);
			if (inloopParent == null || filterOption > inloopParent.size() - 1) {
				log.info("[{}] Limiting traversal to in-loop size to prevent IndexOutOfBoundsException in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));
				System.out.println("Avoiding out of bounds issue by traversing only upto the inloop size");
				return results;
			}

			String filterValue = safeAct.safeGetFilterOptionText(filterOptionsBy, filterOption);

			// A hidden option (for example one under "See more") reads as blank text. There is nothing to click or check,
			// and trying it only costs a reload and a second click, so skip it.
			if (filterValue == null || filterValue.trim().isEmpty()) {
				log.info("[{}] Skipping an option with blank text (index {}): it is hidden, so it cannot be applied", ThreadContext.get("testName"), filterOption);
				continue;
			}

			// if (!safeAct.safeClickBooleanWithScreenShot(productPage.getfilterByTypeAndName(filterName, filterValue),filterName,filterValue)) {
				// System.out.println("Filter click failed for: " + filterValue + ". Skipping this filter option.");
				// continue;
			// }

			// blockerHandler.clearBlockers();

			// // The option must show as selected (bold). If it does not, the products below are not filtered,
			// // so checking them proves nothing: record it, skip this option, and carry on with the next one.
			// if (!genericUtility.isElementVisible(productPage.getAppliedfilterByTypeAndName(filterName, filterValue))) {
				// log.error("[{}] Filter '{}' = '{}' does not show as applied after the click", ThreadContext.get("testName"), filterName, filterValue);
				// notAppliedOptions.add(filterValue);
				// continue;
			// }
			// OLD: the commented lines above clicked once and gave up on the option when it did not show as selected.
			// Click, check it shows as selected (reload once and click again if it does not): see GenericUtility.applyOption.
			// If it still does not, the products below are not filtered, so record the reason, skip this option, and go on.
			String notApplied = genericUtility.applyOption(safeAct, filterName, filterValue, listingUrl);
			blockerHandler.clearBlockers();
			if (notApplied != null) {
				log.error("[{}] Filter '{}': {}", ThreadContext.get("testName"), filterName, notApplied);
				notAppliedOptions.add(notApplied);
				continue;
			}

			
		
			String currentWindow = driver.getWindowHandle();

			genericUtility.waitForCompleteList(productPage.productNameListingPageBy);   // count the products only once the list is complete
			List<WebElement> productNameListingPage = safeAct.safeFindElements(productPage.productNameListingPageBy);
			int productNameListingPageSize=productNameListingPage.size();
			
			
			// if(isCron) {
				// log.info("[{}] Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData", ThreadContext.get("testName"));

				// boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListingCI", false);
				// if(runAllProducts==false) {
					// int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");
					// productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					// System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
					// log.info("[{}] Limiting traversal to in-loop size to prevent IndexOutOfBoundsException in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));

				// }
			// }else {
				
				// log.info("[{}] Execution is Not CRON Job Hence refering to normal keys from UtilData", ThreadContext.get("testName"));

				// boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListing", false);
				// if(runAllProducts==false) {
					
					// int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");

					// productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					// System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
				// }
			// }
			// OLD: the commented if/else above did not know "all" or the page size.
			// How many products (Discount: cards) to read per option. Discount has its own key.
			if (readListingOnly) {
				productNameListingPageSize = ConfigManager.getLimit("discountCardsPerOption", productNameListingPageSize);
			} else if (isCron) {
				productNameListingPageSize = ConfigManager.getLimit("runForAllProductsUnderListingCI", "overideProductsListingCount", productNameListingPageSize);
			} else {
				productNameListingPageSize = ConfigManager.getLimit("runForAllProductsUnderListing", "overideProductsListingCount", productNameListingPageSize);
			}
			
			
			
			
	
			for (int productIndex = 1; productIndex <=productNameListingPageSize-1; productIndex++) {
				
				
			
				productNameListingPage=safeAct.safeFindElements(productPage.productNameListingPageBy);
				
				if (productIndex >= productNameListingPage.size()) {
				    break;
				}
				log.info("[{}] Within productNameListingPage loop in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));
				System.out.println("inside the loop and product name is " + productNameListingPage.get(productIndex).getText());

				// OLD: Map<String, Object> productResult = applyFilterOptionsAndFetchProductDetailsGlobal(productIndex, filterValue, currentWindow, safeAct);
				Map<String, Object> productResult = readListingOnly
						? readListingDiscount(productIndex, filterValue)
						: applyFilterOptionsAndFetchProductDetailsGlobal(productIndex, filterValue, currentWindow, safeAct);
				log.info("[{}]  Added 'filter','title','keyFeatures', 'about', 'techDetails' into the Map -> productResult", ThreadContext.get("testName"));

				results.add(productResult);
				log.info("[{}]  Added 'productResult' Map into the 'results' i.e list of maps ", ThreadContext.get("testName"));
				log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));

			}

			// OLD: safeAct.safeClick(productPage.clearButtonBy);
			genericUtility.clearOption(safeAct, productPage.getAppliedfilterByTypeAndName(filterName, filterValue), listingUrl);
			log.info("[{}] Clearing the filter value ->"+filterValue+"within productNameListingPage loop", ThreadContext.get("testName"));

			log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));
			log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));

		}
		log.info("[{}] returning the 'results' i.e list of Maps", ThreadContext.get("testName"));
		return results;
}

	
	
	
	// Reads the discount shown on a product's card in the results list, for example "(44% off)" -> 44.
	// "discount" stays null when the card shows none. The product page is not opened.
	public Map<String, Object> readListingDiscount(int productIndex, String filterValue) {
		Map<String, Object> productResult = new HashMap<>();
		productResult.put("filter", filterValue);
		productResult.put("title", "");
		productResult.put("discount", null);

		try {
			WebElement title = driver.findElement(productPage.getProductByIndex(productIndex));
			WebElement card = title.findElement(By.xpath("./ancestor::div[@data-component-type='s-search-result']"));
			String cardText = card.getText().replaceAll("\\s+", " ");

			java.util.regex.Matcher percent = java.util.regex.Pattern.compile("(\\d+)% off").matcher(cardText);
			productResult.put("title", title.getText().trim());
			productResult.put("cardText", cardText);   // kept so a surprising number can be traced to the card text
			if (percent.find()) {
				productResult.put("discount", Integer.parseInt(percent.group(1)));
			}
		} catch (Exception e) {
			log.warn("[{}] Could not read the card of product {} for filter '{}': {}", ThreadContext.get("testName"), productIndex, filterValue, e.getMessage());
		}
		return productResult;
	}


	public Map<String, Object>applyFilterOptionsAndFetchProductDetailsGlobal(
			 
		    int productIndex,
		    String filterValue,
	        String currentWindow,
	        SafeActions safeAct

		 ) throws InterruptedException, TimeoutException{
 

	 String testName = ThreadContext.get("logFileName");
	 int beforeProductClick = driver.getWindowHandles().size();
	
	 
	 Map<String, Object> productResult = new HashMap<>();
	 productResult.put("filter", filterValue);
	 
	try {
		
		WebElement productElement = safeAct.safeFindElement(productPage.getProductByIndex(productIndex));
		genericUtility.smoothScrollToElement(productPage.getProductByIndex(productIndex));
		log.info("[{}] Getting product by index in productNameListingPage loop", testName);
		
	
		boolean isWindowOpened=genericUtility.openClickOnNewPage(productElement,beforeProductClick);

		System.out.println("Product clicked with Ctrl+Click to open in new tab.");
		log.info("[{}] Opened product in new tab via Ctrl+Click inside productNameListingPage loop", testName);

		if(isWindowOpened) {
			genericUtility.waitForNewWindowAndSwitch(currentWindow,beforeProductClick);
			log.info("[{}] Swithcing to the new window  within productNameListingPage loop", testName);
		}else {
			 log.warn("[{}] Could not open product in a new tab", testName);
		}
} catch (Exception e) {
	log.info("[{}] Failed to Ctrl+Click product index " + productIndex+"  for filter value->"+filterValue, testName);
    System.out.println("Failed to Ctrl+Click product index " + productIndex);
}

System.out.println("Clicked on the producct name new pop-up should open");
log.info("[{}] Clicked product name to open in new tab from productNameListingPage loop", testName);

// If the click didn't open a new tab, it may have navigated in the same tab (fine, keep going)
// or done nothing at all (still on the listing page). This tells the two apart before we spend
// time on the retry-heavy scrape/click calls below, which would otherwise just time out.
try {
	blockerHandler.clearBlockers();
} catch (Exception e) {
	log.warn("[{}] Blocker on product page did not clear: {}", testName, e.getMessage());
}

// OLD: boolean productPageLoaded = genericUtility.isElementInViewport(productPage.productNameIndividualPage);
// (said "not loaded" for about half of the loaded pages: a wrapped title is covered by its parent at the centre point)
boolean productPageLoaded = genericUtility.isElementVisible(productPage.productNameIndividualPage);
if (!productPageLoaded) {
	log.warn("[{}] Product detail page did not load for index {} filter '{}'; skipping detail scrape to avoid redundant retries/timeouts", testName, productIndex, filterValue);
}

// Wait for the parts of the product page, then read only the parts that exist (a missing part used to cost a long wait
// and a page refresh inside fetchTextWithRetries).
boolean[] sectionPresent = productPageLoaded ? genericUtility.waitForProductSections() : new boolean[4];
String name="";
try {
if (productPageLoaded) {
name = genericUtility.fetchTextWithRetries(productPage.productNameIndividualPage, safeAct);
// OLD: String keyFeatures = genericUtility.fetchTextWithRetries(productPage.productKeyFeatureBlock, safeAct);
String keyFeatures = sectionPresent[1] ? genericUtility.fetchTextWithRetries(productPage.productKeyFeatureBlock, safeAct) : "";
// OLD: String about = genericUtility.fetchTextWithRetries(productPage.aboutThisItemBulletPoint, safeAct);
String about = sectionPresent[2] ? genericUtility.fetchTextWithRetries(productPage.aboutThisItemBulletPoint, safeAct) : "";
// OLD: String techDetails = genericUtility.fetchTextWithRetries(productPage.technicalDetailsBlockIndividualPage, safeAct);
String techDetails = sectionPresent[3] ? genericUtility.fetchTextWithRetries(productPage.technicalDetailsBlockIndividualPage, safeAct) : "";

genericUtility.addFieldIfPresent("title",name ,filterValue,productIndex,productResult);
genericUtility.addFieldIfPresent("keyFeatures",keyFeatures ,filterValue,productIndex,productResult);
genericUtility.addFieldIfPresent("about",about ,filterValue,productIndex,productResult);
genericUtility.addFieldIfPresent("techDetails",techDetails ,filterValue,productIndex,productResult);
log.info("[{}] Extracting 'name' , 'keyFeatures', 'about' , 'techDetails' within productNameListingPage loop", testName);
}

}catch(Exception e) {
	log.info("[{}] Something went wrong while switching or extracting the details", testName);

}


try {
	if (productPageLoaded) {
	 if(genericUtility.isElementInViewport(productPage.showMoreOnlyIndividualPage)) {
     	String productNamePlusIndex="Product Index="+productIndex;
     	genericUtility.smoothScrollToElement(productPage.reportAnIssue);

     	ScreenshotUtilUpdated.capture(testName, filterValue, productNamePlusIndex);
			log.info("[{}] Within Try block  clicking 'show more' hence Taking screen shot available button on ui", testName);
     }else {
    	 genericUtility.smoothScrollToElement(genericUtility.seeMoreProductDetailsButtonIndividualPageBy);

         safeAct.safeClick(genericUtility.seeMoreProductDetailsButtonIndividualPageBy);
			 log.info("[{}] Within try block for clicking see more deatils within productNameListingPage loop", testName);
         System.out.println("'See More Details' clicked.");
     }
	}

} catch (Exception e1) {
    
	genericUtility.smoothScrollToElement(productPage.showMoreOnlyIndividualPage);
	
	
	String productNamePlusIndex="Product Name="+name+"  "+"Product Index="+productIndex;
	ScreenshotUtilUpdated.capture(testName, filterValue, productNamePlusIndex);
	log.info("[{}] Within catch block Cannot click 'see more details' hence Taking screen shot available button on ui", testName);

   
	log.info("[{}] Within catch block for clicking 'see more deatils' within productNameListingPage loop", testName);
 
}finally {
	// Only close/switch if we actually left the listing window; closeCurrentWindowAndSwitchBack
	// closes every handle that isn't currentWindow, so calling it while already back on
	// currentWindow (e.g. click never navigated) would otherwise close all other open windows.
	if(!driver.getWindowHandle().equals(currentWindow)){
	genericUtility.closeCurrentWindowAndSwitchBack(currentWindow);
	log.info("[{}]  going back to product listing via closeCurrentWindowAndSwitchBack ", testName);
	}else {
		log.info("[{}]  Already on the original window -> {}; nothing to close", testName, currentWindow);
	}
}


 return  productResult;


 }


}