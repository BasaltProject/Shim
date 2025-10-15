/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

final class PreferencesXml {
  private static final String kHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
      + "<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
      + "<preferences EXTERNAL_XML_VERSION=\"1.0\">\n";
  private static final String kMapHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
      + "<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
      + "<map MAP_XML_VERSION=\"1.0\">\n";

  private PreferencesXml() {
  }

  static void export(OutputStream os, Preferences node, boolean subtree) throws IOException, BackingStoreException {
    if (node instanceof AbstractPreferences && ((AbstractPreferences) node).isRemovedForExport()) {
      throw new IllegalStateException("Node has been removed");
    }
    StringBuilder sb = new StringBuilder(kHeader);
    List<Preferences> path = new ArrayList<Preferences>();
    for (Preferences p = node; p.parent() != null; p = p.parent()) {
      path.add(0, p);
    }
    Preferences root = node;
    while (root.parent() != null) {
      root = root.parent();
    }
    sb.append("  <root type=\"").append(root.isUserNode() ? "user" : "system").append("\">\n");
    int depth = 2;
    appendMap(sb, root, path.isEmpty() ? node : null, depth);
    for (int i = 0; i < path.size(); i++) {
      Preferences p = path.get(i);
      boolean last = i == path.size() - 1;
      indent(sb, depth).append("<node name=\"").append(MiniXml.escape(p.name())).append("\">\n");
      depth++;
      appendMap(sb, p, last ? p : null, depth);
    }
    if (subtree) {
      appendChildren(sb, node, depth);
    }
    for (int i = path.size() - 1; i >= 0; i--) {
      depth--;
      indent(sb, depth).append("</node>\n");
    }
    sb.append("  </root>\n</preferences>\n");
    os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    os.flush();
  }

  private static void appendChildren(StringBuilder sb, Preferences node, int depth) throws BackingStoreException {
    for (String childName : node.childrenNames()) {
      Preferences child = node.node(childName);
      indent(sb, depth).append("<node name=\"").append(MiniXml.escape(childName)).append("\">\n");
      appendMap(sb, child, child, depth + 1);
      appendChildren(sb, child, depth + 1);
      indent(sb, depth).append("</node>\n");
    }
  }

  private static void appendMap(StringBuilder sb, Preferences node, Preferences withEntries, int depth)
      throws BackingStoreException {
    String[] keys = withEntries == null ? new String[0] : withEntries.keys();
    if (keys.length == 0) {
      indent(sb, depth).append("<map/>\n");
      return;
    }
    java.util.Arrays.sort(keys);
    indent(sb, depth).append("<map>\n");
    for (String key : keys) {
      indent(sb, depth + 1).append("<entry key=\"").append(MiniXml.escape(key)).append("\" value=\"")
          .append(MiniXml.escape(withEntries.get(key, null))).append("\"/>\n");
    }
    indent(sb, depth).append("</map>\n");
  }

  private static StringBuilder indent(StringBuilder sb, int depth) {
    for (int i = 0; i < depth; i++) {
      sb.append("  ");
    }
    return sb;
  }

  static String exportMap(TreeMap<String, String> map) {
    if (map.isEmpty()) {
      return kMapHeader.substring(0, kMapHeader.length() - 2) + "/>\n";
    }
    StringBuilder sb = new StringBuilder(kMapHeader);
    for (java.util.Map.Entry<String, String> e : map.entrySet()) {
      sb.append("  <entry key=\"").append(MiniXml.escape(e.getKey())).append("\" value=\"")
          .append(MiniXml.escape(e.getValue())).append("\"/>\n");
    }
    return sb.append("</map>\n").toString();
  }

  static void importMap(String xml, java.util.Map<String, String> into) throws InvalidPreferencesFormatException {
    MiniXml.Element root;
    try {
      root = MiniXml.parse(xml);
    } catch (MiniXml.ParseException e) {
      throw new InvalidPreferencesFormatException(e);
    }
    if (!root.name.equals("map")) {
      throw new InvalidPreferencesFormatException("Document root element must be \"map\".");
    }
    for (MiniXml.Element entry : root.children) {
      into.put(entry.attributes.get("key"), entry.attributes.get("value"));
    }
  }

  static void importPreferences(InputStream is) throws IOException, InvalidPreferencesFormatException {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n;
    while ((n = is.read(buffer)) > 0) {
      bo.write(buffer, 0, n);
    }
    MiniXml.Element doc;
    try {
      doc = MiniXml.parse(new String(bo.toByteArray(), StandardCharsets.UTF_8));
    } catch (MiniXml.ParseException e) {
      throw new InvalidPreferencesFormatException(e);
    }
    if (!doc.name.equals("preferences")) {
      throw new InvalidPreferencesFormatException("Document root element must be \"preferences\".");
    }
    String version = doc.attributes.get("EXTERNAL_XML_VERSION");
    if (version == null) {
      throw new InvalidPreferencesFormatException("Attribute \"EXTERNAL_XML_VERSION\" is required.");
    }
    if (Double.parseDouble(version) > 1.0) {
      throw new InvalidPreferencesFormatException("Exported preferences file format version " + version
          + " is not supported. This java installation can read versions 1.0 or older. You may need"
          + " to install a newer version of JDK.");
    }
    if (doc.children.size() != 1 || !doc.children.get(0).name.equals("root")) {
      throw new InvalidPreferencesFormatException("Element \"preferences\" must contain exactly one \"root\".");
    }
    MiniXml.Element rootElement = doc.children.get(0);
    String type = rootElement.attributes.get("type");
    if (!"user".equals(type) && !"system".equals(type)) {
      throw new InvalidPreferencesFormatException("Attribute \"type\" must be \"user\" or \"system\".");
    }
    Preferences root = type.equals("user") ? Preferences.userRoot() : Preferences.systemRoot();
    try {
      synchronized (root instanceof AbstractPreferences ? ((AbstractPreferences) root).lockObject() : root) {
        apply(root, rootElement);
      }
      root.flush();
    } catch (BackingStoreException e) {
      throw new IOException(e.getMessage());
    }
  }

  private static void apply(Preferences node, MiniXml.Element element) throws InvalidPreferencesFormatException {
    for (MiniXml.Element child : element.children) {
      if (child.name.equals("map")) {
        for (MiniXml.Element entry : child.children) {
          String key = entry.attributes.get("key");
          String value = entry.attributes.get("value");
          if (key == null || value == null) {
            throw new InvalidPreferencesFormatException("Element \"entry\" requires \"key\" and \"value\".");
          }
          node.put(key, value);
        }
      } else if (child.name.equals("node")) {
        String name = child.attributes.get("name");
        if (name == null) {
          throw new InvalidPreferencesFormatException("Attribute \"name\" is required.");
        }
        apply(node.node(name), child);
      } else {
        throw new InvalidPreferencesFormatException("Unexpected element \"" + child.name + "\".");
      }
    }
  }
}
