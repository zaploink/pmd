package org.zaploink.pmd.rules.intref.config;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.JsonParseException;

@SuppressWarnings("serial")
public class RuleConfigReaderException extends IOException {

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