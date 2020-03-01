#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
if [[ -d /opt/openjdk-secondary ]]; then
	./gradlew -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false --no-daemon --max-workers=4 -PtestJavaHome=/opt/openjdk-secondary -PdeploymentRepository=${repository} build publishAllPublicationsToDeploymentRepository
else
	./gradlew -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false --no-daemon --max-workers=4 -PdeploymentRepository=${repository} build publishAllPublicationsToDeploymentRepository
fi
popd > /dev/null
