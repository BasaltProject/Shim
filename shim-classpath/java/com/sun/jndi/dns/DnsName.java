/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.dns;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import javax.naming.InvalidNameException;
import javax.naming.Name;

/** DNS name whose components are ordered from the root, as in the JDK. */
public final class DnsName implements Name {
  private static final long serialVersionUID = 7040187611324710271L;
  private final ArrayList<String> labels = new ArrayList<String>();

  public DnsName() {
  }

  public DnsName(String name) throws InvalidNameException {
    parse(name);
  }

  private DnsName(List<String> labels) {
    this.labels.addAll(labels);
  }

  private void parse(String name) throws InvalidNameException {
    if (name.isEmpty() || name.equals(".")) {
      return;
    }
    String n = name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    if (n.isEmpty() || n.startsWith(".") || n.contains("..")) {
      throw new InvalidNameException("Invalid DNS name: " + name);
    }
    String[] parts = n.split("\\.");
    for (int i = parts.length - 1; i >= 0; i--) {
      if (parts[i].length() > 63) {
        throw new InvalidNameException("Label " + parts[i] + " exceeds 63 octets");
      }
      labels.add(parts[i]);
    }
  }

  /** Labels from the leftmost one, as they travel on the wire. */
  List<String> wireLabels() {
    ArrayList<String> out = new ArrayList<String>(labels);
    java.util.Collections.reverse(out);
    return out;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = labels.size() - 1; i >= 0; i--) {
      if (sb.length() > 0) {
        sb.append('.');
      }
      sb.append(labels.get(i));
    }
    return sb.toString();
  }

  @Override
  public Object clone() {
    return new DnsName(labels);
  }

  @Override
  public int compareTo(Object obj) {
    DnsName other = (DnsName) obj;
    int n = Math.min(labels.size(), other.labels.size());
    for (int i = 0; i < n; i++) {
      int c = labels.get(i).compareToIgnoreCase(other.labels.get(i));
      if (c != 0) {
        return c;
      }
    }
    return labels.size() - other.labels.size();
  }

  @Override
  public int size() {
    return labels.size();
  }

  @Override
  public boolean isEmpty() {
    return labels.isEmpty();
  }

  @Override
  public Enumeration<String> getAll() {
    return java.util.Collections.enumeration(new ArrayList<String>(labels));
  }

  @Override
  public String get(int posn) {
    return labels.get(posn);
  }

  @Override
  public Name getPrefix(int posn) {
    return new DnsName(labels.subList(0, posn));
  }

  @Override
  public Name getSuffix(int posn) {
    return new DnsName(labels.subList(posn, labels.size()));
  }

  @Override
  public boolean startsWith(Name n) {
    int len = n.size();
    if (len > labels.size()) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (!labels.get(i).equalsIgnoreCase(n.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean endsWith(Name n) {
    int len = n.size();
    int off = labels.size() - len;
    if (off < 0) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (!labels.get(off + i).equalsIgnoreCase(n.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Name addAll(Name suffix) throws InvalidNameException {
    for (int i = 0; i < suffix.size(); i++) {
      labels.add(suffix.get(i));
    }
    return this;
  }

  @Override
  public Name addAll(int posn, Name n) throws InvalidNameException {
    for (int i = 0; i < n.size(); i++) {
      labels.add(posn + i, n.get(i));
    }
    return this;
  }

  @Override
  public Name add(String comp) throws InvalidNameException {
    labels.add(comp);
    return this;
  }

  @Override
  public Name add(int posn, String comp) throws InvalidNameException {
    labels.add(posn, comp);
    return this;
  }

  @Override
  public Object remove(int posn) throws InvalidNameException {
    return labels.remove(posn);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof DnsName && compareTo(obj) == 0;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (String l : labels) {
      h = 31 * h + l.toLowerCase(Locale.ROOT).hashCode();
    }
    return h;
  }
}
