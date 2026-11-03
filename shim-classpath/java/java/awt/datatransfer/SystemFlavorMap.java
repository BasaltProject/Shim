/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SystemFlavorMap implements FlavorMap, FlavorTable {
  private static final SystemFlavorMap instance = new SystemFlavorMap();
  private final Map<String, List<DataFlavor>> nativeToFlavor = new HashMap<>();
  private final Map<DataFlavor, List<String>> flavorToNative = new HashMap<>();
  private static final String JAVA_DATAFLAVOR_PREFIX = "JAVA_DATAFLAVOR:";

  SystemFlavorMap() {
    addUnencodedNativeForFlavor(DataFlavor.stringFlavor, "UTF8_STRING");
    addUnencodedNativeForFlavor(DataFlavor.stringFlavor, "STRING");
    addUnencodedNativeForFlavor(DataFlavor.plainTextFlavor, "TEXT");
    addUnencodedNativeForFlavor(DataFlavor.javaFileListFlavor, "text/uri-list");
    addUnencodedNativeForFlavor(DataFlavor.imageFlavor, "image/png");
  }

  public static FlavorMap getDefaultFlavorMap() {
    return instance;
  }

  public synchronized List<String> getNativesForFlavor(DataFlavor flav) {
    List<String> natives = flavorToNative.get(flav);
    if (natives == null) {
      natives = new ArrayList<>();
      natives.add(encodeDataFlavor(flav));
    }
    return new ArrayList<>(natives);
  }

  public synchronized List<DataFlavor> getFlavorsForNative(String nat) {
    List<DataFlavor> flavors = nativeToFlavor.get(nat);
    if (flavors == null) {
      flavors = new ArrayList<>();
      if (isJavaMIMEType(nat)) {
        try {
          flavors.add(decodeDataFlavor(nat));
        } catch (ClassNotFoundException e) {
          // ignore
        }
      }
    }
    return new ArrayList<>(flavors);
  }

  public synchronized Map<DataFlavor, String> getNativesForFlavors(DataFlavor[] flavors) {
    Map<DataFlavor, String> retval = new LinkedHashMap<>();
    if (flavors == null) {
      for (DataFlavor f : flavorToNative.keySet()) {
        retval.put(f, getNativesForFlavor(f).get(0));
      }
    } else {
      for (DataFlavor f : flavors) {
        retval.put(f, getNativesForFlavor(f).get(0));
      }
    }
    return retval;
  }

  public synchronized Map<String, DataFlavor> getFlavorsForNatives(String[] natives) {
    Map<String, DataFlavor> retval = new LinkedHashMap<>();
    if (natives == null) {
      for (String n : nativeToFlavor.keySet()) {
        retval.put(n, getFlavorsForNative(n).get(0));
      }
    } else {
      for (String n : natives) {
        List<DataFlavor> l = getFlavorsForNative(n);
        if (!l.isEmpty()) {
          retval.put(n, l.get(0));
        }
      }
    }
    return retval;
  }

  public synchronized void addUnencodedNativeForFlavor(DataFlavor flav, String nat) {
    flavorToNative.computeIfAbsent(flav, k -> new ArrayList<>()).add(nat);
    nativeToFlavor.computeIfAbsent(nat, k -> new ArrayList<>()).add(flav);
  }

  public synchronized void setNativesForFlavor(DataFlavor flav, String[] natives) {
    flavorToNative.put(flav, new ArrayList<>(java.util.Arrays.asList(natives)));
  }

  public synchronized void addFlavorForUnencodedNative(String nat, DataFlavor flav) {
    addUnencodedNativeForFlavor(flav, nat);
  }

  public synchronized void setFlavorsForNative(String nat, DataFlavor[] flavors) {
    nativeToFlavor.put(nat, new ArrayList<>(java.util.Arrays.asList(flavors)));
  }

  public static String encodeJavaMIMEType(String mimeType) {
    return (mimeType != null) ? JAVA_DATAFLAVOR_PREFIX + mimeType : null;
  }

  public static String encodeDataFlavor(DataFlavor flav) {
    return (flav != null) ? encodeJavaMIMEType(flav.getMimeType()) : null;
  }

  public static boolean isJavaMIMEType(String str) {
    return (str != null && str.startsWith(JAVA_DATAFLAVOR_PREFIX));
  }

  public static String decodeJavaMIMEType(String nat) {
    return isJavaMIMEType(nat) ? nat.substring(JAVA_DATAFLAVOR_PREFIX.length()).trim() : null;
  }

  public static DataFlavor decodeDataFlavor(String nat) throws ClassNotFoundException {
    String retval_str = decodeJavaMIMEType(nat);
    return (retval_str != null) ? new DataFlavor(retval_str) : null;
  }
}
