# How to Create a Release of Instrumentation Java (for Maintainers Only)

## Build Environments

We deploy Instrumentation Java to Maven Central under the following systems:

-   Ubuntu 14.04

Other systems may also work, but we haven't verified them.

## Prerequisites

### Setup OSSRH and Signing

If you haven't deployed artifacts to Maven Central before, you need to setup
your OSSRH (OSS Repository Hosting) account and signing keys.

-   Follow the instructions on [this
    page](http://central.sonatype.org/pages/ossrh-guide.html) to set up an
    account with OSSRH.
    -   You only need to create the account, not set up a new project
    -   Contact a Instrumentation Java maintainer to add your account after you
        have created it.
-   (For release deployment only) [Install
    GnuPG](http://central.sonatype.org/pages/working-with-pgp-signatures.html#installing-gnupg)
    and [generate your key
    pair](http://central.sonatype.org/pages/working-with-pgp-signatures.html#generating-a-key-pair).
    You'll also need to [publish your public
    key](http://central.sonatype.org/pages/working-with-pgp-signatures.html#distributing-your-public-key)
    to make it visible to the Sonatype servers.
-   Put your GnuPG key password and OSSRH account information in
    `<your-home-directory>/.gradle/gradle.properties`.

```
# You need the signing properties only if you are making release deployment
signing.keyId=<8-character-public-key-id>
signing.password=<key-password>
signing.secretKeyRingFile=<your-home-directory>/.gnupg/secring.gpg

ossrhUsername=<ossrh-username>
ossrhPassword=<ossrh-password>
checkstyle.ignoreFailures=false
```

## Tagging the Release

The first step in the release process is to create a release branch, bump
versions, and create a tag for the release. Our release branches follow the
naming convention of `v<major>.<minor>.x`, while the tags include the patch
version `v<major>.<minor>.<patch>`. For example, the same branch `v0.4.x` would
be used to create all `v0.4` tags (e.g. `v0.4.0`, `v0.4.1`).

1.  Create the release branch and push it to GitHub:

    ```bash
    $ MAJOR=0 MINOR=4 PATCH=0 # Set appropriately for new release
    $ VERSION_FILES=(
      build.gradle
      )
    $ git checkout -b v$MAJOR.$MINOR.x master
    $ git push upstream v$MAJOR.$MINOR.x
    ```

2.  For `master` branch:

    -   Change root build files to the next minor snapshot (e.g.
        `0.5.0-SNAPSHOT`).

    ```bash
    $ git checkout -b bump-version master
    # Change version to next minor (and keep -SNAPSHOT)
    $ sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_INSTRUMENTATION_VERSION\)/'$MAJOR.$((MINOR+1)).0'\1/' \
      "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Start $MAJOR.$((MINOR+1)).0 development cycle"
    ```

    -   Go through PR review and push the master branch to GitHub:

    ```bash
    $ git checkout master
    $ git merge --ff-only bump-version
    $ git push upstream master
    ```

3.  For `vMajor.Minor.x` branch:

    -   Change root build files to remove "-SNAPSHOT" for the next release
        version (e.g. `0.4.0`). Commit the result and make a tag:

    ```bash
    $ git checkout -b release v$MAJOR.$MINOR.x
    # Change version to remove -SNAPSHOT
    $ sed -i 's/-SNAPSHOT\(.*CURRENT_INSTRUMENTATION_VERSION\)/\1/' "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Bump version to $MAJOR.$MINOR.$PATCH"
    $ git tag -a v$MAJOR.$MINOR.$PATCH -m "Version $MAJOR.$MINOR.$PATCH"
    ```

    -   Change root build files to the next snapshot version (e.g.
        `0.4.1-SNAPSHOT`). Commit the result:

    ```bash
    # Change version to next patch and add -SNAPSHOT
    $ sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_INSTRUMENTATION_VERSION\)/'$MAJOR.$MINOR.$((PATCH+1))-SNAPSHOT'\1/' \
     "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Bump version to $MAJOR.$MINOR.$((PATCH+1))-SNAPSHOT"
    ```

    -   Go through PR review and push the release tag and updated release branch
        to GitHub:

    ```bash
    $ git checkout v$MAJOR.$MINOR.x
    $ git merge --ff-only release
    $ git push upstream v$MAJOR.$MINOR.$PATCH
    $ git push upstream v$MAJOR.$MINOR.x
    ```
