package main.java.amazonfilterapplicatione2e.fileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Reads the test data in src/main/resources/data:
//   searchTerms.json    the search terms that exist (Laptop, Mobile, ...)
//   filterCoverage.json for each filter, the search terms it applies to. A test runs only for those terms.
//   filterHeadings.json only the headings that differ from the filter's own name, per search term
//                       (Amazon calls the same filter "HDD Size" on laptops and "Storage Capacity" on phones).
//
// To run a filter for one more product, add the search term to that filter in filterCoverage.json.
//
// OLD: data/testInputs.json held one line per test method with its search terms. It was replaced by
// filterCoverage.json (keyed by filter, not by test method) so the coverage reads like the left navigation.
public class TestInputs {

	// Which filter each test method checks. The name must match a key in filterCoverage.json.
	private static final Map<String, String> FILTER_OF_TEST = new HashMap<>();
	static {
		FILTER_OF_TEST.put("verifyingTheBrandsFilterFunctionality", "brands");
		FILTER_OF_TEST.put("verifyingBatteryCapacityFilterFunctionality", "Battery");
		FILTER_OF_TEST.put("verifyingDiscountFilterFunctionality", "Discount");
		FILTER_OF_TEST.put("verifyingDisplaySizeFilterFunctionality", "Screen Size");
		FILTER_OF_TEST.put("verifyingStorageCapacityFilterFunctionality", "Storage Capacity");
		FILTER_OF_TEST.put("verifyingDisplayTypeFilterFunctionality", "Display Type");
		FILTER_OF_TEST.put("verifyingProcessorSpeedFilterFunctionality", "Processor Speed");
		FILTER_OF_TEST.put("verifyingMobilePhonePrimaryCameraResolutionFilterFunctionality", "Mobile Phone Primary Camera Resolution");
		FILTER_OF_TEST.put("verifyingOperatingSystemVersionFilterFunctionality", "Operating System Version");
		FILTER_OF_TEST.put("verifyingPriceSilderFunctionality", "Price");
		FILTER_OF_TEST.put("verifyingGetItByTodayFilterFunctionality", "Get It Today");
		FILTER_OF_TEST.put("verifyingGetItByTomorrowFilterFunctionality", "Get It by Tomorrow");
		FILTER_OF_TEST.put("verifyingGetItIn2DaysFilterFunctionality", "Get It in 2 Days");
		FILTER_OF_TEST.put("verifyingFreeDeliveryFilterFunctionality", "Free Delivery");
	}

	// Read once, the first time this class is used.
	private static final List<String> SEARCH_TERMS = read("data/searchTerms.json", new TypeToken<List<String>>() { }.getType());
	private static final Map<String, List<String>> COVERAGE = read("data/filterCoverage.json", new TypeToken<Map<String, List<String>>>() { }.getType());
	private static final Map<String, Map<String, String>> HEADINGS = read("data/filterHeadings.json", new TypeToken<Map<String, Map<String, String>>>() { }.getType());

	private static <T> T read(String file, Type type) {
		try (InputStream in = TestInputs.class.getClassLoader().getResourceAsStream(file)) {
			if (in == null) {
				throw new IllegalStateException("File not found on the classpath: " + file);
			}
			return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
		} catch (IOException e) {
			throw new IllegalStateException("Could not read " + file, e);
		}
	}

	// The search terms for one test. A test with no filter or no terms is a mistake, so it fails loudly.
	public static List<String> termsFor(String testName) {
		String filter = FILTER_OF_TEST.get(testName);
		if (filter == null) {
			throw new IllegalStateException("The test '" + testName + "' has no filter in TestInputs.FILTER_OF_TEST");
		}

		List<String> terms = COVERAGE.get(filter);
		if (terms == null || terms.isEmpty()) {
			throw new IllegalStateException("No search terms for the filter '" + filter + "' (test '" + testName + "') in data/filterCoverage.json");
		}
		return terms;
	}

	// The heading Amazon uses for a filter on a given search term: the override from filterHeadings.json,
	// or the filter's own name when there is none.   headingFor("Storage Capacity", "Laptop") -> "HDD Size"
	public static String headingFor(String filter, String searchTerm) {
		Map<String, String> overrides = HEADINGS.get(filter);
		if (overrides != null && overrides.containsKey(searchTerm)) {
			return overrides.get(searchTerm);
		}
		return filter;
	}

	// Read-only views, used by the consistency check (src/test/java/unit/CoverageConsistencyTest).
	public static Map<String, String> filterOfTest() { return FILTER_OF_TEST; }
	public static List<String> searchTerms() { return SEARCH_TERMS; }
	public static Map<String, List<String>> coverage() { return COVERAGE; }
	public static Map<String, Map<String, String>> headings() { return HEADINGS; }
}
