package main.java.amazonfilterapplicatione2e.base;
import java.time.Duration;      
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.driverManager.DriverManager;
import main.java.amazonfilterapplicatione2e.utilities.WaitUtility;




public class BasePage {

	protected WebDriver driver;
	protected WebDriverWait wait;
	protected WaitUtility waitUtil;
	public BasePage() {
		
        this.driver = DriverManager.getDriver();
        PageFactory.initElements(driver, this);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getInt("globalWaitTimeExtendingBasePage", 20)));
        this.waitUtil = new WaitUtility(this.driver);
        
        
    }	

	// Small helpers shared by the flows.

	// A short wait that is not tied to any page element (we are waiting for Amazon, not for the page), so Thread.sleep is used.
	protected void pause(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// The text with every run of whitespace turned into one space, "" for null.
	protected String collapse(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim();
	}

	// The first line of an exception message, "" for null.
	protected String firstLine(String message) {
		return message == null ? "" : message.split("\n")[0];
	}
}
