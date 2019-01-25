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
    python ./buildscripts/check-git-history.py
    ;;
  "BUILD")
    ./gradlew clean assemble --stacktrace
    ./gradlew check :opencensus-all:jacocoTestReport
    ./gradlew verGJF

    # Run codecoverage reporting only if the script is running
    # as a part of KOKORO BUILD. If it is outside of kokoro
    # then there is no access to the codecov token and hence
    # there is no point in running it.
    if [[ -v KOKORO_BUILD_NUMBER ]]; then
      # Get token from file located at
      # $KOKORO_KEYSTORE_DIR/73495_codecov-auth-token
      if [ -f $KOKORO_KEYSTORE_DIR/73495_codecov-auth-token ] ; then
        curl -s https://codecov.io/bash | bash -s -- -Z -t @$KOKORO_KEYSTORE_DIR/73495_codecov-auth-token
      else
        echo "Codecov token file not found"
        exit 1
      fi
    else
      echo "Skipping codecov reporting"
    fi
    ;;
  "CHECKER_FRAMEWORK")
    ./gradlew clean assemble -PcheckerFramework=true
    ;;
  "BUILD_EXAMPLES_GRADLE")
    pushd examples && ./gradlew clean assemble --stacktrace && ./gradlew check && ./gradlew verGJF && popd
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
