package main.java.amazonfilterapplicatione2e.pages;
import java.time.LocalDate;  
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.Assert;
import main.java.amazonfilterapplicatione2e.base.BasePage;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;


public class ProductListingPage extends  BasePage{

	private  final Logger log = LoggerUtility.getLogger(ProductListingPage.class);
	
	@FindBy(xpath="//div[@data-cy='title-recipe']")
	WebElement productNameListingPage;

	@FindBy(xpath="//span[@class='a-price-whole']")
	WebElement productPriceListingPage;

	@FindBy(xpath="//div[@data-cy='delivery-recipe']")
	WebElement productDeliveryDayListingPage;
	
	@FindBy(xpath="//div[@id='s-refinements']//span[@class='a-size-base a-color-base puis-bold-weight-text']")
	public List<WebElement> listOfFilterNameInLeftNav;


	public By listProcessorSpeedOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listBrandsOptionsByNew = By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::div[@class='a-section a-spacing-medium sf-navigation-searchable-content']//span[@class='a-size-base a-color-base']");

	
	
	public By listBrandsOptionsByOld = By.xpath(
			"//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base']"
			);
	
	public By listDisplaySizeOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listDisplayTypeOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Display Type']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	
	public By listDiscountOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Discount']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listBatteryCapacityOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Capacity']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listStorageCapacityOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listDeliveryDayOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listMobilePhonePrimaryCameraResolutionOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Mobile Phone Primary Camera Resolution']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By listOperatingSystemVersionOptionsBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Operating System']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base']"
			);

	public By getItByTomorrowUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It by Tomorrow']"
			);

	public By getItInTwoDaysUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It in 2 Days']"
			);
	
	public By eligibleForFreeDelivery= By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Eligible for Free Delivery']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Free Shipping']");

	public By getItTodayUnderDeliveryDayFilterBy = By.xpath(
			"//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']" +
					"/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='Get It Today']"
			);



	public By seeMoreButtonUnderOperatingSystemFilter=	By.xpath("//a[@aria-label='See more, Operating System']");

	public By seeMoreButtonUnderBrandFilter=By.xpath("//a[@aria-label='See more, Brands']");

	public By listProductCardsBy=By.xpath("//div[@class='a-section a-spacing-small a-spacing-top-small']");

	public By priceMinSliderButton=By.id("p_36/range-slider_slider-item_lower-bound-slider");

	public By priceMaxSliderButton=By.id("p_36/range-slider_slider-item_upper-bound-slider");
	
	public By priceSliderSubmitButton=By.xpath("//div[@class='a-section sf-submit-range-button']");
	
	public By maxPriceFilterApplied=By.xpath("//label[@for='p_36/range-slider_slider-item_upper-bound-slider']");
	
	public By minPriceFilterApplied=By.xpath("//label[@for='p_36/range-slider_slider-item_lower-bound-slider']");
	
	public By productPriceFromProductCards=By.xpath("//span[@class='a-price-whole']");
	
	public  By productNameListingPageBy=By.xpath("//div[@data-cy='title-recipe']");
	
	public By productNameIndividualPage=By.xpath("//span[@id='productTitle']");
	
	public By productKeyFeatureBlock= By.xpath("//div[@class='a-section a-spacing-small a-spacing-top-small']");
	
	public By aboutThisItemBulletPoint=By.xpath("//div[@id='feature-bullets']");
	
	public By technicalDetailsBlockIndividualPage =By.xpath("//div[@id='prodDetails']");

	// The rows of the technical details table. The section counts as present once it has at least one row.
	public By technicalDetailsRowsBy=By.xpath("//div[@id='prodDetails']//tr");

	// The headings of the left navigation (Brands, Storage Capacity, ...).
	public By leftNavHeadingsBy=By.xpath("//div[@id='s-refinements']//span[@class='a-size-base a-color-base puis-bold-weight-text']");
	
	public 	By seeMoreProductDetailsButtonIndividualPageBy=By.xpath("//a[@id='seeMoreDetailsLink']");
	
	public By clearButtonBy=By.xpath("//span[@class='a-size-base a-color-base' and text()='Clear']");

	public By seeMoreButtonIndividualPage=By.xpath("//span[@class='a-expander-prompt' and text()='Show More']");
	
	public By reportAnIssue=By.xpath("//div[@id='CardInstanceCnrYlcUauBaBV5oi0X1UVw']");

	public By showMoreOnlyIndividualPage=By.xpath("//span[@class='a-expander-prompt' and text()='Show More']");

	public By resetPriceRangeProductPage=By.xpath("//span[@class='a-size-base a-color-link' and text()='Reset price range']");
	
		
	
	public By getfilterByTypeAndName(String filterName, String filterOption) {
		log.info("[{}] Within getfilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='" + filterOption + "']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='"+filterOption+"']");
		case "batterycapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base' and text()='" + filterOption + "']");
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
			// OLD: throw new IllegalArgumentException("Unknown filter type: " + filterName);
			// Any other name is used as the heading itself (for example "HDD Size"), so a new filter needs no new case.
			return optionUnderHeading(filterName, filterOption);
			
		}
	}

	
	
	
	public By getAppliedfilterByTypeAndName(String filterName, String filterOption) {
		log.info("[{}] Within getfilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='" + filterOption + "']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+filterOption+"']");
		case "batterycapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Capacity']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='" + filterOption + "']");
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
			// OLD: return By.xpath("//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base' and text()='"+ filterOption + "']");   (a copy of the click locator: a selected brand has the extra class a-text-bold, so it was never found)
			return By.xpath("//ul[@id='filter-p_123']//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		case "screensize" :
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Screen Size']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='"+ filterOption + "']");
		default:
			// OLD: throw new IllegalArgumentException("Unknown filter type: " + filterName);
			// Any other name is used as the heading itself (for example "HDD Size"), so a new filter needs no new case.
			return selectedOptionUnderHeading(filterName, filterOption);
			
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

	// FILTERS BY ANY HEADING. Amazon uses different headings for the same filter on different products
	// ("Storage Capacity" on phones, "HDD Size" on laptops), so the locators below take the heading as a parameter.
	private static final String HEADING_START = "//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='";
	private static final String OPTIONS_AFTER_HEADING = "']/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base";

	// The heading itself, e.g. the "HDD Size" title in the left navigation.
	public By headingBy(String heading) {
		return By.xpath(HEADING_START + heading + "']");
	}

	// All the options listed under a heading.
	public By optionsUnderHeading(String heading) {
		return By.xpath(HEADING_START + heading + OPTIONS_AFTER_HEADING + "']");
	}

	// One option under a heading, to click it.
	public By optionUnderHeading(String heading, String option) {
		return By.xpath(HEADING_START + heading + OPTIONS_AFTER_HEADING + "' and text()='" + option + "']");
	}

	// The same option once it is selected (Amazon makes a selected option bold: a-text-bold).
	public By selectedOptionUnderHeading(String heading, String option) {
		return By.xpath(HEADING_START + heading + OPTIONS_AFTER_HEADING + " a-text-bold' and text()='" + option + "']");
	}

	// The same delivery option once it is selected (Amazon makes a selected option bold: a-text-bold).
	// filterOption is the name returned by getDeliveryDayFilterByName.
	public By getSelectedDeliveryOptionBy(String filterOption) {
		String deliveryDay = "//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Delivery Day']"
				+ "/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='";

		switch (filterOption) {
		case "Get It By Today":
			return By.xpath(deliveryDay + "Get It Today']");
		case "Get It by Tomorrow":
			return By.xpath(deliveryDay + "Get It by Tomorrow']");
		case "Get It in 2 Days":
			return By.xpath(deliveryDay + "Get It in 2 Days']");
		case "Eligible for Free Delivery":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Eligible for Free Delivery']"
					+ "/parent::div/following-sibling::ul//span[@class='a-size-base a-color-base a-text-bold' and text()='Free Shipping']");
		default:
			throw new IllegalArgumentException("Unknown delivery option: " + filterOption);
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
	    
	    

	
	public By getfilterHeaderByTypeAndName(String filterName) {
		log.info("[{}] Within getAppliedfilterByTypeAndName method", ThreadContext.get("testName"));

		switch (filterName.toLowerCase()) {
		case "processorspeed":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Processor Speed']");
		case "storagecapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Storage Capacity']");
		case "brands":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Brands']");
		case "batterycapacity":
			return By.xpath("//span[@class='a-size-base a-color-base puis-bold-weight-text' and text()='Battery Capacity']");
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
			// OLD: throw new IllegalArgumentException("Unknown filter type: " + filterName);
			// Any other name is used as the heading itself (for example "HDD Size"), so a new filter needs no new case.
			return headingBy(filterName);
		}
		

	}
	 
	
	
	
	public By getProductByIndex(int index) {
		log.info("[{}] Within getProductByIndex method", ThreadContext.get("testName"));
		return By.xpath("(//div[@data-cy='title-recipe'])[" + index + "]");
	}
	 
	 

	
	
}