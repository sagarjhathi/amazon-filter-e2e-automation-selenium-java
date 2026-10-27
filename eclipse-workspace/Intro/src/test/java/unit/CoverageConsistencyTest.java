package test.java.unit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import main.java.amazonfilterapplicatione2e.fileReader.TestInputs;
import test.java.tests.AmazonTests;

// Checks that the test data files agree with each other and with the tests. A misspelt name would otherwise
// silently drop tests (a filter that no test uses, or a search term nobody listed).
// Run:  mvn test -Dsurefire.suiteXmlFiles=src/test/runners/UnitTests.xml
public class CoverageConsistencyTest {

	// The @Test methods of AmazonTests.
	private static Set<String> testMethods() {
		Set<String> names = new HashSet<>();
		for (Method method : AmazonTests.class.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Test.class)) {
				names.add(method.getName());
			}
		}
		return names;
	}

	@Test
	public void everyTestHasAFilter_andEveryFilterHasATest() {
		Set<String> methods = testMethods();
		Set<String> mapped = TestInputs.filterOfTest().keySet();

		Set<String> withoutFilter = new HashSet<>(methods);
		withoutFilter.removeAll(mapped);
		assertTrue(withoutFilter.isEmpty(), "Tests with no filter in TestInputs.FILTER_OF_TEST: " + withoutFilter);

		Set<String> withoutTest = new HashSet<>(mapped);
		withoutTest.removeAll(methods);
		assertTrue(withoutTest.isEmpty(), "Entries in TestInputs.FILTER_OF_TEST with no test method: " + withoutTest);
	}

	@Test
	public void everyCoverageFilterIsUsedByATest_andTheOtherWayRound() {
		Set<String> usedByTests = new HashSet<>(TestInputs.filterOfTest().values());
		Set<String> inCoverage = TestInputs.coverage().keySet();

		Set<String> unused = new HashSet<>(inCoverage);
		unused.removeAll(usedByTests);
		assertTrue(unused.isEmpty(), "Filters in filterCoverage.json that no test uses: " + unused);

		Set<String> missing = new HashSet<>(usedByTests);
		missing.removeAll(inCoverage);
		assertTrue(missing.isEmpty(), "Filters a test uses that are missing from filterCoverage.json: " + missing);
	}

	@Test
	public void everyCoverageTermIsAKnownSearchTerm() {
		for (Map.Entry<String, List<String>> entry : TestInputs.coverage().entrySet()) {
			for (String term : entry.getValue()) {
				assertTrue(TestInputs.searchTerms().contains(term),
						"The filter '" + entry.getKey() + "' lists the search term '" + term + "', which is not in searchTerms.json");
			}
		}
	}

	@Test
	public void everyHeadingOverrideIsForAFilterAndTermInCoverage() {
		for (Map.Entry<String, Map<String, String>> entry : TestInputs.headings().entrySet()) {
			String filter = entry.getKey();
			assertTrue(TestInputs.coverage().containsKey(filter), "filterHeadings.json has the filter '" + filter + "', which is not in filterCoverage.json");
			for (String term : entry.getValue().keySet()) {
				assertTrue(TestInputs.coverage().get(filter).contains(term),
						"filterHeadings.json has '" + filter + "' for '" + term + "', but filterCoverage.json does not list that term for it");
			}
		}
	}

	// The tests TestNG will run (one per search term for each test method) must be the tests the coverage file promises.
	@Test
	public void expectedTestCountEqualsTheRealCount() {
		int expected = 0;
		for (String filter : TestInputs.filterOfTest().values()) {
			expected += TestInputs.coverage().get(filter).size();
		}

		int real = 0;
		for (String method : testMethods()) {
			real += TestInputs.termsFor(method).size();
		}
		assertEquals(real, expected, "The number of runs differs from what filterCoverage.json promises");
		System.out.println("COVERAGE: " + real + " runs");
	}
}
