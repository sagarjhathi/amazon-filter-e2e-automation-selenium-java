package test.java.unit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import main.java.amazonfilterapplicatione2e.fileReader.TestInputs;
import main.java.amazonfilterapplicatione2e.flows.Card;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.BadgeFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.RatingFacts;
import main.java.amazonfilterapplicatione2e.flows.ProductPageFlows.SellerFacts;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks.Judged;
import main.java.amazonfilterapplicatione2e.utilities.CardChecks.Verdict;
import test.java.tests.CardFilterChecks;

// Unit tests for the card checks: no browser, no Amazon.
public class CardChecksTest {

	private Card card(String title, String price, String mrp, String discount, String rating, String delivery) {
		Card card = new Card();
		card.title = title;
		card.price = price;
		card.mrp = mrp;
		card.discount = discount;
		card.rating = rating;
		card.delivery = delivery;
		return card;
	}

	private Judged judged(Verdict verdict) {
		return new Judged("item", verdict);
	}

	// ---- numbers and names ----

	@Test
	public void firstNumberReadsThePriceThePercentAndTheStars() {
		Assert.assertEquals(CardChecks.firstNumber("₹26,000"), 26000.0);
		Assert.assertEquals(CardChecks.firstNumber("(44% off)"), 44.0);
		Assert.assertEquals(CardChecks.firstNumber("4.1 out of 5 stars"), 4.1);
		Assert.assertEquals(CardChecks.firstNumber("4 Stars & Up"), 4.0);
		Assert.assertTrue(Double.isNaN(CardChecks.firstNumber("no number")));
		Assert.assertTrue(Double.isNaN(CardChecks.firstNumber("")));
	}

	@Test
	public void sameSellerIgnoresCaseAndTheCutOffEnding() {
		Assert.assertTrue(CardChecks.sameSeller("NEW TECH WORLD ...", "New Tech World Pvt Ltd"));
		Assert.assertTrue(CardChecks.sameSeller("Appario Retail Private Ltd", "appario retail"));
		Assert.assertFalse(CardChecks.sameSeller("Cloudtail India", "Appario Retail"));
	}

	// ---- the three card rules ----

	@Test
	public void brandRuleUsesTheTitleAndKnowsAliases() {
		Assert.assertEquals(CardChecks.BRANDS.judge("Sony", card("Sony WH-1000XM5 Headphones", "", "", "", "", "")), Verdict.FITS);
		Assert.assertEquals(CardChecks.BRANDS.judge("Apple", card("iPhone 15 128 GB", "", "", "", "", "")), Verdict.FITS);
		Assert.assertEquals(CardChecks.BRANDS.judge("Sony", card("boAt Rockerz 450", "", "", "", "", "")), Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.BRANDS.judge("Sony", card("", "", "", "", "", "")), Verdict.NO_VALUE);
		Assert.assertEquals(CardChecks.BRANDS.threshold, 0.9);
	}

	@Test
	public void customerReviewsRuleComparesTheStars() {
		Assert.assertEquals(CardChecks.CUSTOMER_REVIEWS.judge("4 Stars & Up", card("", "", "", "", "4.1 out of 5 stars", "")), Verdict.FITS);
		Assert.assertEquals(CardChecks.CUSTOMER_REVIEWS.judge("4 Stars & Up", card("", "", "", "", "3.9 out of 5 stars", "")), Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.CUSTOMER_REVIEWS.judge("4 Stars & Up", card("", "", "", "", "", "")), Verdict.NO_VALUE);
		Assert.assertEquals(CardChecks.CUSTOMER_REVIEWS.threshold, 0.95);
	}

	@Test
	public void freeDeliveryRuleLooksForTheWords() {
		Assert.assertEquals(CardChecks.FREE_DELIVERY.judge("x", card("", "", "", "", "", "FREE delivery Tue, 4 Nov")), Verdict.FITS);
		Assert.assertEquals(CardChecks.FREE_DELIVERY.judge("x", card("", "", "", "", "", "Delivery ₹40")), Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.FREE_DELIVERY.judge("x", card("", "", "", "", "", "")), Verdict.NO_VALUE);
	}

	// ---- price and self-check ----

	@Test
	public void priceVerdictHandlesOpenSides() {
		Card cheap = card("a", "₹500", "", "", "", "");
		Assert.assertEquals(CardChecks.priceVerdict(cheap, 100.0, 1000.0).verdict, Verdict.FITS);
		Assert.assertEquals(CardChecks.priceVerdict(cheap, 600.0, null).verdict, Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.priceVerdict(cheap, null, 400.0).verdict, Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.priceVerdict(cheap, null, null).verdict, Verdict.FITS);
		Assert.assertEquals(CardChecks.priceVerdict(card("a", "", "", "", "", ""), null, null).verdict, Verdict.NO_VALUE);
	}

	@Test
	public void selfCheckAllowsOnePointOfRounding() {
		Assert.assertEquals(CardChecks.selfCheckVerdict(card("a", "₹700", "₹1,000", "(30% off)", "", "")).verdict, Verdict.FITS);
		Assert.assertEquals(CardChecks.selfCheckVerdict(card("a", "₹700", "₹1,000", "(31% off)", "", "")).verdict, Verdict.FITS);
		Assert.assertEquals(CardChecks.selfCheckVerdict(card("a", "₹700", "₹1,000", "(40% off)", "", "")).verdict, Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.selfCheckVerdict(card("a", "₹700", "", "(30% off)", "", "")).verdict, Verdict.NO_VALUE);
	}

	// ---- product page verdicts ----

	@Test
	public void badgeVerdictTreatsCannotShipAsNoValue() {
		BadgeFacts has = new BadgeFacts();
		has.badge = "Pay on Delivery";
		Assert.assertEquals(CardChecks.badgeVerdict(has, "o").verdict, Verdict.FITS);

		BadgeFacts missing = new BadgeFacts();
		Assert.assertEquals(CardChecks.badgeVerdict(missing, "o").verdict, Verdict.DOES_NOT_FIT);

		BadgeFacts cannotShip = new BadgeFacts();
		cannotShip.cannotShip = true;
		Assert.assertEquals(CardChecks.badgeVerdict(cannotShip, "o").verdict, Verdict.NO_VALUE);

		BadgeFacts problem = new BadgeFacts();
		problem.problem = "page not read";
		Assert.assertEquals(CardChecks.badgeVerdict(problem, "o").verdict, Verdict.NO_VALUE);
	}

	@Test
	public void sellerVerdictLooksAtTheBuyBoxAndTheOtherOffers() {
		SellerFacts buyBox = new SellerFacts();
		buyBox.seller = "Appario Retail Private Ltd";
		Assert.assertEquals(CardChecks.sellerVerdict(buyBox, "Appario Retail").verdict, Verdict.FITS);

		SellerFacts other = new SellerFacts();
		other.seller = "Cloudtail";
		other.offerSellers = Arrays.asList("Cloudtail", "Appario Retail");
		Assert.assertEquals(CardChecks.sellerVerdict(other, "Appario Retail").verdict, Verdict.FITS);

		SellerFacts wrong = new SellerFacts();
		wrong.seller = "Cloudtail";
		Assert.assertEquals(CardChecks.sellerVerdict(wrong, "Appario Retail").verdict, Verdict.DOES_NOT_FIT);

		Assert.assertEquals(CardChecks.sellerVerdict(new SellerFacts(), "Appario Retail").verdict, Verdict.NO_VALUE);
	}

	@Test
	public void ratingVerdictAllowsOneTenth() {
		RatingFacts page = new RatingFacts();
		page.rating = "4.2 out of 5 stars";
		Assert.assertEquals(CardChecks.ratingVerdict(page, "4.1 out of 5 stars").verdict, Verdict.FITS);
		Assert.assertEquals(CardChecks.ratingVerdict(page, "4.5 out of 5 stars").verdict, Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.ratingVerdict(new RatingFacts(), "4.1 out of 5 stars").verdict, Verdict.NO_VALUE);
	}

	// ---- checkShare ----

	@Test
	public void checkShareFailsBelowTheThresholdAndPassesAbove() {
		SoftAssert low = new SoftAssert();
		CardChecks.checkShare(low, "low", Arrays.asList(judged(Verdict.FITS), judged(Verdict.DOES_NOT_FIT)), 0.9);
		Assert.assertThrows(AssertionError.class, low::assertAll);

		SoftAssert high = new SoftAssert();
		CardChecks.checkShare(high, "high", Arrays.asList(judged(Verdict.FITS), judged(Verdict.FITS), judged(Verdict.NO_VALUE)), 0.9);
		high.assertAll();
	}

	@Test
	public void checkShareWithNothingJudgedIsInconclusiveNotFailed() {
		SoftAssert soft = new SoftAssert();
		CardChecks.checkShare(soft, "none", Arrays.asList(judged(Verdict.NO_VALUE), judged(Verdict.NO_VALUE)), 0.9);
		CardChecks.checkShare(soft, "empty", new ArrayList<Judged>(), 0.9);
		soft.assertAll();
	}

	// ---- discount ----

	@Test
	public void discountRuleComparesThePrintedPercentWithTheLevel() {
		Assert.assertEquals(CardChecks.DISCOUNT.judge("50% Off or more", card("a", "", "", "(55% off)", "", "")), Verdict.FITS);
		Assert.assertEquals(CardChecks.DISCOUNT.judge("50% Off or more", card("a", "", "", "(43% off)", "", "")), Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.DISCOUNT.judge("50% Off or more", card("a", "", "", "(45% off)", "", "")), Verdict.FITS);   // 5 points below is allowed
		Assert.assertEquals(CardChecks.DISCOUNT.judge("50% Off or more", card("a", "", "", "(44% off)", "", "")), Verdict.DOES_NOT_FIT);
		Assert.assertEquals(CardChecks.DISCOUNT.judge("50% Off or more", card("a", "", "", "", "", "")), Verdict.NO_VALUE);
		Assert.assertEquals(CardChecks.DISCOUNT.threshold, 0.4);
	}

	// ---- the card tests: their terms, and the 4 CI shards (TestNG groups) ----

	private static final List<String> SHARDS = Arrays.asList(
			"CardSeller", "CardBrandsReviewsDelivery", "CardTopBrandsPayOnDelivery", "CardRatingPriceDiscountSelfCheck");

	// The tests TestNG will run: for each card test method, one run per search term listed for it in filterCoverage.json.
	@Test
	public void cardFilterChecksHasTenKindsOfTestsAndTheCoveragePromisesFiftyRuns() {
		int kinds = 0;
		int runs = 0;
		for (Method method : CardFilterChecks.class.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Test.class)) {
				kinds++;
				runs += TestInputs.termsFor(method.getName()).size();
			}
		}
		Assert.assertEquals(kinds, 10, "kinds of test");
		Assert.assertEquals(runs, 50, "tests in the suite");
	}

	// Every card test is in exactly one of the 4 shards (and in CardChecks, which is all of them), so no test is left out of the CI.
	@Test
	public void everyCardTestIsInExactlyOneShard() {
		for (Method method : CardFilterChecks.class.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(Test.class)) {
				continue;
			}
			List<String> groups = Arrays.asList(method.getAnnotation(Test.class).groups());
			long shards = SHARDS.stream().filter(groups::contains).count();
			Assert.assertEquals(shards, 1, method.getName() + " must be in exactly one shard group, its groups: " + groups);
			Assert.assertTrue(groups.contains("CardChecks"), method.getName() + " must also be in the CardChecks group");
		}
	}
}
