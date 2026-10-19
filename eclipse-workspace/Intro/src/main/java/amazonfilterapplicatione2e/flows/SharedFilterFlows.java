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
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtil;

public class SharedFilterFlows extends BasePage {

	
	
	private  final Logger log = LoggerUtility.getLogger(SharedFilterFlows.class);

	SafeActions safeAct;
	GenericUtility genericUtility;
	BlockerHandler blockerHandler;
	ProductListingPage productPage;
	
	public SharedFilterFlows() {
		
		this.productPage= new ProductListingPage();
		this.safeAct = new SafeActions();
		this.genericUtility = new GenericUtility();
		this.blockerHandler = new BlockerHandler();
	}
	
	
	public List<Map<String, Object>> applyFilterAndValidateProductsWithResult(By filterOptionsBy, String filterName) throws InterruptedException, TimeoutException {

		log.info("[{}] Within applyFilterAndValidateProductsWithResult method", ThreadContext.get("testName"));



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
		
		if(isCron) {

			log.info("[{}] Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData", ThreadContext.get("testName"));

			System.out.println("Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData");

			boolean runAll= ConfigManager.getBoolean("runForAllFilterOptionsCI", false);
			System.out.println("runAll is"+runAll);
			if(runAll==false) {
				int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
				filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
				System.out.println(filterOptionSize +" is the  filterOptionSize");
			}


		}else {
		
			log.info("[{}] Execution is Not CRON Job Hence refering to normal keys from UtilData", ThreadContext.get("testName"));

	    	System.out.println("Execution is Not CRON Job Hence refering to normal keys from UtilData");
			
		    boolean runAll= ConfigManager.getBoolean("runForAllFilterOptions", false);
	     	if(runAll==false) {
	     	int overideFilteOptionsCountDefault=ConfigManager.getInt("overideFilteOptionsCountDefault");
			filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", overideFilteOptionsCountDefault);
			System.out.println(filterOptionSize +" is the  filterOptionSize");
		   }
		
		
		}



		for (int filterOption = 0; filterOption <filterOptionSize; filterOption++) {

			log.info("[{}] Within filterOptions loop in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));

			List<WebElement> inloopParent = safeAct.safeFindElements(filterOptionsBy);
			if (inloopParent == null || filterOption > inloopParent.size() - 1) {
				log.info("[{}] Limiting traversal to in-loop size to prevent IndexOutOfBoundsException in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));
				System.out.println("Avoiding out of bounds issue by traversing only upto the inloop size");
				return results;
			}

			String filterValue = safeAct.safeGetFilterOptionText(filterOptionsBy, filterOption);

			if (!safeAct.safeClickBooleanWithScreenShot(productPage.getFilterByTypeAndName(filterName, filterValue),filterName,filterValue)) {
				System.out.println("Filter click failed for: " + filterValue + ". Skipping this filter option.");
				continue;
			}

			blockerHandler.clearBlockers();

			
		
			String currentWindow = driver.getWindowHandle();

			List<WebElement> productNameListingPage = safeAct.safeFindElements(productPage.productNameListingPageBy());
			int productNameListingPageSize=productNameListingPage.size();
			
			
			if(isCron) {
				log.info("[{}] Execution is scheduled type /CRON Job on CI Hence refering to CI keys from UtilData", ThreadContext.get("testName"));

				boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListingCI", false);
				if(runAllProducts==false) {
					int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");
					productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
					log.info("[{}] Limiting traversal to in-loop size to prevent IndexOutOfBoundsException in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));

				}
			}else {
				
				log.info("[{}] Execution is Not CRON Job Hence refering to normal keys from UtilData", ThreadContext.get("testName"));

				boolean runAllProducts= ConfigManager.getBoolean("runForAllProductsUnderListing", false);
				if(runAllProducts==false) {
					
					int overideProductsListingCountDefault=ConfigManager.getInt("overideProductsListingCountDefault");

					productNameListingPageSize=ConfigManager.getInt("overideProductsListingCount", overideProductsListingCountDefault);
					System.out.println(productNameListingPageSize +" is the  overideProductsListingCount");
				}
			}
			
			
			
			
	
			for (int productIndex = 1; productIndex <=productNameListingPageSize-1; productIndex++) {
				
				
			
				productNameListingPage=safeAct.safeFindElements(productPage.productNameListingPageBy());
				
				if (productIndex >= productNameListingPage.size()) {
				    break;
				}
				log.info("[{}] Within productNameListingPage loop in applyFilterAndValidateProductsWithResult", ThreadContext.get("testName"));
				System.out.println("inside the loop and product name is " + productNameListingPage.get(productIndex).getText());

				Map<String, Object> productResult = applyFilterOptionsAndFetchProductDetailsGlobal(productIndex, filterValue, currentWindow, safeAct);
				log.info("[{}]  Added 'filter','title','keyFeatures', 'about', 'techDetails' into the Map -> productResult", ThreadContext.get("testName"));

				results.add(productResult);
				log.info("[{}]  Added 'productResult' Map into the 'results' i.e list of maps ", ThreadContext.get("testName"));
				log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));

			}

			safeAct.safeClick(productPage.clearButtonBy());
			log.info("[{}] Clearing the filter value ->"+filterValue+"within productNameListingPage loop", ThreadContext.get("testName"));

			log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));
			log.info("[{}]==========================================================================================================", ThreadContext.get("testName"));

		}
		log.info("[{}] returning the 'results' i.e list of Maps", ThreadContext.get("testName"));
		return results;
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

// OLD: the page counted as "loaded" only if the title was visible AND its centre was neither off-screen
// nor covered. In a real run that strict check said "not loaded" for about 70% of product tabs, almost
// instantly (so the page was there), and the whole scrape was skipped:
// boolean productPageLoaded = genericUtility.isElementInViewport(productPage.productNameIndividualPageBy());

// NEW: the page counts as loaded when the product title is visible (reading text does not need it in view).
boolean productPageLoaded = genericUtility.isElementVisible(productPage.productNameIndividualPageBy());
if (!productPageLoaded) {
	log.warn("[{}] Product detail page did not load for index {} filter '{}'; skipping detail scrape to avoid redundant retries/timeouts", testName, productIndex, filterValue);
	// Record WHY (window size, what is on top, ...) and keep a screenshot of this tab.
	genericUtility.logElementDiagnostics(productPage.productNameIndividualPageBy(), "Product title not visible, index " + productIndex);
	ScreenshotUtil.capture(testName, filterValue, "Product page not loaded, Product Index=" + productIndex);
} else if (!genericUtility.isElementInViewport(productPage.productNameIndividualPageBy())) {
	// The title is visible but off-screen or covered. We still scrape, but record the reason.
	genericUtility.logElementDiagnostics(productPage.productNameIndividualPageBy(), "Product title visible but off-screen or covered, index " + productIndex);
	ScreenshotUtil.capture(testName, filterValue, "Product title covered or off-screen, Product Index=" + productIndex);
}

String name="";
try {
if (productPageLoaded) {
name = genericUtility.fetchTextWithRetries(productPage.productNameIndividualPageBy(), safeAct);
String keyFeatures = genericUtility.fetchTextWithRetries(productPage.productKeyFeatureBlockBy(), safeAct);
String about = genericUtility.fetchTextWithRetries(productPage.aboutThisItemBulletPointBy(), safeAct);
String techDetails = genericUtility.fetchTextWithRetries(productPage.technicalDetailsBlockIndividualPageBy(), safeAct);

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
	 if(genericUtility.isElementInViewport(productPage.showMoreOnlyIndividualPageBy())) {
     	String productNamePlusIndex="Product Index="+productIndex;
     	genericUtility.smoothScrollToElement(productPage.reportAnIssueBy());

     	ScreenshotUtil.capture(testName, filterValue, productNamePlusIndex);
			log.info("[{}] Within Try block  clicking 'show more' hence Taking screen shot available button on ui", testName);
     }else {
    	 genericUtility.smoothScrollToElement(genericUtility.seeMoreProductDetailsButtonIndividualPageBy());

         safeAct.safeClick(genericUtility.seeMoreProductDetailsButtonIndividualPageBy());
			 log.info("[{}] Within try block for clicking see more deatils within productNameListingPage loop", testName);
         System.out.println("'See More Details' clicked.");
     }
	}

} catch (Exception e1) {
    
	genericUtility.smoothScrollToElement(productPage.showMoreOnlyIndividualPageBy());
	
	
	String productNamePlusIndex="Product Name="+name+"  "+"Product Index="+productIndex;
	ScreenshotUtil.capture(testName, filterValue, productNamePlusIndex);
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