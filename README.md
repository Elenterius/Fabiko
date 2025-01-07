# Fabiko

A Java inverse kinematics library implementing the FABRIK algorithm.

[![](https://jitpack.io/v/Elenterius/Fabiko.svg)](https://jitpack.io/#Elenterius/Fabiko)

The Fabiko library is a custom fork of the [Caliko](https://github.com/FedUni/caliko) library.

The user guide can be found on the repository [wiki](https://github.com/Elenterius/Fabiko/wiki).

## Features

- FABRIK algorithm for 2D and 3D
- various joint constraints (Local Hinge, Global Hinge, Rotor)
- ability to connect multiple IK chains together in a hierarchy
- visualisation of IK chains

### Improvements over [Caliko](https://github.com/FedUni/caliko)

- migrated build system from maven to gradle
- updated & improved JUnit tests
- migrated performance tests to jmh
- reduced object allocation

### BugFixes

- FabrikChain3d:
  - fix backward pass not constraining local & global hinges of non-base bones
  - fix crash caused by forward pass of 1 bone chains with local hinges

### TODOs

- GitHub action workflow for building releases automatically from conventional commits
- Refactor entire library to use [JOML](https://github.com/JOML-CI/JOML) for algebra operations instead of Caliko's
  custom solution
  - use Quaternions!
- Add parabolic constraint types?
- Streamline Model class object copying?

## License

The library is licensed under the MIT software license and the source code is freely available for use and
modification.

## Credits

Caliko, a free open-source software (FOSS) implementation of the
FABRIK (Forward And Backward Reaching Inverse Kinematics) algorithm created by Aristidou and Lasenby.

## Literature

Further details on the FABRIK algorithm itself can be found in the following paper:
`Aristidou, A., & Lasenby, J. (2011). FABRIK: a fast, iterative solver for the inverse kinematics problem. Graphical Models, 73(5), 243-260.`

## Structure

The library is a multi-module gradle project with the following modules:

The **core** module contains the core IK portion of the library and has a dependency
on [JOML](https://github.com/JOML-CI/JOML)

The **visualisation** module contains the optional visualisation component of the library which provides the ability to
draw various IK structures/chains/bones and depends on the core Fabiko functionality as well as the LWJGL 3.3.5 library.

The **demo** module contains a demonstration of the library utilising both 2D and 3D IK chains in various
configurations. It requires the fabiko-core, fabiko-visualisation and LWJGL 3.3.5 libraries.

## Build and Setup

To build yourself:

`git clone https://github.com/Elenterius/Fabiko`

`gradlew build`

## Usage

To include the library in your own project use the [jitpack maven repository](https://jitpack.io/#Elenterius/Fabiko/) or
download a [release](https://github.com/Elenterius/Fabiko/releases) from GitHub.

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.Elenterius:Fabiko:Tag'
}
```
