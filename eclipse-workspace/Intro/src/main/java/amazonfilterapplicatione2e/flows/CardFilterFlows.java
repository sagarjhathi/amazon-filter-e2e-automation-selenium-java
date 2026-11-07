package main.java.amazonfilterapplicatione2e.flows;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.captcha.BlockerHandler;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.AmazonLandingPage;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtilUpdated;

// Applies each option of a filter, reads the product cards that Amazon then shows, and clears the option again.
// It also has a Price flow. The flow only applies and reads: the tests decide what to assert on the cards.
//
// How an option counts as "applied": the filter group's own "Clear" link shows up (see ProductListingPage.clearLinkBy).
// If it does not, the option is recorded in notAppliedOptions, the listing page is reloaded, and the flow goes on.
// There is no retry chain: one click, one check, a reload on failure.
//
// A blocked page (Continue shopping, captcha, 503) skips the test through BlockerHandler. Its SkipException is never
// caught here: every catch below is narrow, so it cannot swallow the skip.
public class CardFilterFlows extends BasePage {

	private final Logger log = LoggerUtility.getLogger(CardFilterFlows.class);

	private final AmazonLandingPage landingPage = new AmazonLandingPage();
	private final ProductListingPage productPage = new ProductListingPage();
	private final SafeActions safeAct = new SafeActions();
	private final GenericUtility genericUtility = new GenericUtility();
	private final BlockerHandler blockerHandler = new BlockerHandler();

	// Options that could not be applied, as "<option> (<reason>)". The tests report them later.
	public List<String> notAppliedOptions = new ArrayList<>();

	// The address of the results page before any option is applied. A reload goes back to it.
	private String listingUrl = "";

	// The cards read for one option of a filter.
	public static class OptionCards {
		public String option = "";
		public List<Card> cards = new ArrayList<>();
	}

	// The cards read for one preset price range. min and max are in paise (null = open side).
	public static class PriceRangeCards {
		public String option = "";
		public String expectedUrlRange = "";   // built from the link's own min and max, e.g. "7000000-10500000"
		public Long min;
		public Long max;
		public String urlRange = "";           // the p_36 part of the URL after the click
		public String sliderMin = "";          // aria-valuetext of the lower slider
		public String sliderMax = "";          // aria-valuetext of the upper slider
		public List<Card> cards = new ArrayList<>();
	}


	// ---------------------------------------------------------------------------------------------
	// OPEN THE RESULTS
	// ---------------------------------------------------------------------------------------------

	// Opens the landing page, searches, and remembers the results address. A blocked page skips the test, before and after the search.
	public void openSearchResults(String searchTerm) throws InterruptedException {
		notAppliedOptions = new ArrayList<>();

		landingPage.openingLandingPage();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();

		landingPage.givingInputWithinSearchBar(searchTerm);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();

		listingUrl = driver.getCurrentUrl();
		log.info("[{}] SEARCH '{}' -> {}", ThreadContext.get("testName"), searchTerm, listingUrl);
	}


	// The cards of the results as they are, with no filter applied (the card checks that need no option).
	public List<Card> readListingCards() {
		return settleAndReadCards();
	}

	// The heading of the filter that has this option. The heading of Top Brands changes with the search, so it is found by
	// the option. The filters load after the page: wait up to 10 s. "" when the search has no such option.
	public String headingOfOption(String option) {
		genericUtility.isElementVisible(productPage.headingOfOptionBy(option));
		List<WebElement> found = driver.findElements(productPage.headingOfOptionBy(option));
		return found.isEmpty() ? "" : collapse(found.get(0).getAttribute("textContent"));
	}


	// ---------------------------------------------------------------------------------------------
	// A LISTING FILTER (Brands, Eligible for Free Delivery, ...)
	// ---------------------------------------------------------------------------------------------

	public List<OptionCards> applyFilterAndReadCards(String filterName, By optionLinks) throws InterruptedException {
		return applyFilterAndReadCards(filterName, optionLinks, null);
	}

	// Applies each option of the filter one after the other and reads the cards for each. onlyOptions limits the run to
	// those option names (null = every option the page shows). Returns one OptionCards per option that was applied.
	public List<OptionCards> applyFilterAndReadCards(String filterName, By optionLinks, List<String> onlyOptions) throws InterruptedException {
		String testName = ThreadContext.get("testName");
		List<OptionCards> results = new ArrayList<>();

		List<String> options = readOptionNames(filterName, optionLinks);
		log.info("[{}] '{}' options on the page: [{}]", testName, filterName, String.join(" | ", options));

		if (onlyOptions != null) {
			List<String> listed = options;
			options = new ArrayList<>();
			for (String name : listed) {
				for (String wanted : onlyOptions) {
					if (wanted.equalsIgnoreCase(name)) {
						options.add(name);
						break;
					}
				}
			}
			// An option asked for by name that is not in the list is reported, not left out quietly.
			for (String wanted : onlyOptions) {
				boolean seen = false;
				for (String name : listed) {
					seen = seen || wanted.equalsIgnoreCase(name);
				}
				if (!seen) {
					notAppliedOptions.add(wanted + " (not found in the list)");
					log.error("[{}] '{}' was not found among the '{}' options", testName, wanted, filterName);
				}
			}
		}
		int use = optionsToUse(options.size());
		log.info("[{}] FILTER '{}': checking {} of {} option(s)", testName, filterName, use, options.size());

		// Built once, while the page is settled: the locator fails loudly when the page has no such group, and right after
		// a click the page is still loading and the group is briefly missing.
		By clearLink = productPage.clearLinkBy(filterName);

		for (int i = 0; i < use; i++) {
			String option = options.get(i);
			log.info("[{}] OPTION '{}' ({} of {})", testName, option, i + 1, use);

			// After a Clear or a reload the page is still filling in: wait until the option list is complete again.
			genericUtility.waitForCompleteList(optionLinks);
			clickSeeMoreIfShowing(filterName, optionLinks);

			long start = System.currentTimeMillis();
			// OLD: String clickProblem = clickOption(filterName, option);
			String clickProblem = clickOptionWithRetry(filterName, optionLinks, option);
			blockerHandler.clearBlockers();

			if (clickProblem == null && genericUtility.isElementVisible(clearLink)) {
				log.info("[{}] '{}' applied after {} ms", testName, option, System.currentTimeMillis() - start);
				ScreenshotUtilUpdated.capture(filterName, option);

				OptionCards read = new OptionCards();
				read.option = option;
				read.cards = settleAndReadCards();
				log.info("[{}] '{}': {}", testName, option, summary(read.cards));
				results.add(read);

				clearFilter(filterName);
				log.info("[{}] '{}' cleared", testName, option);
			} else {
				String reason = clickProblem != null ? clickProblem : "the Clear link of the group did not appear within 10 s";
				recordNotApplied(filterName, option, reason);
			}
		}
		log.info("[{}] DONE: {} option(s) read, not applied: {}", testName, results.size(), notAppliedOptions);
		return results;
	}

	// Waits until the option list is complete, opens "See more", and reads the option names: the text of each link,
	// whitespace collapsed, each name once, without "See more" / "See less".
	private List<String> readOptionNames(String filterName, By optionLinks) throws InterruptedException {
		genericUtility.waitForCompleteList(optionLinks);
		clickSeeMoreIfShowing(filterName, optionLinks);

		Set<String> names = new LinkedHashSet<>();
		for (WebElement link : driver.findElements(optionLinks)) {
			String name = optionName(link);
			boolean isToggle = name.equalsIgnoreCase("See more") || name.equalsIgnoreCase("See less");
			if (!name.isEmpty() && !isToggle) {
				names.add(name);
			}
		}
		return new ArrayList<>(names);
	}

	// The name of an option. Amazon labels each option link "Apply the filter <name> to narrow results", which is the name even when the
	// link's text is only a picture (the Customer Reviews options are stars: their text is just "& Up"). Otherwise the link's text.
	private String optionName(WebElement link) {
		String label = link.getAttribute("aria-label");
		String prefix = "Apply the filter ";
		String suffix = " to narrow results";
		if (label != null && label.startsWith(prefix) && label.endsWith(suffix)) {
			return collapse(label.substring(prefix.length(), label.length() - suffix.length()));
		}
		return collapse(link.getText());
	}

	// The same run limit the other flows use (UtilData.properties): a number or "all", never more than the page shows.
	private int optionsToUse(int onTheScreen) {
		boolean isCron = Boolean.parseBoolean(System.getenv("IS_CRON"));
		if (isCron) {
			return ConfigManager.getLimit("runForAllFilterOptionsCI", "overideFilteOptionsCount", onTheScreen);
		}
		return ConfigManager.getLimit("runForAllFilterOptions", "overideFilteOptionsCount", onTheScreen);
	}

	// The list can collapse after each Clear, so "See more" is clicked again before every option when it is showing.
	// It looks the link up without waiting: "See more" is missing most of the time (when the list is not collapsed),
	// and the existing safe lookups would wait several seconds for it, and refresh the page, before every option.
	// (A refresh keeps the applied filter, it is in the address, but it resets the scroll position.)
	private void clickSeeMoreIfShowing(String filterName, By optionLinks) {
		try {
			List<WebElement> more = driver.findElements(productPage.seeMoreLinkBy(filterName));
			if (!more.isEmpty() && more.get(0).isDisplayed()) {
				scrollTo(more.get(0));
				more.get(0).click();
				genericUtility.waitForCompleteList(optionLinks);
			}
		} catch (WebDriverException e) {
			log.info("[{}] 'See more' of '{}' could not be clicked: {}", ThreadContext.get("testName"), filterName, e.getMessage());
		}
	}

	// Clicks the option by its whole name. Returns null when the click went through, otherwise why it did not.
	private String clickOption(String filterName, String option) {
		try {
			WebElement link = driver.findElement(productPage.optionLinkBy(filterName, option));
			scrollTo(link);
			link.click();
			return null;
		} catch (WebDriverException e) {
			return "the click failed: " + firstLine(e.getMessage());
		}
	}

	// Clicks the option; when its link is not found (right after a Clear the list can be collapsed or still filling in) the click is tried again:
	//   1. wait for the list, open "See more" again, click once more
	//   2. still not found: reload the results page (unfiltered, like after a Clear), open "See more", click a last time
	// Returns null when a click went through, otherwise why the last one did not.
	private String clickOptionWithRetry(String filterName, By optionLinks, String option) {
		String problem = clickOption(filterName, option);
		if (problem == null) {
			return null;
		}

		log.warn("[{}] '{}' not clickable ({}). Waiting for the list, opening 'See more' and trying again", ThreadContext.get("testName"), option, problem);
		genericUtility.waitForCompleteList(optionLinks);
		clickSeeMoreIfShowing(filterName, optionLinks);
		problem = clickOption(filterName, option);
		if (problem == null) {
			return null;
		}

		log.warn("[{}] '{}' still not clickable ({}). Reloading the results page and trying a last time", ThreadContext.get("testName"), option, problem);
		reloadListing();
		genericUtility.waitForCompleteList(optionLinks);
		clickSeeMoreIfShowing(filterName, optionLinks);
		return clickOption(filterName, option);
	}

	// Records an option that could not be applied, takes the screenshot, and reloads the results page.
	private void recordNotApplied(String filterName, String option, String reason) {
		notAppliedOptions.add(option + " (" + reason + ")");
		log.error("[{}] '{}' of '{}' not applied: {}", ThreadContext.get("testName"), option, filterName, reason);
		ScreenshotUtilUpdated.capture(filterName, "not applied", option);
		reloadListing();
	}

	// Clears the filter with the group's own Clear link and confirms it is gone (reloads the page if it does not go).
	private void clearFilter(String filterName) {
		genericUtility.clearOption(safeAct, filterName, listingUrl);
		blockerHandler.clearBlockers();
	}

	private void reloadListing() {
		driver.get(listingUrl);
		blockerHandler.clearBlockers();
	}


	// ---------------------------------------------------------------------------------------------
	// THE PRICE FLOW
	// ---------------------------------------------------------------------------------------------

	// Applies each preset price range, reads the cards, the price in the URL and the two sliders, and clears again.
	// The ranges change between page loads (the same search showed "₹350 - ₹600" once and "₹350 - ₹700" another time),
	// so a range is never remembered: the links are read by position every time.
	public List<PriceRangeCards> applyPriceRangesAndReadCards() throws InterruptedException {
		String testName = ThreadContext.get("testName");
		List<PriceRangeCards> results = new ArrayList<>();

		genericUtility.waitForCompleteList(productPage.priceRangeLinksBy);
		log.info("[{}] FILTER 'Price': {} preset range(s) on the page", testName, driver.findElements(productPage.priceRangeLinksBy).size());

		for (int position = 0; ; position++) {
			// After a Clear or a reload the page is still filling in: wait until the range list is complete again.
			genericUtility.waitForCompleteList(productPage.priceRangeLinksBy);
			List<WebElement> links = driver.findElements(productPage.priceRangeLinksBy);
			if (position >= links.size()) {
				break;
			}

			String name = collapse(links.get(position).getText());
			String href = links.get(position).getAttribute("href");
			PriceRangeCards range = new PriceRangeCards();
			range.option = name;
			range.min = priceFromHref(href, 1);
			range.max = priceFromHref(href, 2);
			range.expectedUrlRange = formatRange(range.min, range.max);
			log.info("[{}] OPTION '{}' ({}): its link says from {} paise to {} paise; clicking it", testName, name, position + 1, range.min, range.max);

			scrollTo(links.get(position));
			String clickProblem = null;
			try {
				links.get(position).click();
			} catch (WebDriverException e) {
				clickProblem = "the click failed: " + firstLine(e.getMessage());
			}
			blockerHandler.clearBlockers();

			if (clickProblem != null || !genericUtility.isElementVisible(productPage.priceClearLinkBy)) {
				String reason = clickProblem != null ? clickProblem : "the Clear link of the price section did not appear within 10 s";
				recordNotApplied("Price", name, reason);
				continue;
			}

			ScreenshotUtilUpdated.capture("Price", name);
			range.cards = settleAndReadCards();
			range.urlRange = priceRangeInUrl(driver.getCurrentUrl());
			range.sliderMin = sliderText(productPage.priceMinSliderButton);
			range.sliderMax = sliderText(productPage.priceMaxSliderButton);
			log.info("[{}] '{}': the URL shows '{}' (expected '{}'), the sliders read '{}' to '{}'; read {} card(s): {}", testName, name,
					range.urlRange, range.expectedUrlRange, range.sliderMin, range.sliderMax, range.cards.size(), summary(range.cards));
			results.add(range);

			clearPrice();
			log.info("[{}] '{}' cleared", testName, name);
		}
		log.info("[{}] DONE: {} price range(s) read, not applied: {}", testName, results.size(), notAppliedOptions);
		return results;
	}

	// The min or max (group 1 or 2) from p_36:min-max in a link's href, in paise. null when that side is open.
	private Long priceFromHref(String href, int group) {
		Matcher m = Pattern.compile("p_36:(\\d*)-(\\d*)").matcher(decode(href));
		if (m.find() && !m.group(group).isEmpty()) {
			return Long.parseLong(m.group(group));
		}
		return null;
	}

	// "min-max" with an open side left empty: "7000000-10500000", "-7000000" (Up to), "25000000-" (Over).
	private String formatRange(Long min, Long max) {
		return (min == null ? "" : String.valueOf(min)) + "-" + (max == null ? "" : String.valueOf(max));
	}

	// The p_36 range in the page address, written the same way. "" when the address has none.
	private String priceRangeInUrl(String url) {
		Matcher m = Pattern.compile("p_36:(\\d*-\\d*)").matcher(decode(url));
		return m.find() ? m.group(1) : "";
	}

	private String sliderText(By slider) {
		List<WebElement> found = driver.findElements(slider);
		if (found.isEmpty()) {
			return "";
		}
		String text = found.get(0).getAttribute("aria-valuetext");
		return text == null ? "" : text;
	}

	// Clears with the price section's own Clear link and waits until it is gone (reloads the page if it does not go).
	private void clearPrice() {
		try {
			List<WebElement> clear = driver.findElements(productPage.priceClearLinkBy);
			if (!clear.isEmpty() && clear.get(0).isDisplayed()) {
				clear.get(0).click();
			}
			new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.invisibilityOfElementLocated(productPage.priceClearLinkBy));
		} catch (WebDriverException e) {
			log.warn("[{}] The Clear link of the price section did not go. Reloading the results page.", ThreadContext.get("testName"));
			reloadListing();
			return;
		}
		blockerHandler.clearBlockers();
	}


	// ---------------------------------------------------------------------------------------------
	// READING THE CARDS
	// ---------------------------------------------------------------------------------------------

	// Waits until the cards stop changing (the same products on two reads, 1 s apart, 10 s at most), then reads them.
	private List<Card> settleAndReadCards() {
		List<String> before = cardAsins();
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 10_000) {
			pause(1000);
			List<String> now = cardAsins();
			boolean unchanged = now.equals(before);
			before = now;
			if (unchanged && (!now.isEmpty() || System.currentTimeMillis() - start >= 3000)) {
				break;
			}
		}
		List<Card> cards = readCards();
		log.info("[{}] {} card(s) on screen", ThreadContext.get("testName"), cards.size());
		return cards;
	}

	private List<String> cardAsins() {
		List<String> asins = new ArrayList<>();
		try {
			for (WebElement card : driver.findElements(productPage.searchResultCardsBy)) {
				asins.add(card.getAttribute("data-asin"));
			}
		} catch (WebDriverException e) {
			// the list changed while it was read: the next read will tell
		}
		return asins;
	}

	private List<Card> readCards() {
		List<Card> cards = new ArrayList<>();
		for (WebElement element : driver.findElements(productPage.searchResultCardsBy)) {
			try {
				cards.add(readCard(element));
			} catch (WebDriverException e) {
				log.info("[{}] A card changed while it was read and is left out: {}", ThreadContext.get("testName"), firstLine(e.getMessage()));
			}
		}
		return cards;
	}

	// One card. A part the card does not show is "". Each part is looked up with findElements and no waiting,
	// so a missing part costs nothing.
	private Card readCard(WebElement element) {
		Card card = new Card();
		card.asin = collapse(element.getAttribute("data-asin"));
		card.title = partText(element, productPage.cardTitleBy);
		card.price = partText(element, productPage.cardPriceBy);
		card.mrp = partText(element, productPage.cardMrpBy);
		card.delivery = partText(element, productPage.cardDeliveryBy);
		card.rating = partText(element, productPage.cardRatingBy);
		card.cardText = collapse(element.getText());

		Matcher printed = Pattern.compile("\\(\\d+% off\\)").matcher(card.cardText);
		card.discount = printed.find() ? printed.group() : "";

		if (card.cardText.toLowerCase().contains("currently unavailable")) {
			card.availability = "Currently unavailable";
		}
		return card;
	}

	// The text of a part of the card. The price sits in a visually hidden span, so its text comes from textContent.
	private String partText(WebElement card, By part) {
		List<WebElement> found = card.findElements(part);
		if (found.isEmpty()) {
			return "";
		}
		String text = found.get(0).getText();
		if (text == null || text.trim().isEmpty()) {
			text = found.get(0).getAttribute("textContent");
		}
		return collapse(text);
	}

	// One line for the log: how many cards, and how many show a price, a rating, a printed discount, a delivery line, no main offer.
	private String summary(List<Card> cards) {
		int price = 0, rating = 0, discount = 0, delivery = 0, noOffer = 0;
		for (Card card : cards) {
			if (!card.price.isEmpty()) price++;
			if (!card.rating.isEmpty()) rating++;
			if (!card.discount.isEmpty()) discount++;
			if (!card.delivery.isEmpty()) delivery++;
			if (card.hasNoMainOffer()) noOffer++;
		}
		return cards.size() + " card(s): " + price + " with a price, " + rating + " with a rating, " + discount
				+ " with a printed discount, " + delivery + " with a delivery line, " + noOffer + " with no main offer";
	}


	// ---------------------------------------------------------------------------------------------
	// SMALL HELPERS
	// ---------------------------------------------------------------------------------------------

	// Scrolls the element to the middle of the window. The element is already found, so this needs no lookup. The existing
	// smoothScrollToElement would look it up again with the safe lookup, which waits and refreshes when it is missing.
	private void scrollTo(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
	}

	private String decode(String text) {
		return text == null ? "" : URLDecoder.decode(text, StandardCharsets.UTF_8);
	}
}
