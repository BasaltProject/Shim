<!--
Copyright (c) 2026 BasaltMC.
Licensed under MIT; see LICENSE:
https://github.com/BasaltProject/Shim/blob/main/LICENSE
SPDX-License-Identifier: MIT
-->

<p align="center">
  <a href="https://raw.githubusercontent.com/BasaltProject/.github/main/assets/basalt-logo-white.svg#gh-dark-mode-only">
    <img src="https://raw.githubusercontent.com/BasaltProject/.github/main/assets/basalt-logo-white.svg" alt="Basalt" width="360">
  </a>
  <a href="https://raw.githubusercontent.com/BasaltProject/.github/main/assets/basalt-logo-black.svg#gh-light-mode-only">
    <img src="https://raw.githubusercontent.com/BasaltProject/.github/main/assets/basalt-logo-black.svg" alt="Basalt" width="360">
  </a>
</p>

<h2 align="center">Basalt Shim</h2>

<p align="center">
  <strong>A modern, lightweight, embeddable JVM in C++, forked from Avian.</strong>
</p>

<p align="center">
  <img alt="Java target" src="https://img.shields.io/badge/Java_target-25_LTS-ED8B00">
  <img alt="C++" src="https://img.shields.io/badge/language-C%2B%2B-00599C?logo=cplusplus&logoColor=white">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-green">
</p>

## About

Basalt Shim is a fork of [Avian](https://github.com/ReadyTalk/avian) that modernizes its lightweight Java virtual
machine for modern Java LTS releases, starting with **Java 25**. Java 25 conformance is still in progress.

Shim is an independent project: it was born for Basalt, but it is a general purpose VM you can embed in any
application that needs to run Java bytecode, with no JDK or JRE installed on the host. Its scope is the VM and the
class library support needed to run that bytecode.

Within Basalt it is one of the layers that make Java plugins possible. Server embeds it in its binary and starts it
only when a plugin jar is present, and [Stratum](https://github.com/BasaltProject/Stratum) provides the Bukkit,
BungeeCord and Velocity APIs on top of it. Avian's original documentation is kept in
[docs/upstream](docs/upstream/avian.md).

## Releases

Every push to `main` publishes a release named `<year>.<n>` with a `shim-<version>-<platform>.tar.gz` for Linux (glibc
and musl), Windows, macOS, FreeBSD and Android, on x64 and arm64, plus `SHA256SUMS`.

## Building

```bash
cmake -S . -B build -G Ninja
cmake --build build
ctest --test-dir build
```

Needs CMake 3.28+, Clang and a JDK 25+ (`JAVA_HOME`, or `-DSHIM_JAVA_HOME=<jdk>`) for `javac` and the JNI headers.
Windows builds use the MinGW-w64 toolchain, not MSVC.

## License

The original material is under the [MIT License](LICENSE). Avian's code keeps its [ISC License](LICENSE.txt), and
third-party components keep theirs; see the [notices](docs/licenses/third-party.md).

Basalt is not affiliated with Mojang Studios or Microsoft. Minecraft is a trademark of Mojang Studios.
