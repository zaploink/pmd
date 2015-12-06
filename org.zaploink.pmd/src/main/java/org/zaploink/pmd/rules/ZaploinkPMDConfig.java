package org.zaploink.pmd.rules;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Encapsulates the current {@link ZaploinkPMD} configuration. Can be configured in
 * <code>$USER_HOME/.zaploink-pmd/zaploink-pmd.properties</code>.
 *
 * <p>
 * Possible configurations:
 *
 * <pre>
 * log.level={ERROR,WARN,INFO,DEBUG}
 * </pre>
 *
 * <p>
 * Log will be created in <code>$USER_HOME/.zaploink-pmd/zaploink-pmd.log</code> (rolling).
 *
 * @author kvg
 *
 */
public class ZaploinkPMDConfig {
	private final Properties properties;

	public ZaploinkPMDConfig() {
		this.properties = loadProperties();
	}

	private Properties loadProperties() {
		Properties properties = new Properties();
		Path configFile = ZaploinkPMD.getConfigDir().resolve(ZaploinkPMD.ZAPLOINK_CONFIG_FILE);
		if (Files.exists(configFile)) {
			try (InputStream in = Files.newInputStream(configFile)) {
				properties.load(in);
			}
			catch (IOException ex) {
				String msg = MessageFormat.format("Could not load zaploink-pmd config file: {0}", configFile);
				ZaploinkPMD.LOGGER.log(Level.WARNING, msg, ex);
			}
		}
		else {
			ZaploinkPMD.LOGGER.log(Level.INFO, "No zaploink-pmd config file present: {0}", configFile);
		}
		return properties;
	}

	public Level logLevel() {
		String level = this.properties.getProperty("log.level", "INFO");
		// map some common slf4j levels to java levels
		switch (level.toLowerCase()) {
		case "debug":
			return Level.FINEST;
		case "info":
			return Level.INFO;
		case "warn":
			return Level.WARNING;
		case "error":
			return Level.SEVERE;
		default:
			return Level.parse(level);
		}
	}

	public boolean isDebug() {
		return logLevel().intValue() < Level.INFO.intValue();
	}

	Object getProperties() {
		return this.properties;
	}
}