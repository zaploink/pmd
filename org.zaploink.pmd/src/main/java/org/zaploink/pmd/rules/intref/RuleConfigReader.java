package org.zaploink.pmd.rules.intref;

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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class RuleConfigReader {

	private static final Logger LOGGER = Zaploink.getLogger();
	private static final String RULE_NAME = ReferenceToInternal.class.getSimpleName();
	private static final String CONFIG_PROP = "org.zaploink.pmd.intref.configFile";
	private static final String CONFIG_FILE = "ReferenceToInternal.ruleConfig";
	private static final Gson GSON = new Gson();

	public static final RuleConfigData readConfig() throws RuleConfigReaderException {
		Path configFile = readConfigFileFromSystemProperty();
		if (configFile == null) {
			configFile = readConfigFileFromUserHome();
		}
		if (!Files.exists(configFile)) {
			throw new RuntimeException("Missing rule config file: " + configFile);
		}

		return readConfig(configFile);
	}

	private static Path readConfigFileFromSystemProperty() {
		String filePath = System.getProperty(CONFIG_PROP);
		if (filePath == null) {
			String msg = MessageFormat.format("{0} config system property not defined: {1}", RULE_NAME, CONFIG_PROP);
			LOGGER.log(Level.FINE, msg);
			return null;
		}
		return Paths.get(filePath);
	}

	private static Path readConfigFileFromUserHome() {
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

	public static RuleConfigData readConfig(Path configFile) throws RuleConfigReaderException {
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

	public static RuleConfigData readConfig(Reader reader) throws RuleConfigReaderException {
		try {
			return GSON.fromJson(reader, RuleConfigData.class);
		}
		catch (JsonParseException ex) {
			throw RuleConfigReaderException.invalidConfig(ex);
		}
	}

	@SuppressWarnings("serial")
	public static class RuleConfigReaderException extends IOException {

		private RuleConfigReaderException(String message, Throwable cause) {
			super(message, cause);
		}

		public static RuleConfigReaderException noConfig(Path file) {
			return new RuleConfigReaderException("Empty rule config file: " + file, null);
		}

		public static RuleConfigReaderException invalidConfig(JsonParseException cause) {
			return new RuleConfigReaderException("Invalid JSON (not a valid rule config)", cause);
		}

		public static RuleConfigReaderException readError(Path file, IOException cause) {
			return new RuleConfigReaderException("Could not read rule config file: " + file, cause);
		}
	}
}