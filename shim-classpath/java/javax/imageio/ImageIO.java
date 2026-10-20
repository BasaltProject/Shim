/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public final class ImageIO {
  private static boolean useCache = true;
  private static File cacheDirectory;

  private ImageIO() { }

  public static void scanForPlugins() { }

  public static void setUseCache(boolean useCache) {
    ImageIO.useCache = useCache;
  }

  public static boolean getUseCache() {
    return useCache;
  }

  public static void setCacheDirectory(File cacheDirectory) {
    if (cacheDirectory != null && !cacheDirectory.isDirectory()) {
      throw new IllegalArgumentException("Not a directory!");
    }
    ImageIO.cacheDirectory = cacheDirectory;
  }

  public static File getCacheDirectory() {
    return cacheDirectory;
  }

  public static String[] getReaderFormatNames() {
    return new String[] {"png"};
  }

  public static String[] getReaderFileSuffixes() {
    return new String[] {"png"};
  }

  public static String[] getReaderMIMETypes() {
    return new String[] {"image/png"};
  }

  public static String[] getWriterFormatNames() {
    return new String[] {"png"};
  }

  public static String[] getWriterFileSuffixes() {
    return new String[] {"png"};
  }

  public static String[] getWriterMIMETypes() {
    return new String[] {"image/png"};
  }

  public static BufferedImage read(File input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    if (!input.canRead()) {
      throw new IIOException("Can't read input file!");
    }
    try (InputStream in = new FileInputStream(input)) {
      return read(in);
    }
  }

  public static BufferedImage read(InputStream input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    byte[] all = input.readAllBytes();
    if (!shim.Png.isPng(all)) {
      return null;
    }
    return shim.Png.decode(all);
  }

  public static BufferedImage read(URL input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    InputStream in;
    try {
      in = input.openStream();
    } catch (IOException e) {
      throw new IIOException("Can't get input stream from URL!", e);
    }
    try {
      return read(in);
    } finally {
      in.close();
    }
  }

  public static boolean write(RenderedImage im, String formatName, OutputStream output) throws IOException {
    if (im == null) {
      throw new IllegalArgumentException("image == null!");
    }
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    if (!formatName.equalsIgnoreCase("png")) {
      return false;
    }
    output.write(shim.Png.encode(im));
    output.flush();
    return true;
  }

  public static boolean write(RenderedImage im, String formatName, File output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    if (im == null) {
      throw new IllegalArgumentException("image == null!");
    }
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    if (!formatName.equalsIgnoreCase("png")) {
      return false;
    }
    output.delete();
    try (OutputStream out = new FileOutputStream(output)) {
      return write(im, formatName, out);
    }
  }
}
