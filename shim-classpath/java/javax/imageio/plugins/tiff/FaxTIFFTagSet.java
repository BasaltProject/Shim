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

public final class FaxTIFFTagSet extends TIFFTagSet {
  public static final int TAG_BAD_FAX_LINES = 326;
  public static final int TAG_CLEAN_FAX_DATA = 327;
  public static final int CLEAN_FAX_DATA_NO_ERRORS = 0;
  public static final int CLEAN_FAX_DATA_ERRORS_CORRECTED = 1;
  public static final int CLEAN_FAX_DATA_ERRORS_UNCORRECTED = 2;
  public static final int TAG_CONSECUTIVE_BAD_LINES = 328;

  private static FaxTIFFTagSet theInstance;

  private FaxTIFFTagSet() {
    super(createTags());
  }

  public static synchronized FaxTIFFTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new FaxTIFFTagSet();
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
        tag(326, "BadFaxLines", 24, 1),
        tag(327, "CleanFaxData", 8, 1, "0=No errors", "1=Errors corrected", "2=Errors uncorrected"),
        tag(328, "ConsecutiveBadFaxLines", 24, 1),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
