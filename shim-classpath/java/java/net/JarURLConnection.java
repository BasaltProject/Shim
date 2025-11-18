/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public abstract class JarURLConnection extends URLConnection {
  private URL jarFileURL;
  private String entryName;
  protected URLConnection jarFileURLConnection;

  protected JarURLConnection(URL url) throws MalformedURLException {
    super(url);
    parseSpecs(url);
  }

  private void parseSpecs(URL url) throws MalformedURLException {
    String spec = url.getFile();
    int separator = spec.indexOf("!/");
    if (separator == -1) {
      throw new MalformedURLException("no !/ found in url spec:" + spec);
    }
    jarFileURL = new URL(spec.substring(0, separator++));
    entryName = null;
    if (++separator != spec.length()) {
      entryName = decode(spec.substring(separator));
    }
  }

  private static String decode(String s) {
    if (s.indexOf('%') < 0) {
      return s;
    }
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    for (int i = 0; i < s.length(); ) {
      char c = s.charAt(i);
      if (c == '%' && i + 2 < s.length()) {
        out.write(Integer.parseInt(s.substring(i + 1, i + 3), 16));
        i += 3;
      } else if (c == '%') {
        throw new IllegalArgumentException("Malformed escape pair: " + s);
      } else {
        byte[] bytes = String.valueOf(c).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (Character.isHighSurrogate(c) && i + 1 < s.length()) {
          bytes = s.substring(i, i + 2).getBytes(java.nio.charset.StandardCharsets.UTF_8);
          i++;
        }
        out.write(bytes, 0, bytes.length);
        i++;
      }
    }
    return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
  }

  public URL getJarFileURL() {
    return jarFileURL;
  }

  public String getEntryName() {
    return entryName;
  }

  public abstract JarFile getJarFile() throws IOException;

  public Manifest getManifest() throws IOException {
    return getJarFile().getManifest();
  }

  public JarEntry getJarEntry() throws IOException {
    return getJarFile().getJarEntry(entryName);
  }

  public Attributes getAttributes() throws IOException {
    JarEntry e = getJarEntry();
    return e != null ? e.getAttributes() : null;
  }

  public Attributes getMainAttributes() throws IOException {
    Manifest man = getManifest();
    return man != null ? man.getMainAttributes() : null;
  }

  public Certificate[] getCertificates() throws IOException {
    JarEntry e = getJarEntry();
    return e != null ? e.getCertificates() : null;
  }
}
