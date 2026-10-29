package main.java.amazonfilterapplicatione2e.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// WHAT THIS CLASS DOES
// A test clicks a filter such as "Storage Capacity: 7 to 11.9 gb", opens the products shown, and asks:
// "does this product really have what the filter promised?"
//
// Searching the whole page text gives wrong answers (the filter is a range, the product says "8 gb").
// So we read ONE row of the product's spec table, where Amazon states the real value:
//
//     memory storage capacity 8 gb       <- the row, one line: label + value (lowercase)
//
// and compare it with the filter:  "7 to 11.9 gb"  and  8 gb  ->  8 is inside 7 to 11.9  ->  MATCH.
//
// THE THREE ANSWERS
//   MATCH     the product fits the filter
//   MISMATCH  the product has the row and it does NOT fit the filter (the only answer that fails a test)
//   UNKNOWN   the product has no such row, so we cannot tell. The test skips that product instead of failing it.
//
// Nothing here touches the browser, so it can be tested with plain strings.
public class FilterMatcher {

	public enum Status { MATCH, MISMATCH, UNKNOWN }

	// The answer, plus what the spec row said so a failure message can show it ("" if there was no row).
	public static class MatchResult {
		public final Status status;
		public final String rowValue;

		MatchResult(Status status, String rowValue) {
			this.status = status;
			this.rowValue = rowValue;
		}
	}

	// A unit: what it measures, and how many of the base unit it is (1 tb = 1024 gb).
	private static class Unit {
		final String measures;
		final double size;

		Unit(String measures, double size) {
			this.measures = measures;
			this.size = size;
		}
	}

	// A filter turned into numbers, in the base unit. max is infinite for "9 h & above".
	private static class Range {
		final double min;
		final double max;
		final String measures;
		final double size;

		Range(double min, double max, String measures, double size) {
			this.min = min;
			this.max = max;
			this.measures = measures;
			this.size = size;
		}
	}

	// Units we understand. "1 tb" can then be compared with "up to 512 gb".
	private static final Map<String, Unit> UNITS = new HashMap<>();
	static {
		UNITS.put("gb", new Unit("gb", 1));
		UNITS.put("tb", new Unit("gb", 1024));
		UNITS.put("mb", new Unit("gb", 1.0 / 1024));
		UNITS.put("ghz", new Unit("ghz", 1));
		UNITS.put("mhz", new Unit("ghz", 0.001));
		UNITS.put("h", new Unit("h", 1));
		UNITS.put("hr", new Unit("h", 1));
		UNITS.put("hrs", new Unit("h", 1));
		UNITS.put("hour", new Unit("h", 1));
		UNITS.put("hours", new Unit("h", 1));
		UNITS.put("min", new Unit("h", 1.0 / 60));
		UNITS.put("mins", new Unit("h", 1.0 / 60));
		UNITS.put("minute", new Unit("h", 1.0 / 60));
		UNITS.put("minutes", new Unit("h", 1.0 / 60));
		UNITS.put("mah", new Unit("mah", 1));
		UNITS.put("mp", new Unit("mp", 1));
		UNITS.put("megapixels", new Unit("mp", 1));
		UNITS.put("cm", new Unit("cm", 1));
		UNITS.put("centimetre", new Unit("cm", 1));
		UNITS.put("centimetres", new Unit("cm", 1));
		UNITS.put("centimeter", new Unit("cm", 1));
		UNITS.put("centimeters", new Unit("cm", 1));
		UNITS.put("inch", new Unit("cm", 2.54));
		UNITS.put("inches", new Unit("cm", 2.54));
	}

	private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");
	private static final Pattern NUMBER_AND_UNIT = Pattern.compile("(\\d+(?:\\.\\d+)?) ?([a-z]+)?");


	// ---------------------------------------------------------------------------------------------
	// THE CHECK. rowLabels: the names Amazon uses for the row, e.g. "memory storage capacity", "hard disk size".
	// The first label that has a row wins. All text is expected in lowercase.
	// ---------------------------------------------------------------------------------------------
	public static MatchResult rangeMatch(String filter, String[] rowLabels, String keyFeatures, String techDetails) {
		// Step 1: read the spec row. No row, no answer.
		String rowValue = findRowValue(keyFeatures, techDetails, rowLabels);
		if (rowValue.isEmpty()) {
			return new MatchResult(Status.UNKNOWN, "");
		}

		// Step 2: read the filter as a range. If it is not a range we know (for example "4 gb"),
		// the row has to contain the filter text as whole words.
		Range range = parseFilter(filter);
		if (range == null) {
			return new MatchResult(hasWord(rowValue, filter) ? Status.MATCH : Status.MISMATCH, rowValue);
		}

		// Step 3: it is a match if at least one number in the row is inside the range.
		double[] numbers = numbersInRow(rowValue, range);
		if (numbers.length == 0) {
			return new MatchResult(Status.UNKNOWN, rowValue);
		}
		for (double number : numbers) {
			if (number >= range.min && number <= range.max) {
				return new MatchResult(Status.MATCH, rowValue);
			}
		}
		return new MatchResult(Status.MISMATCH, rowValue);
	}


	// ---------------------------------------------------------------------------------------------
	// DISPLAY TYPE. The filter is a plain word such as "lcd" or "oled", not a range.
	// ---------------------------------------------------------------------------------------------
	public static MatchResult displayTypeMatch(String filter, String title, String keyFeatures, String about, String techDetails) {
		// Step 1: the "Display Type" row decides if the product has it. The filter must be a whole word in it:
		// "led" is not found in "controlled", and "oled" is not found in "amoled".
		String rowValue = findRowValue(keyFeatures, techDetails, new String[] { "display type" });
		if (!rowValue.isEmpty()) {
			return new MatchResult(hasWord(rowValue, filter) ? Status.MATCH : Status.MISMATCH, rowValue);
		}

		// Step 2: no such row. Look for the word anywhere in the product text. This is weaker (a title can mention
		// a word for another reason), so it can only PASS a product, never fail it.
		if (hasWord(title + " " + keyFeatures + " " + about + " " + techDetails, filter)) {
			return new MatchResult(Status.MATCH, "");
		}

		// Step 3: nothing to go on.
		return new MatchResult(Status.UNKNOWN, "");
	}


	// Finds the spec row for a label and returns its value.
	//   label "processor speed" and the line "processor speed 2.2 ghz"  ->  "2.2 ghz"
	private static String findRowValue(String keyFeatures, String techDetails, String[] labels) {
		String[] lines = (techDetails + "\n" + keyFeatures).split("\n");
		for (String label : labels) {
			for (String line : lines) {
				String text = line.trim().toLowerCase();
				if (text.startsWith(label) && text.length() > label.length()) {
					return text.substring(label.length()).trim();
				}
			}
		}
		return "";
	}

	// "Super AMOLED, LED" -> " super amoled led "  (a space around every word)
	private static String toWords(String text) {
		StringBuilder words = new StringBuilder(" ");
		for (String word : text.toLowerCase().split("[^a-z0-9]+")) {
			if (!word.isEmpty()) {
				words.append(word).append(" ");
			}
		}
		return words.toString();
	}

	// Is the filter a whole word (or words) inside the text?  "4 gb" in "4 gb" yes, "4 gb" in "64 gb" no.
	public static boolean hasWord(String text, String filter) {
		return toWords(text).contains(toWords(filter));
	}

	// "3,000" -> "3000", so it reads as one number
	private static String removeThousandsCommas(String text) {
		return text.toLowerCase().replaceAll("(\\d),(\\d{3})", "$1$2");
	}

	// Turns the filter text into a range, in the base unit.
	//   "up to 6.9 h" -> 0 to 6.9      "7 to 7.9 h" -> 7 to 7.9      "9 h & above" -> 9 and up
	// Returns null if the filter is not one of these shapes or uses a unit we do not know.
	private static Range parseFilter(String filter) {
		String text = removeThousandsCommas(filter);

		List<Double> numbers = new ArrayList<>();
		Matcher matcher = NUMBER.matcher(text);
		while (matcher.find()) {
			numbers.add(Double.parseDouble(matcher.group()));
		}

		Unit unit = null;
		for (String word : text.split("[^a-z]+")) {
			if (UNITS.containsKey(word)) {
				unit = UNITS.get(word);
				break;
			}
		}
		if (numbers.isEmpty() || unit == null) {
			return null;
		}

		double min = 0;
		double max = Double.POSITIVE_INFINITY;
		if (text.startsWith("up to") || text.startsWith("less than") || text.startsWith("under")) {
			max = numbers.get(0);                    // "up to 6.9 h": only an upper end
		} else if (numbers.size() == 2) {
			min = numbers.get(0);                    // "7 to 7.9 h": both ends
			max = numbers.get(1);
		} else if (text.contains("above") || text.contains("more")) {
			min = numbers.get(0);                    // "9 h & above": only a lower end
		} else {
			return null;
		}
		return new Range(min * unit.size, max * unit.size, unit.measures, unit.size);
	}

	// Reads the numbers out of the spec row, in the same base unit as the filter.
	//   row "2.2 ghz" -> [2.2]     row "800 mhz" -> [0.8]     row "2.0 50.0 mp" -> [50]
	// A number with a known unit is converted. A number with no unit at all is only used when no number
	// in the row has a unit, otherwise "2.0" in "2.0 50.0 mp" could pass a wrong product.
	private static double[] numbersInRow(String row, Range range) {
		// Camera rows list one value per camera and put the unit only at the end ("12, 5, 50 mp").
		// So for megapixels every number in the row counts, and the product passes if ANY one is in the range.
		// Known accepted risk: "2.0 50.0 mp" also counts the 2.0 (an aperture).
		if (range.measures.equals("mp")) {
			List<Double> all = new ArrayList<>();
			Matcher numbers = NUMBER.matcher(removeThousandsCommas(row));
			while (numbers.find()) {
				all.add(Double.parseDouble(numbers.group()));
			}
			double[] values = new double[all.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = all.get(i);
			}
			return values;
		}

		List<Double> withUnit = new ArrayList<>();
		List<Double> bare = new ArrayList<>();

		Matcher matcher = NUMBER_AND_UNIT.matcher(removeThousandsCommas(row));
		while (matcher.find()) {
			double value = Double.parseDouble(matcher.group(1));
			String unitWord = matcher.group(2);
			Unit unit = unitWord == null ? null : UNITS.get(unitWord);

			if (unit != null && unit.measures.equals(range.measures)) {
				withUnit.add(value * unit.size);
			} else if (unitWord == null) {
				bare.add(value * range.size);
			}
		}

		List<Double> chosen = withUnit.isEmpty() ? bare : withUnit;
		double[] result = new double[chosen.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = chosen.get(i);
		}
		return result;
	}
}
