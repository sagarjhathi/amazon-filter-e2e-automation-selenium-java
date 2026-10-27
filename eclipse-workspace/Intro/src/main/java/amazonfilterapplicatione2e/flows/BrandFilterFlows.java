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
import main.java.amazonfilterapplicatione2e.pages.AmazonLandingPage;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.BrandMatcher;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;




public class BrandFilterFlows extends BasePage{

	private  final Logger log = LoggerUtility.getLogger(BrandFilterFlows.class);
	
	    private ProductListingPage productPage;
	    private SafeActions safeAct;
	    private GenericUtility genericUtility;

	    // Brands that did not show as applied after the click. The test reports them (a soft assertion).
	    public List<String> notAppliedOptions = new ArrayList<>();
	
	public BrandFilterFlows() {
        this.productPage = new ProductListingPage();
        this.safeAct = new SafeActions();
        this.genericUtility = new GenericUtility();
    }
	
	
	

	
	
	// No Assert here on purpose — this is a flow method, not a test. It returns per-brand
	// results (brand, isValid, mismatches) so the actual test in AmazonTests decides what to assert.
	public List<Map<Object, Object>> applyFilterAndValidateBrandsFilterWithResult(By filterOptionsBy, String filterName) throws InterruptedException, TimeoutException {
		notAppliedOptions = new ArrayList<>();
		log.info("[{}] Within applyFilterAndValidateBrandsFilterWithResult method", ThreadContext.get("testName"));


		log.info("[{}] Scrolling to the 'More' Button under brands filter section", ThreadContext.get("testName"));
		
		genericUtility.clickMoreButtonIfPresent(safeAct, genericUtility, productPage.seeMoreButtonUnderBrandFilter);


		log.info("[{}] Clicked the 'More' Button under brands filter section", ThreadContext.get("testName"));


	genericUtility.waitForCompleteList(filterOptionsBy);   // count the brands only once the list is complete
	List<WebElement> filterOptions = safeAct.safeFindElements(filterOptionsBy);

		// safeFindElements returns null (not an empty list) once it exhausts its own retries.
		if (filterOptions == null) {
			log.warn("[{}] No filter options found for locator -> {}", ThreadContext.get("testName"), filterOptionsBy);
			return new ArrayList<>();
		}

		int filterOptionSize=filterOptions.size();

		// Quick-run vs full-regression toggle: nightly/full runs test every brand, push/dev
		// runs cap it to a small count so feedback stays fast.
		// boolean runAll= ConfigManager.getBoolean("runForAllFilterOptions", false);
		// if(runAll==false) {
			// filterOptionSize=ConfigManager.getInt("overideFilteOptionsCount", 3);
		// }
		// OLD: the commented lines above used the shared option limit (3 brands on a push run).
		// Brands: every brand the page shows by default (key brandsFilterOptions: a number or "all"). Every product under a brand is already checked.
		filterOptionSize = ConfigManager.getLimit("brandsFilterOptions", filterOptions.size());

		// One Map per brand tested — keys "brand" (String), "isValid" (boolean), "mismatches"
		// (List<String>). AmazonTests reads these three keys to softAssert.fail() on every
		// mismatch detail for a brand instead of just the first, then calls assertAll() once.
		List<Map<Object, Object>> allResults = new ArrayList<>();

		// The address of the results page before any brand is applied. clearOption goes back to it if Clear fails.
		String listingUrl = driver.getCurrentUrl();

		for (int i = 0; i <=filterOptionSize-1; i++) {
			log.info("[{}] Within the filterOptions loop — iteration {} of {}", ThreadContext.get("testName"), i, filterOptionSize);
			
			// Re-fetched every iteration (not reused from before the loop) to avoid
			// StaleElementReferenceException — the filter list re-renders after each click.
			List<WebElement> inloopParent = safeAct.safeFindElements(filterOptionsBy);




			// filterOptionSize was measured once, up front; the live list can be shorter now
			// (layout/DOM changes between iterations), so this guards get(i) below from IndexOutOfBounds.
			// safeFindElements returns null (not an empty list) once it exhausts its own retries.
			if (inloopParent == null || i > inloopParent.size() - 1) {
				log.info("[{}] Avoiding out of bounds — index {} exceeds live list size {}", ThreadContext.get("testName"), i, inloopParent == null ? 0 : inloopParent.size());
				continue;
			}



			// "More" can re-collapse or need re-triggering after a filter click, so this is
			// called again here, not just once at the top of the method.
			genericUtility.clickMoreButtonIfPresent(safeAct, genericUtility, productPage.seeMoreButtonUnderBrandFilter);

			inloopParent = safeAct.safeFindElements(filterOptionsBy);

			// Re-checked here too — the first bounds check above ran against the list before
			// this second fetch, which is the one actually indexed into below.
			if (inloopParent == null || i > inloopParent.size() - 1) {
				log.info("[{}] Avoiding out of bounds — index {} exceeds live list size {}", ThreadContext.get("testName"), i, inloopParent == null ? 0 : inloopParent.size());
				continue;
			}

			String str = inloopParent.get(i).getText().trim();
			// Soft-skip on click failure — one flaky/stale brand checkbox shouldn't fail the whole loop.
			// if (!safeAct.safeClickBooleanWithScreenShot(productPage.getfilterByTypeAndName(filterName, str),filterName,str)) {
				// System.out.println("Filter click failed for: " + str);
				// log.info("[{}] Checking if The Filter is being applied else continuing to next filter , filter option ->"+str+"  ", ThreadContext.get("testName"));
				// continue;
			// }
			// OLD: the commented lines above clicked once and did not check that the brand was selected.
			// Click, check it shows as selected (reload once and click again if it does not): see GenericUtility.applyOption.
			String notApplied = genericUtility.applyOption(safeAct, filterName, str, listingUrl);
			if (notApplied != null) {
				log.error("[{}] Brand filter '{}': {}", ThreadContext.get("testName"), filterName, notApplied);
				notAppliedOptions.add(notApplied);
				continue;
			}

			log.info("[" + ThreadContext.get("testName") + "] Clicked on " + str+"Within the filterOptions loop");

			

			List<WebElement> productNameListingPage = safeAct.safeFindElements(productPage.productNameListingPageBy);
			if (productNameListingPage == null) {
				productNameListingPage = new ArrayList<>();
			}

			boolean isValid = true;
			// Product-level detail nested under this one brand (filter) — one entry per
			// mismatched product, not per brand, so AmazonTests can softAssert.fail() each
			// mismatched product individually instead of lumping them into one message per brand.
			List<String> mismatchDetails = new ArrayList<>();

			// Zero products found is not a vacuous pass — it usually means the filter click
			// didn't actually apply or the page didn't finish loading, so treat it as a mismatch
			// instead of silently leaving isValid = true with nothing checked.
			if (productNameListingPage.isEmpty()) {
				isValid = false;
				mismatchDetails.add("❌ No products found for brand '" + str + "' — filter may not have applied correctly");
			} else {
				for (int k = 0; k < productNameListingPage.size(); k++) {

					String title = productNameListingPage.get(k).getText();

					// OLD: boolean titleMatches = title.toLowerCase().contains(str.toLowerCase());   (substring match, no aliases)
					// The brand must be a whole word in the title, or one of the brand's own aliases (see BrandMatcher).
					boolean titleMatches = BrandMatcher.titleHasBrand(str, title);
					log.info("[{}] Product index {} for brand '{}' — title: '{}', matches: {}",
							ThreadContext.get("testName"), k, str, title, titleMatches);

					if (!titleMatches) {
						isValid = false;
						mismatchDetails.add("❌ Index: " + k + ", Brand: " + str + ", Title: '" + title + "'");
						log.warn("[{}] Brand mismatch — index {}, brand '{}', title: '{}'",
								ThreadContext.get("testName"), k, str, title);
					}
				}
			}

			Map<Object, Object> result = new HashMap<>();
			result.put("brand", str);
			result.put("isValid", isValid);
			result.put("mismatches", mismatchDetails);
			log.info("[{}] Result for brand '{}': isValid={}, mismatches={}",
					ThreadContext.get("testName"), str, isValid, mismatchDetails.size());

			allResults.add(result);
			log.info("[{}] allResults now has {} brand(s) recorded", ThreadContext.get("testName"), allResults.size());

			// Two ways to reset state for the next brand: prefer clicking "clear", but if
			// that button isn't there (layout changed), fall back to navigating back instead.
			// try {
				// if (genericUtility.isElementInViewport(productPage.clearButtonBy)) {
					// safeAct.safeClick(productPage.clearButtonBy);
					// log.info("[" + ThreadContext.get("testName") + "] Clicked clear button for the filter ->"+str);

				// }
			// } catch (Exception e) {
				// driver.navigate().back();
				// log.info("[" + ThreadContext.get("testName") + "] Cannot click clear button hence navigating back to fresh page for filter->"+str);

			// }
			// OLD: the commented block above clicked Clear, or went back one page when that failed.
			// Now: clear, confirm the brand is gone, and fall back to the stored results page address.
			genericUtility.clearOption(safeAct, productPage.getAppliedfilterByTypeAndName(filterName, str), listingUrl);

			// Periodic refresh to mitigate browser memory/storage buildup over a long brand loop.
			if (i % 10 == 0 && i != 0) {
				driver.navigate().refresh();
				log.info("[" + ThreadContext.get("testName") + "] Refreshing the page after 10 products to avoid storage issues for filter->"+str);

			}
		}
		log.info("[" + ThreadContext.get("testName") + "] Returning the allResults List of Maps containing the data");

		return allResults;
	}
}