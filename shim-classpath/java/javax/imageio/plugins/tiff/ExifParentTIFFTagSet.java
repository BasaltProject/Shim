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

public final class ExifParentTIFFTagSet extends TIFFTagSet {
  public static final int TAG_EXIF_IFD_POINTER = 34665;
  public static final int TAG_GPS_INFO_IFD_POINTER = 34853;

  private static ExifParentTIFFTagSet theInstance;

  private ExifParentTIFFTagSet() {
    super(createTags());
  }

  public static synchronized ExifParentTIFFTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new ExifParentTIFFTagSet();
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
        pointer(34665, "ExifIFDPointer", ExifTIFFTagSet.getInstance()),
        pointer(34853, "GPSInfoIFDPointer", ExifGPSTagSet.getInstance()),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
