#!/usr/bin/env bash
# Usage: package.sh <version> <target> <build dir>
# Bundles the executable, the shared VM and the class library into dist/shim-<version>-<target>.tar.gz.
set -euo pipefail

version="${1:?version}"
target="${2:?target}"
build="${3:?build dir}"
name="shim-$version-$target"

lib="$(compgen -G "$build/libjvm.*" || compgen -G "$build/jvm.dll")"
exe="$(compgen -G "$build/shim" || compgen -G "$build/shim.exe")"
stage="$(mktemp -d)/$name"
mkdir -p "$stage" dist
cp "$exe" "$lib" "$build/classpath.jar" LICENSE LICENSE.txt "$stage"/
tar -C "$(dirname "$stage")" -czf "dist/$name.tar.gz" "$name"
