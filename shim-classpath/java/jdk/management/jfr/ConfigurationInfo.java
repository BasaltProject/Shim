/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import jdk.jfr.Configuration;

public final class ConfigurationInfo {
  private final String name;
  private final String label;
  private final String description;
  private final String provider;
  private final String contents;
  private final Map<String, String> settings;

  ConfigurationInfo(Configuration c) {
    this.name = c.getName();
    this.label = c.getLabel();
    this.description = c.getDescription();
    this.provider = c.getProvider();
    this.contents = c.getContents();
    this.settings = c.getSettings();
  }

  private ConfigurationInfo(CompositeData d) {
    this.name = (String) d.get("name");
    this.label = (String) d.get("label");
    this.description = (String) d.get("description");
    this.provider = (String) d.get("provider");
    this.contents = (String) d.get("contents");
    this.settings = new LinkedHashMap<String, String>();
    Object s = d.get("settings");
    if (s instanceof TabularData) {
      for (Object row : ((TabularData) s).values()) {
        CompositeData cd = (CompositeData) row;
        settings.put((String) cd.get("key"), (String) cd.get("value"));
      }
    }
  }

  public static ConfigurationInfo from(CompositeData compositeData) {
    return compositeData == null ? null : new ConfigurationInfo(compositeData);
  }

  public Map<String, String> getSettings() {
    return settings;
  }

  public String getContents() {
    return contents;
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

  public String getProvider() {
    return provider;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("name=\"").append(name).append("\" label=\"").append(label).append("\"");
    if (description != null) {
      sb.append(" description=\"").append(description).append("\"");
    }
    sb.append(" provider=\"").append(provider).append("\"");
    return sb.toString();
  }
}
