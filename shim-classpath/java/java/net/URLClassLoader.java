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

import java.io.File;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class URLClassLoader extends ClassLoader {

  private final File jarFile;

  public URLClassLoader(String name, URL[] urls, ClassLoader parent) {
    super(name, parent);
    if(urls.length != 1) {
      throw new UnsupportedOperationException();
    }
    if(!urls[0].getProtocol().equals("file")) {
      throw new UnsupportedOperationException(urls[0].getProtocol());
    }
    this.jarFile = new File(urls[0].getFile());
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

  public URL getResource(String path) {
    try {
      return new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + path);
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }


}