package org.zaploink.pmd.rules.intref;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON format:
 *
 * <pre>
 *  {
	"version" : 1,

	"automaticDomainRecognition" : {
		"publicPackages" : [ "regex with exactly one matching group = domain name", ...],
		"privatePackages" : [ "regex", ... ]
	},

	"explicitDomainDeclarations" : {
		"example" : {
			"publicParts" : {
				"packages" : [ "fully qualified package name", ...],
				"classes" : [ "fully qualified class name", ... ]
			},
			"privateParts" : {
				"packages" : [ "qualified package name (prefix)", ... ],
				"classes" : [ "fully qualified class name", ... ]
			}
		}
	}
}
 * </pre>
 *
 * @author chb
 *
 */
public class RuleConfigData {
	private int version;
	private final AutoDomainRecognition automaticDomainRecognition;
	private Map<String, DomainDeclaration> explicitDomainDeclarations;

	public RuleConfigData() {
		this.automaticDomainRecognition = new AutoDomainRecognition();
		this.explicitDomainDeclarations = Collections.emptyMap();
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Map<String, DomainDeclaration> getExplicitDomainDeclarations() {
		return this.explicitDomainDeclarations;
	}

	public void setExplicitDomainDeclarations(Map<String, DomainDeclaration> explicitDomainDeclarations) {
		this.explicitDomainDeclarations = explicitDomainDeclarations;
	}

	public AutoDomainRecognition getAutomaticDomainRecognition() {
		return this.automaticDomainRecognition;
	}

	public static class AutoDomainRecognition {
		private final List<String> publicPackages;
		private final List<String> privatePackages;

		public AutoDomainRecognition() {
			this.publicPackages = Collections.emptyList();
			this.privatePackages = Collections.emptyList();
		}

		public List<String> getPublicPackages() {
			return this.publicPackages;
		}

		public List<String> getPrivatePackages() {
			return this.privatePackages;
		}

	}

	public static class DomainDeclaration {
		private DomainParts publicParts;
		private DomainParts privateParts;

		public DomainDeclaration() {
			this.publicParts = new DomainParts();
			this.privateParts = new DomainParts();
		}

		public DomainParts getPublicParts() {
			return this.publicParts;
		}

		public void setPublicParts(DomainParts publicParts) {
			this.publicParts = publicParts;
		}

		public DomainParts getPrivateParts() {
			return this.privateParts;
		}

		public void setPrivateParts(DomainParts privateParts) {
			this.privateParts = privateParts;
		}

	}

	public static class DomainParts {
		private List<String> packages;
		private List<String> classes;

		public DomainParts() {
			this.packages = Collections.emptyList();
			this.classes = Collections.emptyList();
		}

		public List<String> getPackages() {
			return this.packages;
		}

		public void setPackages(List<String> packages) {
			this.packages = packages;
		}

		public List<String> getClasses() {
			return this.classes;
		}

		public void setClasses(List<String> classes) {
			this.classes = classes;
		}

	}
}
