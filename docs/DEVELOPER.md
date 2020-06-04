# Building, Testing and Releasing Argos Parent

This document describes how to set up your development environment to build and test the Argos Parent project.
It also explains the basic mechanics of using `git`, `maven`.

* [Prerequisite Software](#prerequisite-software)
* [Getting the Sources](#getting-the-sources)
* [Building](#building)
* [Running Tests Locally](#running-tests-locally)
* [Create a Release](#create-a-release)
* [Adminstrator recovery](#administrator)

See the [contribution guidelines](https://argosnotary.github.io/docs/80_contributing/10_contributing)
if you'd like to contribute to Argos.

## Prerequisite Software

Before you can build and test the Argos Parent project, you must install and configure the
following products on your development machine:

* [Git](http://git-scm.com) and/or the **GitHub app** (for [Mac](http://mac.github.com) or
  [Windows](http://windows.github.com)); [GitHub's Guide to Installing
  Git](https://help.github.com/articles/set-up-git) is a good source of information.

* [Maven](https://maven.apache.org).

* [Lombok](https://projectlombok.org).

* [Docker](https://www.docker.com).

* jdk8 and jdk11 installation

* [Toolchain](https://maven.apache.org/guides/mini/guide-using-toolchains.html)

For Toolchain add the file `toolchain.xml` with the following content to `M2_HOME`. Change the jdkHome paths to
your own Java installation paths.

```xml
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">
 <!-- JDK toolchains -->
 <toolchain>
   <type>jdk</type>
   <provides>
     <version>1.8</version>
     <vendor>openjdk</vendor>
   </provides>
   <configuration>
     <jdkHome>jdk 8 Java Home</jdkHome>
   </configuration>
 </toolchain>
 <toolchain>
   <type>jdk</type>
   <provides>
     <version>11</version>
     <vendor>openjdk</vendor>
   </provides>
   <configuration>
     <jdkHome>jdk 11 Java Home</jdkHome>
   </configuration>
 </toolchain>
</toolchains>
```


## Getting the Sources

Fork and clone the the Argos Parent repository:

1. Login to your GitHub account or create one by following the instructions given
   [here](https://github.com/signup/free).
2. [Fork](http://help.github.com/forking) the [main Argos
   repository](https://github.com/argosnotary/argos-parent).
3. Clone your fork of the Argos repository and define an `upstream` remote pointing back to
   the Argos repository that you forked in the first place.

```shell
# Clone your GitHub repository:
git clone git@github.com:<github username>/argos-parent.git

# Go to the Argos directory:
cd argos-parent

# Add the main Argos repository as an upstream remote to your repository:
git remote add upstream https://github.com/argosnotary/argos-parent.git
```

## Building

To build Argos Parent run:

```shell
mvn clean install
```

* Results are put in the diverse `target` folders.

## Running Tests Locally

You should execute all test suites before submitting a PR to GitHub:

```shell
mvn -q clean install
cd argos-test
mvn -q clean verify -Pregression-test-drone

```

**Note**: The first test run will be much slower than future runs. This is because future runs will
benefit from Bazel's capability to do incremental builds.

All the tests are executed on our Continuous Integration infrastructure. PRs can only be
merged if the code is formatted properly and all tests are passing.

## Create a Release

A release creates the following artifacts:
* docker.io/argosnotary/argos-service:[version]
* docker.io/argosnotary/argos-frontend:[version]
* com.rabobank.argos.argos4j in Maven Central
* com.rabobank.argos.argos-service in Maven Central

A release can only be made of the master branch.

To make a release perform the following actions:
* When necessary change the version

```
tools/change_project_version.sh [version]
```
* Create a tag and push to github:

```
tools/release_with_tag.sh [version]
```
After this drone.io will create and publish a release.
* Change the version to the possible future version.

```
tools/change_project_version.sh [future version]
```
This version can always be changed in a future release.

