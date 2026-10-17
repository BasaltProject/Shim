/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
public class DateParseModern {
  @SuppressWarnings("deprecation")
  public static void main(String[] a) {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    String[] in = {"Sat, 12 Aug 1995 13:30:00 GMT", "12 Aug 1995 13:30:00 GMT+0430", "12 Aug 1995 13:30:00 GMT-0430", "Mon, 3 Jan 96 10:00 PST", "Tue, 15 Nov 1994 08:12:31 GMT", "8/12/1995", "8/12/95 1:30 PM", "12 August 1995 12:00 AM", "12 Aug 1995 12:00 PM UTC", "Aug 12, 1995", "1 Jan 2000 00:00:00 EST", "1 Jan 2000 00:00:00 EDT", "1 Jan 2000 00:00:00 CST", "1 Jan 2000 00:00:00 MDT", "1 Jan 2000 00:00:00 PDT", "1 Jan 2000 00:00:00 GMT+3", "1 Jan 2000 00:00:00 +0100", "1 Jan 2000 (a comment (nested)) 10:00 GMT", "  1   Jan   2000   ,  ", "Thu, 01 Jan 1970 00:00:00 GMT", "31 Dec 1969 23:59:59 GMT", "1 Jan 1 00:00:00 GMT", "29 Feb 2000 10:00 GMT", "30 Feb 2001 10:00 GMT", "13/40/2000", "garbage", "", "12 Aug", "Aug 1995", "1 Foo 2000", "1 Jan 2000 13:00 PM", "1 Jan 2000 0:00 AM", "1 Jan 2000 10:00:00 GMT+9999", "1 Jan 2000 GMT GMT", "1 Jan 2000 10:20:30 40", "Sat, 12 Aug 1995 13:30:00", "Sat 12 Aug 1995 13:30:00 gmt", "1/2/3", "70/1/1", "1 Jan 69", "1 Jan 70", "1 Jan 99"};
    for (String s : in) {
      try { System.out.println("[" + s + "] " + Date.parse(s)); } catch (Throwable t) { System.out.println("[" + s + "] !" + t.getClass().getName() + ":" + t.getMessage()); }
    }
    try { Date.parse(null); } catch (Throwable t) { System.out.println("null !" + t.getClass().getName()); }
    System.out.println(new Date("Sat, 12 Aug 1995 13:30:00 GMT").getTime());
    try { new Date("nope"); } catch (Throwable t) { System.out.println("ctor !" + t.getClass().getName()); }
  }
}
