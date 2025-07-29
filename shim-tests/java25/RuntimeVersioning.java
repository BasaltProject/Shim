/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 11

import java.lang.Runtime.Version;
import java.util.List;
import java.util.Optional;

public class RuntimeVersioning {
    public static void main(String[] args) {
        testCurrentRuntimeVersion();
        testVersionParsingAndComponents();
        testVersionComparisons();
        testVersionEqualityAndHash();
        testVersionFormatting();
        testInvalidVersionStrings();
        System.out.println("ALL RUNTIME VERSION TESTS PASSED");
    }

    private static void testCurrentRuntimeVersion() {
        Version v = Runtime.version();
        System.out.println("feature >= 25: " + (v.feature() >= 25));
        System.out.println("major >= 25: " + (v.major() >= 25));
        System.out.println("interim >= 0: " + (v.interim() >= 0));
        System.out.println("update >= 0: " + (v.update() >= 0));
        System.out.println("patch >= 0: " + (v.patch() >= 0));
        List<Integer> nums = v.version();
        System.out.println("version numbers non-empty: " + !nums.isEmpty());
        System.out.println("version numbers first >= 25: " + (nums.get(0) >= 25));
    }

    private static void testVersionParsingAndComponents() {
        Version v1 = Version.parse("25");
        System.out.println("v1 feature: " + v1.feature());
        System.out.println("v1 interim: " + v1.interim());
        System.out.println("v1 update: " + v1.update());
        System.out.println("v1 patch: " + v1.patch());
        System.out.println("v1 pre: " + v1.pre());
        System.out.println("v1 build: " + v1.build());
        System.out.println("v1 opt: " + v1.optional());

        Version v2 = Version.parse("25.0.4.1+12-deb");
        System.out.println("v2 feature: " + v2.feature());
        System.out.println("v2 interim: " + v2.interim());
        System.out.println("v2 update: " + v2.update());
        System.out.println("v2 patch: " + v2.patch());
        System.out.println("v2 major: " + v2.major());
        System.out.println("v2 minor: " + v2.minor());
        System.out.println("v2 security: " + v2.security());
        System.out.println("v2 pre: " + v2.pre());
        System.out.println("v2 build: " + v2.build());
        System.out.println("v2 opt: " + v2.optional());
        System.out.println("v2 version list: " + v2.version());

        Version v3 = Version.parse("25-ea+5");
        System.out.println("v3 pre: " + v3.pre());
        System.out.println("v3 build: " + v3.build());
        System.out.println("v3 opt: " + v3.optional());

        Version v4 = Version.parse("25+-opt");
        System.out.println("v4 pre: " + v4.pre());
        System.out.println("v4 build: " + v4.build());
        System.out.println("v4 opt: " + v4.optional());

        Version v5 = Version.parse("25-ea-opt");
        System.out.println("v5 pre: " + v5.pre());
        System.out.println("v5 build: " + v5.build());
        System.out.println("v5 opt: " + v5.optional());
    }

    private static void testVersionComparisons() {
        Version v25 = Version.parse("25");
        Version v25_1 = Version.parse("25.1");
        Version v25_2 = Version.parse("25.2");
        Version v25_1_1 = Version.parse("25.1.1");
        Version v25_ea = Version.parse("25-ea");
        Version v25_beta = Version.parse("25-beta");
        Version v25_1_pre = Version.parse("25-1");
        Version v25_2_pre = Version.parse("25-2");
        Version v25_b1 = Version.parse("25+1");
        Version v25_b2 = Version.parse("25+2");
        Version v25_opt_a = Version.parse("25+1-a");
        Version v25_opt_b = Version.parse("25+1-b");

        System.out.println("25.1 vs 25.2: " + (v25_1.compareTo(v25_2) < 0));
        System.out.println("25.1 vs 25.1.1: " + (v25_1.compareTo(v25_1_1) < 0));
        System.out.println("25 vs 25-ea: " + (v25.compareTo(v25_ea) > 0));
        System.out.println("25-ea vs 25-beta: " + (v25_ea.compareTo(v25_beta) > 0));
        System.out.println("25-1 vs 25-2: " + (v25_1_pre.compareTo(v25_2_pre) < 0));
        System.out.println("25-1 vs 25-ea: " + (v25_1_pre.compareTo(v25_ea) < 0));
        System.out.println("25 vs 25+1: " + (v25.compareTo(v25_b1) < 0));
        System.out.println("25+1 vs 25+2: " + (v25_b1.compareTo(v25_b2) < 0));
        System.out.println("25+1-a vs 25+1-b: " + (v25_opt_a.compareTo(v25_opt_b) < 0));
        System.out.println("25+1-a vs 25+1-b ignoreOpt: " + (v25_opt_a.compareToIgnoreOptional(v25_opt_b) == 0));
    }

    private static void testVersionEqualityAndHash() {
        Version a1 = Version.parse("25.0.1+1-opt");
        Version a2 = Version.parse("25.0.1+1-opt");
        Version b = Version.parse("25.0.1+1-diff");
        Version c = Version.parse("25.0.1+2-opt");

        System.out.println("a1 equals a2: " + a1.equals(a2));
        System.out.println("a1 equals b: " + a1.equals(b));
        System.out.println("a1 equalsIgnoreOptional b: " + a1.equalsIgnoreOptional(b));
        System.out.println("a1 equals c: " + a1.equals(c));
        System.out.println("a1 equalsIgnoreOptional c: " + a1.equalsIgnoreOptional(c));
        System.out.println("hash equality: " + (a1.hashCode() == a2.hashCode()));
    }

    private static void testVersionFormatting() {
        String[] samples = {
            "25",
            "25.0.1",
            "25.0.4.1",
            "25-ea",
            "25+1",
            "25+1-opt",
            "25+-deb",
            "25-ea-opt",
            "25-ea+1-opt"
        };
        for (String s : samples) {
            Version v = Version.parse(s);
            System.out.println("format: " + s + " -> " + v.toString());
        }
    }

    private static void testInvalidVersionStrings() {
        String[] invalid = {
            "0",
            "01",
            "25.",
            ".25",
            "25..1",
            "25-",
            "25+",
            "25+01",
            "25-ea+1+2",
            "25.0"
        };
        for (String s : invalid) {
            try {
                Version.parse(s);
                throw new AssertionError("Expected error for: " + s);
            } catch (IllegalArgumentException expected) {
                System.out.println("rejected: " + s);
            }
        }
        try {
            Version.parse(null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("rejected: null");
        }
    }
}
