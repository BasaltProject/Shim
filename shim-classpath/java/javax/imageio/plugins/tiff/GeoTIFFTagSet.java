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

public final class GeoTIFFTagSet extends TIFFTagSet {
  public static final int TAG_MODEL_PIXEL_SCALE = 33550;
  public static final int TAG_MODEL_TRANSFORMATION = 34264;
  public static final int TAG_MODEL_TIE_POINT = 33922;
  public static final int TAG_GEO_KEY_DIRECTORY = 34735;
  public static final int TAG_GEO_DOUBLE_PARAMS = 34736;
  public static final int TAG_GEO_ASCII_PARAMS = 34737;

  private static GeoTIFFTagSet theInstance;

  private GeoTIFFTagSet() {
    super(createTags());
  }

  public static synchronized GeoTIFFTagSet getInstance() {
    if (theInstance == null) {
      theInstance = new GeoTIFFTagSet();
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
        tag(33550, "ModelPixelScaleTag", 4096, -1),
        tag(33922, "ModelTiepointTag", 4096, -1),
        tag(34264, "ModelTransformationTag", 4096, -1),
        tag(34735, "GeoKeyDirectoryTag", 8, -1),
        tag(34736, "GeoDoubleParamsTag", 4096, -1),
        tag(34737, "GeoAsciiParamsTag", 4, -1),
    }) {
      tags.add(t);
    }
    return tags;
  }
}
