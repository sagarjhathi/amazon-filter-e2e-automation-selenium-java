package main.java.amazonfilterapplicatione2e.constants;

import org.apache.logging.log4j.Logger;

import main.java.amazonfilterapplicatione2e.configManager.ConfigManager;
import main.java.amazonfilterapplicatione2e.logger.LoggerUtility;

public class GlobalConstants {

	private static final Logger log = LoggerUtility.getLogger(GlobalConstants.class);

	public static void main(String args[]) {
		// [3.1] System.out.println(ConfigManager.get("browser"));
		log.info("Configured browser: {}", ConfigManager.get("browser"));
	}
}
