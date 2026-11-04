package main.java.amazonfilterapplicatione2e.flows;

// One product card of the results list, as the card shows it. Every field is a String, and it is "" when the card
// does not show that part. CardFilterFlows fills it; the tests read it.
public class Card {

	public String asin = "";           // the card's data-asin
	public String title = "";
	public String price = "";          // the main price, e.g. "₹10,499"
	public String rating = "";         // e.g. "4.3 out of 5 stars"
	public String discount = "";       // the printed discount, e.g. "(33% off)"
	public String availability = "";   // "Currently unavailable" when the card says so
	public String delivery = "";       // the delivery line
	public String mrp = "";            // the crossed-out M.R.P.
	public String cardText = "";       // the whole card's text, for debugging

	// A card that says "currently unavailable" has no main offer: it has no price to compare.
	public boolean hasNoMainOffer() {
		return !availability.isEmpty();
	}
}
