/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import java.util.ArrayList;
import java.util.List;

public final class ExifInteroperabilityTagSet extends TIFFTagSet {
  public static final int TAG_INTEROPERABILITY_INDEX = 1;
  public static final java.lang.String INTEROPERABILITY_INDEX_R98 = "R98";
  public static final java.lang.String INTEROPERABILITY_INDEX_THM = "THM";

  private static ExifInteroperabilityTagSet theInstance;

  private ExifInteroperabilityTagSet() {
    super(createTags());
  }

  public static synchronized ExifInteroperabilityTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new ExifInteroperabilityTagSet();
    }
    return theInstance;
  }

  private static TIFFTag tag(int number, String name, int types, int count, String... valueNames) {
    final String[] vn = valueNames;
    TIFFTag t = new TIFFTag(name, number, types, count) {
      {
        for (String v : vn) {
          int eq = v.indexOf('=');
          addValueName(Integer.parseInt(v.substring(0, eq)), v.substring(eq + 1));
        }
      }
    };
    return t;
  }

  private static TIFFTag pointer(int number, String name, TIFFTagSet child) {
    return new TIFFTag(name, number, child);
  }

  private static List<TIFFTag> createTags() {
    List<TIFFTag> tags = new ArrayList<TIFFTag>();
    for (TIFFTag t : new TIFFTag[] {
        tag(1, "InteroperabilityIndex", 4, -1),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
