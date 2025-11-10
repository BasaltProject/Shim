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

public class URLClassLoader extends ClassLoader implements Closeable {

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
    try {
      InputStream stream = getResourceAsStream(name.replace(".", "/") + ".class");
      if(stream == null) {
        throw new ClassNotFoundException("couldn't find class " + name);
      }
      byte[] buf = new byte[2048];
      ByteArrayOutputStream mem = new ByteArrayOutputStream();
      try {
        int size;
        while((size = stream.read(buf, 0, buf.length)) > 0) {
          mem.write(buf, 0, size);
        }
        byte[] data = mem.toByteArray();
        return defineClass(name, data, 0, data.length);
      } finally {
        stream.close();
      }
    } catch(IOException e) {
      throw new ClassNotFoundException("couldn't find class " + name, e);
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
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(root)) {
          if (zf.getEntry(path) != null) {
            return new URL("jar:file:" + root.getAbsolutePath() + "!/" + path);
          }
        }
      }
    } catch (IOException ignored) {
    }
    return null;
  }

  @Override
  protected URL findResource(String path) {
    for (File root : roots) {
      URL u = find(root, path);
      if (u != null) {
        return u;
      }
    }
    return null;
  }

  @Override
  protected Enumeration<URL> findResources(String path) throws IOException {
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
