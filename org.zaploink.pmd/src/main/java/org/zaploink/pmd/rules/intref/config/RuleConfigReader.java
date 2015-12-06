package org.zaploink.pmd.rules.intref.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zaploink.pmd.rules.Zaploink;
import org.zaploink.pmd.rules.intref.ReferenceToInternal;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Uses a {@link RuleConfigReaderStrategy} to read the appropriate <code>ReferenceToInternal</code> rule configuration
 * from the current system environment for the next PMD run.
 *
 * <p>
 * This might be different when running from Gradle or command line than when running from Eclipse or another IDE.
 * Clients can set an appropriate strategy singleton instance with a call to
 * {@link #setStrategy(RuleConfigReaderStrategy)}.
 * <p>
 * The {@link DefaultStrategy} first tries to read a system property then a global configuration file placed in
 * <code>$USER_HOME</code> .
 *
 * @author kvg
 *
 */
public class RuleConfigReader {
	public static final DefaultStrategy DEFAULT_STRATEGY = new DefaultStrategy();

	private static RuleConfigReaderStrategy strategy = DEFAULT_STRATEGY;

	public static void setStrategy(RuleConfigReaderStrategy newStrategy) {
		strategy = newStrategy;
	}

	/**
	 * Uses the installed {@link RuleConfigReaderStrategy} to read the appropriate <code>ReferenceToInternal</code> rule
	 * configuration for the next PMD run.
	 *
	 * @return {@link RuleConfigData} instance
	 * @throws RuleConfigReaderException
	 */
	public static RuleConfigData readConfig() throws RuleConfigReaderException {
		return strategy.readConfig();
	}

	public static interface RuleConfigReaderStrategy {
		public RuleConfigData readConfig() throws RuleConfigReaderException;
	}

	public static class DefaultStrategy implements RuleConfigReaderStrategy {

		private static final Logger LOGGER = Zaploink.getLogger();
		private static final String RULE_NAME = ReferenceToInternal.class.getSimpleName();
		private static final String CONFIG_PROP = "org.zaploink.pmd.intref.configFile";
		private static final String CONFIG_FILE = "ReferenceToInternal.ruleConfig";
		private static final Gson GSON = new Gson();

		private DefaultStrategy() {
		}

		@Override
		public RuleConfigData readConfig() throws RuleConfigReaderException {
			Path configFile = readConfigFileFromSystemProperty();
			if (configFile == null) {
				configFile = readConfigFileFromUserHome();
			}
			if (!Files.exists(configFile)) {
				throw new RuntimeException("Missing rule config file: " + configFile);
			}

			return readConfig(configFile);
		}

		private Path readConfigFileFromSystemProperty() {
			String filePath = System.getProperty(CONFIG_PROP);
			if (filePath == null) {
				String msg = MessageFormat.format("{0} config system property not defined: {1}", RULE_NAME,
						CONFIG_PROP);
				LOGGER.log(Level.FINE, msg);
				return null;
			}
			return Paths.get(filePath);
		}

		private Path readConfigFileFromUserHome() {
			String userHome = System.getProperty("user.home");
			Path filePath = Paths.get(userHome, Zaploink.ZAPLOINK_DIR, CONFIG_FILE);
			if (!Files.exists(filePath)) {
				String msg = MessageFormat.format("{0} config file not found in $USER_HOME: {1}", RULE_NAME, filePath);
				LOGGER.log(Level.INFO, msg);
				return null;
			}
			String msg = MessageFormat.format("{0} config file found in $USER_HOME: {1}", RULE_NAME, filePath);
			LOGGER.log(Level.FINE, msg);
			return filePath;
		}

		public RuleConfigData readConfig(Path configFile) throws RuleConfigReaderException {
			try (BufferedReader fileReader = Files.newBufferedReader(configFile)) {
				RuleConfigData config = readConfig(fileReader);
				if (config == null) {
					throw RuleConfigReaderException.noConfig(configFile);
				}
				return config;
			}
			catch (IOException ex) {
				throw RuleConfigReaderException.readError(configFile, ex);
			}
		}

		public RuleConfigData readConfig(Reader reader) throws RuleConfigReaderException {
			try {
				return GSON.fromJson(reader, RuleConfigData.class);
			}
			catch (JsonParseException ex) {
				throw RuleConfigReaderException.invalidConfig(ex);
			}
		}

	}
}