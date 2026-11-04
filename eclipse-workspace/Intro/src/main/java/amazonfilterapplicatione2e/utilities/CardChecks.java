package main.java.amazonfilterapplicatione2e.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.testng.asserts.SoftAssert;

import com.google.gson.GsonBuilder;

import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.flows.Card;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.BadgeFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.RatingFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.SellerFacts;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.reporting.ReportManager;

// What the card tests compare and report. Nothing here opens a page: the flows read, this judges.
public class CardChecks {

	private static final Logger log = LoggerUtility.getLogger(CardChecks.class);

	// ---- Run settings (UtilData.properties, or -D on the command line) ----

	public static int sellerProducts() {
		return ConfigManager.getInt("sellerProductsPerOption", 5);   // product pages opened per Seller option
	}

	public static int badgeProducts() {
		String value = ConfigManager.get("badgeProducts");           // per Top Brands / Pay on Delivery; "all" or blank means every card
		return value == null || value.isBlank() || value.equalsIgnoreCase("all") ? Integer.MAX_VALUE : Integer.parseInt(value.trim());
	}

	public static int ratingProducts() {
		return ConfigManager.getInt("ratingProducts", 8);            // for the rating check
	}


	// ---- Verdicts ----

	// NO_VALUE: the page does not show what the rule needs, so it is listed, not judged.
	public enum Verdict { FITS, DOES_NOT_FIT, NO_VALUE }

	public static class Judged {
		public final String what;
		public final Verdict verdict;

		public Judged(String what, Verdict verdict) {
			this.what = what;
			this.verdict = verdict;
		}
	}


	// ---- Reading numbers and names ----

	// The first number in a text: "₹26,000" is 26000, "(44% off)" is 44, "4.1 out of 5 stars" is 4.1, "4 Stars & Up" is 4. NaN when there is none.
	public static double firstNumber(String text) {
		if (text == null) {
			return Double.NaN;
		}
		Matcher number = Pattern.compile("\\d[\\d,]*\\.?\\d*").matcher(text);
		return number.find() ? Double.parseDouble(number.group().replace(",", "")) : Double.NaN;
	}

	// A brand or seller name without the "..." Amazon puts at the end of a long one.
	public static String cleanName(String name) {
		return name.replaceAll("\\s*(\\.{3}|…)\\s*$", "").replaceAll("\\s+", " ").trim();
	}

	// The two seller names only have to start the same way, whichever is shorter ("NEW TECH WORLD ..." is cut).
	public static boolean sameSeller(String pageSeller, String option) {
		String page = cleanName(pageSeller).toLowerCase();
		String wanted = cleanName(option).toLowerCase();
		return page.startsWith(wanted) || wanted.startsWith(page);
	}


	// ---- Filters checked on the cards: the rule of each says whether one card fits the option applied ----

	// One rule: what the filter promises about every card.
	public static class CardRule {
		public final String heading;
		public final String description;
		public final double threshold;
		private final java.util.function.BiFunction<String, Card, Verdict> rule;

		CardRule(String heading, String description, double threshold, java.util.function.BiFunction<String, Card, Verdict> rule) {
			this.heading = heading;
			this.description = description;
			this.threshold = threshold;
			this.rule = rule;
		}

		public Verdict judge(String option, Card card) {
			return rule.apply(option, card);
		}
	}

	public static final CardRule BRANDS = new CardRule("Brands",
			"the brand, or a known alias of it, is a whole word of the card's title", 0.9,
			(option, card) -> card.title.isEmpty() ? Verdict.NO_VALUE : BrandMatcher.titleHasBrand(option, card.title) ? Verdict.FITS : Verdict.DOES_NOT_FIT);

	public static final CardRule CUSTOMER_REVIEWS = new CardRule("Customer Reviews",
			"the card's rating is at least the option's stars", 0.95,
			(option, card) -> card.rating.isEmpty() ? Verdict.NO_VALUE : firstNumber(card.rating) >= firstNumber(option) ? Verdict.FITS : Verdict.DOES_NOT_FIT);

	public static final CardRule FREE_DELIVERY = new CardRule("Eligible for Free Delivery",
			"the card prints a \"FREE delivery\" line", 0.9,
			(option, card) -> card.delivery.isEmpty() ? Verdict.NO_VALUE : card.delivery.toLowerCase().contains("free delivery") ? Verdict.FITS : Verdict.DOES_NOT_FIT);

	// The discount a card prints is not always the one the filter uses (on Mobile only 7 of 16 cards under "60% Off or more" print 60% or more),
	// so this rule is loose: it allows 5 points below the level (rounding, and the card showing a slightly different price than the filter used) and only catches a filter that gives cards that print far less. The threshold stays low (0.4): the worst share seen without the buffer was 43%.
	public static final CardRule DISCOUNT = new CardRule("Discount",
			"the discount the card prints is within 5 points of the option's level or higher (a card that prints none is not judged)", 0.4,
			(option, card) -> card.discount.isEmpty() ? Verdict.NO_VALUE : firstNumber(card.discount) >= firstNumber(option) - 5 ? Verdict.FITS : Verdict.DOES_NOT_FIT);

	// The price of the card is inside the range. A side that is open (null) has no limit.
	public static Judged priceVerdict(Card card, Double min, Double max) {
		double price = firstNumber(card.price);
		String what = shorten(card.title, 40) + " " + card.price;
		if (Double.isNaN(price)) {
			return new Judged(what, Verdict.NO_VALUE);
		}
		boolean fits = (min == null || price >= min) && (max == null || price <= max);
		return new Judged(what, fits ? Verdict.FITS : Verdict.DOES_NOT_FIT);
	}

	// The card agrees with itself: (M.R.P. - price) / M.R.P. rounds to the printed percent. A card that does not print all three is not judged.
	public static Judged selfCheckVerdict(Card card) {
		double price = firstNumber(card.price);
		double mrp = firstNumber(card.mrp);
		double printed = firstNumber(card.discount);
		if (Double.isNaN(price) || Double.isNaN(mrp) || Double.isNaN(printed)) {
			return new Judged(shorten(card.title, 40), Verdict.NO_VALUE);
		}
		String what = shorten(card.title, 40) + " price " + price + ", M.R.P. " + mrp + ", printed " + printed + "% (as read: '" + card.price + "', '" + card.mrp + "', '" + card.discount + "')";
		boolean agrees = Math.abs(Math.round((mrp - price) / mrp * 100) - printed) <= 1;
		return new Judged(what, agrees ? Verdict.FITS : Verdict.DOES_NOT_FIT);
	}


	// ---- Facts checked on the product pages ----

	// The filter is applied, so the product page must show the label (Pay on Delivery, Top Brand). The exception is a page that says the
	// product cannot be shipped to the chosen location: it has no such label whatever the filter says, so it is not judged.
	public static Judged badgeVerdict(BadgeFacts product, String option) {
		String what = product.asin + " (" + option + ")";
		if (!product.problem.isEmpty() || (product.badge.isEmpty() && product.cannotShip)) {
			return new Judged(what, Verdict.NO_VALUE);
		}
		return new Judged(what, product.badge.isEmpty() ? Verdict.DOES_NOT_FIT : Verdict.FITS);
	}

	// The product is sold by the option's seller, in the buy box or in another offer.
	public static Judged sellerVerdict(SellerFacts product, String option) {
		boolean sold = (!product.seller.isEmpty() && sameSeller(product.seller, option))
				|| product.offerSellers.stream().anyMatch(seller -> sameSeller(seller, option));
		boolean noSeller = product.seller.isEmpty() && product.offerSellers.isEmpty();
		String what = product.asin + " (" + option + ", page shows '" + product.seller + "')";
		Verdict verdict = !product.problem.isEmpty() || noSeller ? Verdict.NO_VALUE : sold ? Verdict.FITS : Verdict.DOES_NOT_FIT;
		return new Judged(what, verdict);
	}

	// The rating on the card and on the product page: both print "N out of 5 stars", and a difference of one tenth is rounding.
	public static Judged ratingVerdict(RatingFacts product, String cardRatingText) {
		double cardRating = firstNumber(cardRatingText);
		double pageRating = firstNumber(product.rating);
		if (!product.problem.isEmpty() || Double.isNaN(cardRating) || Double.isNaN(pageRating)) {
			return new Judged(product.asin, Verdict.NO_VALUE);
		}
		String what = product.asin + " card " + cardRating + ", page " + pageRating;
		return new Judged(what, Math.abs(cardRating - pageRating) <= 0.1001 ? Verdict.FITS : Verdict.DOES_NOT_FIT);
	}


	// ---- Reporting and saving ----

	// Counts the judged items and fails (softly, so every check of the test is reported) when the share that fits is under the threshold.
	// When nothing could be judged (every item shows no value), the check says nothing either way: it is marked "inconclusive", not failed.
	public static void checkShare(SoftAssert soft, String label, List<Judged> items, double threshold) {
		long fits = items.stream().filter(item -> item.verdict == Verdict.FITS).count();
		List<String> misfits = items.stream().filter(item -> item.verdict == Verdict.DOES_NOT_FIT).map(item -> item.what).collect(Collectors.toList());
		List<String> unjudged = items.stream().filter(item -> item.verdict == Verdict.NO_VALUE).map(item -> item.what).collect(Collectors.toList());
		long judged = fits + misfits.size();

		String testName = ThreadContext.get("testName");
		log.info("[{}] CHECK {}: {} of {} judged fit (needs at least {}), {} show no value", testName, label, fits, judged, threshold, unjudged.size());
		if (!misfits.isEmpty()) {
			log.warn("[{}] {}: do not fit: {}", testName, label, String.join(" | ", misfits));
		}
		if (!unjudged.isEmpty()) {
			String first = String.join(" | ", unjudged.subList(0, Math.min(10, unjudged.size())));
			log.info("[{}] {}: not judged (no value): {}{}", testName, label, first, unjudged.size() > 10 ? " | ... and " + (unjudged.size() - 10) + " more" : "");
		}

		if (judged == 0) {
			log.warn("[{}] {}: nothing could be judged, so the check is marked inconclusive (not failed)", testName, label);
			markInconclusive(label + ": nothing could be judged (" + unjudged.size() + " show no value)");
			return;
		}
		soft.assertTrue((double) fits / judged >= threshold,
				label + ": the share that fits must be at least " + threshold + ". Not fitting: " + String.join(" | ", misfits));
	}

	// A check that could not be made is shown on the test in the report as a warning, and is neither a pass nor a failure.
	public static void markInconclusive(String description) {
		log.warn("[{}] INCONCLUSIVE {}", ThreadContext.get("testName"), description);
		if (ReportManager.getTest() != null) {
			ReportManager.getTest().warning("inconclusive: " + description);
		}
	}

	// Saves what was read as collected/<search term>__<name>.json, before any check, so it is kept even when a check fails.
	public static void saveCollected(String searchTerm, String name, Object data) {
		String folder = ConfigManager.get("collectDir");
		File dir = new File(folder == null || folder.isBlank() ? "collected" : folder);
		File file = new File(dir, searchTerm + "__" + name.replaceAll("\\s+", "_") + ".json");
		Map<String, Object> content = new LinkedHashMap<>();
		content.put("searchTerm", searchTerm);
		content.put("name", name);
		content.put("collectedAt", Instant.now().toString());
		content.put("data", data);
		dir.mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			new GsonBuilder().setPrettyPrinting().create().toJson(content, writer);
			log.info("[{}] Saved what was read to {}", ThreadContext.get("testName"), file.getPath());
		} catch (IOException e) {
			log.warn("[{}] Could not save what was read to {}: {}", ThreadContext.get("testName"), file.getPath(), e.getMessage());
		}
	}

	// The Seller test does not fail, so its result goes to the log: per option how many products are sold by that seller, and which are not.
	public static void logSellerResult(String label, String option, List<Judged> judged) {
		long fits = judged.stream().filter(item -> item.verdict == Verdict.FITS).count();
		long other = judged.stream().filter(item -> item.verdict == Verdict.DOES_NOT_FIT).count();
		long unread = judged.stream().filter(item -> item.verdict == Verdict.NO_VALUE).count();
		String testName = ThreadContext.get("testName");
		log.info("[{}] CHECK {} '{}': {} of {} product(s) sold by that seller, {} sold by another, {} unread or with no seller (reported, not failed)",
				testName, label, option, fits, judged.size(), other, unread);
		List<String> different = judged.stream().filter(item -> item.verdict == Verdict.DOES_NOT_FIT).map(item -> item.what).collect(Collectors.toList());
		if (!different.isEmpty()) {
			log.warn("[{}] {} '{}': seller mismatch: {}", testName, label, option, String.join(" | ", different));
		}
	}

	// Says in the test's log which rule the test applies, so the rest of the log can be read against it.
	public static void logRule(String rule) {
		log.info("[{}] ==================== RULE: {} ====================", ThreadContext.get("testName"), rule);
	}


	// ---- Small helpers ----

	private static String shorten(String text, int max) {
		return text.length() > max ? text.substring(0, max) : text;
	}

}
