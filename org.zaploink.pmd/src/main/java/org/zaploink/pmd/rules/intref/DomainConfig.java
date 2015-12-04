package org.zaploink.pmd.rules.intref;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zaploink.pmd.rules.Zaploink;
import org.zaploink.pmd.rules.intref.NodeWrapper.Type;
import org.zaploink.pmd.rules.intref.RuleConfig.DomainDeclaration;

import com.google.gson.Gson;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;

public class DomainConfig {

	private static final Logger LOGGER = Zaploink.getLogger();
	private static final String RULE_NAME = ReferenceToInternal.class.getSimpleName();
	private static final String CONFIG_PROP = "org.zaploink.pmd.intref.configFile";
	private static final String CONFIG_FILE = "ReferenceToInternal.ruleConfig";
	private static final Gson GSON = new Gson();

	private final List<Pattern> publicPackageRecs = new ArrayList<>();
	private final List<Pattern> privatePackageRecs = new ArrayList<>();
	private final RuleConfig ruleConfig;

	private DomainConfig(RuleConfig ruleConfig) {
		this.ruleConfig = ruleConfig;
		ruleConfig.getAutomaticDomainRecognition().getPublicPackages()
				.stream()
				.filter(Objects::nonNull)
				.forEach(regex -> this.publicPackageRecs.add(Pattern.compile(regex)));
		ruleConfig.getAutomaticDomainRecognition().getPrivatePackages()
				.stream()
				.filter(Objects::nonNull)
				.forEach(regex -> this.privatePackageRecs.add(Pattern.compile(regex)));
	}

	public static final DomainConfig readConfig() {
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

	public static DomainConfig readConfig(Path configFile) {
		try (BufferedReader fileReader = Files.newBufferedReader(configFile)) {
			DomainConfig config = readConfig(fileReader);
			if (config == null) {
				throw new RuntimeException("Empty rule config file: " + configFile);
			}
			return config;
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not read rule config file: " + configFile, ex);
		}
	}

	public static DomainConfig readConfig(Reader reader) {
		RuleConfig ruleConfig = GSON.fromJson(reader, RuleConfig.class);
		return (ruleConfig == null) ? null : new DomainConfig(ruleConfig);
	}

	public NodeWrapper resolveDomain(String packageName, ASTClassOrInterfaceDeclaration classDecl) {
		String className = String.format("%s.%s", packageName, classDecl.getImage());
		return resolveDomainFor(new NodeWrapper(classDecl, className, packageName));
	}

	public NodeWrapper resolveDomain(ASTImportDeclaration importDecl) {
		return resolveDomainFor(new NodeWrapper(importDecl, importDecl.getImportedName(), importDecl.getPackageName()));
	}

	// public overrule private part definitions, explicit part definitions overrules automatic domain recognition
	NodeWrapper resolveDomainFor(NodeWrapper assoc) {
		// 1.1 match against explicit list of public packages and classes
		for (Entry<String, DomainDeclaration> entry : this.ruleConfig.getExplicitDomainDeclarations().entrySet()) {
			for (String className : entry.getValue().getPublicParts().getClasses()) {
				if (className.equals(assoc.getNodeName())) {
					assoc.setDomain(entry.getKey(), Type.PUBLIC);
					return assoc;
				}
			}
			for (String packageName : entry.getValue().getPublicParts().getPackages()) {
				if (packageName.equals(assoc.getPackageName())) {
					assoc.setDomain(entry.getKey(), Type.PUBLIC);
					return assoc;
				}
			}
		}

		// 1.2 try automatic public package recognition
		for (Pattern rec : this.publicPackageRecs) {
			Matcher matcher = rec.matcher(assoc.getPackageName());
			if (matcher.matches()) {
				assoc.setDomain(matcher.group(1), Type.PUBLIC);
				return assoc;
			}
		}

		// 2.1 match against explicit list of private packages and classes
		for (Entry<String, DomainDeclaration> entry : this.ruleConfig.getExplicitDomainDeclarations().entrySet()) {
			for (String className : entry.getValue().getPrivateParts().getClasses()) {
				if (className.equals(assoc.getNodeName())) {
					assoc.setDomain(entry.getKey(), Type.PRIVATE);
					return assoc;
				}
			}
			// explicit private package definitions will match as prefix (everything below is private, too)
			for (String packageName : entry.getValue().getPrivateParts().getPackages()) {
				if (assoc.getPackageName().startsWith(packageName)) {
					assoc.setDomain(entry.getKey(), Type.PRIVATE);
					return assoc;
				}
			}
		}

		// 2.2 try automatic private package recognition
		for (Pattern rec : this.privatePackageRecs) {
			Matcher matcher = rec.matcher(assoc.getPackageName());
			if (matcher.matches()) {
				assoc.setDomain(matcher.group(1), Type.PRIVATE);
				return assoc;
			}
		}

		// no domain could be inferred
		return assoc;
	}

}