Commons
=======

<a href="https://raw.githubusercontent.com/ArpNetworking/commons/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.com/ArpNetworking/commons">
    <img src="https://travis-ci.com/ArpNetworking/commons.svg?branch=master"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.commons%22%20a%3A%22commons%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.commons/commons.svg"
         alt="Maven Artifact">
</a>
<a href="http://www.javadoc.io/doc/com.arpnetworking.commons/commons">
    <img src="http://www.javadoc.io/badge/com.arpnetworking.commons/commons.svg"
         alt="Javadocs">
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

### Object Mapper Factory

The factory exposes a global singleton Jackson's ```ObjectMapper``` via the ```getInstance``` method to reduce memory
footprint and to ensure a consistent global configuration for ```ObjectMapper```. The global instance is read-only and
is protected by a dynamic proxy which prevents modification. The factory can also create new modifiable instances with
the same base configuration via the ```createInstance``` method. 

If these modules are available they are registered with ```ObjectMapper```:

* com.fasterxml.jackson.datatype.guava.GuavaModule (from: com.fasterxml.jackson.datatype:jackson-datatype-guava)
* com.fasterxml.jackson.datatype.jdk8.Jdk8Module (from: com.fasterxml.jackson.datatype:jackson-datatype-jdk8)
* com.fasterxml.jackson.datatype.joda.JodaModule (from: com.fasterxml.jackson.datatype:jackson-datatype-joda)
* com.fasterxml.jackson.datatype.jsr310.JavaTimeModule (from: com.fasterxml.jackson.datatype:jackson-datatype-jsr310)

Additionally, you may specify a comma separated list of additional module class names to register using the system
property _commons.object-mapper-additional-module-class-names_. For example:

```
-Dcommons.object-mapper-additional-module-class-names=com.example.MyModule,com.fasterxml.jackson.module.afterburner.AfterburnerModule
```

### Builder Validation Class Processor

Add the [Maven Javassist Plugin](https://github.com/ArpNetworking/maven-javassist) to your project and configure it to execute the _ValidationProcessor_. Optionally, enable
processing of test classes with the _test-process_ goal.

```xml
<plugin>
  <groupId>com.arpnetworking.commons</groupId>
  <artifactId>javassist-maven-plugin</artifactId>
  <version>0.1.0</version>
  <executions>
    <execution>
      <id>javassist-process</id>
      <goals>
        <goal>process</goal>
      </goals>
      <configuration>
        <processor>com.arpnetworking.commons.builder.ValidationProcessor</processor>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Next, add a dependency on maven-javassist-core:

```xml
<dependency>
   <groupId>com.arpnetworking.commons</groupId>
   <artifactId>javassist-maven-core</artifactId>
   <version>0.1.0</version>
</dependency>
```

Processing the validation rules into your classes instead of using OVal's built-in reflective processing can greatly
improve the performance of your application.

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (Or Invoke with JDKW)

Building:

    commons> ./jdk-wrapper.sh ./mvnw verify

To use the local version you must first install it locally:

    commons> ./jdk-wrapper.sh ./mvnw install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2015
