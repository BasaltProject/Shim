/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class PropertyResourceBundle extends ResourceBundle {
  private final Map<String, Object> lookup;

  public PropertyResourceBundle(InputStream stream) throws IOException {
    byte[] data = readAll(Objects.requireNonNull(stream));
    Properties properties = new Properties();
    Reader reader;
    try {
      reader = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT));
      properties.load(reader);
    } catch (CharacterCodingException e) {
      properties = new Properties();
      properties.load(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.ISO_8859_1));
    }
    lookup = copy(properties);
  }

  public PropertyResourceBundle(Reader reader) throws IOException {
    Properties properties = new Properties();
    properties.load(Objects.requireNonNull(reader));
    lookup = copy(properties);
  }

  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n;
    while ((n = in.read(buffer)) >= 0) {
      out.write(buffer, 0, n);
    }
    return out.toByteArray();
  }

  private static Map<String, Object> copy(Properties properties) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (String key : properties.stringPropertyNames()) {
      map.put(key, properties.getProperty(key));
    }
    return map;
  }

  public Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return lookup.get(key);
  }

  public Enumeration<String> getKeys() {
    ResourceBundle parent = this.parent;
    return new ResourceBundleEnumeration(lookup.keySet(), parent != null ? parent.getKeys() : null);
  }

  protected Set<String> handleKeySet() {
    return lookup.keySet();
  }
}
