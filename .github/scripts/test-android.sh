#!/usr/bin/env bash
# Runs the Java test programs on the emulator against a cross-built Shim.
set -euo pipefail
build="${1:?build directory}"
remote=/data/local/tmp/shim-ci
adb shell "rm -rf $remote && mkdir -p $remote"
adb push "$build/shim" "$remote/shim"
adb push "$build/test" "$remote/test"
adb shell "chmod 755 $remote/shim"
for program in Hello Strings Exceptions Collections Floats Integers Longs Widening Misc Enums Reflection Threads GC InvokeDynamic; do
    echo "== $program"
    adb shell "cd $remote && ./shim -cp test $program; echo exit=\$?" | tee /tmp/out.txt
    grep -q '^exit=0' /tmp/out.txt
done
