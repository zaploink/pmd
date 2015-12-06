package org.zaploink.pmd.rules.intref;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.ClassUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainResolverTest {

	private static DomainResolver dr;

	@BeforeClass
	public static void setUpOnce() throws IOException {
		String resource = "ReferenceToInternal.testRuleConfig";
		try (InputStream is = DomainResolverTest.class.getResourceAsStream(resource)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			RuleConfigData ruleConfig = RuleConfigReader.readConfig(reader);
			dr = new DomainResolver(ruleConfig);
		}
	}

	@Test
	public void setUpOnce_testConfigIsAvailable() {
		assertThat(dr, is(notNullValue()));
	}

	@Test
	public void resolveDomain_forClassOutsideAnyDefinedDomain_isPublicNoDomain() {
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.fully.qualified.Class"))), is("?-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo"))), is("?-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.import.Foo"))), is("?-public"));
	}

	// "automaticDomainRecognition" : {
	// "publicPackages" : [ "com\\.acme\\.service\\.([^\\.]+)", "com\\.legacy\\.pub\\.([^\\.]+)" ],
	// "privatePackages" : [ "com\\.acme\\.service\\.([^\\.]+)\\..+", "com\\.legacy\\.priv\\.([^\\.]+)(?:\\..+)?" ]
	// }
	@Test
	public void resolveDomain_autoRecognition() {
		// -- public
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo.MyZonk"))), is("foo-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo.Faz"))), is("foo-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.bar.Ark"))), is("bar-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.gurk.Gum"))), is("gurk-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.legacy.pub.gill.APub"))), is("gill-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.legacy.pub.gill.BPub"))), is("gill-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.legacy.pub.gill.gull.CPub"))), is("?-public")); // no domain

		// -- private
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo.internal.Fub"))),
				is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo.impl.Fab"))),
				is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.bar.sub.Zob"))), is("bar-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.bar.a.b.c.Donkey"))),
				is("bar-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.legacy.priv.gill.Gak"))), is("gill-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.legacy.priv.gill.gull.Gok"))),
				is("gill-private"));
	}

	// "explicitDomainDeclarations" : {
	// "foo" : {
	// "publicParts" : {
	// "packages" : [ "com.acme.service.foo.extra", "com.acme.import.foo" ],
	// "classes" : [ "com.acme.facade.import.FooImport" ]
	// },
	// "privateParts" : {
	// "packages" : [ "com.acme.storage.foo" ],
	// "classes" : [ "com.acme.storage.db.FooDAO" ]
	// }
	// }
	// }
	@Test
	public void resolveDomain_additionalFooPackagesAndClasses() {
		// an explicitly public sub package (overrides general "private package" rule for "service.foo.**.X")
		// this allows to define a list of subpackages to be public while all other subpackages are generally private

		// -- public
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.service.foo.extra.Pub"))),
				is("foo-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.import.foo.Tug"))), is("foo-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.import.foo.foo.Tug"))), is("?-public")); // no domain
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.facade.import.FooImport"))),
				is("foo-public"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.facade.import.BarImport"))),
				is("?-public")); // no domain

		// -- private
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.foo.Dat0"))), is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.foo.data.Dat1"))),
				is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.foo.data.Dat2"))),
				is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.foo.data.sub.Dat3"))),
				is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.foo.data.sub.sub.Dat4"))),
				is("foo-private"));

		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.db.FooDAO"))), is("foo-private"));
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.db.FooDAO2"))), is("?-public")); // not private
		assertThat(domainString(dr.resolveDomainFor(className("com.acme.storage.db.BarDAO"))), is("?-public")); // not private
	}

	private String domainString(DomainElement wrapper) {
		String domain = wrapper.getDomain();
		return String.format("%s-%s", domain == null ? "?" : domain, wrapper.isPublic() ? "public" : "private");
	}

	private DomainElement className(String className) {
		return new DomainElement(null, className, ClassUtils.getPackageCanonicalName(className));
	}

}
