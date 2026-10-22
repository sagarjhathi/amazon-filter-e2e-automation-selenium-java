package test.java.tests;

import static org.testng.Assert.expectThrows;       
import java.time.Duration; 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import main.java.amazonfilterapplicatione2e.base.BaseTest;
import main.java.amazonfilterapplicatione2e.captcha.CaptchaHandler;
import main.java.amazonfilterapplicatione2e.captcha.BlockerHandler;
import main.java.amazonfilterapplicatione2e.flows.BrandFilterFlows;
import main.java.amazonfilterapplicatione2e.flows.DeliveryFilterFlows;
import main.java.amazonfilterapplicatione2e.flows.OperatingSystemFilterFlows;
import main.java.amazonfilterapplicatione2e.flows.PriceSliderFlows;
import main.java.amazonfilterapplicatione2e.flows.SharedFilterFlows;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;
import main.java.amazonfilterapplicatione2e.pages.AmazonLandingPage;
import main.java.amazonfilterapplicatione2e.pages.ProductListingPage;
import main.java.amazonfilterapplicatione2e.reporting.ReportManager;
import main.java.amazonfilterapplicatione2e.safeActions.SafeActions;
import main.java.amazonfilterapplicatione2e.utilities.GenericUtility;
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtilUpdated;
import test.java.retry.RetryFailedTest;
import test.testDataProvider.TestDataProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class AmazonTests extends BaseTest {

	
	private  final Logger log = LoggerUtility.getLogger(AmazonTests.class);
	
	
	
	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingGetItByTomorrowFilterFunctionality(String input) throws InterruptedException, TimeoutException{
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
	    GenericUtility genericUtility=new GenericUtility();
	    DeliveryFilterFlows deliveryFilterFlows = new DeliveryFilterFlows();
		
		landingPage.openingLandingPage();
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();                   
		ScreenshotUtilUpdated.capture(input);
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");
       
       
        if (!genericUtility.isElementInViewport(productPage.getItByTomorrowUnderDeliveryDayFilterBy)) {
		    System.out.println("Filter option 'Get It by Tomorrow' does not exist. Skipping the test.");
		    log.warn("[{}]   Filter option 'Get It by Tomorrow' does not exist. Skipping the test.", testName);
		    // OLD: return;   (TestNG counts a plain return as a PASS)
		    throw new SkipException("Filter 'Get It by Tomorrow' is not available for the search term '" + input + "'");
		}
        
	
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItByTomorrowUnderDeliveryDayFilterBy);
			
	    
	    boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery filter validation failed at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);      
			
		}
	
	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class,groups = { "DeliveryPriceOs" }
		)
	public void verifyingGetItIn2DaysFilterFunctionality(String input) throws InterruptedException, TimeoutException{
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		 
		 AmazonLandingPage landingPage=new AmazonLandingPage();
		 CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		 GenericUtility genericUtility=new GenericUtility();
		 ProductListingPage productPage=new ProductListingPage();
		 DeliveryFilterFlows deliveryFilterFlows = new DeliveryFilterFlows();
		 
		 ScreenshotUtilUpdated.capture(input);
		 
		 landingPage.openingLandingPage();
			SafeActions safeAct = new SafeActions();
			safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
 		 blockerHandler.clearBlockers();
 		 landingPage.givingInputWithinSearchBar(input);
         landingPage.clickingOnSubmitSearchButton();

		 blockerHandler.clearBlockers();   
		 String testName=ThreadContext.get("testName");
		 
		 
 		if (!genericUtility.isElementInViewport(productPage.getItInTwoDaysUnderDeliveryDayFilterBy)) {
 		    System.out.println("Filter option 'Get It in 2 Days' does not exist. Skipping the test.");
 		    log.warn("[{}]  Filter option 'Get It in 2 Days' does not exist. Skipping the test.", testName);
 		    // OLD: return;   (TestNG counts a plain return as a PASS)
 		    throw new SkipException("Filter 'Get It in 2 Days' is not available for the search term '" + input + "'");
 		}                 
		            	
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItInTwoDaysUnderDeliveryDayFilterBy);
		
	    boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery filter validation failed at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);      		
	    
		}
	



	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingGetItByTodayFilterFunctionality(String input) throws InterruptedException, TimeoutException{
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
 		ProductListingPage productPage=new ProductListingPage();
 		DeliveryFilterFlows deliveryFilterFlows = new DeliveryFilterFlows();
		
 		
 		landingPage.openingLandingPage();
 		ScreenshotUtilUpdated.capture();
 		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		ScreenshotUtilUpdated.capture(input);
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		ScreenshotUtilUpdated.capture(input);
 		blockerHandler.clearBlockers();
 		
 		 String testName=ThreadContext.get("testName");
 		if (!genericUtility.isElementInViewport(productPage.getItTodayUnderDeliveryDayFilterBy)) {
 		    System.out.println("Filter option 'Get It Today' does not exist. Skipping the test.");
 		   log.warn("[{}]  Filter option 'Get It Today' does not exist. Skipping the test.", ThreadContext.get("testName"));
 		    // OLD: return;   (TestNG counts a plain return as a PASS)
 		    throw new SkipException("Filter 'Get It Today' is not available for the search term '" + input + "'");
 		}                   
		            		
		            		
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItTodayUnderDeliveryDayFilterBy);
		
		boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery filter validation failed at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);                 		  
		            	    
	    
	}
		    
	
	
	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingFreeDeliveryFilterFunctionality(String input) throws InterruptedException, TimeoutException{
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
 		ProductListingPage productPage=new ProductListingPage();
 		DeliveryFilterFlows deliveryFilterFlows = new DeliveryFilterFlows();
		
 		
 		landingPage.openingLandingPage();
 		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
 		blockerHandler.clearBlockers();
 		
 		 String testName=ThreadContext.get("testName");
 		if (!genericUtility.isElementInViewport(productPage.eligibleForFreeDelivery)) {
 		    System.out.println("Filter option 'Eligible for Free Delivery does not. Skipping the test.");
 		   log.warn("[{}]  Filter option 'Eligible for Free Delivery' does not exist. Skipping the test.", ThreadContext.get("testName"));
 		    // OLD: return;   (TestNG counts a plain return as a PASS)
 		    throw new SkipException("Filter 'Eligible for Free Delivery' is not available for the search term '" + input + "'");
 		}                   
		            		
		            		
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.eligibleForFreeDelivery);
		
		boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery filter validation failed at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);                 		  
		            	    
	    
	}
		            		  
	
	
	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BrandsScreenSizeTypeCamEtc" }
		)
	public void verifyingTheBrandsFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");

		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
		SafeActions safeAct=new SafeActions();
		GenericUtility genericUtility=new GenericUtility();
		BrandFilterFlows brandFilterFlows = new BrandFilterFlows();
		SoftAssert softAssert = new SoftAssert();


		landingPage.openingLandingPage();
	
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		if (!genericUtility.filterCheckUnderList("brands")) {
			System.out.println("Filter option 'brands' does not exist in the list. Skipping the test.");
			log.warn("[{}] Filter option 'brands' does not exist in the list. Skipping the test.", testName);
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'brands' is not available for the search term '" + input + "'");
		}


		List<WebElement> filterOptions = safeAct.safeFindElements(productPage.listBrandsOptionsByNew);
		List<Map<Object, Object>> allResults=null;


		if(filterOptions==null) {
			allResults = brandFilterFlows.applyFilterAndValidateBrandsFilterWithResult(productPage.listBrandsOptionsByOld, "brandsold");
		}else {
			allResults = brandFilterFlows.applyFilterAndValidateBrandsFilterWithResult(productPage.listBrandsOptionsByNew, "brands");
		}


		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(allResults != null && !allResults.isEmpty(), "No products were validated for the filter 'brands' (search term '" + input + "')");

		for (Map<Object, Object> result : allResults) {

			boolean isValid = (boolean) result.get("isValid");
			String brand = (String) result.get("brand");
			List<String> mismatches = (List<String>) result.get("mismatches");

			if (!isValid) {
				log.error("[{}] Brand validation failed for '{}'. {} mismatches found.", testName, brand, mismatches.size());
				System.out.println("Brand validation failed for brand: " + brand);
				for (String detail : mismatches) {
					System.out.println(detail);
					log.error("[{}]  Mismatch for '{}': {}", testName, brand, detail);
					softAssert.fail(detail);
				}
			} else {
				log.info("[{}] All product titles matched for brand: '{}'", testName, brand);
				System.out.println("All product titles matched for brand: " + brand);
			}
		}

		log.info("[{}]  Asserting all soft assertions now...", testName);
		softAssert.assertAll();


	}
	
	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BatteryProcessorStorage" }
		)
	public void verifyingStorageCapacityFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
		ProductListingPage productPage=new ProductListingPage();
		SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
		SoftAssert softAssert = new SoftAssert();
		
		
		
		landingPage.openingLandingPage();
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");
		
		
		if (!genericUtility.filterCheckUnderList("Storage Capacity")) {
			System.out.println("Filter option 'Storage Capacity' does not exist in the list. Skipping the test.");
			log.warn("[{}]  Filter option 'Storage Capacity' does not exist in the list. Skipping the test.", ThreadContext.get("testName"));
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Storage Capacity' is not available for the search term '" + input + "'");
		}


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listStorageCapacityOptionsBy,"storagecapacity");
		log.info("[{}] Retrieved {} results from applyFilterAndValidateProductsWithResult.",testName,"And result size->" +results.size());
		

		// Drop products whose page details were not captured, so they are not reported as mismatches.
		results = genericUtility.onlyCapturedProducts(results, testName);
		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Storage Capacity' (search term '" + input + "')");

		for (Map<String, Object> product : results) {
			String filter = genericUtility.safeLower(product.get("filter"));
    	    String title = genericUtility.safeLower(product.get("title"));
    	    String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
    	    String about = genericUtility.safeLower(product.get("about"));
    	    String techDetails = genericUtility.safeLower(product.get("techDetails"));

    	    boolean isMatch = title.contains(filter)
    	                   || keyFeatures.contains(filter)
    	                   || about.contains(filter)
    	                   || techDetails.contains(filter);

			if (!isMatch) {

				log.error("[{}]  Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				log.debug("[{}]  Key Features: {}", testName, keyFeatures);
				log.debug("[{}]  About: {}", testName, about);
				log.debug("[{}]  Tech Details: {}", testName, techDetails);


				StringBuilder failureMessage = new StringBuilder();
				failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				.append("-------------------------------------------------------------\n")
				.append(" Title: ").append(title).append("\n")
				.append(" Key Features: ").append(keyFeatures).append("\n")
				.append(" About: ").append(about).append("\n")
				.append(" Tech Details: ").append(techDetails).append("\n")
				.append("-------------------------------------------------------------");

				softAssert.fail(failureMessage.toString());
			} else {
				System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
			}
		}

		softAssert.assertAll(); 

	}

	
	
	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingPriceSilderFunctionality(String str) throws InterruptedException {
		
	//	ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");

		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
		ProductListingPage productPage=new ProductListingPage();
		PriceSliderFlows priceSliderFlows = new PriceSliderFlows();
		SoftAssert softAssert = new SoftAssert();
		

		landingPage.openingLandingPage();
		ScreenshotUtilUpdated.capture("Test test ");
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(str);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");


		if (!genericUtility.filterCheckUnderList("Price")) {
			System.out.println("Filter option 'Price' does not exist in the list. Skipping the test.");
			log.warn("[{}]   Filter option 'Price' does not exist in the list. Skipping the test.", testName);
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Price' is not available for the search term '" + str + "'");
		}

		List<Integer> minValues = Arrays.asList(61, 91, 131);
		List<Integer> maxValues = Arrays.asList(81, 121, 161);


		List<Map<String, Object>> results = priceSliderFlows.applyPriceSliderAndValidateWithResult(minValues, maxValues);


		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Price' (search term '" + str + "')");

		for (Map<String, Object> result : results) {
			boolean isValid = (boolean) result.get("isValid");
			int min = (int) result.get("min");
			int max = (int) result.get("max");
			List<String> mismatches = (List<String>) result.get("mismatches");

			if (!isValid) {
				log.error("[{}]  Validation failed for price range: {} - {}", testName, min, max);
				System.out.println(" Validation failed for price range: " + min + " - " + max);
				for (String msg : mismatches) {
					System.out.println(msg);
					log.error("[{}]  {}", testName, msg);
					softAssert.fail(msg);
				}

			} else {
				log.info("[{}]  Price range validated successfully: {} - {}", testName, min, max);
				System.out.println("Price range validated: " + min + " - " + max);
			}
			System.out.println("------------------------------------------------------------");		
			log.info("[{}] ------------------------------------------------------------", testName);
		}

		log.info("[{}]  Asserting all soft assertions now...",testName);
		softAssert.assertAll();
					
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BatteryProcessorStorage" }
		)
	public void verifyingBatteryCapacityFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");

		AmazonLandingPage landingPage = new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage =new ProductListingPage();
		GenericUtility genericUtility=new GenericUtility();
		SoftAssert softAssert = new SoftAssert();
		SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();


		landingPage.openingLandingPage();
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		if (!genericUtility.filterCheckUnderList("Battery Capacity")) {
			log.warn("[{}]  Filter option 'Battery Capacity' does not exist in the list. Skipping the test.", testName);
			System.out.println("Filter option 'Battery Capacity' does not exist in the list. Skipping the test.");
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Battery Capacity' is not available for the search term '" + input + "'");
		}


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listBatteryCapacityOptionsBy,"batterycapacity");




		if(results.size()==0) {
			softAssert.fail();
		}
		// Drop products whose page details were not captured, so they are not reported as mismatches.
		results = genericUtility.onlyCapturedProducts(results, testName);
		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Battery Capacity' (search term '" + input + "')");

		for (Map<String, Object> product : results) {
			String filter = genericUtility.safeLower(product.get("filter"));
			String title = genericUtility.safeLower(product.get("title"));
			String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
			String about = genericUtility.safeLower(product.get("about"));
			String techDetails = genericUtility.safeLower(product.get("techDetails"));

			boolean isMatch = title.contains(filter)
					|| keyFeatures.contains(filter)
					|| about.contains(filter)
					|| techDetails.contains(filter);

			if (!isMatch) {

				log.error("[{}]  Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				log.debug("[{}]  Key Features: {}", testName, keyFeatures);
				log.debug("[{}]  About: {}", testName, about);
				log.debug("[{}]  Tech Details: {}", testName, techDetails);


				StringBuilder failureMessage = new StringBuilder();
				failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				.append("---------------------------------------------------------------------------------------\n")
				.append(" Title: ").append(title).append("\n")
				.append(" Key Features: ").append(keyFeatures).append("\n")
				.append(" About: ").append(about).append("\n")
				.append(" Tech Details: ").append(techDetails).append("\n")
				.append("-----------------------------------------------------------------------------------------");

				softAssert.fail(failureMessage.toString());

			} else {
				log.info("[{}]  Filter '{}' matched in product details.", testName, filter);
				System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
			}
		}

		log.info("[{}] Asserting all soft assertions now...", testName);
		softAssert.assertAll(); 
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BrandsScreenSizeTypeCamEtc" }
		)
	public void verifyingDisplaySizeFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
        ProductListingPage productPage=new ProductListingPage();
        SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
        SoftAssert softAssert = new SoftAssert();
        

        landingPage.openingLandingPage();
    	SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
        blockerHandler.clearBlockers();
        landingPage.givingInputWithinSearchBar(input);
        landingPage.clickingOnSubmitSearchButton();
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");

        if (!genericUtility.filterCheckUnderList("Screen Size")) {
        	log.warn("[{}]  Filter option 'Display Size' does not exist in the list. Skipping the test.", testName);
        	System.out.println("Filter option 'Display Size' does not exist in the list. Skipping the test.");
        	// OLD: return;   (TestNG counts a plain return as a PASS)
        	throw new SkipException("Filter 'Display Size' is not available for the search term '" + input + "'");
        }



        List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDisplaySizeOptionsBy,"screensize");



        // Drop products whose page details were not captured, so they are not reported as mismatches.
        results = genericUtility.onlyCapturedProducts(results, testName);
        // Nothing validated means the test checked nothing, so fail instead of passing.
        Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Display Size' (search term '" + input + "')");

        for (Map<String, Object> product : results) {
        	String filter = genericUtility.safeLower(product.get("filter"));
        	String title = genericUtility.safeLower(product.get("title"));
        	String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
        	String about = genericUtility.safeLower(product.get("about"));
        	String techDetails = genericUtility.safeLower(product.get("techDetails"));

        	boolean isMatch = title.contains(filter)
        			|| keyFeatures.contains(filter)
        			|| about.contains(filter)
        			|| techDetails.contains(filter);

        	if (!isMatch) {

        		log.error("[{}]  Mismatch found for filter '{}' on product '{}'",testName, filter, title);
        		log.debug("[{}]  Key Features: {}", testName, keyFeatures);
        		log.debug("[{}]  About: {}", testName, about);
        		log.debug("[{}]  Tech Details: {}", testName, techDetails);


        		StringBuilder failureMessage = new StringBuilder();
        		failureMessage.append("filter '").append(filter).append("' not found in product details.\n")
        		.append("--------------------------------------------------------------------------------------\n")
        		.append(" Title: ").append(title).append("\n")
        		.append(" Key Features: ").append(keyFeatures).append("\n")
        		.append(" About: ").append(about).append("\n")
        		.append(" Tech Details: ").append(techDetails).append("\n")
        		.append("------------------------------------------------------------------------------------------");

        		softAssert.fail(failureMessage.toString());


        	} else {
        		log.info("[{}]  Filter '{}' matched in at least one section.",testName, filter);

        		System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
        	}
        }

        log.info("[{}]  Final assertion for display size filter validation starting...", testName);
        softAssert.assertAll(); 
	
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BatteryProcessorStorage" }
		)
	public void verifyingProcessorSpeedFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
        GenericUtility genericUtility =new GenericUtility();
        SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
        SoftAssert softAssert = new SoftAssert();
        StringBuilder failureMessage = new StringBuilder();
		
       
        landingPage.openingLandingPage();
    	SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
        blockerHandler.clearBlockers();
        landingPage.givingInputWithinSearchBar(input);
        landingPage.clickingOnSubmitSearchButton();
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");


        if (!genericUtility.filterCheckUnderList("Processor Speed")) {
        	log.warn("[{}]  Filter option 'Processor Speed' not available. Skipping test.", testName);
        	// OLD: return;   (TestNG counts a plain return as a PASS)
        	throw new SkipException("Filter 'Processor Speed' is not available for the search term '" + input + "'");
        }



        List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listProcessorSpeedOptionsBy,"processorspeed");



        // Drop products whose page details were not captured, so they are not reported as mismatches.
        results = genericUtility.onlyCapturedProducts(results, testName);
        // Nothing validated means the test checked nothing, so fail instead of passing.
        Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Processor Speed' (search term '" + input + "')");

        for (Map<String, Object> product : results) {
        	String filter = genericUtility.safeLower(product.get("filter"));
        	String title = genericUtility.safeLower(product.get("title"));
        	String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
        	String about = genericUtility.safeLower(product.get("about"));
        	String techDetails = genericUtility.safeLower(product.get("techDetails"));

        	boolean isMatch = title.contains(filter)
        			|| keyFeatures.contains(filter)
        			|| about.contains(filter)
        			|| techDetails.contains(filter);

        	if (!isMatch) {



        		log.error("[{}] Mismatch found for filter '{}' on product '{}'",testName, filter, title);
        		log.debug("[{}] Key Features: {}", testName, keyFeatures);
        		log.debug("[{}] About: {}", testName, about);
        		log.debug("[{}] Tech Details: {}", testName, techDetails);



        		failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
        		.append("-------------------------------------------------------------\n")
        		.append(" Title: ").append(title).append("\n")
        		.append(" Key Features: ").append(keyFeatures).append("\n")
        		.append(" About: ").append(about).append("\n")
        		.append(" Tech Details: ").append(techDetails).append("\n")
        		.append("-------------------------------------------------------------");

        		softAssert.fail(failureMessage.toString());
        	} else {
        		log.info("[{}]  Filter '{}' matched in at least one section.", testName, filter);
        		System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
        	}
        }
        log.info("[{}] Final assertion for processor speed filter validation starting...", testName);
        softAssert.assertAll(); 
		
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BrandsScreenSizeTypeCamEtc" }
		)
	public void verifyingDisplayTypeFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");

		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
		GenericUtility genericUtility=new GenericUtility();
		SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
		SoftAssert softAssert = new SoftAssert();


		landingPage.openingLandingPage();
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		if (!genericUtility.filterCheckUnderList("Display Type")){
			System.out.println("Filter option 'Display Type' does not exist. Skipping the test.");
			log.warn("[{}]  Filter option 'Display Type' does not exist. Skipping the test.", testName);
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Display Type' is not available for the search term '" + input + "'");
		}


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDisplayTypeOptionsBy ,"displaytype");



		// Drop products whose page details were not captured, so they are not reported as mismatches.
		results = genericUtility.onlyCapturedProducts(results, testName);
		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Display Type' (search term '" + input + "')");

		for (Map<String, Object> product : results) {
			String filter = genericUtility.safeLower(product.get("filter"));
			String title = genericUtility.safeLower(product.get("title"));
			String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
			String about = genericUtility.safeLower(product.get("about"));
			String techDetails = genericUtility.safeLower(product.get("techDetails"));

			boolean isMatch = title.contains(filter)
					|| keyFeatures.contains(filter)
					|| about.contains(filter)
					|| techDetails.contains(filter);

			if (!isMatch) {

				log.error("[{}] Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				log.debug("[{}] Key Features: {}", testName, keyFeatures);
				log.debug("[{}] About: {}", testName, about);
				log.debug("[{}] Tech Details: {}", testName, techDetails);


				StringBuilder failureMessage = new StringBuilder();
				failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				.append("-------------------------------------------------------------\n")
				.append(" Title: ").append(title).append("\n")
				.append(" Key Features: ").append(keyFeatures).append("\n")
				.append(" About: ").append(about).append("\n")
				.append(" Tech Details: ").append(techDetails).append("\n")
				.append("-------------------------------------------------------------");

				softAssert.fail(failureMessage.toString());
			} else {
				log.info("[{}]  Filter '{}' matched in at least one section.", testName, filter);
				System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
			}
		}

		log.info("[{}]  Final assertion for display type filter validation...", testName);
		softAssert.assertAll(); 
	    
		}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingOperatingSystemVersionFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		GenericUtility genericUtility=new GenericUtility();
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
		OperatingSystemFilterFlows operatingSystemFilterFlows = new OperatingSystemFilterFlows();
		SoftAssert softAssert = new SoftAssert();
		
		
		  landingPage.openingLandingPage();	
			SafeActions safeAct = new SafeActions();
			safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		  blockerHandler.clearBlockers();
		  landingPage.givingInputWithinSearchBar(input);
		  landingPage.clickingOnSubmitSearchButton();
		  blockerHandler.clearBlockers();
		  String testName=ThreadContext.get("testName");

		  if (!genericUtility.filterCheckUnderList("Operating System", "Operating System Version")) {
			  System.out.println("Filter option 'Operating System' or 'Operating System Version' does not exist in the list. Skipping the test.");
			  log.warn("[{}]  Filter option 'Operating System' or 'Operating System Version' not found. Skipping test.", ThreadContext.get("testName"));
			  // OLD: return;   (TestNG counts a plain return as a PASS)
			  throw new SkipException("Filter 'Operating System' is not available for the search term '" + input + "'");
		  }



		  List<Map<String, Object>> results = operatingSystemFilterFlows.applyOperatingSystemFilterAndValidateProductsWithResults(productPage.listOperatingSystemVersionOptionsBy, "operatingsystem");



		  // Drop products whose page details were not captured, so they are not reported as mismatches.
		  results = genericUtility.onlyCapturedProducts(results, testName);
		  // Nothing validated means the test checked nothing, so fail instead of passing.
		  Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Operating System' (search term '" + input + "')");

		  for (Map<String, Object> product : results) {
			  String filter = genericUtility.safeLower(product.get("filter"));
			  String title = genericUtility.safeLower(product.get("title"));
			  String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
			  String about = genericUtility.safeLower(product.get("about"));
			  String techDetails = genericUtility.safeLower(product.get("techDetails"));

			  boolean isMatch = title.contains(filter)
					  || keyFeatures.contains(filter)
					  || about.contains(filter)
					  || techDetails.contains(filter);

			  if (!isMatch) {


				  log.error("[{}]  Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				  log.debug("[{}]  Key Features: {}", testName, keyFeatures);
				  log.debug("[{}]  About: {}", testName, about);
				  log.debug("[{}]  Tech Details: {}", testName, techDetails);


				  StringBuilder failureMessage = new StringBuilder();
				  failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				  .append("-------------------------------------------------------------\n")
				  .append(" Title: ").append(title).append("\n")
				  .append(" Key Features: ").append(keyFeatures).append("\n")
				  .append(" About: ").append(about).append("\n")
				  .append(" Tech Details: ").append(techDetails).append("\n")
				  .append("-------------------------------------------------------------");

				  softAssert.fail(failureMessage.toString());
			  } else {
				  log.info("[{}]  Filter '{}' matched in at least one section.", testName, filter);
				  System.out.println("Filter '" + filter + "' matched in at least one section of product details.");
			  }
		  }

		  log.info("[{}]  Final assertion for OS Version filter validation...", testName);
		  softAssert.assertAll(); 
		
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BrandsScreenSizeTypeCamEtc" }
		)
	public void verifyingMobilePhonePrimaryCameraResolutionFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility= new GenericUtility();
		ProductListingPage productPage=new ProductListingPage();
		SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
		SoftAssert softAssert = new SoftAssert();
		StringBuilder failureMessage = new StringBuilder();

		landingPage.openingLandingPage();
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		if (!genericUtility.filterCheckUnderList("Mobile Phone Primary Camera Resolution")) {
			System.out.println("Filter option 'Mobile Phone Primary Camera Resolution' does not exist in the list. Skipping the test.");
			log.warn("[{}]   Filter 'Mobile Phone Primary Camera Resolution' not found. Skipping test.",
					ThreadContext.get("testName"));
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Mobile Phone Primary Camera Resolution' is not available for the search term '" + input + "'");
		}



		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listMobilePhonePrimaryCameraResolutionOptionsBy, "mobilephoneprimarycameraresolution");



		// Drop products whose page details were not captured, so they are not reported as mismatches.
		results = genericUtility.onlyCapturedProducts(results, testName);
		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Mobile Phone Primary Camera Resolution' (search term '" + input + "')");

		for (Map<String, Object> product : results) {
			String filter = genericUtility.safeLower(product.get("filter"));
			String title = genericUtility.safeLower(product.get("title"));
			String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
			String about = genericUtility.safeLower(product.get("about"));
			String techDetails = genericUtility.safeLower(product.get("techDetails"));

			boolean isMatch = title.contains(filter)
					|| keyFeatures.contains(filter)
					|| about.contains(filter)
					|| techDetails.contains(filter);

			if (!isMatch) {


				log.error("[{}]  Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				log.debug("[{}]  Key Features: {}",testName, keyFeatures);
				log.debug("[{}]  About: {}", testName, about);
				log.debug("[{}]  Tech Details: {}", testName, techDetails);



				failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				.append("-------------------------------------------------------------\n")
				.append(" Title: ").append(title).append("\n")
				.append(" Key Features: ").append(keyFeatures).append("\n")
				.append(" About: ").append(about).append("\n")
				.append(" Tech Details: ").append(techDetails).append("\n")
				.append("-------------------------------------------------------------");

				softAssert.fail(failureMessage.toString());
			} else {
				log.info("[{}]  Filter '{}' matched in product details.", testName, filter);
				System.out.println(" Filter '" + filter + "' matched in at least one section of product details.");
			}
		}

		log.info("[{}]  Final assertion for Camera Resolution filter validation...", testName);
		softAssert.assertAll(); 
		    
	}

	

	@Test(
		    // OLD: dataProvider = "ExcelData",   (every search term from Products.xlsx)
		    dataProvider = "TestRuns",   // the search terms for this test come from data/testInputs.json
		    dataProviderClass = TestDataProvider.class, groups = { "BatteryProcessorStorage" }
		)
	public void verifyingDiscountFilterFunctionality(String input) throws InterruptedException, TimeoutException {
		
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + input + "</b>");
		
		
		AmazonLandingPage landingPage=new AmazonLandingPage();
		SafeActions act=new SafeActions();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		ProductListingPage productPage=new ProductListingPage();
		GenericUtility genericUtility=new GenericUtility();
		SharedFilterFlows sharedFilterFlows = new SharedFilterFlows();
		SoftAssert softAssert = new SoftAssert();
		

		landingPage.openingLandingPage();
		act.safeFindElement(landingPage.amazonLogoLandingPage);
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		if (!genericUtility.filterCheckUnderList("Discount")) {
			log.warn("[{}]   Filter 'Discount' not available. Skipping test.", ThreadContext.get("testName"));
			System.out.println("Filter option 'Discount' does not exist in the list. Skipping the test.");
			// OLD: return;   (TestNG counts a plain return as a PASS)
			throw new SkipException("Filter 'Discount' is not available for the search term '" + input + "'");
		}




		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDiscountOptionsBy,"discount");



		// Drop products whose page details were not captured, so they are not reported as mismatches.
		results = genericUtility.onlyCapturedProducts(results, testName);
		// Nothing validated means the test checked nothing, so fail instead of passing.
		Assert.assertTrue(results != null && !results.isEmpty(), "No products were validated for the filter 'Discount' (search term '" + input + "')");

		for (Map<String, Object> product : results) {
			String filter = genericUtility.safeLower(product.get("filter"));
			String title = genericUtility.safeLower(product.get("title"));
			String keyFeatures = genericUtility.safeLower(product.get("keyFeatures"));
			String about = genericUtility.safeLower(product.get("about"));
			String techDetails = genericUtility.safeLower(product.get("techDetails"));

			boolean isMatch = genericUtility.containsCheck(filter, title)
					|| genericUtility.containsCheck(filter,keyFeatures)
					|| genericUtility.containsCheck(filter,about)
					|| genericUtility.containsCheck(filter,techDetails);

			if (!isMatch) {

				log.error("[{}] Mismatch found for filter '{}' on product '{}'",testName, filter, title);
				log.debug("[{}]  Key Features: {}", testName, keyFeatures);
				log.debug("[{}]  About: {}", testName, about);
				log.debug("[{}]  Tech Details: {}", testName, techDetails);


				StringBuilder failureMessage = new StringBuilder();
				failureMessage.append(" filter '").append(filter).append("' not found in product details.\n")
				.append("-------------------------------------------------------------\n")
				.append(" Title: ").append(title).append("\n")
				.append(" Key Features: ").append(keyFeatures).append("\n")
				.append(" About: ").append(about).append("\n")
				.append(" Tech Details: ").append(techDetails).append("\n")
				.append("-------------------------------------------------------------");

				softAssert.fail(failureMessage.toString());
			} else {
				log.info("[{}]  Filter '{}' matched in at least one product section.", testName, filter);
				System.out.println("Filter '" + filter + "' matched in at least one section of product details.");
			}
		}

		log.info("[{}]  Final assertion for Discount filter validation...", testName);
		softAssert.assertAll(); 
	}
}