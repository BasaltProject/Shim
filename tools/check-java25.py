#!/usr/bin/env python3
# Copyright (c) 2026 BasaltMC.
# Licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
# SPDX-License-Identifier: MIT

"""Compare programs compiled by JDK 25 against Shim and OpenJDK."""

import argparse
import os
from pathlib import Path
import shutil
import struct
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def run(command):
    return subprocess.run(command, capture_output=True, text=True, timeout=30)


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
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
