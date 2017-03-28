# How to submit a bug report

If you received an error message, please include it and any exceptions.

We commonly need to know what platform you are on:

*   JDK/JRE version (i.e., `java -version`)
*   Operating system (i.e., `uname -a`)

# How to contribute

We definitely welcome patches and contributions to Instrumentation! Here are
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

We also follow these project-specific guidelines:

### Javadoc

* All public classes and their public and protected methods MUST have javadoc.
  It MUST be complete (all params documented etc.) Everything else
  (package-protected classes, private) MAY have javadoc, at the code writer's
  whim. It does not have to be complete, and reviewers are not allowed to
  require or disallow it.
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

## Proposing changes

Make sure that `./gradlew clean assemble check` completes successfully without
any new warnings. Then create a Pull Request with your changes. When the changes
are accepted, they will be merged or cherry-picked by an Instrumentation core
developer.
