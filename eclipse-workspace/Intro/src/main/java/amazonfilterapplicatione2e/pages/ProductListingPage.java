package main.java.amazonfilterapplicatione2e.pages;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;


public class ProductListingPage extends  BasePage{

	private  final Logger log = LoggerUtility.getLogger(ProductListingPage.class);
	
	@FindBy(xpath="//div[@data-cy='title-recipe']")
	WebElement productNameListingPage;

	@FindBy(xpath="//span[@class='a-price-whole']")
	WebElement productPriceListingPage;

	@FindBy(xpath="//div[@data-cy='delivery-recipe']")
	WebElement productDeliveryDayListingPage;
	
	// OLD CODE (kept as comments): this list had no wait, so a page that was still loading looked like "no filters".
	// It was only used by the old filterCheckUnderList(...) methods in GenericUtility.
	// @FindBy(xpath="//div[@id='s-refinements']//span[@class='a-size-base a-color-base puis-bold-weight-text']")
	// public List<WebElement> listOfFilterNameInLeftNav;

	// NEW CODE: same XPath as a By locator, used by getFilterNames(...) below, which waits for it.
	// Headings of the filters in the left navigation (Brands, Storage Capacity, ...).
	private By filterNameHeadingsBy = By.xpath("//div[@id='s-refinements']//span[@class='a-size-base a-color-base puis-bold-weight-text']");

	// How long to wait for the filters panel to appear after a search.
	public static final Duration FILTER_PANEL_TIMEOUT = Duration.ofSeconds(10);

	// Waits for the first filter heading to appear, then returns the text of all headings.
	// (findElements() alone does not wait, so a page that is still loading would look like "no filters".)
	// If the panel never appears, an empty list is returned instead of an exception.
	public List<String> getFilterNames(Duration timeout) {
		List<String> names = new ArrayList<>();

		try {
			new WebDriverWait(driver, timeout)
					.until(ExpectedConditions.visibilityOfElementLocated(filterNameHeadingsBy));
		} catch (TimeoutException e) {
			System.out.println("EMPTY LISTTTTTTTTTT");
			return names; // empty list: the panel did not show up in time
		}

		for (WebElement heading : driver.findElements(filterNameHeadingsBy)) {
			String text = heading.getText().trim();
			if (!text.isEmpty()) {
				names.add(text);
			}
		}
		return names;
	}


	// Locator rules:
	// 1. Every locator field (type By) ends with the word "By".
	// 2. Locators are private. Other classes read them through the accessor methods at the bottom of this file,
	//    for example: productPage.reportAnIssueBy()
	private By listProcessorSpeedOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listBrandsOptionsNewBy = By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::div[@class='a-section a-spacing-medium sf-navigation-searchable-content']//span[@class='a-size-base a-color-base']");

	
	
	private By listBrandsOptionsOldBy = By.xpath(
			"//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base']"
			);
	
	private By listDisplaySizeOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listDisplayTypeOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Type']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	
	private By listDiscountOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Discount']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listBatteryCapacityOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Life']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listStorageCapacityOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listDeliveryDayOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listMobilePhonePrimaryCameraResolutionOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Mobile Phone Primary Camera Resolution']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By listOperatingSystemVersionOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	private By getItByTomorrowUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It by Tomorrow']"
			);

	private By getItInTwoDaysUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It in 2 Days']"
			);
	
	private By eligibleForFreeDeliveryBy= By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Eligible for Free Delivery']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Free Shipping']");

	private By getItTodayUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It Today']"
			);



	private By seeMoreButtonUnderOperatingSystemFilterBy=	By.xpath("//a[@aria-label='See more, Operating System']");

	private By seeMoreButtonUnderBrandFilterBy=By.xpath("//a[@aria-label='See more, Brands']");

	private By listProductCardsBy=By.xpath("//div[@class='a-section a-spacing-small a-spacing-top-small']");

	private By priceMinSliderButtonBy=By.id("p_36/range-slider_slider-item_lower-bound-slider");

	private By priceMaxSliderButtonBy=By.id("p_36/range-slider_slider-item_upper-bound-slider");
	
	private By priceSliderSubmitButtonBy=By.xpath("//div[@class='a-section sf-submit-range-button']");
	
	private By maxPriceFilterAppliedBy=By.xpath("//label[@for='p_36/range-slider_slider-item_upper-bound-slider']");
	
	private By minPriceFilterAppliedBy=By.xpath("//label[@for='p_36/range-slider_slider-item_lower-bound-slider']");
	
	private By productPriceFromProductCardsBy=By.xpath("//span[@class='a-price-whole']");
	
	private By productNameListingPageBy=By.xpath("//div[@data-cy='title-recipe']");
	
	private By productNameIndividualPageBy=By.xpath("//span[@id='productTitle']");
	
	private By productKeyFeatureBlockBy= By.xpath("//div[@class='a-section a-spacing-small a-spacing-top-small']");
	
	private By aboutThisItemBulletPointBy=By.xpath("//div[@id='feature-bullets']");
	
	private By technicalDetailsBlockIndividualPageBy =By.xpath("//div[@id='prodDetails']");
	
	private By seeMoreProductDetailsButtonIndividualPageBy=By.xpath("//a[@id='seeMoreDetailsLink']");
	
	private By clearButtonBy=By.xpath("//span[@class='a-size-base a-color-base' and text()='Clear']");

	private By seeMoreButtonIndividualPageBy=By.xpath("//span[@class='a-expander-prompt' and text()='Show More']");
	
	private By reportAnIssueBy=By.xpath("//div[@id='CardInstanceCnrYlcUauBaBV5oi0X1UVw']");

	private By showMoreOnlyIndividualPageBy=By.xpath("//span[@class='a-expander-prompt' and text()='Show More']");

	private By resetPriceRangeProductPageBy=By.xpath("//span[@class='a-size-base a-color-link' and text()='Reset price range']");
	
		
	
	public By getFilterByTypeAndName(String filterName, String filterOption) {
		log.info("[{}] Within getFilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='" + filterOption + "']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+filterOption+"']");
		case "batterylife":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Life']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='" + filterOption + "']");
		case "displaysize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Size']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "displaytype":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Type']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "operatingsystem":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");			
		case "mobilephoneprimarycameraresolution":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Mobile Phone Primary Camera Resolution']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "discount":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Discount']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "Operating System Version":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System Version']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "brandsold":
			return By.xpath("//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "screensize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		default:
			throw new IllegalArgumentException("Unknown filter type: " + filterName);
			
		}
	}

	
	
	
	public By getAppliedFilterByTypeAndName(String filterName, String filterOption) {
		log.info("[{}] Within getFilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='" + filterOption + "']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+filterOption+"']");
		case "batterylife":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Life']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='" + filterOption + "']");
		case "displaysize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Size']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "displaytype":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Type']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "operatingsystem":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");			
		case "mobilephoneprimarycameraresolution":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Mobile Phone Primary Camera Resolution']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "discount":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Discount']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "Operating System Version":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System Version']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "brandsold":
			return By.xpath("//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "screensize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		default:
			throw new IllegalArgumentException("Unknown filter type: " + filterName);
			
		}
	}
	
	
		
	
	    
	    
	public By getMoreButtonByFilterTypeAndName(String filterName) {
		log.info("[{}] Within getMoreButtonByFilterTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "brands":
			return By.xpath("//a[@aria-label='See more, Brands']");
		case "operatingsystem":
			return  By.xpath("//a[@aria-label='See more, Operating System']");

		default:
			throw new IllegalArgumentException("Unknown filter type: " + filterName);
		}
	}
	    
	
	
	    
	public String getDeliveryDayFilterByName(String filterName) {
		log.info("[{}] Within getMoreButtonByFilterTypeAndName method", ThreadContext.get("testName"));

		switch (filterName) {
		case "verifyingGetItByTomorrowFilterFunctionality":
			return "Get It by Tomorrow";
		case "verifyingGetItIn2DaysFilterFunctionality":
			return  "Get It in 2 Days";
		case "verifyingGetItByTodayFilterFunctionality":
			return  "Get It By Today";
		case "verifyingFreeDeliveryFilterFunctionality":
			return  "Eligible for Free Delivery";
		default:
			throw new IllegalArgumentException("Unknown filter type: " + filterName);
		}
	}
	    
	    

	
	public By getFilterHeaderByTypeAndName(String filterName) {
		log.info("[{}] Within getAppliedFilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']");
		case "batterylife":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Life']");
		case "displaysize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Size']");
		case "displaytype":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Type']");
		case "operatingsystem":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System']");	
		case "mobilephoneprimarycameraresolution":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Mobile Phone Primary Camera Resolution']");
		case "discount":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Discount']");
		case "brandsold":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']");
		case "screensize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']");
		
		default:
			throw new IllegalArgumentException("Unknown filter type: " + filterName);
		}
		

	}
	 
	
	
	
	public By getProductByIndex(int index) {
		log.info("[{}] Within getProductByIndex method", ThreadContext.get("testName"));
		return By.xpath("(//div[@data-cy='title-recipe'])[" + index + "]");
	}
	 
	 

	
	

	// Accessors: the locator fields above are private, other classes read them through these methods.
	public By listProcessorSpeedOptionsBy() {
		return listProcessorSpeedOptionsBy;
	}

	public By listBrandsOptionsNewBy() {
		return listBrandsOptionsNewBy;
	}

	public By listBrandsOptionsOldBy() {
		return listBrandsOptionsOldBy;
	}

	public By listDisplaySizeOptionsBy() {
		return listDisplaySizeOptionsBy;
	}

	public By listDisplayTypeOptionsBy() {
		return listDisplayTypeOptionsBy;
	}

	public By listDiscountOptionsBy() {
		return listDiscountOptionsBy;
	}

	public By listBatteryCapacityOptionsBy() {
		return listBatteryCapacityOptionsBy;
	}

	public By listStorageCapacityOptionsBy() {
		return listStorageCapacityOptionsBy;
	}

	public By listDeliveryDayOptionsBy() {
		return listDeliveryDayOptionsBy;
	}

	public By listMobilePhonePrimaryCameraResolutionOptionsBy() {
		return listMobilePhonePrimaryCameraResolutionOptionsBy;
	}

	public By listOperatingSystemVersionOptionsBy() {
		return listOperatingSystemVersionOptionsBy;
	}

	public By getItByTomorrowUnderDeliveryDayFilterBy() {
		return getItByTomorrowUnderDeliveryDayFilterBy;
	}

	public By getItInTwoDaysUnderDeliveryDayFilterBy() {
		return getItInTwoDaysUnderDeliveryDayFilterBy;
	}

	public By eligibleForFreeDeliveryBy() {
		return eligibleForFreeDeliveryBy;
	}

	public By getItTodayUnderDeliveryDayFilterBy() {
		return getItTodayUnderDeliveryDayFilterBy;
	}

	public By seeMoreButtonUnderOperatingSystemFilterBy() {
		return seeMoreButtonUnderOperatingSystemFilterBy;
	}

	public By seeMoreButtonUnderBrandFilterBy() {
		return seeMoreButtonUnderBrandFilterBy;
	}

	public By listProductCardsBy() {
		return listProductCardsBy;
	}

	public By priceMinSliderButtonBy() {
		return priceMinSliderButtonBy;
	}

	public By priceMaxSliderButtonBy() {
		return priceMaxSliderButtonBy;
	}

	public By priceSliderSubmitButtonBy() {
		return priceSliderSubmitButtonBy;
	}

	public By maxPriceFilterAppliedBy() {
		return maxPriceFilterAppliedBy;
	}

	public By minPriceFilterAppliedBy() {
		return minPriceFilterAppliedBy;
	}

	public By productPriceFromProductCardsBy() {
		return productPriceFromProductCardsBy;
	}

	public By productNameListingPageBy() {
		return productNameListingPageBy;
	}

	public By productNameIndividualPageBy() {
		return productNameIndividualPageBy;
	}

	public By productKeyFeatureBlockBy() {
		return productKeyFeatureBlockBy;
	}

	public By aboutThisItemBulletPointBy() {
		return aboutThisItemBulletPointBy;
	}

	public By technicalDetailsBlockIndividualPageBy() {
		return technicalDetailsBlockIndividualPageBy;
	}

	public By seeMoreProductDetailsButtonIndividualPageBy() {
		return seeMoreProductDetailsButtonIndividualPageBy;
	}

	public By clearButtonBy() {
		return clearButtonBy;
	}

	public By seeMoreButtonIndividualPageBy() {
		return seeMoreButtonIndividualPageBy;
	}

	public By reportAnIssueBy() {
		return reportAnIssueBy;
	}

	public By showMoreOnlyIndividualPageBy() {
		return showMoreOnlyIndividualPageBy;
	}

	public By resetPriceRangeProductPageBy() {
		return resetPriceRangeProductPageBy;
	}
}
