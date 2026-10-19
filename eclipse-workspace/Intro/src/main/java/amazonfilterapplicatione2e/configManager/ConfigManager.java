package main.java.amazonfilterapplicatione2e.configManager;
import java.io.FileInputStream; 
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

// Central place every flow/test reads tunable values from (retry counts, "run all vs override
// count" toggles, timeouts, etc.) instead of hardcoding them, so CI and local runs can behave
// differently without code changes - see the resolution order on get(String).
public final class ConfigManager {
	
	
	private static final Logger log = LogManager.getLogger(ConfigManager.class);
	private static final Properties props = new Properties();
	private static final String RESOURCE = "configs/UtilData.properties";   // matches the actual location
	
	static {
	    loadProperties();
	}

	private static void loadProperties() {
	    try {
	        // -DUtilData.file=<path> lets a run point at a different properties file (e.g. a CI-only
	        // config) without touching the one packaged on the classpath; if it's not set, fall
	        // back to the bundled configs/UtilData.properties below.
	        String external = System.getProperty("UtilData.file");

	        if (external != null && !external.isBlank()) {
	            try (FileInputStream fis = new FileInputStream(external)) {
	                props.load(fis);
	                log.info("Loaded config from external file: {}", external);
	                return;
	            }
	        }

	        try (InputStream in = ConfigManager.class.getClassLoader()
	                .getResourceAsStream(RESOURCE)) {
	            if (in != null) {
	                props.load(in);
	                log.info("Loaded config from classpath: {}", RESOURCE);
	            } else {
	                log.warn("Config not found: {}", RESOURCE);
	            }
	        }

	    } catch (IOException e) {
	        throw new RuntimeException("Failed to load config", e);
	    }
	}

	    private ConfigManager() {} // prevent instantiation


	    
	    /** Resolution order: system property → env variable → file */
	    public static String get(String key) {
	        String value = System.getProperty(key);

	        if (value == null || value.isBlank()) {
	            
	            value = System.getenv(key.toUpperCase().replace('.', '_'));
	        }

	        if (value == null || value.isBlank()) {
	            value = props.getProperty(key);
	        }

	        return value;
	    }

	    public static String get(String key, String defaultValue) {
	        String value = get(key);
	        return (value == null || value.isBlank()) ? defaultValue : value;
	    }

	    public static boolean getBoolean(String key, boolean defaultValue) {
	        String value = get(key);
	        return (value == null || value.isBlank())
	                ? defaultValue
	                : Boolean.parseBoolean(value.trim());
	    }

	    // A malformed value (typo in the properties file, bad env var) falls back to defaultValue
    // rather than throwing, since a broken config value shouldn't crash the whole test run.
    public static int getInt(String key, int defaultValue) {
	        String value = get(key);
	        try {
	            return (value == null || value.isBlank())
	                    ? defaultValue
	                    : Integer.parseInt(value.trim());
	        } catch (NumberFormatException e) {
	            return defaultValue;
	        }
	    }

	    public static int getInt(String key) {
	        return getInt(key, 0);
	    }
	    
	
	    
	    
	    
	    
	}

 



