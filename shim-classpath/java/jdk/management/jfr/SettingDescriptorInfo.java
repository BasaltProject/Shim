/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import javax.management.openmbean.CompositeData;
import jdk.jfr.SettingDescriptor;

public final class SettingDescriptorInfo {
  private final String name;
  private final String label;
  private final String description;
  private final String typeName;
  private final String contentType;
  private final String defaultValue;

  SettingDescriptorInfo(SettingDescriptor s) {
    this.name = s.getName();
    this.label = s.getLabel();
    this.description = s.getDescription();
    this.typeName = s.getTypeName();
    this.contentType = s.getContentType();
    this.defaultValue = s.getDefaultValue();
  }

  private SettingDescriptorInfo(CompositeData d) {
    this.name = (String) d.get("name");
    this.label = (String) d.get("label");
    this.description = (String) d.get("description");
    this.typeName = (String) d.get("typeName");
    this.contentType = (String) d.get("contentType");
    this.defaultValue = (String) d.get("defaultValue");
  }

  public static SettingDescriptorInfo from(CompositeData compositeData) {
    return compositeData == null ? null : new SettingDescriptorInfo(compositeData);
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getContentType() {
    return contentType;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("name=\"").append(name).append("\" label=\"").append(label).append("\"");
    if (description != null) {
      sb.append(" description=\"").append(description).append("\"");
    }
    sb.append(" typeName=\"").append(typeName).append("\"");
    if (contentType != null) {
      sb.append(" contentType=\"").append(contentType).append("\"");
    }
    sb.append(" defaultValue=\"").append(defaultValue).append("\"");
    return sb.toString();
  }
}
