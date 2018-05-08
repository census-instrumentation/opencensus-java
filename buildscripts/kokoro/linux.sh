#!/bin/bash

# This file is used for Linux builds.
# To run locally:
#  ./buildscripts/kokoro/linux.sh

# This script assumes `set -e`. Removing it may lead to undefined behavior.
set -exu -o pipefail

# It would be nicer to use 'readlink -f' here but osx does not support it.
readonly OPENCENSUS_JAVA_DIR="$(cd "$(dirname "$0")"/../.. && pwd)"

# cd to the root dir of opencensus-java
cd $(dirname $0)/../..

# Run tests
./gradlew clean build

OS=`uname`
# Check the example only on Linux.
if [ "$OS" = "Linux" ] ; then
    pushd examples; ./gradlew clean assemble check --stacktrace; popd
fi
