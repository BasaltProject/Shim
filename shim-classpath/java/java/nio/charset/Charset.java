/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import sun.nio.cs.StandardCharsets;

public abstract class Charset implements Comparable<Charset> {
  private final String name;
  private final String[] aliases;
  private Set<String> aliasSet;

  private static void checkName(String s) {
    int n = s.length();
    if (n == 0) {
      throw new IllegalCharsetNameException(s);
    }
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        continue;
      }
      if (c >= 'a' && c <= 'z') {
        continue;
      }
      if (c >= '0' && c <= '9') {
        continue;
      }
      if (c == '-' && i != 0) {
        continue;
      }
      if (c == '+' && i != 0) {
        continue;
      }
      if (c == ':' && i != 0) {
        continue;
      }
      if (c == '_' && i != 0) {
        continue;
      }
      if (c == '.' && i != 0) {
        continue;
      }
      throw new IllegalCharsetNameException(s);
    }
  }

  protected Charset(String canonicalName, String[] aliases) {
    String[] as = aliases == null ? new String[0] : aliases;
    checkName(canonicalName);
    for (int i = 0; i < as.length; i++) {
      checkName(as[i]);
    }
    this.name = canonicalName;
    this.aliases = as;
  }

  private static CharsetProvider[] providers;

  private static synchronized CharsetProvider[] providers() {
    if (providers == null) {
      java.util.List<CharsetProvider> list = new java.util.ArrayList<CharsetProvider>();
      try {
        for (CharsetProvider p : java.util.ServiceLoader.load(CharsetProvider.class,
            ClassLoader.getSystemClassLoader())) {
          list.add(p);
        }
      } catch (Throwable t) {
        // providers are optional
      }
      providers = list.toArray(new CharsetProvider[0]);
    }
    return providers;
  }

  private static Charset lookup(String charsetName) {
    if (charsetName == null) {
      throw new IllegalArgumentException("Null charset name");
    }
    checkName(charsetName);
    Charset cs = StandardCharsets.lookup(charsetName);
    if (cs != null) {
      return cs;
    }
    for (CharsetProvider p : providers()) {
      cs = p.charsetForName(charsetName);
      if (cs != null) {
        return cs;
      }
    }
    return null;
  }

  public static boolean isSupported(String charsetName) {
    return lookup(charsetName) != null;
  }

  public static Charset forName(String charsetName) {
    Charset cs = lookup(charsetName);
    if (cs != null) {
      return cs;
    }
    throw new UnsupportedCharsetException(charsetName);
  }

  public static Charset forName(String charsetName, Charset fallback) {
    try {
      Charset cs = lookup(charsetName);
      return cs != null ? cs : fallback;
    } catch (IllegalCharsetNameException icne) {
      return fallback;
    }
  }

  public static SortedMap<String, Charset> availableCharsets() {
    TreeMap<String, Charset> m = new TreeMap<String, Charset>(String.CASE_INSENSITIVE_ORDER);
    for (Charset cs : StandardCharsets.all()) {
      m.put(cs.name(), cs);
    }
    for (CharsetProvider p : providers()) {
      for (Iterator<Charset> i = p.charsets(); i.hasNext();) {
        Charset cs = i.next();
        if (!m.containsKey(cs.name())) {
          m.put(cs.name(), cs);
        }
      }
    }
    return Collections.unmodifiableSortedMap(m);
  }

  public static Charset defaultCharset() {
    return sun.nio.cs.UTF_8.INSTANCE;
  }

  public final String name() {
    return name;
  }

  public final Set<String> aliases() {
    if (aliasSet != null) {
      return aliasSet;
    }
    Set<String> set = HashSet.newHashSet(aliases.length);
    for (String alias : aliases) {
      set.add(alias);
    }
    aliasSet = Collections.unmodifiableSet(set);
    return aliasSet;
  }

  public String displayName() {
    return name;
  }

  public final boolean isRegistered() {
    return !name.startsWith("X-") && !name.startsWith("x-");
  }

  public String displayName(Locale locale) {
    return name;
  }

  public abstract boolean contains(Charset cs);

  public abstract CharsetDecoder newDecoder();

  public abstract CharsetEncoder newEncoder();

  public boolean canEncode() {
    return true;
  }

  public final CharBuffer decode(ByteBuffer bb) {
    try {
      return newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE).decode(bb);
    } catch (CharacterCodingException x) {
      throw new Error(x);
    }
  }

  public final ByteBuffer encode(CharBuffer cb) {
    try {
      return newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
          .onUnmappableCharacter(CodingErrorAction.REPLACE).encode(cb);
    } catch (CharacterCodingException x) {
      throw new Error(x);
    }
  }

  public final ByteBuffer encode(String str) {
    return encode(CharBuffer.wrap(str));
  }

  public final int compareTo(Charset that) {
    return name().compareToIgnoreCase(that.name());
  }

  public final int hashCode() {
    return name().hashCode();
  }

  public final boolean equals(Object ob) {
    if (this == ob) {
      return true;
    }
    if (!(ob instanceof Charset)) {
      return false;
    }
    return name.equals(((Charset) ob).name());
  }

  public final String toString() {
    return name();
  }
}
