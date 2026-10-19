package test.java.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
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
import main.java.amazonfilterapplicatione2e.utilities.ScreenshotUtil;
import test.testDataProvider.TestDataProvider;

public class AmazonTests extends BaseTest {

	
	private  final Logger log = LoggerUtility.getLogger(AmazonTests.class);
	
	
	
	@Test(
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();                   
		ScreenshotUtil.capture(input);
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");
       
       
        if (!genericUtility.isElementInViewport(productPage.getItByTomorrowUnderDeliveryDayFilterBy())) {
		    System.out.println("Filter option 'Get It by Tomorrow' does not exist. Skipping the test.");
		    log.warn("[{}]   Filter option 'Get It by Tomorrow' does not exist. Skipping the test.", testName);
		    return;
		}
        
	
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItByTomorrowUnderDeliveryDayFilterBy());
			
	    
	    boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery date mismatch at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);      
			
		}
	
	

	@Test(
		    dataProvider = "ExcelData",
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
		 
		 ScreenshotUtil.capture(input);
		 
		 landingPage.openingLandingPage();
			SafeActions safeAct = new SafeActions();
			safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
 		 blockerHandler.clearBlockers();
 		 landingPage.givingInputWithinSearchBar(input);
         landingPage.clickingOnSubmitSearchButton();

		 blockerHandler.clearBlockers();   
		 String testName=ThreadContext.get("testName");
		 
		 
 		if (!genericUtility.isElementInViewport(productPage.getItInTwoDaysUnderDeliveryDayFilterBy())) {
 		    System.out.println("Filter option 'Get It in 2 Days' does not exist. Skipping the test.");
 		    log.warn("[{}]  Filter option 'Get It in 2 Days' does not exist. Skipping the test.", testName);
 		    return ;
 		}                 
		            	
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItInTwoDaysUnderDeliveryDayFilterBy());
		
	    boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery date mismatch at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);      		
	    
		}
	



	@Test(
		    dataProvider = "ExcelData",
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
 		ScreenshotUtil.capture();
 		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		ScreenshotUtil.capture(input);
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		ScreenshotUtil.capture(input);
 		blockerHandler.clearBlockers();
 		
 		 String testName=ThreadContext.get("testName");
 		if (!genericUtility.isElementInViewport(productPage.getItTodayUnderDeliveryDayFilterBy())) {
 		    System.out.println("Filter option 'Get It Today' does not exist. Skipping the test.");
 		   log.warn("[{}]  Filter option 'Get It Today' does not exist. Skipping the test.", ThreadContext.get("testName"));
 		    return;
 		}                   
		            		
		            		
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.getItTodayUnderDeliveryDayFilterBy());
		
		boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery date mismatch at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);                 		  
		            	    
	    
	}
		    
	
	
	@Test(
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
 		blockerHandler.clearBlockers();
 		
 		 String testName=ThreadContext.get("testName");
 		if (!genericUtility.isElementInViewport(productPage.eligibleForFreeDeliveryBy())) {
 		    System.out.println("Filter option 'Eligible for Free Delivery does not. Skipping the test.");
 		   log.warn("[{}]  Filter option 'Eligible for Free Delivery' does not exist. Skipping the test.", ThreadContext.get("testName"));
 		    return;
 		}                   
		            		
		            		
		List<Object> result = deliveryFilterFlows.validateDeliveryFilterOptionsWithResult(productPage.eligibleForFreeDeliveryBy());
		
		boolean isValid = (boolean) result.get(0);
	    String productName = (String) result.get(1);
	    int productIndex = (int) result.get(2);
	    
	    log.info("[{}] Asserting delivery filter: isValid={}, index={}, text={}",testName, isValid, productIndex, productName);
	    Assert.assertTrue(isValid,"Delivery date mismatch at index " + productIndex + ". Text: " + productName);
	    System.out.println(isValid+"  Text from function =>"+productName+" index no is "+productIndex);                 		  
		            	    
	    
	}
		            		  
	
	
	@Test(
		    dataProvider = "ExcelData",
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
	
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("brands")) {
		//   System.out.println("Filter option 'brands' does not exist in the list. Skipping the test.");
		//   log.warn("[{}] Filter option 'brands' does not exist in the list. Skipping the test.", testName);
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("brands");


		List<WebElement> filterOptions = safeAct.safeFindElements(productPage.listBrandsOptionsNewBy());
		List<Map<Object, Object>> allResults=null;


		if(filterOptions==null) {
			allResults = brandFilterFlows.applyFilterAndValidateBrandsFilterWithResult(productPage.listBrandsOptionsOldBy(), "brandsold");
		}else {
			allResults = brandFilterFlows.applyFilterAndValidateBrandsFilterWithResult(productPage.listBrandsOptionsNewBy(), "brands");
		}


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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");
		
		
		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Storage Capacity")) {
		//   System.out.println("Filter option 'Storage Capacity' does not exist in the list. Skipping the test.");
		//   log.warn("[{}]  Filter option 'Storage Capacity' does not exist in the list. Skipping the test.", ThreadContext.get("testName"));
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Storage Capacity");


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listStorageCapacityOptionsBy(),"storagecapacity");
		log.info("[{}] Retrieved {} results from applyFilterAndValidateProductsWithResult.",testName,"And result size->" +results.size());
		

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
		    dataProvider = "ExcelData",
		    dataProviderClass = TestDataProvider.class, groups = { "DeliveryPriceOs" }
		)
	public void verifyingPriceSliderFunctionality(String str) throws InterruptedException {
		
		ReportManager.getTest().info("Test Input Parameter: <b>" + str + "</b>");

		AmazonLandingPage landingPage=new AmazonLandingPage();
		CaptchaHandler capHandler=new CaptchaHandler();
		BlockerHandler blockerHandler = new BlockerHandler();
		GenericUtility genericUtility=new GenericUtility();
		ProductListingPage productPage=new ProductListingPage();
		PriceSliderFlows priceSliderFlows = new PriceSliderFlows();
		SoftAssert softAssert = new SoftAssert();
		

		landingPage.openingLandingPage();
		ScreenshotUtil.capture("Test test ");
		SafeActions safeAct = new SafeActions();
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(str);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");


		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Price")) {
		//   System.out.println("Filter option 'Price' does not exist in the list. Skipping the test.");
		//   log.warn("[{}]   Filter option 'Price' does not exist in the list. Skipping the test.", testName);
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Price");

		List<Integer> minValues = Arrays.asList(61, 91, 131);
		List<Integer> maxValues = Arrays.asList(81, 121, 161);


		List<Map<String, Object>> results = priceSliderFlows.applyPriceSliderAndValidateWithResult(minValues, maxValues);


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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Battery Capacity")) {
		//   log.warn("[{}]  Filter option 'Battery Capacity' does not exist in the list. Skipping the test.", testName);
		//   System.out.println("Filter option 'Battery Capacity' does not exist in the list. Skipping the test.");
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Battery Capacity");


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listBatteryCapacityOptionsBy(),"batterycapacity");




		if(results.size()==0) {
			softAssert.fail();
		}
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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
        blockerHandler.clearBlockers();
        landingPage.givingInputWithinSearchBar(input);
        landingPage.clickingOnSubmitSearchButton();
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");

        // OLD (no wait, and a missing filter ended the test as PASSED):
        //   if (!genericUtility.filterCheckUnderList("Screen Size")) {
        //   log.warn("[{}]  Filter option 'Display Size' does not exist in the list. Skipping the test.", testName);
        //   System.out.println("Filter option 'Display Size' does not exist in the list. Skipping the test.");
        //   return ;
        //   }
        // NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
        genericUtility.skipIfFilterMissing("Screen Size");



        List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDisplaySizeOptionsBy(),"screensize");



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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
        blockerHandler.clearBlockers();
        landingPage.givingInputWithinSearchBar(input);
        landingPage.clickingOnSubmitSearchButton();
        blockerHandler.clearBlockers();
        String testName=ThreadContext.get("testName");


        // OLD (no wait, and a missing filter ended the test as PASSED):
        //   if (!genericUtility.filterCheckUnderList("Processor Speed")) {
        //   log.warn("[{}]  Filter option 'Processor Speed' not available. Skipping test.", testName);
        //   return ;
        //   }
        // NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
        genericUtility.skipIfFilterMissing("Processor Speed");



        List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listProcessorSpeedOptionsBy(),"processorspeed");



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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Display Type")){
		//   System.out.println("Filter option 'Display Type' does not exist. Skipping the test.");
		//   log.warn("[{}]  Filter option 'Display Type' does not exist. Skipping the test.", testName);
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Display Type");


		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDisplayTypeOptionsBy() ,"displaytype");



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
		    dataProvider = "ExcelData",
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
			safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		  blockerHandler.clearBlockers();
		  landingPage.givingInputWithinSearchBar(input);
		  landingPage.clickingOnSubmitSearchButton();
		  blockerHandler.clearBlockers();
		  String testName=ThreadContext.get("testName");

		  // OLD (no wait, and a missing filter ended the test as PASSED):
		  //   if (!genericUtility.filterCheckUnderList("Operating System", "Operating System Version")) {
		  //   System.out.println("Filter option 'Operating System' or 'Operating System Version' does not exist in the list. Skipping the test.");
		  //   log.warn("[{}]  Filter option 'Operating System' or 'Operating System Version' not found. Skipping test.", ThreadContext.get("testName"));
		  //   return;
		  //   }
		  // NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		  genericUtility.skipIfFilterMissing("Operating System", "Operating System Version");



		  List<Map<String, Object>> results = operatingSystemFilterFlows.applyOperatingSystemFilterAndValidateProductsWithResults(productPage.listOperatingSystemVersionOptionsBy(), "operatingsystem");



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
		    dataProvider = "ExcelData",
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
		safeAct.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Mobile Phone Primary Camera Resolution")) {
		//   System.out.println("Filter option 'Mobile Phone Primary Camera Resolution' does not exist in the list. Skipping the test.");
		//   log.warn("[{}]   Filter 'Mobile Phone Primary Camera Resolution' not found. Skipping test.",
		//   ThreadContext.get("testName"));
		//   return;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Mobile Phone Primary Camera Resolution");



		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listMobilePhonePrimaryCameraResolutionOptionsBy(), "mobilephoneprimarycameraresolution");



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
		    dataProvider = "ExcelData",
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
		act.safeFindElement(landingPage.amazonLogoLandingPageBy());
		blockerHandler.clearBlockers();
		landingPage.givingInputWithinSearchBar(input);
		landingPage.clickingOnSubmitSearchButton();
		blockerHandler.clearBlockers();
		String testName=ThreadContext.get("testName");

		// OLD (no wait, and a missing filter ended the test as PASSED):
		//   if (!genericUtility.filterCheckUnderList("Discount")) {
		//   log.warn("[{}]   Filter 'Discount' not available. Skipping test.", ThreadContext.get("testName"));
		//   System.out.println("Filter option 'Discount' does not exist in the list. Skipping the test.");
		//   return ;
		//   }
		// NEW (waits for the filters panel; a missing filter SKIPS the test with a clear reason):
		genericUtility.skipIfFilterMissing("Discount");




		List<Map<String, Object>> results = sharedFilterFlows.applyFilterAndValidateProductsWithResult(productPage.listDiscountOptionsBy(),"discount");



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