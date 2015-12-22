Commons
=======

<a href="https://raw.githubusercontent.com/ArpNetworking/commons/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.org/ArpNetworking/commons/">
    <img src="https://travis-ci.org/ArpNetworking/metrics-client-java.png"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.commons%22%20a%3A%22commons%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.commons/commons.svg"
         alt="Maven Artifact">
</a>

Common utility methods and classes.

Usage
-----

### Add Dependency

Determine the latest version of the commons in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.commons%22%20a%3A%22commons%22).

#### Maven

Add a dependency to your pom:

```xml
<dependency>
    <groupId>com.arpnetworking.commons</groupId>
    <artifactId>commons</artifactId>
    <version>VERSION</version>
</dependency>
```

The Maven Central repository is included by default.

#### Gradle

Add a dependency to your build.gradle:

    compile group: 'com.arpnetworking.commons', name: 'commons', version: 'VERSION'

Add the Maven Central Repository into your *build.gradle*:

```groovy
repositories {
    mavenCentral()
}
```

#### SBT

Add a dependency to your project/Build.scala:

```scala
val appDependencies = Seq(
    "com.arpnetworking.commons" % "commons" % "VERSION"
)
```

The Maven Central repository is included by default.

### Transitive Dependencies

The project does not declare any non-essential transitive dependencies. For example, it does declare a dependency on
[SLF4J](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22slf4j-api%22); however, most other dependencies are marked as
provided and should be declared by users of the library. This allows clients to declare only the dependencies of the
features that they use, instead of automatically inheriting all dependencies of commons. Each component documents its
dependencies and the version defined in the pom for testing should be considered the minimum version of each dependency.
As the library grows we may consider splitting it into a multi-module project with each submodule explicitly declaring
its dependencies.

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Building:

    commons> ./mvnw verify

To use the local version you must first install it locally:

    commons> ./mvnw install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2015
