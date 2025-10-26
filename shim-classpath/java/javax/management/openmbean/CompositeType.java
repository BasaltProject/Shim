/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class CompositeType extends OpenType<CompositeData> {
  private static final long serialVersionUID = -5366242454346948798L;

  private final TreeMap<String, String> nameToDescription;
  private final TreeMap<String, OpenType<?>> nameToType;
  private transient Integer myHashCode;
  private transient String myToString;
  private transient Set<String> myNamesSet;

  public CompositeType(String typeName, String description, String[] itemNames, String[] itemDescriptions,
      OpenType<?>[] itemTypes) throws OpenDataException {
    super(CompositeData.class.getName(), typeName, description);
    if (itemNames == null || itemDescriptions == null || itemTypes == null) {
      throw new IllegalArgumentException("Argument itemNames, itemDescriptions or itemTypes cannot be null or empty");
    }
    if (itemNames.length == 0) {
      throw new IllegalArgumentException("Argument itemNames, itemDescriptions or itemTypes cannot be null or empty");
    }
    if (itemDescriptions.length != itemNames.length || itemTypes.length != itemNames.length) {
      throw new IllegalArgumentException("Array arguments itemNames[], itemDescriptions[] and itemTypes[] "
          + "should be of same length (got " + itemNames.length + ", " + itemDescriptions.length + " and "
          + itemTypes.length + ").");
    }
    nameToDescription = new TreeMap<String, String>();
    nameToType = new TreeMap<String, OpenType<?>>();
    for (int i = 0; i < itemNames.length; i++) {
      String name = itemNames[i];
      if (name == null || name.trim().equals("")) {
        throw new IllegalArgumentException("Argument's element itemNames[" + i + "] cannot be null or empty");
      }
      String desc = itemDescriptions[i];
      if (desc == null || desc.trim().equals("")) {
        throw new IllegalArgumentException("Argument's element itemDescriptions[" + i
            + "] cannot be null or empty");
      }
      if (itemTypes[i] == null) {
        throw new IllegalArgumentException("Argument's element itemTypes[" + i + "] cannot be null");
      }
      String key = name.trim();
      if (nameToDescription.containsKey(key)) {
        throw new OpenDataException("Argument's element itemNames[" + i + "]=\"" + itemNames[i]
            + "\" duplicates a previous item names.");
      }
      nameToDescription.put(key, desc.trim());
      nameToType.put(key, itemTypes[i]);
    }
  }

  public boolean containsKey(String itemName) {
    if (itemName == null) {
      return false;
    }
    return nameToDescription.containsKey(itemName);
  }

  public String getDescription(String itemName) {
    if (itemName == null) {
      return null;
    }
    return nameToDescription.get(itemName);
  }

  public OpenType<?> getType(String itemName) {
    if (itemName == null) {
      return null;
    }
    return nameToType.get(itemName);
  }

  public Set<String> keySet() {
    if (myNamesSet == null) {
      myNamesSet = Collections.unmodifiableSet(nameToDescription.keySet());
    }
    return myNamesSet;
  }

  @Override
  public boolean isValue(Object obj) {
    if (!(obj instanceof CompositeData)) {
      return false;
    }
    CompositeData value = (CompositeData) obj;
    CompositeType valueType = value.getCompositeType();
    return this.isAssignableFrom(valueType);
  }

  boolean isAssignableFrom(OpenType<?> ot) {
    if (!(ot instanceof CompositeType)) {
      return false;
    }
    CompositeType ct = (CompositeType) ot;
    if (!ct.getTypeName().equals(getTypeName())) {
      return false;
    }
    if (!ct.keySet().containsAll(keySet())) {
      return false;
    }
    for (String key : keySet()) {
      OpenType<?> otype = ct.getType(key);
      OpenType<?> mine = getType(key);
      if (mine instanceof CompositeType) {
        if (!((CompositeType) mine).isAssignableFrom(otype)) {
          return false;
        }
      } else if (!mine.equals(otype)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CompositeType)) {
      return false;
    }
    CompositeType other = (CompositeType) obj;
    if (!this.getTypeName().equals(other.getTypeName())) {
      return false;
    }
    return this.nameToType.equals(other.nameToType);
  }

  @Override
  public int hashCode() {
    if (myHashCode == null) {
      int value = 0;
      value += this.getTypeName().hashCode();
      for (String key : nameToDescription.keySet()) {
        value += key.hashCode();
        value += this.nameToType.get(key).hashCode();
      }
      myHashCode = Integer.valueOf(value);
    }
    return myHashCode.intValue();
  }

  @Override
  public String toString() {
    if (myToString == null) {
      StringBuilder result = new StringBuilder();
      result.append(this.getClass().getName());
      result.append("(name=");
      result.append(getTypeName());
      result.append(",items=(");
      int i = 0;
      Iterator<String> k = nameToType.keySet().iterator();
      while (k.hasNext()) {
        String key = k.next();
        if (i > 0) {
          result.append("),");
        }
        result.append("(itemName=");
        result.append(key);
        result.append(",itemType=");
        result.append(nameToType.get(key).toString());
        i++;
      }
      result.append("))");
      myToString = result.toString();
    }
    return myToString;
  }
}
