/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.jar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Manifest implements Cloneable {
  private final Attributes mainAttributes;
  private final Map<String, Attributes> entries;

  public Manifest() {
    this.mainAttributes = new Attributes();
    this.entries = new HashMap<String, Attributes>();
  }

  public Manifest(InputStream is) throws IOException {
    this();
    read(is);
  }

  public Manifest(Manifest man) {
    this.mainAttributes = new Attributes(man.mainAttributes);
    this.entries = new HashMap<String, Attributes>();
    for (Map.Entry<String, Attributes> e : man.entries.entrySet()) {
      this.entries.put(e.getKey(), new Attributes(e.getValue()));
    }
  }

  public Attributes getMainAttributes() {
    return mainAttributes;
  }

  public Map<String, Attributes> getEntries() {
    return entries;
  }

  public Attributes getAttributes(String name) {
    return entries.get(name);
  }

  public void clear() {
    mainAttributes.clear();
    entries.clear();
  }

  public void read(InputStream is) throws IOException {
    Objects.requireNonNull(is, "InputStream");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    List<String> logicalLines = new ArrayList<String>();

    String line;
    StringBuilder currentLogical = null;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith(" ")) {
        if (currentLogical != null) {
          currentLogical.append(line.substring(1));
        }
      } else {
        if (currentLogical != null) {
          logicalLines.add(currentLogical.toString());
        }
        if (line.isEmpty()) {
          logicalLines.add("");
          currentLogical = null;
        } else {
          currentLogical = new StringBuilder(line);
        }
      }
    }
    if (currentLogical != null) {
      logicalLines.add(currentLogical.toString());
    }

    Attributes currentAttr = mainAttributes;
    boolean inMain = true;

    for (String l : logicalLines) {
      if (l.isEmpty()) {
        inMain = false;
        currentAttr = null;
        continue;
      }
      int colon = l.indexOf(':');
      if (colon == -1) continue;

      String key = l.substring(0, colon).trim();
      String val = l.substring(colon + 1);
      if (val.startsWith(" ")) {
        val = val.substring(1);
      }

      if (key.equalsIgnoreCase("Name")) {
        currentAttr = new Attributes();
        entries.put(val, currentAttr);
        inMain = false;
      } else {
        if (currentAttr != null) {
          currentAttr.putValue(key, val);
        } else if (inMain) {
          mainAttributes.putValue(key, val);
        }
      }
    }
  }

  public void write(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);

    // Write Manifest-Version first if present
    String ver = mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION);
    if (ver != null) {
      writeHeader(dos, "Manifest-Version", ver);
    }

    for (Map.Entry<Object, Object> e : mainAttributes.entrySet()) {
      String key = e.getKey().toString();
      if (ver != null && key.equalsIgnoreCase("Manifest-Version")) {
        continue;
      }
      writeHeader(dos, key, e.getValue().toString());
    }
    dos.writeBytes("\r\n");

    for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
      writeHeader(dos, "Name", entry.getKey());
      for (Map.Entry<Object, Object> e : entry.getValue().entrySet()) {
        writeHeader(dos, e.getKey().toString(), e.getValue().toString());
      }
      dos.writeBytes("\r\n");
    }
    dos.flush();
  }

  private static void writeHeader(DataOutputStream dos, String name, String val) throws IOException {
    String header = name + ": " + val;
    byte[] bytes = header.getBytes(StandardCharsets.UTF_8);
    if (bytes.length <= 72) {
      dos.write(bytes);
      dos.writeBytes("\r\n");
    } else {
      dos.write(bytes, 0, 72);
      dos.writeBytes("\r\n");
      int offset = 72;
      while (offset < bytes.length) {
        dos.writeBytes(" ");
        int len = Math.min(71, bytes.length - offset);
        dos.write(bytes, offset, len);
        dos.writeBytes("\r\n");
        offset += len;
      }
    }
  }

  @Override
  public Object clone() {
    return new Manifest(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Manifest) {
      Manifest other = (Manifest) o;
      return mainAttributes.equals(other.mainAttributes) && entries.equals(other.entries);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return mainAttributes.hashCode() ^ entries.hashCode();
  }
}
