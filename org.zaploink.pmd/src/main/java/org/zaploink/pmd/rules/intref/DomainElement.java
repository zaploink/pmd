package org.zaploink.pmd.rules.intref;

import net.sourceforge.pmd.lang.ast.Node;

public class DomainElement {
	public enum Type {
		PRIVATE, PUBLIC
	};

	private final Node node;
	private final String packageName;
	private final String nodeName;
	private String domain;
	private Type type;

	public DomainElement(Node node, String nodeName, String packageName) {
		this.node = node;
		this.nodeName = nodeName;
		this.packageName = packageName;
		this.type = Type.PUBLIC;
	}

	public void setDomain(String domain, Type type) {
		this.domain = domain;
		this.type = type;
	}

	public Node getNode() {
		return this.node;
	}

	public String getNodeName() {
		return this.nodeName;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public boolean hasDomain() {
		return this.domain != null;
	}

	public String getDomain() {
		return this.domain;
	}

	public boolean isPublic() {
		return this.type == Type.PUBLIC;
	}

	/**
	 * Checks the dependency of this domain to the given other domain.
	 *
	 * @param otherNode
	 *            referenced node
	 * @return <code>true</code> if OK, <code>false</code> otherwise (i.e. not permitted)
	 */
	public boolean checkDependencyTo(DomainElement otherNode) {
		// must either be same domain or - if different domain - reference to other domain must be public
		return this.isSameDomainAs(otherNode) || otherNode.isPublic();
	}

	private boolean isSameDomainAs(DomainElement otherDomain) {
		if (this.domain == null) {
			return false;
		}
		return this.domain.equals(otherDomain.domain);
	}

	@Override
	public String toString() {
		return (this.domain == null)
				? String.format("no domain: %s", this.nodeName)
				: String.format("%s %s: %s", isPublic() ? "public" : "private", this.domain, this.nodeName);
	}
}