/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Currency;
import java.util.IdentityHashMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class UtilEdgeModern {
  static void t(String name, Callable<Object> op) {
    try {
      System.out.println(name + " = " + op.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static Object npe(Callable<Object> op) throws Exception {
    try {
      return op.call();
    } catch (NullPointerException e) {
      return "NullPointerException";
    }
  }

  public static void main(String[] args) {
    for (String code : new String[] {"USD", "usd", "ABC", "1US", "US", "$$$", "US1", "xxx"}) {
      t("currency-" + code, () -> Currency.getInstance(code));
    }
    t("currency-xk", () -> Currency.getInstance(Locale.of("", "XK")));
    t("hex-empty", () -> HexFormat.fromHexDigits(""));
    t("hex-long-empty", () -> HexFormat.fromHexDigitsToLong("", 0, 0));
    t("hex-nine", () -> HexFormat.fromHexDigits("123456789"));
    t("hex-seventeen", () -> HexFormat.fromHexDigitsToLong("12345678901234567"));
    t("mime-separator", () -> Base64.getMimeEncoder(0, new byte[] {'a'}));
    t("locale-dotted-i", () -> Locale.of("İstanbul"));
    t("number-null-locale", () -> npe(() -> NumberFormat.getInstance((Locale) null)));
    t("percent-null-locale", () -> npe(() -> NumberFormat.getPercentInstance((Locale) null)));
    t("date-style-order", () -> DateFormat.getDateTimeInstance(-1, 16, Locale.ROOT));
    t("zone-ids-mutable", () -> java.time.ZoneId.getAvailableZoneIds().remove("UTC"));
    t("stream-range", () -> Arrays.stream(new int[3], 7, 10));
    t("stream-order", () -> Arrays.stream(new int[3], 100, 0));
    t("sort-order", () -> {
      StringBuilder log = new StringBuilder();
      Integer[] values = {7, 3, 9, 3, 1, 8, 2, 5, 5, 0};
      Arrays.sort(values, (a, b) -> {
        log.append(a).append(':').append(b).append(' ');
        return a - b;
      });
      return log + Arrays.toString(values);
    });
    t("sort-mixed", () -> {
      Arrays.sort(new Object[] {"b", 1});
      return "sorted";
    });
    t("box-cache", () -> {
      Object a = 1;
      Object b = 1;
      Integer c = 128;
      Integer d = 128;
      Character e = 'a';
      Character f = 'a';
      return (a == b) + " " + (c == d) + " " + (Long.valueOf(-128) == Long.valueOf(-128)) + " "
          + (Short.valueOf((short) 5) == Short.valueOf((short) 5)) + " "
          + (Byte.valueOf((byte) -1) == Byte.valueOf((byte) -1)) + " " + (e == f) + " "
          + (Character.valueOf('\u00e9') == Character.valueOf('\u00e9'));
    });
    t("identity-map", () -> {
      IdentityHashMap<Integer, String> map = new IdentityHashMap<>();
      map.put(1, "a");
      return map.get(1) + " " + map.entrySet() + " " + map.clone() + " " + map.containsKey(1);
    });
    t("weak-map-entries", () -> {
      WeakHashMap<Integer, String> map = new WeakHashMap<>();
      map.put(7, "x");
      return map.entrySet() + " " + map.entrySet().iterator().next().hashCode() + " " + map.keySet();
    });
    t("tree-submap-type", () -> {
      TreeMap<Object, String> map = new TreeMap<>();
      map.put("k", "v");
      return map.headMap(new int[0], true);
    });
    t("skiplist-cast", () -> {
      ConcurrentSkipListMap<Object, String> map = new ConcurrentSkipListMap<>();
      map.put(1, "a");
      return map.ceilingEntry("s");
    });
    t("skiplist-null-remove", () -> new ConcurrentSkipListMap<Integer, String>().remove(null));
    t("skiplist-null-value-remove", () -> new ConcurrentSkipListMap<Integer, String>().remove(1, null));
    t("skipset-null-remove", () -> new ConcurrentSkipListSet<Integer>().remove(null));
    t("deque-null-remove", () -> new ConcurrentLinkedDeque<Integer>().remove(null));
    t("cow-iterator-index", () -> new CopyOnWriteArrayList<>(java.util.List.of(1, 2)).listIterator(5));
    t("spliterator-negative", () -> Arrays.spliterator(new long[3], -1, 2));
  }
}
