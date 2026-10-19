/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabularType extends OpenType<TabularData> {
  static final long serialVersionUID = 6554071860220659261L;

  private final CompositeType rowType;
  private final List<String> indexNames;
  private transient Integer myHashCode;
  private transient String myToString;

  public TabularType(String typeName, String description, CompositeType rowType, String[] indexNames)
      throws OpenDataException {
    super(TabularData.class.getName(), typeName, description);
    if (rowType == null) {
      throw new IllegalArgumentException("Argument rowType cannot be null.");
    }
    if (indexNames == null || indexNames.length == 0) {
      throw new IllegalArgumentException("Argument indexNames[] cannot be null or empty.");
    }
    for (int i = 0; i < indexNames.length; i++) {
      String name = indexNames[i];
      if (name == null) {
        throw new IllegalArgumentException("Argument's element indexNames[" + i + "] cannot be null.");
      }
      if (name.trim().equals("")) {
        throw new IllegalArgumentException("Argument's element indexNames[" + i + "] cannot be an empty string.");
      }
      if (!rowType.containsKey(name.trim())) {
        throw new OpenDataException("Argument's element value indexNames[" + i + "]=\"" + name
            + "\" is not a valid item name for rowType.");
      }
    }
    this.rowType = rowType;
    List<String> names = new ArrayList<String>(indexNames.length);
    for (String name : indexNames) {
      names.add(name.trim());
    }
    this.indexNames = Collections.unmodifiableList(names);
  }

  public CompositeType getRowType() {
    return rowType;
  }

  public List<String> getIndexNames() {
    return indexNames;
  }

  @Override
  public boolean isValue(Object obj) {
    if (!(obj instanceof TabularData)) {
      return false;
    }
    return getTabularTypeOf((TabularData) obj).equals(this);
  }

  private static TabularType getTabularTypeOf(TabularData data) {
    return data.getTabularType();
  }

  boolean isAssignableFrom(OpenType<?> ot) {
    return equals(ot);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TabularType)) {
      return false;
    }
    TabularType other = (TabularType) obj;
    return getTypeName().equals(other.getTypeName()) && rowType.equals(other.rowType)
        && indexNames.equals(other.indexNames);
  }

  @Override
  public int hashCode() {
    if (myHashCode == null) {
      int value = 0;
      value += getTypeName().hashCode();
      value += rowType.hashCode();
      for (String name : indexNames) {
        value += name.hashCode();
      }
      myHashCode = Integer.valueOf(value);
    }
    return myHashCode.intValue();
  }

  @Override
  public String toString() {
    if (myToString == null) {
      StringBuilder sb = new StringBuilder(getClass().getName());
      sb.append("(name=").append(getTypeName()).append(",rowType=").append(rowType).append(",indexNames=(");
      int i = 0;
      for (String name : indexNames) {
        sb.append(i++ > 0 ? "," : "").append(name);
      }
      sb.append("))");
      myToString = sb.toString();
    }
    return myToString;
  }
}
