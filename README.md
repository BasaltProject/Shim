<!--
Copyright (c) 2026 BasaltMC.
Licensed under MIT; see LICENSE:
https://github.com/BasaltProject/Shim/blob/main/LICENSE
SPDX-License-Identifier: MIT
-->

<p align="center">
  <img src="docs/assets/basalt-logo.png" alt="Basalt" width="360">
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

The organization's long-term motivation is to run legacy Bukkit `.jar` plugins
without requiring an external JVM installation or process. Shim provides the
runtime foundation for that goal. Translating Bukkit APIs into Basalt APIs and
integrating plugins with Basalt will belong to a separate project.

## Related repositories

| Repository | What it is |
|---|---|
| [Avian](https://github.com/ReadyTalk/avian) | The upstream VM that Shim forks |
| [Server](https://github.com/BasaltProject/Server) | The native Minecraft server motivating future runtime integration |
| [Proxy](https://github.com/BasaltProject/Proxy) | The Basalt proxy component |

## License

The original material in this repository is distributed under the [MIT License](LICENSE).
Avian's original code retains its [ISC License](LICENSE.txt). Basalt Shim
modifications use MIT. Third-party components retain their respective licenses;
see [third-party notices](docs/licenses/third-party.md).

Basalt is not affiliated with Mojang Studios or Microsoft. Minecraft is a
trademark of Mojang Studios.
