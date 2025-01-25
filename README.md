# Fabiko

A Java inverse kinematics library implementing the FABRIK algorithm.

[![](https://jitpack.io/v/Elenterius/Fabiko.svg)](https://jitpack.io/#Elenterius/Fabiko)

The Fabiko library is a hard fork of the [Caliko](https://github.com/FedUni/caliko) library.

The user guide can be found on the repository [wiki](https://github.com/Elenterius/Fabiko/wiki).

## Features

- FABRIK algorithm for 3D
- various joint constraints (Local Rotor, Global Rotor, Local Hinge, Global Hinge)
- ability to connect multiple IK chains together in a hierarchy
- visualization of IK chains

> [!important]
> This library does **not** implement the FABRIK algorithm for 2D.

### Improvements over [Caliko](https://github.com/FedUni/caliko)

- migrated build system from maven to gradle
- refactored entire library to use [JOML](https://github.com/JOML-CI/JOML) for algebra operations instead of Caliko's
  own custom algebra operators
- reduced object allocations
- updated & improved JUnit tests
- migrated performance tests to jmh
- fix backward pass failing to constrain local & global hinges of non-base bones (doesn't seem to make any difference)

### Cons

- no support for 2D FABRIK
- requires JOML

### TODOs

- [ ] add GitHub action workflow for building releases automatically from conventional commits
- [ ] add prismatic joint/constraint
- refactor FABRIK algorithm to properly use Quaternions or redesign it to
  use [Dual-Quaternions](https://cs.gmu.edu/~jmlien/teaching/cs451/uploads/Main/dual-quaternion.pdf)?

## License

The library is licensed under the MIT software license.

## Credits

- [Caliko](https://github.com/FedUni/caliko), a free open-source software (FOSS) implementation of the
FABRIK (Forward And Backward Reaching Inverse Kinematics) algorithm created by Aristidou and Lasenby.
- [JOML](https://github.com/JOML-CI/JOML), a math library for linear algebra operations needed by 3D Applications.

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
repositories {
    maven { url 'https://jitpack.io' }
}
```

```gradle
dependencies {
    implementation 'com.github.Elenterius:Fabiko:TAG'
}
```
