package main.java.amazonfilterapplicatione2e.utilities;

import java.util.HashMap;
import java.util.Map;

// Decides if a product title belongs to a brand.
//
// A title must contain the brand as a WHOLE WORD ("mi" is not found in "mixer"), or one of the brand's aliases.
// Aliases are looked up only in that brand's own list, so an alias cannot let another brand's product pass:
// the title "Redmi Note 15" passes for the brands xiaomi and mi, and fails for samsung.
//
// Deliberately NOT added: aliases for Amazon's own typos ("Radmi", "beyerdymanic"), resellers, titles that only
// have a model name (Plantronics, Shure, AKG) and brands written as one word ("GADGETSWRAP"). An alias for a typo
// would hide wrong data, and those tests should keep failing so the data problem stays visible.
public class BrandMatcher {

	// Key = brand in lowercase. Value = other names the brand's products use in titles.
	private static final Map<String, String[]> ALIASES = new HashMap<>();
	static {
		ALIASES.put("motorola", new String[] { "moto" });
		ALIASES.put("apple", new String[] { "iphone" });
		ALIASES.put("panasonic", new String[] { "technics" });
		ALIASES.put("itel", new String[] { "zeno" });
		ALIASES.put("jio", new String[] { "jiobharat", "jiophone", "jiotag" });
		ALIASES.put("xiaomi", new String[] { "redmi", "poco" });
		ALIASES.put("mi", new String[] { "redmi" });
		ALIASES.put("tecno", new String[] { "spark" });
		ALIASES.put("creative", new String[] { "sound blaster" });
		ALIASES.put("leaf studios", new String[] { "leaf" });
		ALIASES.put("ant esports", new String[] { "ant" });
	}

	public static boolean titleHasBrand(String brand, String title) {
		if (FilterMatcher.hasWord(title, brand)) {
			return true;
		}

		String[] aliases = ALIASES.get(brand.trim().toLowerCase());
		if (aliases == null) {
			return false;
		}
		for (String alias : aliases) {
			if (FilterMatcher.hasWord(title, alias)) {
				return true;
			}
		}
		return false;
	}
}
