## About ##

The purpose of this `checkstyle-resources` module is to provide a uniform set of Checkstyle rules used by Data Conservancy projects that wish to use Checkstyle.

## Checkstyle Rules

[Checkstyle](http://checkstyle.sourceforge.net/) is a platform for helping Java programmers adhere to a particular coding convention.  Checkstyle _rules_ are encoded as XML in a so-called _rules file_.  The rules file embodies the constraints associated with a particular coding convention.  For example, there is a rules file for [Oracle (Sun)](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/sun_checks.xml) coding conventions and one for [Google](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml).  

The Data Conservancy [rules file](src/main/resources/dc-checkstyle/checkstyle.xml) is based off of the Fedora 4 [rules file](https://github.com/fcrepo4/fcrepo-build-tools/blob/master/src/main/resources/fcrepo-checkstyle/checkstyle.xml), with some modifications.

Checkstyle comes with [rules out of the box](http://checkstyle.sourceforge.net/checks.html), and [custom rules](http://checkstyle.sourceforge.net/writingchecks.html) can be developed as well.

## Rationale

Basically, Checkstyle will help:

* Keep code reviews more substantive; the review will be about the code itself, and not have to be concerned about braces, or whitespace, or any of the other mundane things (it is assumed that code submitted for review adheres to the rules)
* Ease code review; diffs will be cleaner because a mix of tabs and spaces won't be being used, or the difference between brace styles that might otherwise be introduced will be disallowed
* Maintain clean dependency trees by rejecting unused imports
* Make the code easier to look at; uniformity will reduce the (admittedly small) impact of looking at one coding style vs another

Using Checkstyle in your project is optional, but ideally the _rules_ used by those projects will be uniform (i.e. they will all use the Data Conservancy [rules file](src/main/resources/dc-checkstyle/checkstyle.xml)).

## Using Checkstyle with your project

Checkstyle is essentially a software library that executes a set of rules over source code, and provides the results of executing those rules to the caller for evaluation.  The design of Checkstyle provides for independence of a specific development environment, allowing it to adapt to popular build tools and IDEs.  

In order to run Checkstyle, the development environment needs to provide support for invoking Checkstyle with a rules file.  The sections below detail the setup of Checkstyle for various development environments.

### Maven

To run Checkstyle in Maven, you need to configure the `maven-checkstyle-plugin` and make sure an instance of the plugin is configured in your POM.  

For Data Conservancy, the plugin is *defined and configured* in the [project-pom](https://github.com/DataConservancy/dcs/blob/master/maven/project/pom.xml), starting with version 9.  It looks something like this:

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-checkstyle-plugin</artifactId>
      <version>2.17</version>
      <dependencies>
        <!-- Note 'checkstyle-resources' is a child maven module of 'project-pom' -->
        <dependency>
          <groupId>org.dataconservancy</groupId>
          <artifactId>checkstyle-resources</artifactId>
          <version>1</version>
        </dependency>
      </dependencies>
      <configuration>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
        <consoleOutput>true</consoleOutput>
        <logViolationsToConsole>true</logViolationsToConsole>
        <failsOnError>true</failsOnError>
        <failOnViolation>true</failOnViolation>
        <violationSeverity>warning</violationSeverity>
        <configLocation>dc-checkstyle/checkstyle.xml</configLocation>
        <suppressionsLocation>dc-checkstyle/checkstyle-suppressions.xml</suppressionsLocation>
      </configuration>
      <executions>
        <execution>
          <id>checkstyle</id>
          <phase>verify</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

In order for the plugin to be *active*, it must be instantiated in a POM that inherits from the Data Conservancy `project-pom` (this is the optional part).  Including the following snippet in your POM's `<build>/<plugins>` section will activate Checkstyle for your project:

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-checkstyle-plugin</artifactId>
    </plugin>

No additional configuration or version parameters are needed in your POM; they will be inherited from the `project-pom`.

In the Data Conservancy environment, complying with style is a requirement for projects that decide to use Checkstyle.  Therefore, your build will fail in the `verify` phase if your code does not comply with style.

Checkstyle can be skipped by adding `-Dcheckstyle.skip` to the `mvn` command line.

As always, Maven is the final arbiter of any discrepancies with Checkstyle.  If your IDE says your code style is OK, but Maven doesn't, Maven wins.

### IDEA

Checkstyle in IDEA is not perfect per my experience.  The annoyances center around the real-time updating of Checkstyle errors in the editing window:

* not all style mistakes are highlighted in the source editor - errors like trailing whitespace are not always obvious
* error highlighting does not go away after fixing a style mistake

Manually running Checkstyle within IDEA against a particular source file works, so if you're unsure if any errors exist (e.g. because a style mistake isn't highlighted, or you've corrected a style mistake and the highlighting hasn't gone away in the editor window), running the check manually will let you know.

With that, here's how I use Checkstyle in IDEA:

1. Download the most recent version of [rules](https://github.com/emetsger/dcs/blob/checkstyle/maven/checkstyle-resources/src/main/resources/dc-checkstyle/checkstyle.xml)
2. Enable the Checkstyle-IDEA plugin.  I have version 4.34.0.
3. Configure the plugin to point to the rules file that you downloaded in step 1.  
4. I configure the plugin to cover all source files, production and test (because that aligns with how Maven will execute Checkstyle)

### Eclipse

TODO
