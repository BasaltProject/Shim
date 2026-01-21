/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

public class FeatureDescriptor {
  private boolean expert;
  private boolean hidden;
  private boolean preferred;
  private String shortDescription;
  private String name;
  private String displayName;
  private Hashtable<String, Object> table;

  public FeatureDescriptor() { }

  FeatureDescriptor(FeatureDescriptor x, FeatureDescriptor y) {
    expert = x.expert | y.expert;
    hidden = x.hidden | y.hidden;
    preferred = x.preferred | y.preferred;
    name = y.name;
    shortDescription = x.shortDescription;
    if (y.shortDescription != null) {
      shortDescription = y.shortDescription;
    }
    displayName = x.displayName;
    if (y.displayName != null) {
      displayName = y.displayName;
    }
    addTable(x.table);
    addTable(y.table);
  }

  FeatureDescriptor(FeatureDescriptor x) {
    expert = x.expert;
    hidden = x.hidden;
    preferred = x.preferred;
    name = x.name;
    shortDescription = x.shortDescription;
    displayName = x.displayName;
    addTable(x.table);
  }

  private void addTable(Hashtable<String, Object> source) {
    if (source != null && !source.isEmpty()) {
      getTable().putAll(source);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    if (displayName == null) {
      return getName();
    }
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isExpert() {
    return expert;
  }

  public void setExpert(boolean expert) {
    this.expert = expert;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public boolean isPreferred() {
    return preferred;
  }

  public void setPreferred(boolean preferred) {
    this.preferred = preferred;
  }

  public String getShortDescription() {
    if (shortDescription == null) {
      return getDisplayName();
    }
    return shortDescription;
  }

  public void setShortDescription(String text) {
    shortDescription = text;
  }

  public void setValue(String attributeName, Object value) {
    getTable().put(attributeName, value);
  }

  public Object getValue(String attributeName) {
    return table == null ? null : table.get(attributeName);
  }

  public Enumeration<String> attributeNames() {
    return getTable().keys();
  }

  private Hashtable<String, Object> getTable() {
    if (table == null) {
      table = new Hashtable<String, Object>();
    }
    return table;
  }

  boolean hasValues() {
    return table != null && !table.isEmpty();
  }

  void copyValuesFrom(FeatureDescriptor other) {
    addTable(other.table);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[name=").append(name);
    appendTo(sb, "displayName", displayName);
    appendTo(sb, "shortDescription", shortDescription);
    appendTo(sb, "preferred", preferred);
    appendTo(sb, "hidden", hidden);
    appendTo(sb, "expert", expert);
    if (table != null && !table.isEmpty()) {
      sb.append("; values={");
      boolean first = true;
      for (Map.Entry<String, Object> e : table.entrySet()) {
        if (!first) {
          sb.append("; ");
        }
        first = false;
        sb.append(e.getKey()).append('=').append(e.getValue());
      }
      sb.append('}');
    }
    appendTo(sb);
    return sb.append(']').toString();
  }

  void appendTo(StringBuilder sb) { }

  static void appendTo(StringBuilder sb, String name, Object value) {
    if (value != null) {
      sb.append("; ").append(name).append('=').append(value);
    }
  }

  static void appendTo(StringBuilder sb, String name, boolean value) {
    if (value) {
      sb.append("; ").append(name);
    }
  }
}
