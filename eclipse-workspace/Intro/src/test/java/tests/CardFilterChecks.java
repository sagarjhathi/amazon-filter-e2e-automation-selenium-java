package test.java.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import main.java.amazonfilterapplicatione2e.base.BaseTest;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.fileReader.TestInputs;
import main.java.amazonfilterapplicatione2e.flows.Card;
import main.java.amazonfilterapplicatione2e.flows.CardFilterFlows;
import main.java.amazonfilterapplicatione2e.flows.CardFilterFlows.OptionCards;
import main.java.amazonfilterapplicatione2e.flows.CardFilterFlows.PriceRangeCards;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.BadgeFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.RatingFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.SellerFacts;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.reporting.ReportManager;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks.CardRule;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks.Judged;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;

// Checks the filters whose result the page states exactly (numbers, labels, ASINs), against the product cards or the product pages.
// One test per kind of check and search term (10 kinds x the search terms). A filter the search does not show is skipped, with the reason.
// What was read is saved in collected/ before any check. The rules and thresholds are in utilities/CardChecks.java; the steps on the pages
// are in flows/CardFilterFlows.java and flows/ProductPageFlows.java. This class only says what to open, what to read and what to compare.
//
// Run (no retries, own suite file):  mvn test -Dsurefire.suiteXmlFiles=src/test/runners/CardFilterChecks.xml
//   -DcardChecks.terms=Headphones   only these search terms (comma separated); blank = all of searchTerms.json
//   -DsellerProductsPerOption=5  -DbadgeProducts=all|N  -DratingProducts=8  -DpageDelayMs=2000
public class CardFilterChecks extends BaseTest {

	private final Logger log = LoggerUtility.getLogger(CardFilterChecks.class);

	// A test that reads no cards at all has found nothing to judge.
	private static final String EXPECT_CARDS = "At least one card was read";

	@DataProvider(name = "SearchTerms")
	public Object[][] searchTerms() {
		String only = ConfigManager.get("cardChecks.terms");
		List<String> terms = only == null || only.isBlank()
				? TestInputs.searchTerms()
				: Arrays.stream(only.split(",")).map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());
		Object[][] data = new Object[terms.size()][1];
		for (int i = 0; i < terms.size(); i++) {
			data[i][0] = terms.get(i);
		}
		return data;
	}


	// ---------------------------------------------------------------------------------------------
	// FILTERS CHECKED ON THE CARDS
	// ---------------------------------------------------------------------------------------------

	@Test(dataProvider = "SearchTerms", description = "<term>: Brands filter, checked on the cards")
	public void brandsFilterCheckedOnTheCards(String searchTerm) throws InterruptedException {
		checkCardRule(searchTerm, CardChecks.BRANDS);
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: Customer Reviews filter, checked on the cards")
	public void customerReviewsFilterCheckedOnTheCards(String searchTerm) throws InterruptedException {
		checkCardRule(searchTerm, CardChecks.CUSTOMER_REVIEWS);
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: Eligible for Free Delivery filter, checked on the cards")
	public void freeDeliveryFilterCheckedOnTheCards(String searchTerm) throws InterruptedException {
		checkCardRule(searchTerm, CardChecks.FREE_DELIVERY);
	}

	// Loose rule and a low threshold, because the printed discount is not always the one the filter uses (see CardChecks.DISCOUNT).
	@Test(dataProvider = "SearchTerms", description = "<term>: Discount filter, checked on the cards")
	public void discountFilterCheckedOnTheCards(String searchTerm) throws InterruptedException {
		checkCardRule(searchTerm, CardChecks.DISCOUNT);
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: Price range filter, checked on the cards")
	public void priceRangeFilterCheckedOnTheCards(String searchTerm) throws InterruptedException {
		start(searchTerm + ": Price range filter, checked on the cards",
				searchTerm + " Price: every range is applied; the URL and both sliders show the range, and at least 90% of the card prices are inside it");
		SoftAssert soft = new SoftAssert();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		List<PriceRangeCards> results = cardFilterFlows.applyPriceRangesAndReadCards();

		// Only a search that offers no ranges is skipped. Ranges that were offered but could not be applied fail the test below.
		if (results.isEmpty() && cardFilterFlows.notAppliedOptions.isEmpty()) {
			throw new SkipException("This search offers no price ranges");
		}
		CardChecks.saveCollected(searchTerm, "Price", results);

		soft.assertEquals(cardFilterFlows.notAppliedOptions, new ArrayList<String>(), "Ranges not applied");
		soft.assertTrue(!results.isEmpty(), "At least one price range was read");
		for (PriceRangeCards range : results) {
			// Three places state the range: the URL (in paise), the two sliders, and the prices on the cards. A side that is open has no limit to compare.
			Double min = range.min == null ? null : range.min / 100.0;
			Double max = range.max == null ? null : range.max / 100.0;
			soft.assertEquals(range.urlRange, range.expectedUrlRange, "'" + range.option + "': the range in the URL");
			if (min != null) {
				soft.assertEquals(CardChecks.firstNumber(range.sliderMin), (double) min, "'" + range.option + "': the lower slider reads '" + range.sliderMin + "'");
			}
			if (max != null) {
				soft.assertEquals(CardChecks.firstNumber(range.sliderMax), (double) max, "'" + range.option + "': the upper slider reads '" + range.sliderMax + "'");
			}
			CardChecks.checkShare(soft, searchTerm + " Price '" + range.option + "'",
					range.cards.stream().map(card -> CardChecks.priceVerdict(card, min, max)).collect(Collectors.toList()), 0.9);
		}
		soft.assertAll();
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: price, M.R.P. and printed discount of each card agree")
	public void cardSelfCheck(String searchTerm) throws InterruptedException {
		start(searchTerm + ": price, M.R.P. and printed discount of each card agree",
				searchTerm + " card self-check: (M.R.P. - price) / M.R.P. rounds to the printed discount; at least 95% of the cards must agree");
		SoftAssert soft = new SoftAssert();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		List<Card> cards = cardFilterFlows.readListingCards();
		CardChecks.saveCollected(searchTerm, "Card self-check", cards);

		CardChecks.checkShare(soft, searchTerm + " card self-check",
				cards.stream().map(CardChecks::selfCheckVerdict).collect(Collectors.toList()), 0.95);
		soft.assertAll();
	}


	// ---------------------------------------------------------------------------------------------
	// FILTERS AND FACTS CHECKED ON THE PRODUCT PAGES
	// ---------------------------------------------------------------------------------------------

	@Test(dataProvider = "SearchTerms", description = "<term>: Pay On Delivery filter, checked on the product pages")
	public void payOnDeliveryFilterCheckedOnTheProductPages(String searchTerm) throws InterruptedException {
		start(searchTerm + ": Pay On Delivery filter, checked on the product pages",
				searchTerm + " Pay On Delivery: the filter is applied; every product page read must show the Pay on Delivery card (a product that cannot be shipped to the location is not judged)");
		SoftAssert soft = new SoftAssert();
		ProductListingPage productPage = new ProductListingPage();
		GenericUtility genericUtility = new GenericUtility();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();
		ProductPageFlows productPageFlows = new ProductPageFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		genericUtility.skipIfFilterMissing("Pay On Delivery", searchTerm);

		List<OptionCards> results = cardFilterFlows.applyFilterAndReadCards("Pay On Delivery", productPage.optionLinksBy("Pay On Delivery"));
		soft.assertEquals(cardFilterFlows.notAppliedOptions, new ArrayList<String>(), "Options not applied");
		soft.assertTrue(!results.isEmpty(), "The filter was applied");

		// The filter is applied, so every product page must show the Pay on Delivery card. A product with no main offer is not opened.
		List<Judged> judged = new ArrayList<>();
		List<Object> saved = new ArrayList<>();
		for (OptionCards result : results) {
			List<BadgeFacts> products = productPageFlows.readPayOnDeliveryBadges(withMainOffer(result.cards), CardChecks.badgeProducts());
			saved.add(savedOption(result.option, products));
			products.forEach(product -> judged.add(CardChecks.badgeVerdict(product, result.option)));
		}
		CardChecks.saveCollected(searchTerm, "Pay On Delivery pages", saved);
		CardChecks.checkShare(soft, searchTerm + " Pay on Delivery card on the product pages", judged, 1);
		soft.assertAll();
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: Top Brands filter, checked on the product pages")
	public void topBrandsFilterCheckedOnTheProductPages(String searchTerm) throws InterruptedException {
		start(searchTerm + ": Top Brands filter, checked on the product pages",
				searchTerm + " Top Brands: the filter is applied; every product page read must show the Top Brand card");
		SoftAssert soft = new SoftAssert();
		ProductListingPage productPage = new ProductListingPage();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();
		ProductPageFlows productPageFlows = new ProductPageFlows();
		String topBrands = "Top Brands";

		cardFilterFlows.openSearchResults(searchTerm);

		// The heading changes with the search ("Cell Phone Brands" for mobiles), so it is found by the option.
		String heading = cardFilterFlows.headingOfOption(topBrands);
		if (heading.isEmpty()) {
			throw new SkipException("This search has no '" + topBrands + "' option");
		}

		List<OptionCards> results = cardFilterFlows.applyFilterAndReadCards(heading, productPage.optionLinksBy(heading), Arrays.asList(topBrands));
		soft.assertEquals(cardFilterFlows.notAppliedOptions, new ArrayList<String>(), "Options not applied");
		soft.assertTrue(!results.isEmpty(), "The filter was applied");

		// The filter is applied, so every product page must show the Top Brand card. A product with no main offer is not opened.
		List<Judged> judged = new ArrayList<>();
		List<Object> saved = new ArrayList<>();
		for (OptionCards result : results) {
			List<BadgeFacts> products = productPageFlows.readTopBrandBadges(withMainOffer(result.cards), CardChecks.badgeProducts());
			saved.add(savedOption(result.option, products));
			products.forEach(product -> judged.add(CardChecks.badgeVerdict(product, result.option)));
		}
		CardChecks.saveCollected(searchTerm, "Top Brands pages", saved);
		CardChecks.checkShare(soft, searchTerm + " Top Brand card on the product pages", judged, 1);
		soft.assertAll();
	}

	// How reliable the Seller filter is has not been settled yet, so a seller that does not match is reported, not failed.
	@Test(dataProvider = "SearchTerms", description = "<term>: Seller filter, checked on the product pages")
	public void sellerFilterCheckedOnTheProductPages(String searchTerm) throws InterruptedException {
		start(searchTerm + ": Seller filter, checked on the product pages",
				searchTerm + " Seller: each option is applied; the product must be sold by that seller in the buy box or in another offer (reported, not failed)");
		SoftAssert soft = new SoftAssert();
		ProductListingPage productPage = new ProductListingPage();
		GenericUtility genericUtility = new GenericUtility();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();
		ProductPageFlows productPageFlows = new ProductPageFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		genericUtility.skipIfFilterMissing("Seller", searchTerm);

		List<OptionCards> results = cardFilterFlows.applyFilterAndReadCards("Seller", productPage.optionLinksBy("Seller"));
		soft.assertEquals(cardFilterFlows.notAppliedOptions, new ArrayList<String>(), "Options not applied");

		// The product is sold by the option's seller in the buy box or in another offer. An unavailable product has no offer to read, so it is not opened.
		List<Judged> judged = new ArrayList<>();
		List<Object> saved = new ArrayList<>();
		for (OptionCards result : results) {
			List<SellerFacts> products = productPageFlows.readSellers(withMainOffer(result.cards), CardChecks.sellerProducts(),
					seller -> CardChecks.sameSeller(seller, result.option));
			saved.add(savedOption(result.option, products));
			List<Judged> verdicts = products.stream().map(product -> CardChecks.sellerVerdict(product, result.option)).collect(Collectors.toList());
			CardChecks.logSellerResult(searchTerm + " Seller", result.option, verdicts);
			judged.addAll(verdicts);
		}
		CardChecks.saveCollected(searchTerm, "Seller pages", saved);

		for (Judged item : judged) {
			if (item.verdict == CardChecks.Verdict.DOES_NOT_FIT && ReportManager.getTest() != null) {
				ReportManager.getTest().warning("seller-mismatch: " + item.what);
			}
		}
		soft.assertTrue(!judged.isEmpty(), "At least one product page was read");
		soft.assertAll();
	}

	@Test(dataProvider = "SearchTerms", description = "<term>: rating on the card equals the rating on the product page")
	public void ratingOnTheCardEqualsRatingOnTheProductPage(String searchTerm) throws InterruptedException {
		start(searchTerm + ": rating on the card equals the rating on the product page",
				searchTerm + " rating: the rating on the card equals the rating on the product page (within 0.1); at least 90% must agree");
		SoftAssert soft = new SoftAssert();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();
		ProductPageFlows productPageFlows = new ProductPageFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		List<Card> cards = cardFilterFlows.readListingCards().stream()
				.filter(card -> !card.rating.isEmpty() && !card.hasNoMainOffer())
				.collect(Collectors.toList());
		Map<String, String> cardRatingOf = new HashMap<>();
		cards.forEach(card -> cardRatingOf.putIfAbsent(card.asin, card.rating));

		List<RatingFacts> products = productPageFlows.readRatings(cards, CardChecks.ratingProducts());
		List<Object> saved = new ArrayList<>();
		products.forEach(product -> saved.add(savedRating(product, cardRatingOf.getOrDefault(product.asin, ""))));
		CardChecks.saveCollected(searchTerm, "Rating pages", saved);

		CardChecks.checkShare(soft, searchTerm + " card rating against product page",
				products.stream().map(product -> CardChecks.ratingVerdict(product, cardRatingOf.getOrDefault(product.asin, ""))).collect(Collectors.toList()), 0.9);
		soft.assertAll();
	}


	// ---------------------------------------------------------------------------------------------
	// STEPS
	// ---------------------------------------------------------------------------------------------

	// Brands, Customer Reviews and Eligible for Free Delivery: every option is applied and each card is judged by the rule of the filter.
	private void checkCardRule(String searchTerm, CardRule rule) throws InterruptedException {
		String heading = rule.heading;
		start(searchTerm + ": " + heading + " filter, checked on the cards",
				searchTerm + " " + heading + ": every option is applied; " + rule.description + "; at least " + Math.round(rule.threshold * 100) + "% of the cards must fit");
		SoftAssert soft = new SoftAssert();
		ProductListingPage productPage = new ProductListingPage();
		GenericUtility genericUtility = new GenericUtility();
		CardFilterFlows cardFilterFlows = new CardFilterFlows();

		cardFilterFlows.openSearchResults(searchTerm);
		genericUtility.skipIfFilterMissing(heading, searchTerm);

		List<OptionCards> results = cardFilterFlows.applyFilterAndReadCards(heading, productPage.optionLinksBy(heading));
		CardChecks.saveCollected(searchTerm, heading, results);

		soft.assertEquals(cardFilterFlows.notAppliedOptions, new ArrayList<String>(), "Options not applied");
		for (OptionCards result : results) {
			List<Judged> judged = result.cards.stream()
					.map(card -> new Judged(shorten(card.title, 50), rule.judge(result.option, card)))
					.collect(Collectors.toList());
			CardChecks.checkShare(soft, searchTerm + " " + heading + " '" + result.option + "'", judged, rule.threshold);
		}
		soft.assertTrue(countCards(results) > 0, EXPECT_CARDS);
		soft.assertAll();
	}

	// The first lines of a test: its name (as in the Playwright spec) and the rule it applies, in the test's own log.
	private void start(String name, String rule) {
		log.info("[{}] TEST: {}", ThreadContext.get("testName"), name);
		if (ReportManager.getTest() != null) {
			ReportManager.getTest().info("<b>" + name + "</b>");
		}
		CardChecks.logRule(rule);
	}

	// How the test ended, in the test's own log (the thread's log, so tests running side by side do not mix).
	@AfterMethod(alwaysRun = true)
	public void logFinished(ITestResult result) {
		String outcome = result.getStatus() == ITestResult.SUCCESS ? "passed" : result.getStatus() == ITestResult.SKIP ? "skipped" : "failed";
		long took = result.getEndMillis() - result.getStartMillis();
		log.info("[{}] Finished '{}': {} in {} ms", ThreadContext.get("testName"), result.getMethod().getMethodName(), outcome, took);
		if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
			String[] lines = result.getThrowable().getMessage().split("\n");
			for (int i = 0; i < Math.min(3, lines.length); i++) {
				log.info("[{}]   {}", ThreadContext.get("testName"), lines[i]);
			}
		}
	}

	private int countCards(List<OptionCards> results) {
		return results.stream().mapToInt(result -> result.cards.size()).sum();
	}

	// A product with no main offer ("currently unavailable") has nothing to read on its page, so it is not opened.
	private List<Card> withMainOffer(List<Card> cards) {
		return cards.stream().filter(card -> !card.hasNoMainOffer()).collect(Collectors.toList());
	}

	private Map<String, Object> savedOption(String option, Object products) {
		Map<String, Object> entry = new HashMap<>();
		entry.put("option", option);
		entry.put("products", products);
		return entry;
	}

	private Map<String, Object> savedRating(RatingFacts product, String cardRating) {
		Map<String, Object> entry = new HashMap<>();
		entry.put("product", product);
		entry.put("cardRating", cardRating);
		return entry;
	}

	private String shorten(String text, int max) {
		return text.length() > max ? text.substring(0, max) : text;
	}
}
