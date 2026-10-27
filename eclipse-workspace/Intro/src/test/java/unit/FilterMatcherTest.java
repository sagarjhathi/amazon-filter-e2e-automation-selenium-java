package test.java.unit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import main.java.amazonfilterapplicatione2e.utilities.BrandMatcher;
import main.java.amazonfilterapplicatione2e.utilities.FilterMatcher;
import main.java.amazonfilterapplicatione2e.utilities.FilterMatcher.Status;

// Unit tests: no browser, no Amazon. Plain strings go in, an answer comes out.
// Run:  mvn test -Dsurefire.suiteXmlFiles=src/test/runners/UnitTests.xml
public class FilterMatcherTest {

	// A spec table is one line per row: "<label> <value>".
	private static Status range(String filter, String label, String value) {
		return FilterMatcher.rangeMatch(filter, new String[] { label }, label + " " + value, "").status;
	}

	// ---------------------------------------------------------------------------------------------
	// RANGES AND UNITS
	// ---------------------------------------------------------------------------------------------

	@Test
	public void upToRange() {
		assertEquals(range("up to 6.9 h", "battery life", "5 h"), Status.MATCH);
		assertEquals(range("up to 6.9 h", "battery life", "8 h"), Status.MISMATCH);
	}

	@Test
	public void fromToRange() {
		assertEquals(range("7 to 7.9 h", "battery life", "7.5 hours"), Status.MATCH);
		assertEquals(range("7 to 7.9 h", "battery life", "9 hours"), Status.MISMATCH);
	}

	@Test
	public void andAboveRange() {
		assertEquals(range("9 h & above", "battery life", "10 hours"), Status.MATCH);
		assertEquals(range("9 h & above", "battery life", "6 hours"), Status.MISMATCH);
	}

	@Test
	public void lessThanRange() {
		assertEquals(range("less than 0.99 ghz", "processor speed", "800 mhz"), Status.MATCH);   // 800 mhz = 0.8 ghz
		assertEquals(range("less than 0.99 ghz", "processor speed", "2.2 ghz"), Status.MISMATCH);
	}

	@Test
	public void thousandsCommasAreRemoved() {
		assertEquals(range("3,000 to 3,999 mah", "battery capacity", "3500 mah"), Status.MATCH);
		assertEquals(range("up to 2,999 mah", "battery capacity", "6300 mah"), Status.MISMATCH);
	}

	@Test
	public void unitsAreConverted() {
		assertEquals(range("up to 512 gb", "memory storage capacity", "1 tb"), Status.MISMATCH);   // 1 tb = 1024 gb
		assertEquals(range("1 - 1.49 ghz", "processor speed", "1200 mhz"), Status.MATCH);
		assertEquals(range("up to 6.9 h", "battery life", "90 min"), Status.MATCH);                // 90 min = 1.5 h
		assertEquals(range("up to 34 cm", "screen size", "11.6 inches"), Status.MATCH);            // 29.5 cm
	}

	// Decision recorded on purpose: the bucket "35 to 39 cm" does NOT take a 39.6 cm (15.6 inch) laptop.
	@Test
	public void bucketUpperEndIsNotRounded() {
		assertEquals(range("35 to 39 cm", "screen size", "15.6 inches"), Status.MISMATCH);
	}

	@Test
	public void noRowMeansUnknown() {
		Status status = FilterMatcher.rangeMatch("up to 6.9 h", new String[] { "battery life" }, "brand x\nscreen size 6 inches", "").status;
		assertEquals(status, Status.UNKNOWN);
	}

	// A bare number is ignored whenever any number in the row has a unit ("dimensity 7300" must not count as 7300).
	@Test
	public void bareNumbersAreIgnoredWhenAnotherNumberHasAUnit() {
		assertEquals(range("less than 0.99 ghz", "processor speed", "dimensity 7300 2.5 ghz"), Status.MISMATCH);
		assertEquals(range("2 - 2.9 ghz", "processor speed", "dimensity 7300 2.5 ghz"), Status.MATCH);
	}

	@Test
	public void filterThatIsNotARangeNeedsTheWholeWordInTheRow() {
		assertEquals(range("4 gb", "memory storage capacity", "4 gb"), Status.MATCH);
		assertEquals(range("4 gb", "memory storage capacity", "64 gb"), Status.MISMATCH);   // "4 gb" is not inside "64 gb"
	}

	// ---------------------------------------------------------------------------------------------
	// CAMERA (MP): every number in the row counts, any one in range passes
	// ---------------------------------------------------------------------------------------------

	@Test
	public void cameraRowWithOneValuePerCamera() {
		assertEquals(range("4 - 7.9 mp", "rear camera", "12, 5, 50 mp"), Status.MATCH);        // the 5
		assertEquals(range("8 - 11.9 mp", "rear camera", "50, 8, 2, 2 mp"), Status.MATCH);     // the 8
		assertEquals(range("up to 3.9 mp", "rear camera", "12, 5, 50 mp"), Status.MISMATCH);   // none is 3.9 or less
	}

	// Known accepted risk, written down as a test so it is not forgotten: the 2.0 (an aperture) counts.
	@Test
	public void cameraAcceptedRiskAperture() {
		assertEquals(range("up to 3.9 mp", "rear camera", "2.0 50.0 mp"), Status.MATCH);
	}

	// ---------------------------------------------------------------------------------------------
	// WHOLE WORDS (display type)
	// ---------------------------------------------------------------------------------------------

	private static Status displayType(String filter, String title, String keyFeatures) {
		return FilterMatcher.displayTypeMatch(filter, title, keyFeatures, "", "").status;
	}

	@Test
	public void displayTypeUsesWholeWords() {
		assertEquals(displayType("led", "tv", "display type led, ips"), Status.MATCH);
		assertEquals(displayType("led", "tv", "display type controlled"), Status.MISMATCH);   // "led" is not in "controlled"
		assertEquals(displayType("oled", "tv", "display type amoled"), Status.MISMATCH);      // "oled" is not in "amoled"
		assertEquals(displayType("oled", "tv", "display type oled panel"), Status.MATCH);
	}

	@Test
	public void displayTypeWithoutARowLooksInTheTextButCannotFail() {
		assertEquals(displayType("lcd", "lenovo tab with lcd display", "brand lenovo"), Status.MATCH);
		assertEquals(displayType("lcd", "lenovo tab", "brand lenovo"), Status.UNKNOWN);   // never MISMATCH without a row
	}

	// ---------------------------------------------------------------------------------------------
	// BRANDS AND ALIASES
	// ---------------------------------------------------------------------------------------------

	@Test
	public void brandMustBeAWholeWord() {
		assertTrue(BrandMatcher.titleHasBrand("Samsung", "Samsung Galaxy M07 (Black)"));
		assertFalse(BrandMatcher.titleHasBrand("mi", "Philips Mixer Grinder"));   // "mi" is not inside "Mixer"
	}

	@Test
	public void aliasesPassTheirOwnBrand() {
		assertTrue(BrandMatcher.titleHasBrand("Motorola", "moto g37 5G"));
		assertTrue(BrandMatcher.titleHasBrand("Apple", "iPhone 17 Pro 512 GB"));
		assertTrue(BrandMatcher.titleHasBrand("Xiaomi", "Redmi 15C 5G"));
		assertTrue(BrandMatcher.titleHasBrand("Xiaomi", "POCO C85x 5G"));
		assertTrue(BrandMatcher.titleHasBrand("Mi", "Redmi Note 15"));
		assertTrue(BrandMatcher.titleHasBrand("Jio", "JioBharat V4 4G keypad phone"));
		assertTrue(BrandMatcher.titleHasBrand("Creative", "Sound Blaster X5"));
		assertTrue(BrandMatcher.titleHasBrand("Leaf Studios", "Leaf Bass Headphones"));
		assertTrue(BrandMatcher.titleHasBrand("ANT Esports", "Ant Mouse Pad"));
	}

	// An alias is looked up only in the brand's own list, so it cannot let another brand's product pass.
	@Test
	public void aliasesDoNotLeakToOtherBrands() {
		assertFalse(BrandMatcher.titleHasBrand("Samsung", "Redmi Note 15"));
		assertFalse(BrandMatcher.titleHasBrand("Oppo", "iPhone 15"));
	}

	// Amazon's typos are NOT aliased on purpose: an alias for a typo would hide wrong data.
	@Test
	public void typosAreNotAliased() {
		assertFalse(BrandMatcher.titleHasBrand("Xiaomi", "Radmi Note 15"));
	}
}
