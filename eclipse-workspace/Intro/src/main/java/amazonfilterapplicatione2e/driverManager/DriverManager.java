package main.java.amazonfilterapplicatione2e.driverManager;
import java.util.Locale;

import org.apache.logging.log4j.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;







import main.java.amazonfilterapplicatione2e.SeleniumGrid.GlobalGridUtility;
import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;





public class DriverManager  {


	protected static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private  static Logger log = LoggerUtility.getLogger(DriverManager.class);


	public static WebDriver getDriver() {
		return driver.get();
	}

	public static void initDriver() {
		if(driver.get()==null) {

			log.info("No existing WebDriver found for current thread. Initializing a new ChromeDriver...");

			try {

				String browser=System.getProperty("browser")!=null?System.getProperty("browser"):ConfigManager.get("browser", "chrome");
				if (browser != null) browser = browser.trim().toLowerCase(Locale.ENGLISH);

				switch (browser) {
				case "firefox": {

					org.openqa.selenium.firefox.FirefoxOptions firefoxOptions = new org.openqa.selenium.firefox.FirefoxOptions();

					if (ConfigManager.getBoolean("firefox.disableGpu", true)) {
						firefoxOptions.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("firefox.disableDevShmUsage", true)) {
						firefoxOptions.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("firefox.noSandbox", true)) {
						firefoxOptions.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("firefox.disableExtensions", true)) {
						firefoxOptions.addArguments("--disable-extensions");
					}
					if (ConfigManager.getBoolean("firefox.headless", false)) {
						firefoxOptions.addArguments("-headless");
						// NEW: same reason as for Chrome. Firefox takes the width and height as two separate arguments.
						firefoxOptions.addArguments("--width=" + ConfigManager.getInt("headless.windowWidth", 1920));
						firefoxOptions.addArguments("--height=" + ConfigManager.getInt("headless.windowHeight", 1080));
					}
					if(!ConfigManager.getBoolean("firefox.image.loading", true)) {
						firefoxOptions.addArguments(
							    "--blink-settings=imagesEnabled=false",
							    "--disable-gpu",
							    "--disable-extensions"
							);
					}


					try {
						io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
						log.info("WebDriverManager: geckodriver setup OK");
					} catch (Exception e) {
						log.warn("WDM geckodriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.firefox.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.gecko.driver", local);
					}

					WebDriver firefoxDriver = new org.openqa.selenium.firefox.FirefoxDriver(firefoxOptions);

					if(ConfigManager.getBoolean("firefox.runOnGrid", false)) {
					firefoxDriver=GlobalGridUtility.createRemoteFirefoxDriver(ConfigManager.get("firefox.gridHubUrl"), firefoxOptions);
					}

					driver.set(firefoxDriver);
					try { firefoxDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}

					if (ConfigManager.getBoolean("firefox.startMaximized", true)) {
						try { firefoxDriver.manage().window().maximize(); } catch (Exception ignored) {}
					}
					log.info("FirefoxDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}

				case "edge": {

					org.openqa.selenium.edge.EdgeOptions edgeOptions = new org.openqa.selenium.edge.EdgeOptions();

					if (ConfigManager.getBoolean("edge.startMaximized", true)) {
						edgeOptions.addArguments("--start-maximized");
					}
					if (ConfigManager.getBoolean("edge.disableGpu", true)) {
						edgeOptions.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("edge.disableDevShmUsage", true)) {
						edgeOptions.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("edge.noSandbox", true)) {
						edgeOptions.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("edge.disableExtensions", true)) {
						edgeOptions.addArguments("--disable-extensions");
					}
					if(ConfigManager.getBoolean("edge.headless", false)) {
						edgeOptions.addArguments("--headless");
						// NEW: same reason as for Chrome (small default window in headless mode).
						edgeOptions.addArguments("--window-size=" + ConfigManager.getInt("headless.windowWidth", 1920)
								+ "," + ConfigManager.getInt("headless.windowHeight", 1080));
					}
					if(!ConfigManager.getBoolean("edge.image.loading", true)) {
						edgeOptions.addArguments(
							    "--blink-settings=imagesEnabled=false",
							    "--disable-gpu",
							    "--disable-extensions"
							);
					}

					boolean useExtEdge = ConfigManager.getBoolean("edge.use.automation.extension", false);
					edgeOptions.setExperimentalOption("useAutomationExtension", useExtEdge);

					try {

						io.github.bonigarcia.wdm.WebDriverManager.edgedriver().browserVersion("17134").setup();
						log.info("WebDriverManager: edgedriver setup OK");
					} catch (Exception e) {
						log.warn("WDM edgedriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.edge.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.edge.driver", local);
					}
					System.setProperty("SE_MSEDGEDRIVER_MIRROR_URL", "https://msedgedriver.microsoft.com");
					log.info("Trying local fallback with == System.setProperty(\"SE_MSEDGEDRIVER_MIRROR_URL\", \"https://msedgedriver.microsoft.com\")");


					WebDriver edgeDriver = new org.openqa.selenium.edge.EdgeDriver(edgeOptions);


					if(ConfigManager.getBoolean("edge.runOnGrid", false)) {
						edgeDriver=GlobalGridUtility.createRemoteEdgeDriver(ConfigManager.get("edge.gridHubUrl"), edgeOptions);
					}
					driver.set(edgeDriver);
					try { edgeDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}
					log.info("EdgeDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}


				default: {

					org.openqa.selenium.chrome.ChromeOptions options = new org.openqa.selenium.chrome.ChromeOptions();



					if (ConfigManager.getBoolean("chrome.startMaximized", true)) {
						options.addArguments("--start-maximized");
					}
					if (ConfigManager.getBoolean("chrome.disableGpu", true)) {
						options.addArguments("--disable-gpu");
					}
					if (ConfigManager.getBoolean("chrome.disableBlinkAutomationControl", true)) {
						options.addArguments("--disable-blink-features=AutomationControlled");
					}
					if (ConfigManager.getBoolean("chrome.disableDevShmUsage", true)) {
						options.addArguments("--disable-dev-shm-usage");
					}
					if (ConfigManager.getBoolean("chrome.noSandbox", true)) {
						options.addArguments("--no-sandbox");
					}
					if (ConfigManager.getBoolean("chrome.disableExtensions", true)) {
						options.addArguments("--disable-extensions");

					}
					if(ConfigManager.getBoolean("chrome.headless", false)) {
						options.addArguments("--headless");
						// NEW: a headless browser opens a small window by default, which changes the page layout
						// (for example the filters panel can disappear). --start-maximized does nothing in headless mode,
						// so the size is set directly (from UtilData.properties, default 1920x1080).
						options.addArguments("--window-size=" + ConfigManager.getInt("headless.windowWidth", 1920)
								+ "," + ConfigManager.getInt("headless.windowHeight", 1080));
					}
					if(!ConfigManager.getBoolean("chrome.image.loading", true)) {
						options.addArguments(
							    "--blink-settings=imagesEnabled=false",
							    "--disable-gpu",
							    "--disable-extensions"
							);
					}


					try {
						io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
						log.info("WebDriverManager: chromedriver setup OK");
					} catch (Exception e) {
						log.warn("WDM chromedriver setup failed: {}. Will try local fallback if configured.", e.getMessage());
						String local = ConfigManager.get("webdriver.chrome.local.path", "");
						if (!local.isBlank()) System.setProperty("webdriver.chrome.driver", local);
					}

					WebDriver chromeDriver = new org.openqa.selenium.chrome.ChromeDriver(options);


					if(ConfigManager.getBoolean("chrome.runOnGrid", false)) {
						chromeDriver=GlobalGridUtility.createRemoteChromeDriver(ConfigManager.get("chrome.gridHubUrl"), options);
					}


					driver.set(chromeDriver);

					try { chromeDriver.manage().deleteAllCookies(); } catch (Exception ignored) {}
					log.info("ChromeDriver initialized successfully for thread: {}", Thread.currentThread().threadId());
					break;
				}
				}


			}catch(Exception e) {
				log.error("Failed to initialize WebDriver: {}", e.getMessage(), e);
				throw new RuntimeException("WebDriver initialization failed", e);
			}

		}
		else {
			log.info("Reusing existing WebDriver instance for thread: {}", Thread.currentThread().threadId());
		}

	}




	/** Quit and clean up WebDriver for current thread */
	public static void quitDriver() {
		WebDriver currentDriver = driver.get();

		if (currentDriver != null) {
			try {
				currentDriver.quit();
			} catch (Exception e) {

			} finally {
				driver.remove();
			}
		} else {
			log.warn("quitDriver() called but no WebDriver was found for thread: {}", Thread.currentThread().threadId());
		}
	}






}
