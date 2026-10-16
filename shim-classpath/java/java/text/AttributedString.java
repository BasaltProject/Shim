/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributedString {
  private final String text;
  private final List<Map<AttributedCharacterIterator.Attribute, Object>> perChar;

  AttributedString(String text, List<Map<AttributedCharacterIterator.Attribute, Object>> perChar) {
    this.text = text;
    this.perChar = perChar;
  }

  public AttributedString(AttributedCharacterIterator text) {
    this(text, text.getBeginIndex(), text.getEndIndex(), null);
  }

  public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex) {
    this(text, beginIndex, endIndex, null);
  }

  public AttributedString(AttributedCharacterIterator text, int beginIndex, int endIndex,
      AttributedCharacterIterator.Attribute[] attributes) {
    if (text == null) {
      throw new NullPointerException();
    }
    int textBeginIndex = text.getBeginIndex();
    int textEndIndex = text.getEndIndex();
    if (beginIndex < textBeginIndex || endIndex > textEndIndex || beginIndex > endIndex) {
      throw new IllegalArgumentException("Invalid substring range");
    }
    StringBuilder sb = new StringBuilder();
    text.setIndex(beginIndex);
    while (text.getIndex() < endIndex) {
      sb.append(text.current());
      text.next();
    }
    this.text = sb.toString();
    this.perChar = new ArrayList<Map<AttributedCharacterIterator.Attribute, Object>>();
    Set<AttributedCharacterIterator.Attribute> wanted = null;
    if (attributes != null) {
      wanted = new HashSet<AttributedCharacterIterator.Attribute>();
      for (AttributedCharacterIterator.Attribute a : attributes) {
        wanted.add(a);
      }
    } else {
      wanted = text.getAllAttributeKeys();
    }
    for (int i = beginIndex; i < endIndex; i++) {
      text.setIndex(i);
      Map<AttributedCharacterIterator.Attribute, Object> map =
          new HashMap<AttributedCharacterIterator.Attribute, Object>();
      for (Map.Entry<AttributedCharacterIterator.Attribute, Object> e : text.getAttributes().entrySet()) {
        if (wanted.contains(e.getKey())) {
          map.put(e.getKey(), e.getValue());
        }
      }
      perChar.add(map);
    }
  }

  public AttributedString(String text) {
    if (text == null) {
      throw new NullPointerException();
    }
    this.text = text;
    this.perChar = new ArrayList<Map<AttributedCharacterIterator.Attribute, Object>>();
    for (int i = 0; i < text.length(); i++) {
      perChar.add(new HashMap<AttributedCharacterIterator.Attribute, Object>());
    }
  }

  public AttributedString(String text, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
    this(text);
    if (attributes == null) {
      throw new NullPointerException();
    }
    if (text.isEmpty()) {
      if (!attributes.isEmpty()) {
        throw new IllegalArgumentException("Can't add attribute to 0-length text");
      }
      return;
    }
    addAttributes(attributes, 0, text.length());
  }

  public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value) {
    if (attribute == null) {
      throw new NullPointerException();
    }
    int len = text.length();
    if (len == 0) {
      throw new IllegalArgumentException("Can't add attribute to 0-length text");
    }
    addAttribute(attribute, value, 0, len);
  }

  public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex,
      int endIndex) {
    if (attribute == null) {
      throw new NullPointerException();
    }
    if (beginIndex < 0 || endIndex > text.length() || beginIndex >= endIndex) {
      throw new IllegalArgumentException("Invalid substring range");
    }
    for (int i = beginIndex; i < endIndex; i++) {
      perChar.get(i).put(attribute, value);
    }
  }

  public void addAttributes(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes, int beginIndex,
      int endIndex) {
    if (attributes == null) {
      throw new NullPointerException();
    }
    if (beginIndex < 0 || endIndex > text.length() || beginIndex > endIndex) {
      throw new IllegalArgumentException("Invalid substring range");
    }
    if (beginIndex == endIndex) {
      if (attributes.isEmpty()) {
        return;
      }
      throw new IllegalArgumentException("Can't add attribute to 0-length text");
    }
    for (Map.Entry<? extends AttributedCharacterIterator.Attribute, ?> e : attributes.entrySet()) {
      addAttribute(e.getKey(), e.getValue(), beginIndex, endIndex);
    }
  }

  public AttributedCharacterIterator getIterator() {
    return getIterator(null, 0, text.length());
  }

  public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes) {
    return getIterator(attributes, 0, text.length());
  }

  public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex,
      int endIndex) {
    return new Iterator0(this, attributes, beginIndex, endIndex);
  }

  private static final class Iterator0 implements AttributedCharacterIterator {
    private final AttributedString owner;
    private final Set<Attribute> filter;
    private final int begin;
    private final int end;
    private int index;

    Iterator0(AttributedString owner, Attribute[] attributes, int begin, int end) {
      if (begin < 0 || begin > end || end > owner.text.length()) {
        throw new IllegalArgumentException("Invalid substring range");
      }
      this.owner = owner;
      this.begin = begin;
      this.end = end;
      this.index = begin;
      if (attributes == null) {
        filter = null;
      } else {
        filter = new HashSet<Attribute>();
        for (Attribute a : attributes) {
          filter.add(a);
        }
      }
    }

    private Map<Attribute, Object> at(int i) {
      Map<Attribute, Object> result = new HashMap<Attribute, Object>();
      result.putAll(owner.perChar.get(i));
      return result;
    }

    private static boolean same(Object a, Object b) {
      return a == null ? b == null : a.equals(b);
    }

    private boolean equalOn(int i, int j, Set<? extends Attribute> keys) {
      if (keys == null) {
        return owner.perChar.get(i).equals(owner.perChar.get(j));
      }
      Map<Attribute, Object> a = at(i);
      Map<Attribute, Object> b = at(j);
      for (Attribute k : keys) {
        if (!same(a.get(k), b.get(k))) {
          return false;
        }
      }
      return true;
    }

    private int runStart(Set<? extends Attribute> keys) {
      if (begin == end) {
        return begin;
      }
      int i = index;
      if (i >= end) {
        i = end - 1;
      }
      while (i > begin && equalOn(i - 1, index >= end ? end - 1 : index, keys)) {
        i--;
      }
      return i;
    }

    private int runLimit(Set<? extends Attribute> keys) {
      if (begin == end) {
        return end;
      }
      if (index >= end) {
        return end;
      }
      int i = index;
      while (i < end - 1 && equalOn(i + 1, index, keys)) {
        i++;
      }
      return i + 1;
    }

    private Set<Attribute> single(Attribute a) {
      Set<Attribute> s = new HashSet<Attribute>();
      s.add(a);
      return s;
    }

    public int getRunStart() {
      return runStart(null);
    }

    public int getRunStart(Attribute attribute) {
      return runStart(single(attribute));
    }

    public int getRunStart(Set<? extends Attribute> attributes) {
      return runStart(attributes);
    }

    public int getRunLimit() {
      return runLimit(null);
    }

    public int getRunLimit(Attribute attribute) {
      return runLimit(single(attribute));
    }

    public int getRunLimit(Set<? extends Attribute> attributes) {
      return runLimit(attributes);
    }

    public Map<Attribute, Object> getAttributes() {
      if (begin == end || index >= end) {
        return new HashMap<Attribute, Object>();
      }
      return at(index);
    }

    public Object getAttribute(Attribute attribute) {
      if (begin == end || index >= end) {
        return null;
      }
      return at(index).get(attribute);
    }

    public Set<Attribute> getAllAttributeKeys() {
      Set<Attribute> keys = new HashSet<Attribute>();
      for (int i = begin; i < end; i++) {
        for (Attribute key : owner.perChar.get(i).keySet()) {
          if (filter == null || filter.contains(key)) {
            keys.add(key);
          }
        }
      }
      return keys;
    }

    public char first() {
      index = begin;
      return current();
    }

    public char last() {
      index = end > begin ? end - 1 : end;
      return current();
    }

    public char current() {
      if (index >= begin && index < end) {
        return owner.text.charAt(index);
      }
      return DONE;
    }

    public char next() {
      if (index < end - 1) {
        index++;
        return owner.text.charAt(index);
      }
      index = end;
      return DONE;
    }

    public char previous() {
      if (index > begin) {
        index--;
        return owner.text.charAt(index);
      }
      return DONE;
    }

    public char setIndex(int position) {
      if (position < begin || position > end) {
        throw new IllegalArgumentException("Invalid index");
      }
      index = position;
      return current();
    }

    public int getBeginIndex() {
      return begin;
    }

    public int getEndIndex() {
      return end;
    }

    public int getIndex() {
      return index;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError(e);
      }
    }
  }
}
