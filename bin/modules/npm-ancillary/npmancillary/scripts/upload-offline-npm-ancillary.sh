#!/bin/sh
# Upload Offline NPM Ancillary Module
# Supports: Linux or Mac
# 1. Downloads the smartedit-module and npm-ancillary-module artifact from either the release repository or the snapshot repository.
# 2. Start verdaccio.
# 3. Build smartedittools: this will use verdaccio and generate .cache folder in npm-ancillary.
# 4. Stop verdaccio.
# 5. Zips the npm-ancillary module along with the .cache folder and uploads to artifactory(release or snapshot).
OS_NAME=$(uname -s)

SAP_RELEASE_REPO_ID="milestones"
SAP_SNAPSHOT_REPO_ID="snapshots"
ARTIFACTORY_REPOSITORY_ID="hybris-repository"

SAP_RELEASE_REPO="https://common.repositories.sap.ondemand.com/artifactory/deploy-milestones-cx-commerce-maven"
SAP_SNAPSHOT_REPO="https://common.repositories.sap.ondemand.com/artifactory/deploy-snapshots-cx-commerce-maven"
ARTIFACTORY_RELEASE_REPO="https://repository.hybris.com/hybris-release"
ARTIFACTORY_SNAPSHOT_REPO="https://repository.hybris.com/hybris-snapshot"

PROJECT_GROUPID="de.hybris.platform"
NPMANCILLARY_PROJECT_ARTIFACTID="npm-ancillary-module"
SMARTEDIT_PROJECT_ARTIFACTID="smartedit-module"

ARTIFACT_VERSION=$1
WORKSPACE_DIRECTORY=$2

if [[ "${ARTIFACT_VERSION}" == "" ]] ; then
    echo "Local usage: ./upload-offline-npm-ancillary.sh ARTIFACT_VERSION"
    echo "Example local usage: ./upload-offline-npm-ancillary.sh 6.6.0.0-RC4-SNAPSHOT"
    exit -1
fi

if [[ "${WORKSPACE_DIRECTORY}" == "" ]] ; then
    WORKSPACE=$(pwd)/build
else
    WORKSPACE=${WORKSPACE_DIRECTORY}
fi

NPM_MODULE_HOME=${WORKSPACE}/hybris/bin/ext-content/npmancillary
SMARTEDIT_EXTENSION_HOME=${WORKSPACE}/hybris/bin/ext-content/smartedit
CMSSMARTEDIT_EXTENSION_HOME=${WORKSPACE}/hybris/bin/ext-smartedit/cmssmartedit
SMARTEDITTOOLS_MODULE_HOME=${WORKSPACE}/hybris/bin/ext-content/smartedittools
NPM_RESOURCE_HOME=${NPM_MODULE_HOME}/resources/npm
OFFLINE_DIRECTORY=offline

if [[ "${ARTIFACT_VERSION}" == *SNAPSHOT ]] ; then
    SAP_REPOSITORY_ID=$SAP_SNAPSHOT_REPO_ID
    SAP_TARGET_REPO=$SAP_SNAPSHOT_REPO
    ARTIFACTORY_TARGET_REPO=$ARTIFACTORY_SNAPSHOT_REPO
else
    SAP_REPOSITORY_ID=$SAP_RELEASE_REPO_ID
    SAP_TARGET_REPO=$SAP_RELEASE_REPO
    ARTIFACTORY_TARGET_REPO=$ARTIFACTORY_RELEASE_REPO
fi

if [ "${OS_NAME}" = "Darwin" ] ; then
    NODE_HOME=${NPM_RESOURCE_HOME}/node/node-v16.0.0-darwin-x64/bin
    OFFLINE_PROJECT_ARTIFACT_ID=offline-darwin-${NPMANCILLARY_PROJECT_ARTIFACTID}
elif [ "${OS_NAME}" = "Linux" ] ; then
    NODE_HOME=${NPM_RESOURCE_HOME}/node/node-v16.0.0-linux-x64/bin
    OFFLINE_PROJECT_ARTIFACT_ID=offline-linux-${NPMANCILLARY_PROJECT_ARTIFACTID}
fi

echo """
Running upload-offline-npm-ancillary.sh

OS_NAME: ${OS_NAME}
WORKSPACE: ${WORKSPACE}

Repository Information:
SAP_REPOSITORY_ID: ${SAP_REPOSITORY_ID}
ARTIFACTORY_REPOSITORY_ID: ${ARTIFACTORY_REPOSITORY_ID}
SAP_TARGET_REPO: ${SAP_TARGET_REPO}
ARTIFACTORY_TARGET_REPO: ${ARTIFACTORY_TARGET_REPO}

Project Information:
PROJECT_GROUPID: ${PROJECT_GROUPID}
NPMANCILLARY_PROJECT_ARTIFACTID: ${NPMANCILLARY_PROJECT_ARTIFACTID}
SMARTEDIT_PROJECT_ARTIFACTID: ${SMARTEDIT_PROJECT_ARTIFACTID}
ARTIFACT_VERSION: ${ARTIFACT_VERSION}

NPM_RESOURCE_HOME: ${NPM_RESOURCE_HOME}

NODE_HOME: ${NODE_HOME}
OFFLINE_PROJECT_ARTIFACT_ID: ${OFFLINE_PROJECT_ARTIFACT_ID}

"""

# Create workspace and download artifacts
# rm -rf $WORKSPACE
# mkdir -p $WORKSPACE
cd $WORKSPACE

echo "Downloading npm-ancillary-module artifact with dest=${WORKSPACE}/${NPMANCILLARY_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip"
mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:get \
    -Dartifact=${PROJECT_GROUPID}:${NPMANCILLARY_PROJECT_ARTIFACTID}:${ARTIFACT_VERSION}:zip \
    -Ddest=${WORKSPACE}/${NPMANCILLARY_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip
echo "Unzipping npm-ancillary artifact"
unzip -oqq ${WORKSPACE}/${NPMANCILLARY_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip

echo "Downloading smartedit-module artifact with dest=${WORKSPACE}/${SMARTEDIT_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip"
mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:get \
    -Dartifact=${PROJECT_GROUPID}:${SMARTEDIT_PROJECT_ARTIFACTID}:${ARTIFACT_VERSION}:zip \
    -Ddest=${WORKSPACE}/${SMARTEDIT_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip
echo "Unzipping smartedit artifact"
unzip -oqq ${WORKSPACE}/${SMARTEDIT_PROJECT_ARTIFACTID}-${ARTIFACT_VERSION}.zip

echo "Updating PATH for node binary to $NODE_HOME"
export PATH=$NODE_HOME:$PATH

echo "We need to copy verdaccio-lib from local repo since we already exclude from artifact"
cp -r ${WORKSPACE}/npm-repo/npmancillary/resources/npm/verdaccio/verdaccio-lib ${NPM_RESOURCE_HOME}/verdaccio/verdaccio-lib

echo "Set & Repair npm permission"
if [ "${OS_NAME}" = "Darwin" ] ; then
    sh ${NPM_RESOURCE_HOME}/repairnpm.sh darwin
elif [ "${OS_NAME}" = "Linux" ] ; then
    sh ${NPM_RESOURCE_HOME}/repairnpm.sh linux
else
    echo "Do nothing"
fi

echo "Starting verdaccio"
cd ${NPM_RESOURCE_HOME}/verdaccio
sh start-verdaccio.sh config.yaml true

echo "Build smartedittools module"
cd ${SMARTEDITTOOLS_MODULE_HOME}
rm -rf ~/.pnpm-store && rm -rf ~/.rush/
npm cache clear --force
node ./apps/smartedit-scripts/scripts/link-smartedit-project.js $SMARTEDIT_EXTENSION_HOME,$CMSSMARTEDIT_EXTENSION_HOME .
${NPM_RESOURCE_HOME}/rush/bin/rush update --full

echo "Stopping verdaccio"
cd ${NPM_RESOURCE_HOME}/verdaccio
sh stop-verdaccio.sh config.yaml true

echo "create offline folder structure"
cd $WORKSPACE
mkdir -p offline/hybris/bin/ext-content
cd offline/hybris/bin/ext-content
cp -r ${NPM_MODULE_HOME} npmancillary

echo "Zipping offline ancillary"
cd $WORKSPACE/offline/
zip -ryq ${WORKSPACE}/${OFFLINE_PROJECT_ARTIFACT_ID}-${ARTIFACT_VERSION}.zip ./hybris

echo "Deploying artifact ${OFFLINE_PROJECT_ARTIFACT_ID}-${ARTIFACT_VERSION}.zip to SAP artifactory"
cd $WORKSPACE
mvn deploy:deploy-file -DrepositoryId=${SAP_REPOSITORY_ID} -Durl=${SAP_TARGET_REPO} -Dfile=${OFFLINE_PROJECT_ARTIFACT_ID}-${ARTIFACT_VERSION}.zip -DgroupId=${PROJECT_GROUPID} -Dversion=${ARTIFACT_VERSION} -DartifactId=${OFFLINE_PROJECT_ARTIFACT_ID} -DgeneratePom=true

echo "Deploying artifact ${OFFLINE_PROJECT_ARTIFACT_ID}-${ARTIFACT_VERSION}.zip to Hybris artifactory"
cd $WORKSPACE
mvn deploy:deploy-file -DrepositoryId=${ARTIFACTORY_REPOSITORY_ID} -Durl=${ARTIFACTORY_TARGET_REPO} -Dfile=${OFFLINE_PROJECT_ARTIFACT_ID}-${ARTIFACT_VERSION}.zip -DgroupId=${PROJECT_GROUPID} -Dversion=${ARTIFACT_VERSION} -DartifactId=${OFFLINE_PROJECT_ARTIFACT_ID} -DgeneratePom=true

echo "Cleaning workspace"
# rm -rf $WORKSPACE
