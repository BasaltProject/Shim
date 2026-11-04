/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Configuration {
  private static List<Configuration> predefined;

  private final String name;
  private final String label;
  private final String description;
  private final String provider;
  private final String contents;
  private final Map<String, String> settings;

  private Configuration(String name, String contents) throws ParseException {
    this.name = name;
    this.contents = contents;
    Matcher root = Pattern.compile("<configuration\\b([^>]*)>").matcher(contents);
    if (!root.find()) {
      throw new ParseException("Missing <configuration> element", 0);
    }
    String attrs = root.group(1);
    this.label = attr(attrs, "label");
    this.description = attr(attrs, "description");
    this.provider = attr(attrs, "provider");
    Map<String, String> s = new LinkedHashMap<String, String>();
    Matcher ev = Pattern.compile("<event\\s+name=\"([^\"]+)\"[^>]*>(.*?)</event>", Pattern.DOTALL)
        .matcher(contents);
    Pattern setting = Pattern.compile("<setting\\s+name=\"([^\"]+)\"[^>]*>([^<]*)</setting>");
    while (ev.find()) {
      Matcher st = setting.matcher(ev.group(2));
      while (st.find()) {
        s.put(ev.group(1) + "#" + st.group(1), st.group(2).trim());
      }
    }
    this.settings = Collections.unmodifiableMap(s);
  }

  private static String attr(String attrs, String key) {
    Matcher m = Pattern.compile("\\b" + key + "=\"([^\"]*)\"").matcher(attrs);
    return m.find() ? m.group(1) : null;
  }

  public Map<String, String> getSettings() {
    return new LinkedHashMap<String, String>(settings);
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

  public String getContents() {
    return contents;
  }

  public static Configuration getConfiguration(String name) throws IOException, ParseException {
    java.util.Objects.requireNonNull(name);
    for (Configuration c : getConfigurations()) {
      if (name.equals(c.name)) {
        return c;
      }
    }
    throw new NoSuchFileException("Could not locate configuration with name " + name);
  }

  public static Configuration create(Path path) throws IOException, ParseException {
    java.util.Objects.requireNonNull(path);
    String fn = path.getFileName().toString();
    int dot = fn.lastIndexOf('.');
    String n = dot > 0 ? fn.substring(0, dot) : fn;
    return new Configuration(n, new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8));
  }

  public static Configuration create(Reader reader) throws IOException, ParseException {
    java.util.Objects.requireNonNull(reader);
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = reader.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    return new Configuration(null, sb.toString());
  }

  public static synchronized List<Configuration> getConfigurations() {
    if (predefined == null) {
      List<Configuration> l = new ArrayList<Configuration>();
      try {
        l.add(new Configuration("default", jfc("Continuous",
            "Low overhead configuration safe for continuous use in production environments, "
                + "typically less than 1 % overhead.", "true")));
        l.add(new Configuration("profile", jfc("Profiling",
            "Low overhead configuration for profiling, typically around 2 % overhead.", "true")));
      } catch (ParseException e) {
        throw new InternalError(e);
      }
      predefined = Collections.unmodifiableList(l);
    }
    return predefined;
  }

  private static String jfc(String label, String description, String enabled) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append("<configuration version=\"2.0\" label=\"").append(label).append("\" description=\"")
        .append(description).append("\" provider=\"Oracle\">\n");
    for (EventInfo i : JfrRuntime.REGISTERED.values()) {
      if (!i.builtin) {
        continue;
      }
      sb.append("  <event name=\"").append(i.name).append("\">\n");
      sb.append("    <setting name=\"enabled\">").append(enabled).append("</setting>\n");
      sb.append("    <setting name=\"stackTrace\">false</setting>\n");
      sb.append("    <setting name=\"threshold\">0 ms</setting>\n");
      if (i.defPeriod != null) {
        sb.append("    <setting name=\"period\">").append(i.defPeriod).append("</setting>\n");
      }
      sb.append("  </event>\n");
    }
    sb.append("</configuration>\n");
    return sb.toString();
  }
}
