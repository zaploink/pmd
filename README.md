# Custom PMD rules
Custom PMD Rules for Java. Currently available:
- **ReferenceToInternal** Marks invalid references to internal classes

## ReferenceToInternal Rule
Unless you are using OSGi or an early preview of Java 9 it is hard to enforce rules about access to (module) internal classes.

Your team might have some conventions and policies in place, e.g. that modules need to have a public package and all private or internal implementation have to be placed in or below packages named `internal`. Code that is placed in an `internal` package should never be referenced from outside the module in order to preserve encapsulation.

Placing code in the right place is established fairly easily, but ensuring that it is  not referenced from other places is rather hard. If you are using an IDE it does not take more than a *Ctrl-T* to select and import a class and you will not even be confronted with the fully qualified name of the class, so how should you know that it stems from an `internal`package?

The **ReferenceToInternal** rule allows you to declare the boundaries of your modules and will subsequently report violations of those boundaries, e.g. when references to internal module classes are made from outside the module.

###Rule Configuration

The rule configuration is used to declare *domains* (or *modules*) and assumes that you have one or several of those, comprising of a public and a private part (which both can span one or more packages).

You can have your *domain* packages automatically identified by using a *regex*, or you can explicitly list packages and classes that belong to a domain (or combine the two).

####Example 1
This assumes that you have your code cleanly structured into modules by using a package name scheme such as
- `com.acme.<modulename>` for public packages and
- `com.acme.<modulename>.internal.**` for internal (module-private) code.

A very simple rule configuration can enforce this policy:

```
{
	"version" : 1,
	"automaticDomainRecognition" : {
		"publicPackages" : [ "com\\.acme\\.([^\\.]+)$" ],
		"privatePackages" : [ "com\\.acme\\.([^\\.]+)\\.internal\\..*" ]
	}
}
```

The two regular expressions will correctly identify the classes `com.acme.customers.CustomerService` and `com.acme.customers.internal.storage.CustomerDatabase` as belonging to the *public* and *private* parts of the domain *customers*, respectively. While import of `CustomerService` will be legal anywhere in the code, imports of `CustomerDatabase` will only be permitted within classes of the *customers* domain.

==Note==: You can add more than one regular expression to identify public and private packages (e.g. to support multiple naming schemes). All of them must have exactly one _capturing group_ that will be interpreted as a *domain name* in order to identify public and private classes of the same domain. If you need another group, e.g. to express sub-package alternatives such as *either* `internal` *or* `impl`, then use a non-capturing group for those: `com\\.acme\\.([^\\.]+)\\.(?:internal|impl)\\..*`.

####Example 2
You can extend and/or replace the *automaticDomainRecognition* with explicitly listed packages and classes that are outside your naming scheme.

If you have an additional public packages that are sub-packages of the main module package or an additional private package(s) that lay outside the naming scheme, you can list them explicitly as follows:

```
{
	"version" : 1,
	"automaticDomainRecognition" : {
		"publicPackages" : [ "com\\.acme\\.([^\\.]+)$" ],
		"privatePackages" : [ "com\\.acme\\.([^\\.]+)\\.internal\\..*" ]
	},
	"explicitDomainDeclarations" : {
		"customers" : {
			"publicParts" : {
				"packages" : [ "com.acme.customers.bonusprogram", "com.acme.util.customers" ],
				"classes" : []
			},
			"privateParts" : {
				"packages" : [ "com.acme.transfer.customers" ],
				"classes" : []
			}
		},
        "products" : {
            ...
        }
   }
}
```

Please note the following:
- there can be more than one explicit domain declaration

- explicit domain declarations are plain strings, they do ++not++ use regular expressions

- explicit public declarations will override any private classifications. This permits to specify a general rule to declare a module-internal space and then exposing some of that as public, if necessary.

- explicit private package names will be effectively be treated as *prefix*, i.e. it is assumed that any sub-packages of a private package are also private (unless exposed with a public package declaration)

- you can also explicitly add fully qualified class names to the public and private part of a domain

You can drop the `automaticDomainRecognition` section of the rule config completely in favor of `explicitDomainDeclarations`. This might be desirable if you don't have a simple naming convention in place or if you want to ensure that access to some parts of the code is restricted to specific classes (e.g. legacy packages should only be accessed from a public *Facade* class).

### More Examples
See `org.zaploink.pmd.test`project.

# Installation
1. The `zaploink-pmd`JAR needs to be added to the PMD classpath. The `ReferenceToInternal` Rule requires [GSON](https://github.com/google/gson) to parse the rule configuration, therefore GSON must also be added to the classpath of PMD.

1. To enable the Zaploink custom rule set you must add `zaploink-custom` to your PMD rule set configuration.

1. Finally, to point the `ReferenceToInternal` Rule to *your* domain configuration file you must set the system property `org.zaploink.pmd.intref.configFile` before invoking PMD.

## Run PMD from the Command Line

Add the `zaploink-pmd-0.x.jar` and `gson.jar` to the `lib` folder of your PMD installation.

Now run PMD against your sources like this:
```
java
  -classpath "<pmd-home>/lib/*" \
  -Dorg.zaploink.pmd.intref.configFile=<path-to-rule-config> \
  net.sourceforge.pmd.PMD \
  -dir <path-to-src-folder-of-classes-to-check> \
  -R java-basic,zaploink-custom \
  -language java
```
Alternatively you can add the `zaploink-pmd-xx.jar` and `gson-xx.jar` explicitly to the classpath of PMD (make sure to use the correct classpath separator, i.e. `;` on Windows and `:` on Unix).

++Note:++ Both JAR-Files are available from Maven Central: [gson.jar](http://mvnrepository.com/artifact/com.google.code.gson/gson), [zaploink-pmd.jar](http://mvnrepository.com/artifact/org.zaploink/zaploink-pmd)

## Run PMD with Gradle

Example Gradle build file (assumes that `org.zaploink.pmd` JAR and your rule configuration are located in a project directory called `pmd-ext`):

```
apply plugin: 'java'
apply plugin: 'pmd'

repositories {
    jcenter()
}

dependencies {
	pmd 'org.zapoink:zaploink-pmd:0.1.0'  // the zaploink pmd rules
	pmd 'com.google.code.gson:gson:2.4'   // required by zaploink-pmd to parse project-specific rule config
	pmd 'net.sourceforge.pmd:pmd-core:5.4.0'
	pmd 'net.sourceforge.pmd:pmd-java:5.4.0'
}

pmd {
	ignoreFailures true
	ruleSets = [ "java-basic" , "zaploink-custom" ] // activates the zaploink custom rule set
}

// the following points the 'ReferenceToInternal' rule to your custom rule config
pmdMain.doFirst {
	File configFile = file("pmd-ext/ReferenceToInternal.myRuleConfig");
	System.setProperty("org.zaploink.pmd.intref.configFile", configFile.absolutePath)
}
```

# Debugging

The easiest way to debug PMD and any custom rule code is to set up a debug launch configuration that calls the PMD main class with all the necessary arguments. Make sure you put the custom rule code on the class path.

++Note:++ An example debug configuration for Eclipse can be found in the `org.zaploink.pmd.test` project, see the `PMD-debugging.launch` file.

