/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class TIFFTagSet {
  private final Map<Integer, TIFFTag> allowedTagsByNumber = new HashMap<Integer, TIFFTag>();
  private final Map<String, TIFFTag> allowedTagsByName = new HashMap<String, TIFFTag>();
  private SortedSet<Integer> tagNumbers;
  private SortedSet<String> tagNames;

  private TIFFTagSet() { }

  public TIFFTagSet(List<TIFFTag> tags) {
    if (tags == null) {
      throw new NullPointerException("tags == null!");
    }
    for (TIFFTag tag : tags) {
      allowedTagsByNumber.put(Integer.valueOf(tag.getNumber()), tag);
      allowedTagsByName.put(tag.getName(), tag);
    }
  }

  public TIFFTag getTag(int tagNumber) { return allowedTagsByNumber.get(Integer.valueOf(tagNumber)); }

  public TIFFTag getTag(String tagName) {
    if (tagName == null) {
      throw new NullPointerException("tagName == null!");
    }
    return allowedTagsByName.get(tagName);
  }

  public SortedSet<Integer> getTagNumbers() {
    if (tagNumbers == null) {
      tagNumbers = java.util.Collections.unmodifiableSortedSet(new TreeSet<Integer>(allowedTagsByNumber.keySet()));
    }
    return tagNumbers;
  }

  public SortedSet<String> getTagNames() {
    if (tagNames == null) {
      tagNames = java.util.Collections.unmodifiableSortedSet(new TreeSet<String>(allowedTagsByName.keySet()));
    }
    return tagNames;
  }
}
