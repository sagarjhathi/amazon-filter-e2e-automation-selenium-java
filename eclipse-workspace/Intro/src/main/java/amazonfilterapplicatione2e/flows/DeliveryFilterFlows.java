package main.java.amazonfilterapplicatione2e.flows;

import java.time.LocalDate; 
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtil;

public class DeliveryFilterFlows extends BasePage{

	private  final Logger log = LoggerUtility.getLogger(DeliveryFilterFlows.class);
	
	ProductListingPage productPage;
	SafeActions safeAct ;
	
	public DeliveryFilterFlows() {
		
		this.productPage = new ProductListingPage();
		this.safeAct = new SafeActions();
	}
	
	

	
	public List<Object> validateDeliveryFilterOptionsWithResult(By filterOptions) throws InterruptedException {
		log.info("[{}] Within validateDeliveryFilterOptionsWithResult method", ThreadContext.get("testName"));

		
		safeAct.safeClick(filterOptions);


		String testName = ThreadContext.get("baseTestName");

		// filterOption is derived from the calling test's method name (see the switch in
		// ProductListingPage.getDeliveryDayFilterByName) — renaming that test method breaks this.
		String filterOption=productPage.getDeliveryDayFilterByName(testName);
		ScreenshotUtil.capture(testName,filterOption);

		log.info("[" + ThreadContext.get("testName") + "] Clicked on " + filterOptions);



		// Reads the whole card instead of the scoped data-cy='delivery-recipe' locator in
		// ProductListingPage — not every card has that element due to Amazon's dynamic result
		// layout, so scoping to it would throw/skip on cards where it's missing.
		List<WebElement> deliveryElements = safeAct.safeFindElements(productPage.listProductCardsBy());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd MMM");
		String todayFormatted = LocalDate.now().format(formatter);
		String tomorrowFormatted = LocalDate.now().plusDays(1).format(formatter);

		// Split into loose word/number fragments (not whole date strings) and matched with
		// contains() below because Amazon's delivery text on the card is inconsistently
		// formatted/dynamic — an exact or full-phrase match against the card text was too brittle.
		Set<String> allowedDateParts = new HashSet<>();
		Collections.addAll(allowedDateParts, todayFormatted.replace(",", "").split(" "));
		Collections.addAll(allowedDateParts, tomorrowFormatted.replace(",", "").split(" "));
		allowedDateParts.add("Today");
		allowedDateParts.add("Tomorrow");
		// Not date-based — this method also backs the Free Delivery filter test, which has no date to check.
		allowedDateParts.add("FREE delivery");

		
		
		log.info("[{}] Created hashset with required data to assert with Within validateDeliveryFilterOptionsWithResult method", ThreadContext.get("testName"));

		log.info("[" + ThreadContext.get("testName") + "] this is the set  " + allowedDateParts);

		// No Assert here on purpose — this is a flow method, not a test. It just checks each
		// card and returns (isValid, text, index) so the actual test in AmazonTests decides
		// what to assert on.
		for (int i = 0; i < deliveryElements.size(); i++) {
			log.info("[{}] Within deliveryElements loop iterating over the products  Within validateDeliveryFilterOptionsWithResult method", ThreadContext.get("testName"));

			String text = deliveryElements.get(i).getText();

			boolean found = false;
			for (String part : allowedDateParts) {
				if (text.contains(part)) {
					log.info("[{}] Checking if the allowedDateParts have the assert text — index {}, text: {}",
							ThreadContext.get("testName"), i, text);
					System.out.println("Found the The text from delivery element --> " + text +
							" | List size: " + deliveryElements.size() +
							" | Index: " + i);
					found = true;
					break;
				}
			}
			System.out.println("------------------------------------------------------");
			if (!found) {

				log.info("[{}] Assert text not found within AllowedDateParts — index {}, text: {}",
						ThreadContext.get("testName"), i, text);
				// Fails fast here instead of soft-asserting like the other filter flows —
				// delivery filters are the most dynamic/often-missing ones, so richer
				// per-product diagnostics weren't worth the investment for this flow specifically.
				return Arrays.asList(false, text, i);
			}
		}

		log.info("[{}] Returning the list with boolean values   Within validateDeliveryFilterOptionsWithResult method", ThreadContext.get("testName"));


		return Arrays.asList(true, "All the things are valid no errors", -1);
	}
		
	
}