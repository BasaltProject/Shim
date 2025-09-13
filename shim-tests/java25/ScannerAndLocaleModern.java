/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ScannerAndLocaleModern {
    public static void main(String[] args) {
        Scanner s = new Scanner("10 20 30 hello 40.5 true").useLocale(Locale.US);
        System.out.println(s.nextInt() + s.nextInt() + s.nextInt());
        System.out.println(s.hasNextInt());
        System.out.println(s.next());
        System.out.println(s.nextDouble());
        System.out.println(s.nextBoolean());
        System.out.println(s.hasNext());

        Scanner lines = new Scanner("first line\nsecond line\n");
        System.out.println(lines.nextLine());
        System.out.println(lines.nextLine());
        System.out.println(lines.hasNextLine());

        System.out.println(new Scanner("one,two,three").useDelimiter(",").tokens().toList());
        new Scanner("abc123def456").findAll(Pattern.compile("[0-9]+")).forEach(m -> System.out.println(m.group()));

        try {
            new Scanner("x").nextInt();
        } catch (java.util.InputMismatchException e) {
            System.out.println("mismatch");
        }

        Locale a = Locale.of("EN", "us");
        System.out.println(a + " " + a.getLanguage() + " " + a.getCountry());
        System.out.println(a.equals(Locale.US) + " " + (a.hashCode() == Locale.US.hashCode()));
        System.out.println(a.toLanguageTag());
        System.out.println(Locale.forLanguageTag("es-ES"));
        try {
            Locale.of(null);
        } catch (NullPointerException e) {
            System.out.println("npe");
        }
    }
}
