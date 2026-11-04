package main.java.amazonfilterapplicatione2e.flows;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.captcha.BlockerHandler;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtilUpdated;

// Opens the product pages of the cards of a listing, one at a time, and reads one thing from each. There is one method per thing:
// each waits only for what it reads, and has its own fallback when that is not there. When what it reads is missing, a screenshot
// of the page is taken.
// The listing tab is never touched: every product opens in its own tab, and that tab is always closed.
// A page that is odd never throws: its facts come back with "problem" filled in. The one exception is a blocked page (503, captcha,
// "Continue shopping"): BlockerHandler skips the test, and nothing below catches that skip.
public class ProductPageFlows extends BasePage {

	private final Logger log = LoggerUtility.getLogger(ProductPageFlows.class);

	private final ProductListingPage productPage = new ProductListingPage();
	private final BlockerHandler blockerHandler = new BlockerHandler();

	// Every read says which product it was, and whether its page could be read: problem is "" when it was, otherwise why not
	// (the other facts are then empty).
	public static class Visit {
		public String asin = "";
		public String title = "";
		public String problem = "";
	}

	public static class SellerFacts extends Visit {
		public String seller = "";                                // the seller of the main offer, "" when the buy box shows none
		public List<String> offerSellers = new ArrayList<>();     // the sellers of all the offers, read only when the main offer is not the expected seller
	}

	public static class BadgeFacts extends Visit {
		public String badge = "";            // the text of the card (Top Brand, Pay on Delivery), "" when the page has none
		public boolean cannotShip = false;   // the page says the product cannot be shipped to the chosen location, so a missing card says nothing about the filter
	}

	public static class RatingFacts extends Visit {
		public String rating = "";           // for example "4.2 out of 5 stars", "" when the page shows none
	}

	// What one kind of page read does, once the product page is open.
	private interface PageReader<T extends Visit> {
		void read(T facts);
	}


	// ---------------------------------------------------------------------------------------------
	// THE FLOWS: ONE PER THING READ
	// ---------------------------------------------------------------------------------------------

	// The seller of each product. Fallback: when the main offer is missing or is not the expected seller, the sellers of all the offers are read.
	public List<SellerFacts> readSellers(List<Card> cards, int limit, Predicate<String> isExpectedSeller) {
		return visitProducts(cards, limit, SellerFacts::new, facts -> {
			waitUntilPresent(productPage.pageSellerBy, 10);   // the buy box loads after the page
			facts.seller = textOf(productPage.pageSellerBy);

			boolean mainOfferIsExpected = !facts.seller.isEmpty() && isExpectedSeller.test(facts.seller);
			facts.offerSellers = mainOfferIsExpected ? new ArrayList<>() : readSellersOfAllOffers(facts.asin);
			log.info("[{}] Product {}: buy box seller '{}', offers' sellers {}", ThreadContext.get("testName"), facts.asin,
					facts.seller.isEmpty() ? "none" : facts.seller, facts.offerSellers);

			if (!mainOfferIsExpected && facts.offerSellers.stream().noneMatch(isExpectedSeller)) {
				capture("expected seller not found", facts.asin);
			}
		});
	}

	// The "Top Brand" card of each product. Fallback: when it is not there, the page is reloaded once.
	public List<BadgeFacts> readTopBrandBadges(List<Card> cards, int limit) {
		return visitProducts(cards, limit, BadgeFacts::new, facts -> {
			readBadge(facts, productPage.pageTopBrandBadgeBy);
			log.info("[{}] Product {}: Top Brand card '{}'{}", ThreadContext.get("testName"), facts.asin,
					facts.badge.isEmpty() ? "none" : facts.badge, facts.cannotShip ? " (cannot be shipped to the chosen location)" : "");
			if (facts.badge.isEmpty()) {
				capture(facts.cannotShip ? "cannot be shipped here" : "no Top Brand card", facts.asin);
			}
		});
	}

	// The "Pay on Delivery" card of each product. Fallback: when it is not there, the page is reloaded once.
	public List<BadgeFacts> readPayOnDeliveryBadges(List<Card> cards, int limit) {
		return visitProducts(cards, limit, BadgeFacts::new, facts -> {
			readBadge(facts, productPage.pagePayOnDeliveryBadgeBy);
			log.info("[{}] Product {}: Pay on Delivery card '{}'{}", ThreadContext.get("testName"), facts.asin,
					facts.badge.isEmpty() ? "none" : facts.badge, facts.cannotShip ? " (cannot be shipped to the chosen location)" : "");
			if (facts.badge.isEmpty()) {
				capture(facts.cannotShip ? "cannot be shipped here" : "no Pay on Delivery card", facts.asin);
			}
		});
	}

	// The star rating of each product page. Fallback: when it is not there, the page is reloaded once.
	public List<RatingFacts> readRatings(List<Card> cards, int limit) {
		return visitProducts(cards, limit, RatingFacts::new, facts -> {
			if (waitForElement(productPage.pageRatingBy)) {
				String title = driver.findElement(productPage.pageRatingBy).getAttribute("title");
				facts.rating = title == null ? "" : collapse(title);
			}
			log.info("[{}] Product {}: rating '{}'", ThreadContext.get("testName"), facts.asin, facts.rating.isEmpty() ? "none" : facts.rating);
			if (facts.rating.isEmpty()) {
				capture("no rating", facts.asin);
			}
		});
	}


	// ---------------------------------------------------------------------------------------------
	// STEPS
	// ---------------------------------------------------------------------------------------------

	// Opens the first `limit` products of the cards, each ASIN once, with a pause between pages, and reads each with `reader`.
	private <T extends Visit> List<T> visitProducts(List<Card> cards, int limit, Supplier<T> newFacts, PageReader<T> reader) {
		Set<String> asins = new LinkedHashSet<>();
		for (Card card : cards) {
			if (!card.asin.isEmpty()) {
				asins.add(card.asin);
			}
		}
		List<String> toOpen = new ArrayList<>(asins);
		if (toOpen.size() > limit) {
			toOpen = toOpen.subList(0, limit);
		}

		List<T> reads = new ArrayList<>();
		log.info("[{}] PRODUCT PAGES: {} to open, of {} card(s) given (the limit is {})", ThreadContext.get("testName"), toOpen.size(), cards.size(), limit);
		for (int index = 0; index < toOpen.size(); index++) {
			String asin = toOpen.get(index);
			T facts = newFacts.get();
			facts.asin = asin;
			facts.title = titleOf(cards, asin);
			log.info("[{}] Product {} of {}: {} '{}'", ThreadContext.get("testName"), index + 1, toOpen.size(), asin, shorten(facts.title, 70));

			visitProduct(facts, reader);
			reads.add(facts);
			pause(ConfigManager.getInt("pageDelayMs", 2000));
		}
		return reads;
	}

	private <T extends Visit> void visitProduct(T facts, PageReader<T> reader) {
		String listingTab = driver.getWindowHandle();
		driver.switchTo().newWindow(WindowType.TAB);
		try {
			// 1. Open the page. One more try when it does not load.
			String loadProblem = openProduct(facts.asin);
			if (!loadProblem.isEmpty()) {
				log.warn("[{}] Product {}: {}", ThreadContext.get("testName"), facts.asin, loadProblem);
				facts.problem = loadProblem;
				return;
			}

			// 2. A blocked page skips the test. This is outside the try block below, which would swallow nothing but a page error.
			blockerHandler.clearBlockers();
			log.info("[{}] Product {}: the page loaded", ThreadContext.get("testName"), facts.asin);

			// 3. It must be a product page. A page that has not loaded has no title, and would be judged as a page that lacks what the test looks for.
			if (!waitUntilPresent(productPage.productNameIndividualPage, 10)) {
				facts.problem = "the page is not a product page (no title within 10s)";
				log.warn("[{}] Product {}: {}", ThreadContext.get("testName"), facts.asin, facts.problem);
				capture("page not read", facts.asin);
				return;
			}
			log.info("[{}] Product {}: it is a product page (its title is there); reading it", ThreadContext.get("testName"), facts.asin);

			// 4. Read it. Whatever goes wrong with the page is reported on the product, not thrown.
			try {
				reader.read(facts);
			} catch (WebDriverException e) {
				facts.problem = "reading the page failed: " + firstLine(e.getMessage());
				log.warn("[{}] Product {}: {}", ThreadContext.get("testName"), facts.asin, facts.problem);
				capture("page not read", facts.asin);
			}
		} finally {
			closeTabAndGoBack(listingTab);
		}
	}

	// Loads the product page, with one more try. Returns "" when it loaded, otherwise the reason.
	private String openProduct(String asin) {
		String problem = "";
		for (int attempt = 1; attempt <= 2; attempt++) {
			try {
				driver.get(ConfigManager.get("Url") + "dp/" + asin);
				return "";
			} catch (WebDriverException e) {
				problem = "the page did not load (attempt " + attempt + "): " + firstLine(e.getMessage());
			}
		}
		return problem;
	}

	// Always closes the product tab and returns to the listing tab.
	private void closeTabAndGoBack(String listingTab) {
		try {
			driver.close();
		} catch (WebDriverException e) {
			log.warn("[{}] The product tab could not be closed: {}", ThreadContext.get("testName"), firstLine(e.getMessage()));
		}
		// Back to the listing tab. If the browser died meanwhile, say so clearly instead of a bare driver error.
		try {
			driver.switchTo().window(listingTab);
		} catch (WebDriverException e) {
			throw new IllegalStateException("Could not go back to the results tab after reading a product page (the browser may have closed): " + firstLine(e.getMessage()), e);
		}
	}

	// The text of a card on the product page, "" when it is not there, and whether the page says the product cannot be shipped to
	// the chosen location (then there is no card to wait for). Otherwise the card is waited for, with the reload fallback of waitForElement.
	private void readBadge(BadgeFacts facts, By card) {
		if (isShown(productPage.pageCannotShipBy)) {
			facts.badge = textOf(card);
			facts.cannotShip = true;
			return;
		}
		facts.badge = waitForElement(card) ? textOf(card) : "";
		facts.cannotShip = facts.badge.isEmpty() && isShown(productPage.pageCannotShipBy);
	}

	// Waits (5 s) for the element to be in the page. When it is not, reloads the page once and waits again. True when it showed up.
	private boolean waitForElement(By element) {
		for (int attempt = 1; attempt <= 2; attempt++) {
			if (waitUntilPresent(element, 5)) {
				return true;
			}
			if (attempt == 1) {
				log.info("[{}] The element was not in the page after 5s; reloading the page once and waiting again", ThreadContext.get("testName"));
				driver.navigate().refresh();
				blockerHandler.clearBlockers();   // a blocked page skips the test, instead of counting as a page without the element
			}
		}
		return false;
	}

	// Opens the panel with all the offers of a product page and returns the seller of each offer (each name once).
	// The panel is opened with "See All Buying Options" (no buy box seller) or the "New (n) from" link (a buy box seller).
	// When the page has neither, the same product is loaded with ?aod=1, which opens the panel by itself. Returns [] if it cannot be read.
	private List<String> readSellersOfAllOffers(String asin) {
		log.info("[{}] Product {}: the main offer is not from the expected seller, so the sellers of all the offers are read", ThreadContext.get("testName"), asin);
		try {
			List<WebElement> openOffers = driver.findElements(productPage.pageSeeAllBuyingOptionsBy);
			if (openOffers.isEmpty()) {
				openOffers = driver.findElements(productPage.pageOtherOffersLinkBy);
			}
			if (!openOffers.isEmpty() && openOffers.get(0).isDisplayed()) {
				openOffers.get(0).click();
			} else {
				driver.get(ConfigManager.get("Url") + "dp/" + asin + "?aod=1");
			}

			// The seller names are in the page about 2 s after the click, but the panel keeps them in a collapsed part, so wait for them
			// to be in the page, not visible. Nothing in the panel needs to be clicked.
			new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(productPage.pageOfferSellersBy));
			pause(1000);

			// The panel lists about 12 offers and loads more as it is scrolled. Scroll to its end until no more appear.
			for (int round = 0; round < 5; round++) {
				List<WebElement> before = driver.findElements(productPage.pageOfferSellersBy);
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'end'});", before.get(before.size() - 1));
				pause(1000);
				if (driver.findElements(productPage.pageOfferSellersBy).size() == before.size()) {
					break;
				}
			}

			Set<String> names = new LinkedHashSet<>();
			for (WebElement seller : driver.findElements(productPage.pageOfferSellersBy)) {
				String name = collapse(seller.getAttribute("textContent"));
				if (!name.isEmpty()) {
					names.add(name);
				}
			}
			log.info("[{}] Product {}: the offers panel lists {} seller(s)", ThreadContext.get("testName"), asin, names.size());
			return new ArrayList<>(names);
		} catch (WebDriverException e) {
			log.warn("[{}] Product {}: could not read the sellers of all offers: {}", ThreadContext.get("testName"), asin, firstLine(e.getMessage()));
			return new ArrayList<>();
		}
	}


	// ---------------------------------------------------------------------------------------------
	// SMALL HELPERS
	// ---------------------------------------------------------------------------------------------

	// True when the element is in the page within the given seconds.
	private boolean waitUntilPresent(By element, int seconds) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.presenceOfElementLocated(element));
			return true;
		} catch (WebDriverException e) {
			return false;
		}
	}

	private boolean isShown(By element) {
		List<WebElement> found = driver.findElements(element);
		return !found.isEmpty() && found.get(0).isDisplayed();
	}

	// The text of the first element, "" when it is not there. Hidden text comes from textContent.
	private String textOf(By element) {
		List<WebElement> found = driver.findElements(element);
		if (found.isEmpty()) {
			return "";
		}
		String text = found.get(0).getText();
		if (text == null || text.trim().isEmpty()) {
			text = found.get(0).getAttribute("textContent");
		}
		return collapse(text);
	}

	private String titleOf(List<Card> cards, String asin) {
		for (Card card : cards) {
			if (card.asin.equals(asin)) {
				return card.title;
			}
		}
		return "";
	}

	// A screenshot of the product page, taken as evidence of what was missing. Only taken when something is missing.
	private void capture(String what, String asin) {
		ScreenshotUtilUpdated.capture(what, asin);
	}

	private String shorten(String text, int max) {
		return text.length() > max ? text.substring(0, max) : text;
	}
}
