/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import java.util.HashMap;
import java.util.Map;

public class TIFFTag {
  public static final int TIFF_BYTE = 1;
  public static final int TIFF_ASCII = 2;
  public static final int TIFF_SHORT = 3;
  public static final int TIFF_LONG = 4;
  public static final int TIFF_RATIONAL = 5;
  public static final int TIFF_SBYTE = 6;
  public static final int TIFF_UNDEFINED = 7;
  public static final int TIFF_SSHORT = 8;
  public static final int TIFF_SLONG = 9;
  public static final int TIFF_SRATIONAL = 10;
  public static final int TIFF_FLOAT = 11;
  public static final int TIFF_DOUBLE = 12;
  public static final int TIFF_IFD_POINTER = 13;
  public static final int MIN_DATATYPE = TIFF_BYTE;
  public static final int MAX_DATATYPE = TIFF_IFD_POINTER;
  public static final String UNKNOWN_TAG_NAME = "UnknownTag";

  private static final int[] kSizeOfType = {0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8, 4};

  private int number;
  private String name;
  private int dataTypes;
  private int count;
  private TIFFTagSet tagSet = null;
  private Map<Integer, String> valueNames = null;

  public TIFFTag(String name, int number, int dataTypes, int count) {
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    if (dataTypes < 0 || dataTypes > (1 << (MAX_DATATYPE + 1)) - 1) {
      throw new IllegalArgumentException("dataTypes out of range");
    }
    this.name = name;
    this.number = number;
    this.dataTypes = dataTypes;
    this.count = count;
  }

  public TIFFTag(String name, int number, int dataTypes) { this(name, number, dataTypes, -1); }

  public TIFFTag(String name, int number, TIFFTagSet tagSet) {
    this(name, number, 1 << TIFF_IFD_POINTER | 1 << TIFF_LONG, 1);
    if (tagSet == null) {
      throw new NullPointerException("tagSet == null");
    }
    this.tagSet = tagSet;
  }

  public static int getSizeOfType(int dataType) {
    if (dataType < MIN_DATATYPE || dataType > MAX_DATATYPE) {
      throw new IllegalArgumentException("dataType out of range!");
    }
    return kSizeOfType[dataType];
  }

  public String getName() { return name; }

  public int getNumber() { return number; }

  public int getDataTypes() { return dataTypes; }

  public int getCount() { return count; }

  public boolean isDataTypeOK(int dataType) {
    if (dataType < MIN_DATATYPE || dataType > MAX_DATATYPE) {
      throw new IllegalArgumentException("datatype not in range!");
    }
    return (dataTypes & (1 << dataType)) != 0;
  }

  public TIFFTagSet getTagSet() { return tagSet; }

  public boolean isIFDPointer() { return tagSet != null; }

  public boolean hasValueNames() { return valueNames != null; }

  protected void addValueName(int value, String name) {
    if (valueNames == null) {
      valueNames = new HashMap<Integer, String>();
    }
    valueNames.put(Integer.valueOf(value), name);
  }

  public String getValueName(int value) {
    if (valueNames == null) {
      return null;
    }
    return valueNames.get(Integer.valueOf(value));
  }

  public int[] getNamedValues() {
    int[] intValues = null;
    if (valueNames != null) {
      java.util.Set<Integer> values = valueNames.keySet();
      intValues = new int[values.size()];
      int i = 0;
      for (Integer value : values) {
        intValues[i++] = value.intValue();
      }
    }
    return intValues;
  }
}
