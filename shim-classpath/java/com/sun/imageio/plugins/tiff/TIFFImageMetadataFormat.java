/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.imageio.plugins.tiff;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class TIFFImageMetadataFormat extends IIOMetadataFormatImpl {
  private static TIFFImageMetadataFormat theInstance;

  public static synchronized IIOMetadataFormat getInstance() {
    if (theInstance == null) {
      theInstance = new TIFFImageMetadataFormat();
    }
    return theInstance;
  }

  private TIFFImageMetadataFormat() {
    super(TIFFImageMetadata.kNativeMetadataFormatName, CHILD_POLICY_SEQUENCE);
    addElement("TIFFIFD", TIFFImageMetadata.kNativeMetadataFormatName, 0, Integer.MAX_VALUE);
    addAttribute("TIFFIFD", "tagSets", DATATYPE_STRING, true, null);
    addAttribute("TIFFIFD", "parentTagNumber", DATATYPE_INTEGER, false, null, "0", "65535", true, true);
    addAttribute("TIFFIFD", "parentTagName", DATATYPE_STRING, false, null);
    addChildElement("TIFFIFD", "TIFFIFD");
    addElement("TIFFField", "TIFFIFD", CHILD_POLICY_CHOICE);
    addAttribute("TIFFField", "number", DATATYPE_INTEGER, true, null, "0", "65535", true, true);
    addAttribute("TIFFField", "name", DATATYPE_STRING, false, null);
    String[] types = {"Byte", "Ascii", "Short", "Long", "Rational", "SByte", "SShort", "SLong", "SRational", "Float",
        "Double"};
    for (String t : types) {
      addElement("TIFF" + t + "s", "TIFFField", 0, Integer.MAX_VALUE);
      addElement("TIFF" + t, "TIFF" + t + "s", CHILD_POLICY_EMPTY);
      addAttribute("TIFF" + t, "value", DATATYPE_STRING, true, null);
      addAttribute("TIFF" + t, "description", DATATYPE_STRING, false, null);
    }
    addElement("TIFFUndefined", "TIFFField", CHILD_POLICY_EMPTY);
    addAttribute("TIFFUndefined", "value", DATATYPE_STRING, true, null);
  }

  public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return false; }
}
