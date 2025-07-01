#!/usr/bin/env python3
# Copyright (c) 2026 BasaltMC.
# Licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
# SPDX-License-Identifier: MIT

"""Compare programs compiled by JDK 25 against Shim and OpenJDK."""

import argparse
import os
import re
from pathlib import Path
import shutil
import struct
import subprocess
import sys

sys.dont_write_bytecode = True
from java25_constants import generate as generate_constants
from java25_dynamic import generate as generate_dynamic
from java25_verify import generate as generate_verification

ROOT = Path(__file__).resolve().parents[1]


def run(command):
    return subprocess.run(command, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=30)


def constant_pool_offsets(data):
    count = struct.unpack_from(">H", data, 8)[0]
    index, position = 1, 10
    entries = {}
    while index < count:
        tag = data[position]
        entries[index] = (tag, position)
        position += 1
        if tag == 1:
            position += 2 + struct.unpack_from(">H", data, position)[0]
        elif tag in (5, 6):
            position += 8
            index += 1
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            position += 4
        elif tag == 15:
            position += 3
        elif tag in (7, 8, 16, 19, 20):
            position += 2
        else:
            raise RuntimeError("Unexpected fixture constant pool tag")
        index += 1
    return entries


def check_versions(java, runtimes, directory):
    original = (directory / "ClassFileValidation.class").read_bytes()
    variants = [
        ("future", 70, 0, "UnsupportedClassVersionError"),
        ("too-old", 44, 0, "UnsupportedClassVersionError"),
        ("preview", 69, 65535, "UnsupportedClassVersionError"),
        ("old-preview", 61, 65535, "UnsupportedClassVersionError"),
        ("invalid-minor", 69, 1, "UnsupportedClassVersionError"),
        ("legacy-minor", 52, 1, None),
        ("bad-magic", 69, 0, "ClassFormatError"),
        ("truncated", 69, 0, "ClassFormatError"),
        ("truncated-pool", 69, 0, "ClassFormatError"),
        ("bad-pool-size", 69, 0, "ClassFormatError"),
        ("bad-pool-tag", 69, 0, "ClassFormatError"),
        ("long-without-slot", 69, 0, "ClassFormatError"),
        ("cyclic-class", 69, 0, "ClassFormatError"),
        ("wrong-reference-tag", 69, 0, "ClassFormatError"),
    ]
    failures = 0
    for name, major, minor, error in variants:
        output = directory / name
        output.mkdir()
        data = bytearray(original)
        struct.pack_into(">HH", data, 4, minor, major)
        if name == "bad-magic":
            data[0] = 0
        elif name == "truncated":
            data = data[:7]
        elif name == "truncated-pool":
            data = data[:12]
        elif name == "bad-pool-size":
            struct.pack_into(">H", data, 8, 0)
        elif name == "bad-pool-tag":
            data[10] = 0
        elif name == "long-without-slot":
            struct.pack_into(">H", data, 8, 2)
            data[10] = 5
        elif name in ("cyclic-class", "wrong-reference-tag"):
            entries = constant_pool_offsets(data)
            class_index = next(index for index, (tag, _) in entries.items() if tag == 7)
            position = entries[class_index][1]
            target = class_index if name == "cyclic-class" else next(
                index for index, (tag, _) in entries.items() if tag == 10)
            struct.pack_into(">H", data, position + 1, target)
        (output / "ClassFileValidation.class").write_bytes(data)
        reference = run([java, "-cp", str(output), "ClassFileValidation"])
        if error:
            if reference.returncode <= 0 or error not in reference.stderr:
                raise RuntimeError("Unexpected OpenJDK error for " + name)
        elif reference.returncode:
            raise RuntimeError("Unexpected OpenJDK rejection for " + name)
        for runtime in runtimes:
            try:
                result = run([runtime, "-cp", str(output), "ClassFileValidation"])
                passed = (result.returncode > 0 and error in result.stderr) if error else (
                    result.returncode == 0 and result.stdout == reference.stdout)
                detail = result.stderr
            except subprocess.TimeoutExpired:
                passed, detail = False, "Timed out"
            print("{} class-file={} {}".format("PASS" if passed else "FAIL", name, runtime))
            if not passed:
                failures += 1
                print(detail, file=sys.stderr)
    return failures


def check_dynamic_formats(java, runtimes, directory):
    failures = 0
    for variant in ("old-dynamic-version", "void-dynamic-type"):
        output = directory.parent / (directory.name + "-" + variant)
        if output.exists():
            shutil.rmtree(output)
        shutil.copytree(directory, output)
        path = output / "DynamicConstantData.class"
        data = bytearray(path.read_bytes())
        if variant == "old-dynamic-version":
            struct.pack_into(">H", data, 6, 54)
        else:
            for tag, position in constant_pool_offsets(data).values():
                if tag == 1 and struct.unpack_from(">H", data, position + 1)[0] == 1 and data[position + 3] == ord("B"):
                    data[position + 3] = ord("V")
                    break
            else:
                raise RuntimeError("Missing dynamic descriptor fixture")
        path.write_bytes(data)
        reference = run([java, "-cp", str(output), "DynamicConstants"])
        if reference.returncode <= 0 or "ClassFormatError" not in reference.stderr:
            raise RuntimeError("Unexpected OpenJDK dynamic-format result")
        for runtime in runtimes:
            result = run([runtime, "-cp", str(output), "DynamicConstants"])
            passed = result.returncode > 0 and "ClassFormatError" in result.stderr
            print("{} dynamic-format={} {}".format("PASS" if passed else "FAIL", variant, runtime))
            if not passed:
                failures += 1
                print(result.stderr, file=sys.stderr)
    return failures


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--shim", action="append", type=Path, required=True)
    parser.add_argument("--jdk", type=Path)
    parser.add_argument("--release", action="append", type=int, choices=(8, 17, 21, 25))
    parser.add_argument("--case", action="append")
    args = parser.parse_args()
    jdk = args.jdk or Path(os.environ.get("JAVA_HOME") or Path(shutil.which("javac") or "javac").resolve().parents[1])
    java = str(jdk / "bin/java")
    javac = str(jdk / "bin/javac")
    runtimes = [str(binary.resolve()) for binary in args.shim]
    fixtures = sorted((ROOT / "shim-tests/java25").glob("*.java"))
    if args.case:
        fixtures = [source for source in fixtures if source.stem in args.case]
        missing = set(args.case) - {source.stem for source in fixtures}
        if missing:
            parser.error("Unknown case: " + ", ".join(sorted(missing)))
    failures = 0
    for release in args.release or [8, 17, 21, 25]:
        for source in fixtures:
            minimum = re.search(r"Minimum release:\s*(\d+)", source.read_text())
            if minimum and release < int(minimum.group(1)):
                continue
            output = ROOT / "build/java25-tests" / str(release) / source.stem
            if output.exists():
                shutil.rmtree(output)
            output.mkdir(parents=True)
            compile_result = run([javac, "--release", str(release), "-d", str(output), str(source)])
            if compile_result.returncode:
                print(compile_result.stderr, file=sys.stderr)
                return 1
            major = struct.unpack(">H", (output / (source.stem + ".class")).read_bytes()[6:8])[0]
            if major != release + 44:
                raise RuntimeError("Unexpected class-file version")
            if source.stem == "LoadableConstants":
                generate_constants(output, major)
            elif source.stem == "DynamicConstants":
                generate_dynamic(output, major)
            elif source.stem == "BytecodeValidation":
                generate_verification(output, major)
            reference = run([java, "-cp", str(output), source.stem])
            if reference.returncode:
                print(reference.stderr, file=sys.stderr)
                return 1
            for runtime in runtimes:
                try:
                    result = run([runtime, "-cp", str(output), source.stem])
                    passed = result.returncode == 0 and result.stdout == reference.stdout
                    detail = result.stderr or result.stdout
                except subprocess.TimeoutExpired:
                    passed, detail = False, "Timed out"
                print("{} release={} {} {}".format(
                    "PASS" if passed else "FAIL", release, source.stem,
                    str(Path(runtime).relative_to(ROOT)) if Path(runtime).is_relative_to(ROOT) else runtime))
                if not passed:
                    failures += 1
                    print("Expected:\n" + reference.stdout + "Observed:\n" + detail, file=sys.stderr)
            if source.stem == "ClassFileValidation" and release == 25:
                failures += check_versions(java, runtimes, output)
            if source.stem == "DynamicConstants" and release == 25:
                failures += check_dynamic_formats(java, runtimes, output)
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
