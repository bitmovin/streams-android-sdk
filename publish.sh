#!/bin/sh

setEnvVariable() {
  COMMENT="\n# Created by bitmovin-streams publish script"
  EXPORT_TOKEN="export $1=$2"
  if test -e ~/.bashrc; then
    echo "$COMMENT" >>~/.bashrc
    echo "$EXPORT_TOKEN" >>~/.bashrc
    export "$1"="$2"
    echo "Appending to '~/.bashrc' export of ENV variable '$1'."
  elif test -e ~/.bash_profile; then
    echo "$COMMENT" >>~/.bash_profile
    echo "$EXPORT_TOKEN" >>~/.bash_profile
    export "$1"="$2"
    echo "Appending to '~/.bash_profile' export of ENV variable '$1'."
  elif test -e ~/.zshrc; then
    echo "$COMMENT" >>~/.zshrc
    echo "$EXPORT_TOKEN" >>~/.zshrc
    export "$1"="$2"
    echo "Appending to '~/.zshrc' export of ENV variable '$1'."
  else
    echo "No profile file found. Please add the following line to your shell profile manually."
    echo "$EXPORT_TOKEN"
    exit 1
  fi
}

# Continue only if the user confirms
waitForApproval() {
  # shellcheck disable=SC2039
  read -p "Yes/No: " answer
  if [ "$answer" = "Yes" ] || [ "$answer" = "yes" ] || [ "$answer" = "Y" ] || [ "$answer" = "y" ]; then
    echo "Continuing..."
  else
    echo "Exiting..."
    exit 1
  fi
}

getVersion() {
  # Get the version from the gradle.properties file
  VERSION=$(grep "streamsVersion" gradle.properties | cut -d "=" -f 2)
}

isSnapshotVersion() {
  getVersion
  if [[ $VERSION == *"-SNAPSHOT" ]]; then
    return 0
  else
    return 1
  fi
}

if test -e ~/.bashrc; then
  # shellcheck disable=SC2039,SC1090
  source ~/.bashrc
fi

if test -e ~/.bash_profile; then
  # shellcheck disable=SC2039,SC1090
  source ~/.bash_profile
fi

if test -e ~/.zshrc; then
  # shellcheck disable=SC2039,SC1090
  source ~/.zshrc
fi

if [ -z "$ARTIFACTORY_USER" ]; then
  echo "Enter the token:"
  read -r ARTIFACTORY_USER
  echo ""
  setEnvVariable "ARTIFACTORY_USER" "$ARTIFACTORY_USER"
fi

if [ -z "$ARTIFACTORY_PASSWORD" ]; then
  echo "Enter the token:"
  read -r ARTIFACTORY_PASSWORD
  echo ""
  setEnvVariable "ARTIFACTORY_PASSWORD" "$ARTIFACTORY_PASSWORD"
fi



echo ""

#If not a snapshot version
if ! isSnapshotVersion; then
  echo "Git Checkout and pull 'main' branch..."
  git checkout main
  git pull
  # And no work in progress
  if git status | grep -q "nothing to commit, working tree clean"; then
    echo "No work in progress. Continuing..."
  else
    echo "There is work in progress. Please commit or stash your changes before continuing."
    exit 1
  fi
fi

echo "Did you Before publishing, make sure the version has been already bumped in the 'README.md' and 'CHANGELOG.md' and already merged as release PR into 'main' branch !"
waitForApproval

getVersion
echo "Is the version $VERSION correct?"
waitForApproval


echo "---------- Summary ----------"
echo "VERSION=$VERSION"
echo ""
echo "Artifact to publish:"
echo "  - com.bitmovin.streams:streams-android-sdk:$VERSION"
echo ""
echo "Correct?"
waitForApproval

echo ""
echo "Check correct code style..."
if ! ./gradlew ktlintCheck --daemon; then
  echo "Code style violations detected, please fix them first on main as otherwise the build will fail."
  exit
fi

if ! ./gradlew test; then
  echo "Tests failed, please fix them first on main as otherwise the build will fail."
  exit
fi

echo "Checking is JFrog API is available..."
if ! curl -s -u "$ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD" "https://bitmovin.jfrog.io/bitmovin/api/system/ping" | grep -q "OK"; then
  echo "JFrog API is not available. Please check the credentials or wait."
  exit
fi

echo ""

echo "streams-android-sdk building and publishing..."
./gradlew clean || exit
./gradlew build || exit
./gradlew streams:assembleRelease || exit
./gradlew publishToMavenLocal || exit
./gradlew artifactoryPublish || exit
./gradlew verifyLatestDocsIsUpToDate || exit
echo "streams-android-sdk built and published! (Locally)"

echo ""
echo "Please verify that the artifact works as expected in another project by using either the local maven repository or the private artifact in the jfrog repository."

echo "Git release"
echo "Git create tag v$VERSION"
git tag -a "v$VERSION" -m "v$VERSION"

echo "Git push tag 'v$VERSION' to internal repo."
git push origin main "v$VERSION"

echo "Git push 'main' and tag 'v$VERSION' to public repo."
git push git@github.com:bitmovin/streams-android-sdk main "v$VERSION"

echo "Creating release in public repo."

echo "Please create a release in the public repository"
open "https://github.com/bitmovin-engineering/streams-android-sdk/releases/new?tag=v$VERSION"
# shellcheck disable=SC2039
read -p "Press enter to continue when the release is created..."

# If it's not a SNAPSHOT version (finish with "-SNAPSHOT"), copy the artifacts to the public repository
if isSnapshotVersion ; then
  echo "Version is a SNAPSHOT version. Skipping copying to public repository."
else
  echo "Copying artifacts from libs-release-local to public-releases in jfrog ..."
  curl -H "Content-Type: application/json" -X POST -u "${ARTIFACTORY_USER}":"${ARTIFACTORY_PASSWORD}" "https://bitmovin.jfrog.io/bitmovin/api/copy/libs-release-local/com/bitmovin/streams/streams-android-sdk/${VERSION}?to=/public-releases/com/bitmovin/streams/streams-android-sdk/${VERSION}"

  echo ""

  echo "Copied artifacts to public jfrog repo."

fi

# There is no release notes from now. Maybe be added later on
#echo "Don't forget to update the changelog in readme.io"
#open "<LINK IF ANY>"
