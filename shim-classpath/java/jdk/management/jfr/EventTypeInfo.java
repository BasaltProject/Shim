/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import java.util.ArrayList;
import java.util.List;
import javax.management.openmbean.CompositeData;
import jdk.jfr.EventType;
import jdk.jfr.SettingDescriptor;

public final class EventTypeInfo {
  private final long id;
  private final String name;
  private final String label;
  private final String description;
  private final List<String> categoryNames;
  private final List<SettingDescriptorInfo> settings;

  EventTypeInfo(EventType t) {
    this.id = t.getId();
    this.name = t.getName();
    this.label = t.getLabel();
    this.description = t.getDescription();
    this.categoryNames = new ArrayList<String>(t.getCategoryNames());
    this.settings = new ArrayList<SettingDescriptorInfo>();
    for (SettingDescriptor s : t.getSettingDescriptors()) {
      settings.add(new SettingDescriptorInfo(s));
    }
  }

  private EventTypeInfo(CompositeData d) {
    this.id = (Long) d.get("id");
    this.name = (String) d.get("name");
    this.label = (String) d.get("label");
    this.description = (String) d.get("description");
    this.categoryNames = new ArrayList<String>();
    Object[] cn = (Object[]) d.get("categoryNames");
    for (Object o : cn) {
      categoryNames.add((String) o);
    }
    this.settings = new ArrayList<SettingDescriptorInfo>();
    for (Object o : (Object[]) d.get("settingDescriptors")) {
      settings.add(SettingDescriptorInfo.from((CompositeData) o));
    }
  }

  public static EventTypeInfo from(CompositeData compositeData) {
    return compositeData == null ? null : new EventTypeInfo(compositeData);
  }

  public long getId() {
    return id;
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

  public List<String> getCategoryNames() {
    return categoryNames;
  }

  public List<SettingDescriptorInfo> getSettingDescriptors() {
    return settings;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("id=").append(id).append(" name=\"").append(name).append("\" label=\"").append(label).append("\"");
    if (description != null) {
      sb.append(" description=\"").append(description).append("\"");
    }
    sb.append(" category=\"{");
    for (int i = 0; i < categoryNames.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(categoryNames.get(i));
    }
    sb.append("}\"");
    return sb.toString();
  }
}
