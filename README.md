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

## Overview

Basalt Shim is a fork of [Avian](https://github.com/ReadyTalk/avian) focused on
modernizing its lightweight Java virtual machine for modern Java LTS releases.
The initial target is **Java 25 LTS**. Avian's original technical documentation
is preserved in [the upstream reference](docs/upstream/avian.md).

Shim is an independent runtime project within the Basalt ecosystem. Its scope
is the VM and the Java class library support needed to execute Java bytecode:
class loading, verification, execution, memory management and native interfaces.

The organization's motivation is to run Bukkit, Spigot and Paper `.jar` plugins
without requiring an external JVM installation or process. Basalt Server does
this today: it embeds Shim in its binary and starts it only when a jar is present
in `plugins-bukkit/`. The Bukkit API and plugin lifecycle live in a Server module,
not in Shim, which only provides Java bytecode execution. BungeeCord and Velocity
adapters are planned as modules of Proxy.

Java 25 conformance is still in progress.

## Releases

Every push to `main` publishes a release named `<year>.<n>` (for example `26.5`), where `n` counts
the releases of that year and starts over in January. Each one carries a
`shim-<version>-<platform>.tar.gz` for Linux (glibc and musl), Windows, macOS, FreeBSD and Android, each on x64 and arm64, holding the `shim` executable, `libjvm`, `classpath.jar` and the licenses, plus a
`SHA256SUMS` file.

## Project layout

| Directory | Responsibility |
|---|---|
| `shim-runtime/` | VM execution, class loading, JNI entry points and embedding |
| `shim-codegen/` | Compiler, intermediate representation and architecture backends |
| `shim-classpath/` | Java class library, native library methods and OpenJDK adapters |
| `shim-system/` | Operating system services, memory and signal handling |
| `shim-heap/` | Heap and garbage collection support |
| `shim-util/` | Shared native utilities |
| `shim-tools/` | Type generation, object writing and binary embedding tools |
| `shim-tests/` | Native unit tests and Java compatibility tests |
| `cmake/` | Build configuration |
| `tools/` | Test runner and debugging support |
| `docs/` | Documentation, upstream reference and license notices |

Native modules keep headers in `include/shim/` and implementations in `src/`.
The class library lives in `shim-classpath/java/`; its native methods live in
`shim-classpath/src/`.

CMake builds everything: the VM (`libjvm`, `libshim.a`, the `shim` executables),
`classpath.jar`, the tools and the tests. It needs CMake 3.28 or newer, Clang and a
JDK (set `JAVA_HOME`, or `-DSHIM_JAVA_HOME=<jdk>`) for `javac` and the JNI headers.

```bash
cmake -S . -B build -G Ninja
cmake --build build
ctest --test-dir build
```

| Option | Default | Effect |
|---|---|---|
| `SHIM_PROCESS` | `compile` (`interpret` on Apple Silicon) | JIT compiler or interpreter |
| `SHIM_MODE` | `fast` | `debug`, `debug-fast`, `fast`, `stress`, `stress-major` or `small` |
| `SHIM_FRAME_POINTER` | `OFF` | Keep frame pointers so native stacks can be walked |
| `SHIM_TAILS`, `SHIM_CONTINUATIONS` | `OFF` | Tail calls and continuations (x86 only) |
| `SHIM_LTO` | `OFF` | Link time optimization |
| `SHIM_WERROR` | `OFF` | Warnings as errors |

Linux (glibc and musl), Android, macOS, Windows (MinGW-w64 toolchain, not MSVC) and FreeBSD are supported on x64 and arm64; CI builds and tests all twelve targets.

## License

The original material in this repository is distributed under the [MIT License](LICENSE).
Avian's original code retains its [ISC License](LICENSE.txt). Basalt Shim
modifications use MIT. Third-party components retain their respective licenses;
see [third-party notices](docs/licenses/third-party.md).

Basalt is not affiliated with Mojang Studios or Microsoft. Minecraft is a
trademark of Mojang Studios.
