# How to Create a Release of OpenCensus Java (for Maintainers Only)

## Build Environments

We deploy OpenCensus Java to Maven Central under the following systems:

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
    -   Contact a OpenCensus Java maintainer on JIRA to add your account after you
        have created it.
        -   Comment under [this JIRA bug](https://issues.sonatype.org/browse/OSSRH-32121)
            for release permission to io.opencensus.
-   (For release deployment only) [Install
    GnuPG](http://central.sonatype.org/pages/working-with-pgp-signatures.html#installing-gnupg)
    and [generate your key
    pair](http://central.sonatype.org/pages/working-with-pgp-signatures.html#generating-a-key-pair).
    You'll also need to [publish your public
    key](http://central.sonatype.org/pages/working-with-pgp-signatures.html#distributing-your-public-key)
    to make it visible to the Sonatype servers.
-   Put your GnuPG key password and OSSRH account information in
    `<your-home-directory>/.gradle/gradle.properties`:

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

In this section upstream repository refers to the main opencensus-java github
repository.

Before any push to the upstream repository you need to create a [personal access
token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/).

1.  Create the release branch and push it to GitHub:

    ```bash
    $ MAJOR=0 MINOR=4 PATCH=0 # Set appropriately for new release
    $ VERSION_FILES=(
      build.gradle
      api/src/main/java/io/opencensus/common/OpenCensusLibraryInformation.java
      )
    $ git checkout -b v$MAJOR.$MINOR.x master
    $ git push upstream v$MAJOR.$MINOR.x
    ```

2. Enable branch protection for the new branch, if you have admin access.
   Otherwise, let someone with admin access know that there is a new release
   branch.

    - Open the branch protection settings for the new branch, by following
      [Github's instructions](https://help.github.com/articles/configuring-protected-branches/).
    - Copy the settings from a previous branch, i.e., check
      - `Protect this branch`
      - `Require pull request reviews before merging`
      - `Require status checks to pass before merging`
      - `Include administrators`

      Enable the following required status checks:
      - `cla/google`
      - `continuous-integration/appveyor/pr`
      - `continuous-integration/travis-ci`
    - Uncheck everything else.
    - Click "Save changes".

3.  For `master` branch:

    -   Change root build files to the next minor snapshot (e.g.
        `0.5.0-SNAPSHOT`).

    ```bash
    $ git checkout -b bump-version master
    # Change version to next minor (and keep -SNAPSHOT)
    $ sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_OPENCENSUS_VERSION\)/'$MAJOR.$((MINOR+1)).0'\1/' \
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

4.  For `vMajor.Minor.x` branch:

    -   Change root build files to remove "-SNAPSHOT" for the next release
        version (e.g. `0.4.0`). Commit the result and make a tag:

    ```bash
    $ git checkout -b release v$MAJOR.$MINOR.x
    # Change version to remove -SNAPSHOT
    $ sed -i 's/-SNAPSHOT\(.*CURRENT_OPENCENSUS_VERSION\)/\1/' "${VERSION_FILES[@]}"
    $ ./gradlew build
    $ git commit -a -m "Bump version to $MAJOR.$MINOR.$PATCH"
    $ git tag -a v$MAJOR.$MINOR.$PATCH -m "Version $MAJOR.$MINOR.$PATCH"
    ```

    -   Change root build files to the next snapshot version (e.g.
        `0.4.1-SNAPSHOT`). Commit the result:

    ```bash
    # Change version to next patch and add -SNAPSHOT
    $ sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\(.*CURRENT_OPENCENSUS_VERSION\)/'$MAJOR.$MINOR.$((PATCH+1))-SNAPSHOT'\1/' \
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

## Deployment

Deployment to Maven Central (or the snapshot repo) is for all of the artifacts
from the project.

### Branch

Before building/deploying, be sure to switch to the appropriate branch or tag.
For the current release use:

```bash
$ git checkout -b v$MAJOR.$MINOR.$PATCH tags/v$MAJOR.$MINOR.$PATCH
```

### Initial Deployment

The following command will build the whole project and upload it to Maven
Central. Parallel building [is not safe during
uploadArchives](https://issues.gradle.org/browse/GRADLE-3420).

```bash
$ ./gradlew clean build && ./gradlew -Dorg.gradle.parallel=false uploadArchives
```

If the version has the `-SNAPSHOT` suffix, the artifacts will automatically go
to the snapshot repository. Otherwise it's a release deployment and the
artifacts will go to a staging repository.

When deploying a Release, the deployment will create [a new staging
repository](https://oss.sonatype.org/#stagingRepositories). You'll need to look
up the ID in the OSSRH UI (usually in the form of `opencensus-*`).

## Releasing on Maven Central

Once all of the artifacts have been pushed to the staging repository, the
repository must first be `closed`, which will trigger several sanity checks on
the repository. If this completes successfully, the repository can then be
`released`, which will begin the process of pushing the new artifacts to Maven
Central (the staging repository will be destroyed in the process). You can see
the complete process for releasing to Maven Central on the [OSSRH
site](http://central.sonatype.org/pages/releasing-the-deployment.html).

## Known Issues

### Deployment for tag v0.5.0
To rebuild the releases on the tag v0.5.0 use:
```bash
$ ./gradlew clean build && ./gradlew uploadArchives
```

If option `-Dorg.gradle.parallel=false` is used, you will hit [this bug](https://issues.sonatype.org/browse/OSSRH-19485)
caused by [this bug](https://github.com/gradle/gradle/issues/1827) in gradle 3.5.
