# zaploink-pmd
Custom PMD Rules for Java

Version 0.1 is still under development, check [README.md on develop branch](https://github.com/zaploink/pmd/blob/develop/README.md) for more information.

Version [0.1.0-SNAPSHOT](https://oss.sonatype.org/content/repositories/snapshots/org/zaploink/zaploink-pmd/0.1.0-SNAPSHOT/) is available from *Sontatype's OSSRH Snapshot Repository*. 

Follow the instructions for a Gradle build in the [README.md on the develop branch](https://github.com/zaploink/pmd/blob/develop/README.md), but configure the build script to use the SNAPSHOT version:

```
repositories {
    jcenter()
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
	pmd 'org.zaploink:zaploink-pmd:0.1.0-SNAPSHOT'
	pmd 'net.sourceforge.pmd:pmd-core:5.4.0'
	pmd 'net.sourceforge.pmd:pmd-java:5.4.0'
}
```
