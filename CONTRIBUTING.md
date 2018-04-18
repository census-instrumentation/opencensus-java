# How to submit a bug report

If you received an error message, please include it and any exceptions.

We commonly need to know what platform you are on:

*   JDK/JRE version (i.e., `java -version`)
*   Operating system (i.e., `uname -a`)

# How to contribute

We definitely welcome patches and contributions to OpenCensus! Here are
some guidelines and information about how to do so.

## Before getting started

In order to protect both you and ourselves, you will need to sign the
[Contributor License Agreement](https://cla.developers.google.com/clas).

[Eclipse](https://google-styleguide.googlecode.com/svn/trunk/eclipse-java-google-style.xml)
and
[IntelliJ](https://google-styleguide.googlecode.com/svn/trunk/intellij-java-google-style.xml)
style configurations are commonly useful. For IntelliJ 14, copy the style to
`~/.IdeaIC14/config/codestyles/`, start IntelliJ, go to File > Settings > Code
Style, and set the Scheme to `GoogleStyle`.

## Style
We follow the [Google Java Style
Guide](https://google.github.io/styleguide/javaguide.html). Our
build automatically will provide warnings for simple style issues.

Run the following command to format all files. This formatter uses
[google-java-format](https://github.com/google/google-java-format):

### OS X or Linux

`./gradlew goJF`

### Windows

`gradlew.bat goJF`

We also follow these project-specific guidelines:

### Javadoc

* All public classes and their public and protected methods MUST have javadoc.
  It MUST be complete (all params documented etc.) Everything else
  (package-protected classes, private) MAY have javadoc, at the code writer's
  whim. It does not have to be complete, and reviewers are not allowed to
  require or disallow it.
* Each API element should have a `@since` tag specifying the minor version when
  it was released (or the next minor version).
* There MUST be NO javadoc errors.
* See
  [section 7.3.1](https://google.github.io/styleguide/javaguide.html#s7.3.1-javadoc-exception-self-explanatory)
  in the guide for exceptions to the Javadoc requirement.
* Reviewers may request documentation for any element that doesn't require
  Javadoc, though the style of documentation is up to the author.
* Try to do the least amount of change when modifying existing documentation.
  Don't change the style unless you have a good reason.

### AutoValue

* Use [AutoValue](https://github.com/google/auto/tree/master/value), when
  possible, for any new value classes. Remember to add package-private
  constructors to all AutoValue classes to prevent classes in other packages
  from extending them.

## Building opencensus-java

Continuous integration builds the project, runs the tests, and runs multiple
types of static analysis.

Run the following commands to build, run tests and most static analysis, and
check formatting:

### OS X or Linux

`./gradlew clean assemble check verGJF`

### Windows

`gradlew.bat clean assemble check verGJF`

Use these commands to run Checker Framework null analysis:

### OS X or Linux

`./gradlew clean assemble -PcheckerFramework`

### Windows

`gradlew.bat clean assemble -PcheckerFramework`

### Checker Framework null analysis

OpenCensus uses the [Checker Framework](https://checkerframework.org/) to
prevent NullPointerExceptions. Since the project uses Java 6, and Java 6 doesn't
allow annotations on types, all Checker Framework type annotations must be
[put in comments](https://checkerframework.org/manual/#backward-compatibility).
Putting all Checker Framework annotations and imports in comments also avoids a
dependency on the Checker Framework library.

OpenCensus uses `org.checkerframework.checker.nullness.qual.Nullable` for all
nullable annotations on types, since `javax.annotation.Nullable` cannot be
applied to types. However, it uses `javax.annotation.Nullable` in API method
signatures whenever possible, so that the annotations can be uncommented and
be included in .class files and Javadocs.

### Checkstyle import control

This project uses Checkstyle to specify the allowed dependencies between
packages, using its ImportControl feature
(http://checkstyle.sourceforge.net/config_imports.html#ImportControl).
`buildscripts/import-control.xml` specifies the allowed imports. An error
messsage such as
`Disallowed import - edu.umd.cs.findbugs.annotations.SuppressFBWarnings. [ImportControl]`
could mean that `import-control.xml` needs to be updated.

## Proposing changes

Create a Pull Request with your changes. The continuous integration build will
run the tests and static analysis. It will also check that the pull request
branch has no merge commits. When the changes are accepted, they will be merged
or cherry-picked by an OpenCensus core developer.
