/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HttpCookie implements Cloneable {

  private final String name;
  private String value;
  private String comment;
  private String commentURL;
  private boolean toDiscard;
  private String domain;
  private long maxAge = -1;
  private String path;
  private String portlist;
  private boolean secure;
  private boolean httpOnly;
  private int version = 1;

  public HttpCookie(String name, String value) {
    String trimmed = name.trim();
    if (trimmed.isEmpty() || trimmed.charAt(0) == '$') {
      throw new IllegalArgumentException("Illegal cookie name: " + name);
    }
    this.name = trimmed;
    this.value = value;
  }

  public static List<HttpCookie> parse(String header) {
    if (header == null) {
      throw new NullPointerException("header");
    }
    List<HttpCookie> cookies = new ArrayList<HttpCookie>();
    String[] parts = header.split(";");
    if (parts.length == 0) {
      return cookies;
    }
    String[] first = parts[0].trim().split("=", 2);
    if (first.length != 2) {
      return cookies;
    }
    HttpCookie cookie = new HttpCookie(first[0].trim(), first[1].trim());
    for (int i = 1; i < parts.length; i++) {
      String[] pair = parts[i].trim().split("=", 2);
      String k = pair[0].trim();
      String v = pair.length > 1 ? pair[1].trim() : "";
      if (k.equalsIgnoreCase("Path")) {
        cookie.setPath(v);
      } else if (k.equalsIgnoreCase("Domain")) {
        cookie.setDomain(v);
      } else if (k.equalsIgnoreCase("Max-Age")) {
        try {
          cookie.setMaxAge(Long.parseLong(v));
        } catch (Exception ignored) {}
      } else if (k.equalsIgnoreCase("Secure")) {
        cookie.setSecure(true);
      } else if (k.equalsIgnoreCase("HttpOnly")) {
        cookie.setHttpOnly(true);
      } else if (k.equalsIgnoreCase("Version")) {
        try {
          cookie.setVersion(Integer.parseInt(v));
        } catch (Exception ignored) {}
      }
    }
    cookies.add(cookie);
    return cookies;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String pattern) {
    this.domain = pattern != null ? pattern.toLowerCase() : null;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long expiry) {
    this.maxAge = expiry;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String uri) {
    this.path = uri;
  }

  public boolean getSecure() {
    return secure;
  }

  public void setSecure(boolean flag) {
    this.secure = flag;
  }

  public boolean isHttpOnly() {
    return httpOnly;
  }

  public void setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int v) {
    this.version = v;
  }

  public boolean hasExpired() {
    return maxAge == 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof HttpCookie)) return false;
    HttpCookie other = (HttpCookie) obj;
    return Objects.equals(name, other.name) && Objects.equals(domain, other.domain) && Objects.equals(path, other.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, domain, path);
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return name + "=" + value;
  }
}
