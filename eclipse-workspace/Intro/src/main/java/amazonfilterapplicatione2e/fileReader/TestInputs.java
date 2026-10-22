package main.java.amazonfilterapplicatione2e.fileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// Reads data/testInputs.json: for each test, the search terms it runs for.
// Example line:  "verifyingStorageCapacityFilterFunctionality": ["Mobile", "Tablet"]
// To run a test for one more product, add its search term to that test's line.
public class TestInputs {

	private static final String FILE = "data/testInputs.json";

	// Read once, the first time this class is used.
	private static final Map<String, List<String>> INPUTS = load();

	private static Map<String, List<String>> load() {
		try (InputStream in = TestInputs.class.getClassLoader().getResourceAsStream(FILE)) {
			if (in == null) {
				throw new IllegalStateException("File not found on the classpath: " + FILE);
			}

			// TypeToken tells Gson the type we want: test name -> list of search terms.
			Type type = new TypeToken<Map<String, List<String>>>() { }.getType();
			return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);

		} catch (IOException e) {
			throw new IllegalStateException("Could not read " + FILE, e);
		}
	}

	// The search terms for one test. A test without a line in the file is a mistake, so it fails loudly.
	public static List<String> termsFor(String testName) {
		List<String> terms = INPUTS.get(testName);

		if (terms == null || terms.isEmpty()) {
			throw new IllegalStateException("No search terms for the test '" + testName + "' in " + FILE);
		}
		return terms;
	}
}
