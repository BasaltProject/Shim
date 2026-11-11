/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Config {
  private static Config instance;

  private final Map<String, Object> root = new LinkedHashMap<String, Object>();
  private final boolean fromProperties;

  private Config() throws KrbException {
    String realm = System.getProperty("java.security.krb5.realm");
    String kdc = System.getProperty("java.security.krb5.kdc");
    if ((realm == null) != (kdc == null)) {
      throw new KrbException("Either both java.security.krb5.realm and java.security.krb5.kdc should be set, or neither");
    }
    if (realm != null) {
      fromProperties = true;
      Map<String, Object> libdefaults = section("libdefaults");
      libdefaults.put("default_realm", realm);
      Map<String, Object> realms = section("realms");
      Map<String, Object> r = new LinkedHashMap<String, Object>();
      r.put("kdc", kdc);
      realms.put(realm, r);
      return;
    }
    fromProperties = false;
    String path = System.getProperty("java.security.krb5.conf");
    if (path == null) {
      String home = System.getProperty("java.home");
      File f = new File(home, "conf/security/krb5.conf");
      path = f.exists() ? f.getPath() : "/etc/krb5.conf";
    }
    File f = new File(path);
    if (f.isFile()) {
      try {
        parse(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new KrbException("Could not load configuration file " + path + " (" + e.getMessage() + ")");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> section(String name) {
    Object o = root.get(name);
    if (o == null) {
      o = new LinkedHashMap<String, Object>();
      root.put(name, o);
    }
    return (Map<String, Object>) o;
  }

  @SuppressWarnings("unchecked")
  private void parse(String text) {
    Map<String, Object> current = null;
    List<Map<String, Object>> stack = new ArrayList<Map<String, Object>>();
    for (String raw : text.split("\r?\n")) {
      String line = raw.trim();
      if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == ';') {
        continue;
      }
      if (line.startsWith("[")) {
        int e = line.indexOf(']');
        if (e > 0) {
          current = section(line.substring(1, e).trim());
          stack.clear();
          stack.add(current);
        }
        continue;
      }
      if (current == null) {
        continue;
      }
      if (line.startsWith("}")) {
        if (stack.size() > 1) {
          stack.remove(stack.size() - 1);
        }
        continue;
      }
      int eq = line.indexOf('=');
      if (eq < 0) {
        continue;
      }
      String key = line.substring(0, eq).trim();
      String value = line.substring(eq + 1).trim();
      Map<String, Object> scope = stack.get(stack.size() - 1);
      if (value.equals("{")) {
        Map<String, Object> child = new LinkedHashMap<String, Object>();
        Object prev = scope.get(key);
        if (prev instanceof Map) {
          child = (Map<String, Object>) prev;
        } else {
          scope.put(key, child);
        }
        stack.add(child);
      } else {
        int hash = value.indexOf(" #");
        if (hash >= 0 && !value.startsWith("\"")) {
          value = value.substring(0, hash).trim();
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
          value = value.substring(1, value.length() - 1);
        }
        Object prev = scope.get(key);
        if (prev instanceof String && !key.equals("default_realm")) {
          value = prev + " " + value;
        }
        scope.put(key, value);
      }
    }
  }

  public static synchronized Config getInstance() throws KrbException {
    if (instance == null) {
      instance = new Config();
    }
    return instance;
  }

  public static synchronized void refresh() throws KrbException {
    instance = new Config();
  }

  public String get(String... path) {
    Object cur = root;
    for (String p : path) {
      if (!(cur instanceof Map)) {
        return null;
      }
      cur = findKey((Map<?, ?>) cur, p);
    }
    return cur instanceof String ? (String) cur : null;
  }

  private static Object findKey(Map<?, ?> m, String key) {
    Object v = m.get(key);
    if (v != null) {
      return v;
    }
    for (Map.Entry<?, ?> e : m.entrySet()) {
      if (((String) e.getKey()).equalsIgnoreCase(key)) {
        return e.getValue();
      }
    }
    return null;
  }

  public boolean has(String... path) {
    Object cur = root;
    for (String p : path) {
      if (!(cur instanceof Map)) {
        return false;
      }
      cur = findKey((Map<?, ?>) cur, p);
    }
    return cur != null;
  }

  public Boolean getBoolean(String... path) {
    String v = get(path);
    if (v == null) {
      return null;
    }
    v = v.toLowerCase(Locale.ROOT);
    if (v.equals("yes") || v.equals("true") || v.equals("on") || v.equals("1")) {
      return Boolean.TRUE;
    }
    if (v.equals("no") || v.equals("false") || v.equals("off") || v.equals("0")) {
      return Boolean.FALSE;
    }
    return null;
  }

  public int getInt(int dflt, String... path) {
    String v = get(path);
    if (v == null) {
      return dflt;
    }
    try {
      return Integer.parseInt(v.trim());
    } catch (NumberFormatException e) {
      return dflt;
    }
  }

  public int getDurationSeconds(int dflt, String... path) {
    String v = get(path);
    if (v == null) {
      return dflt;
    }
    return parseDuration(v.trim(), dflt);
  }

  static int parseDuration(String v, int dflt) {
    try {
      if (v.matches("\\d+")) {
        return Integer.parseInt(v);
      }
      if (v.contains(":")) {
        String[] p = v.split(":");
        return Integer.parseInt(p[0]) * 3600 + Integer.parseInt(p[1]) * 60 + (p.length > 2 ? Integer.parseInt(p[2]) : 0);
      }
      int total = 0;
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*([dhms])").matcher(v);
      boolean any = false;
      while (m.find()) {
        any = true;
        int n = Integer.parseInt(m.group(1));
        switch (m.group(2).charAt(0)) {
          case 'd':
            total += n * 86400;
            break;
          case 'h':
            total += n * 3600;
            break;
          case 'm':
            total += n * 60;
            break;
          default:
            total += n;
        }
      }
      return any ? total : dflt;
    } catch (RuntimeException e) {
      return dflt;
    }
  }

  public String defaultRealm() throws KrbException {
    String r = get("libdefaults", "default_realm");
    if (r == null) {
      String env = System.getProperty("java.security.krb5.realm");
      if (env != null) {
        return env;
      }
      throw new KrbException("Cannot locate default realm");
    }
    return r;
  }

  public List<String> kdcList(String realm) {
    List<String> out = new ArrayList<String>();
    String v = get("realms", realm, "kdc");
    if (v != null) {
      for (String s : v.trim().split("[\\s,]+")) {
        if (!s.isEmpty()) {
          out.add(s);
        }
      }
    }
    return out;
  }

  public String realmOfHost(String host) {
    Object map = root.get("domain_realm");
    if (host == null) {
      return null;
    }
    String h = host.toLowerCase(Locale.ROOT);
    if (map instanceof Map) {
      Map<?, ?> m = (Map<?, ?>) map;
      String best = null;
      String bestKey = null;
      for (Map.Entry<?, ?> e : m.entrySet()) {
        String k = ((String) e.getKey()).toLowerCase(Locale.ROOT);
        boolean match = k.startsWith(".") ? h.endsWith(k) : h.equals(k);
        if (match && (bestKey == null || k.length() > bestKey.length())) {
          bestKey = k;
          best = (String) e.getValue();
        }
      }
      if (best != null) {
        return best;
      }
      for (String dot = h; dot != null;) {
        int i = dot.indexOf('.');
        if (i < 0) {
          break;
        }
        dot = dot.substring(i);
        Object v = findKey(m, dot);
        if (v instanceof String) {
          return (String) v;
        }
        dot = dot.substring(1);
      }
    }
    return null;
  }

  public boolean allowWeakCrypto() {
    return Boolean.TRUE.equals(getBoolean("libdefaults", "allow_weak_crypto"));
  }

  private static final String kBuiltin = "aes256-cts-hmac-sha1-96 aes128-cts-hmac-sha1-96 aes256-cts-hmac-sha384-192 "
      + "aes128-cts-hmac-sha256-128";

  static int parseEtype(String token) {
    String t = token.toLowerCase(Locale.ROOT);
    switch (t) {
      case "des-cbc-crc":
        return 1;
      case "des-cbc-md5":
        return 3;
      case "des3-cbc-sha1":
      case "des3-hmac-sha1":
      case "des3-cbc-sha1-kd":
        return 16;
      case "aes128-cts":
      case "aes128-cts-hmac-sha1-96":
        return 17;
      case "aes256-cts":
      case "aes256-cts-hmac-sha1-96":
        return 18;
      case "aes128-sha2":
      case "aes128-cts-hmac-sha256-128":
        return 19;
      case "aes256-sha2":
      case "aes256-cts-hmac-sha384-192":
        return 20;
      case "arcfour-hmac":
      case "rc4-hmac":
      case "arcfour-hmac-md5":
        return 23;
      default:
        try {
          return Integer.parseInt(t);
        } catch (NumberFormatException e) {
          return -1000;
        }
    }
  }

  public int[] etypes(String key) throws KrbException {
    String v = get("libdefaults", key);
    if (v == null && key.equals("default_tgs_enctypes")) {
      v = null;
    }
    String list = v == null ? kBuiltin : v;
    String[] tokens = list.trim().split("[\\s,]+");
    boolean weak = allowWeakCrypto();
    String permitted = get("libdefaults", "permitted_enctypes");
    List<Integer> allowed = null;
    if (permitted != null) {
      allowed = new ArrayList<Integer>();
      for (String t : permitted.trim().split("[\\s,]+")) {
        allowed.add(parseEtype(t));
      }
    }
    List<Integer> out = new ArrayList<Integer>();
    for (String t : tokens) {
      int et = parseEtype(t);
      boolean isWeak = et == 1 || et == 3 || et == 16 || et == 23;
      boolean supported = Crypto.isSupported(et) || et == 1 || et == 3;
      if (!supported || (isWeak && !weak) || (allowed != null && !allowed.contains(et)) || out.contains(et)) {
        continue;
      }
      out.add(et);
    }
    if (out.isEmpty()) {
      throw new KrbException("out of " + tokens.length + " default etypes no supported etypes found for configuration "
          + key);
    }
    int[] r = new int[out.size()];
    for (int i = 0; i < r.length; i++) {
      r[i] = out.get(i);
    }
    return r;
  }

  public static String canonicalHost(String host) {
    try {
      return InetAddress.getByName(host).getCanonicalHostName().toLowerCase(Locale.ROOT);
    } catch (UnknownHostException | SecurityException e) {
      return host.toLowerCase(Locale.ROOT);
    }
  }
}
