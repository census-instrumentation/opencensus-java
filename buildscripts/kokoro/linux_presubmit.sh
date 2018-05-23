#!/bin/bash

# This file is used for Linux builds.
# It expects TASK environment variable is defined.
# To run locally:
#  ./buildscripts/kokoro/linux.sh

# This script assumes `set -e`. Removing it may lead to undefined behavior.
set -exu -o pipefail

# It would be nicer to use 'readlink -f' here but osx does not support it.
readonly OPENCENSUS_JAVA_DIR="$(cd "$(dirname "$0")"/../.. && pwd)"

# cd to the root dir of opencensus-java
cd $(dirname $0)/../..

valid_tasks() {
  echo "Valid tasks are"
  echo ""
  echo "- BUILD"
  echo "- BUILD_EXAMPLES_BAZEL"
  echo "- BUILD_EXAMPLES_GRADLE"
  echo "- BUILD_EXAMPLES_MAVEN"
  echo "- CHECKER_FRAMEWORK"
  echo "- CHECK_EXAMPLES_FORMAT"
  echo "- CHECK_EXAMPLES_LICENSE"
  echo "- CHECK_GIT_HISTORY"
}

if [[ ! -v TASK ]]; then
  set +x
  echo "TASK not set in environment"
  valid_tasks
  exit 1
fi

case "$TASK" in
  "CHECK_GIT_HISTORY")
    python ./scripts/check-git-history.py
    ;;
  "BUILD")
    ./gradlew clean assemble --stacktrace
    ./gradlew check :opencensus-all:jacocoTestReport
    ./gradlew verGJF

    # Get token from file located at
    # $KOKORO_KEYSTORE_DIR/73495_codecov-auth-token
    if [ -f $KOKORO_KEYSTORE_DIR/73495_codecov-auth-token ] ; then
      export CODECOV_TOKEN=`cat $KOKORO_KEYSTORE_DIR/73495_codecov-auth-token`
    fi
    if [[ ! -v CODECOV_TOKEN ]]; then
      echo "Environment variable CODECOV_TOKEN not set"
      exit 1
    fi
    curl -s https://codecov.io/bash | bash
    ;;
  "CHECKER_FRAMEWORK")
    ./gradlew clean assemble -PcheckerFramework=true
    ;;
  "CHECK_EXAMPLES_LICENSE")
    curl -L -o checkstyle-8.0-all.jar https://sourceforge.net/projects/checkstyle/files/checkstyle/8.0/checkstyle-8.0-all.jar/download
    java -DrootDir=. -jar checkstyle-8.0-all.jar -c buildscripts/checkstyle.xml examples/src/
    ;;
  "CHECK_EXAMPLES_FORMAT")
    curl -L -o google-java-format-1.5-all-deps.jar \
      https://github.com/google/google-java-format/releases/download/google-java-format-1.5/google-java-format-1.5-all-deps.jar
    java -jar google-java-format-1.5-all-deps.jar --set-exit-if-changed --dry-run `find examples/src/ -name '*.java'`
    ;;
  "BUILD_EXAMPLES_GRADLE")
    pushd examples && ./gradlew clean assemble --stacktrace && popd
    ;;
  "BUILD_EXAMPLES_MAVEN")
    pushd examples && mvn clean package appassembler:assemble -e && popd
    ;;
  "BUILD_EXAMPLES_BAZEL")
    pushd examples && bazel clean && bazel build :all && popd
    ;;
  *)
    set +x
    echo "Unknown task $TASK"
    valid_tasks
    exit 1
    ;;
esac
