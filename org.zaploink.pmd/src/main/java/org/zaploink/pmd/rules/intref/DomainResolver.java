package org.zaploink.pmd.rules.intref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zaploink.pmd.rules.intref.DomainElement.Type;
import org.zaploink.pmd.rules.intref.RuleConfigData.DomainDeclaration;

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;

class DomainResolver {

	private final List<Pattern> publicPackageRecs = new ArrayList<>();
	private final List<Pattern> privatePackageRecs = new ArrayList<>();
	private final RuleConfigData ruleConfig;

	DomainResolver(RuleConfigData ruleConfig) {
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

	public DomainElement resolveDomain(String packageName, ASTClassOrInterfaceDeclaration classDecl) {
		String className = String.format("%s.%s", packageName, classDecl.getImage());
		return resolveDomainFor(new DomainElement(classDecl, className, packageName));
	}

	public DomainElement resolveDomain(ASTImportDeclaration importDecl) {
		return resolveDomainFor(
				new DomainElement(importDecl, importDecl.getImportedName(), importDecl.getPackageName()));
	}

	// public overrule private part definitions, explicit part definitions overrules automatic domain recognition
	DomainElement resolveDomainFor(DomainElement assoc) {
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