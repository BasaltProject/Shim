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

package java.net;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class URLClassLoader extends java.security.SecureClassLoader implements Closeable {

  private final List<URL> urls = new ArrayList<URL>();
  private final List<File> roots = new ArrayList<File>();

  public URLClassLoader(String name, URL[] urls, ClassLoader parent) {
    super(name, parent);
    for (URL u : urls) {
      addURL(u);
    }
  }

  public URLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    this(name, urls, parent);
  }

  public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    this(null, urls, parent, factory);
  }

  public URLClassLoader(URL[] urls, ClassLoader parent) {
    this(null, urls, parent);
  }

  public URLClassLoader(URL[] urls) {
    this(urls, getSystemClassLoader());
  }

  public static URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
    return new URLClassLoader(urls, parent);
  }

  public static URLClassLoader newInstance(final URL[] urls) {
    return new URLClassLoader(urls);
  }

  protected void addURL(URL url) {
    if (url == null) {
      throw new NullPointerException();
    }
    if (!url.getProtocol().equals("file")) {
      throw new UnsupportedOperationException(url.getProtocol());
    }
    urls.add(url);
    roots.add(new File(url.getFile()));
  }

  public URL[] getURLs() {
    return urls.toArray(new URL[0]);
  }

  public void close() throws IOException { }

  protected Class findClass(String name) throws ClassNotFoundException {
    String path = name.replace('.', '/') + ".class";
    try {
      for (File root : roots) {
        byte[] data = readBytes(root, path);
        if (data != null) {
          return defineClass(name, data, 0, data.length, codeSource(root));
        }
      }
    } catch (IOException e) {
      throw new ClassNotFoundException("couldn't find class " + name, e);
    }
    throw new ClassNotFoundException("couldn't find class " + name);
  }

  private static java.security.CodeSource codeSource(File root) {
    try {
      return new java.security.CodeSource(root.toURI().toURL(), (java.security.cert.Certificate[]) null);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private static byte[] readBytes(File root, String path) throws IOException {
    InputStream stream = null;
    try {
      if (root.isDirectory()) {
        File f = new File(root, path);
        if (!f.isFile()) {
          return null;
        }
        stream = new java.io.FileInputStream(f);
      } else if (root.isFile()) {
        java.util.jar.JarFile jar = shim.JarCache.open(root);
        java.util.zip.ZipEntry entry = jar.getEntry(path);
        if (entry == null) {
          return null;
        }
        stream = jar.getInputStream(entry);
        if (stream == null) {
          return null;
        }
      } else {
        return null;
      }
      ByteArrayOutputStream mem = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int size;
      while ((size = stream.read(buf, 0, buf.length)) > 0) {
        mem.write(buf, 0, size);
      }
      return mem.toByteArray();
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  private URL find(File root, String path) {
    try {
      if (root.isDirectory()) {
        File f = new File(root, path);
        if (f.exists()) {
          return f.toURI().toURL();
        }
      } else if (root.exists()) {
        if (shim.JarCache.open(root).getEntry(path) != null) {
          return new URL("jar:file:" + root.getAbsolutePath() + "!/" + path);
        }
      }
    } catch (IOException ignored) {
    }
    return null;
  }

  @Override
  public URL findResource(String path) {
    for (File root : roots) {
      URL u = find(root, path);
      if (u != null) {
        return u;
      }
    }
    return null;
  }

  @Override
  public Enumeration<URL> findResources(String path) throws IOException {
    List<URL> found = new ArrayList<URL>();
    for (File root : roots) {
      URL u = find(root, path);
      if (u != null) {
        found.add(u);
      }
    }
    return Collections.enumeration(found);
  }
}
