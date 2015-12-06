package org.zaploink.pmd.rules.intref;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zaploink.pmd.rules.Zaploink;
import org.zaploink.pmd.rules.intref.config.RuleConfigData;
import org.zaploink.pmd.rules.intref.config.RuleConfigReader;
import org.zaploink.pmd.rules.intref.config.RuleConfigReaderException;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPackageDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Detects references to classes that are located in packages that are declared internal.
 *
 * This rule needs an external configuration of the format {@link RuleConfigData}.
 *
 * @author kvg
 */
public class ReferenceToInternal extends AbstractJavaRule {
	private static final Logger LOGGER = Zaploink.getLogger();
	private static final String CONFIG_KEY = "ReferenceToInternal.ruleConfig";
	private static final DomainResolver DOMAIN_RESOLVER;

	static {
		RuleConfigData ruleConfig = null;
		try {
			ruleConfig = RuleConfigReader.readConfig();
		}
		catch (RuleConfigReaderException ex) {
			LOGGER.log(Level.SEVERE, "Could not initialize domain resolver", ex);
		}
		DOMAIN_RESOLVER = (ruleConfig == null) ? null : new DomainResolver(ruleConfig);
	}

	private String packageName;
	private List<DomainElement> imports;

	@Override
	public void start(RuleContext ctx) {
		String ruleName = ReferenceToInternal.class.getSimpleName();
		try {
			ctx.setAttribute(CONFIG_KEY, DOMAIN_RESOLVER);
			LOGGER.log(Level.FINE, "Successfully loaded config for {0} rule.", ruleName);
		}
		catch (Exception ex) {
			String msg = MessageFormat.format("Could not load configuration for {0} rule.", ruleName);
			LOGGER.log(Level.SEVERE, msg, ex);
		}
		super.start(ctx);
	}

	private DomainResolver getConfig(Object ctx) {
		return ((DomainResolver) ((RuleContext) ctx).getAttribute(CONFIG_KEY));
	}

	@Override
	public Object visit(ASTPackageDeclaration node, Object ctx) {
		this.packageName = node.getPackageNameImage(); // save for later use
		this.imports = new ArrayList<>();
		return null;
	}

	@Override
	public Object visit(ASTImportDeclaration node, Object ctx) {
		DomainResolver config = getConfig(ctx);
		if (config != null) {
			DomainElement importDecl = config.resolveDomain(node);
			if (importDecl.hasDomain()) {
				this.imports.add(importDecl); // save for later use
			}
		}
		return null;
	}

	@Override
	public Object visit(ASTClassOrInterfaceDeclaration node, Object ctx) {
		DomainResolver config = getConfig(ctx);
		if (config != null) {
			DomainElement thisClass = config.resolveDomain(this.packageName, node);
			// check all imports (dependencies) of this class
			for (DomainElement importDecl : this.imports) {
				if (importDecl.hasDomain()) {
					checkImport(thisClass, importDecl, ctx);
				}
				// imports without a domain don't have to be checked (are always considered public)
			}
		}
		return null;
	}

	private void checkImport(DomainElement classDecl, DomainElement importDecl, Object ctx) {
		boolean ok = classDecl.checkDependencyTo(importDecl);
		if (!ok) {
			Object[] args = new Object[] { importDecl.getDomain(), importDecl.getNodeName() };
			addViolation(ctx, importDecl.getNode(), args);
		}
	}

	@Override
	public void end(RuleContext ctx) {
		ctx.removeAttribute(CONFIG_KEY);
		super.end(ctx);
	}

}
