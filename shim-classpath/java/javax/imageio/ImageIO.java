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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public final class ImageIO {
  private static boolean useCache = true;
  private static File cacheDirectory;

  private ImageIO() { }

  private static IIORegistry registry() {
    return IIORegistry.getDefaultInstance();
  }

  public static void scanForPlugins() {
    registry().registerApplicationClasspathSpis();
  }

  public static void setUseCache(boolean useCache) {
    ImageIO.useCache = useCache;
  }

  public static boolean getUseCache() {
    return useCache;
  }

  public static void setCacheDirectory(File cacheDirectory) {
    if ((cacheDirectory != null) && !(cacheDirectory.isDirectory())) {
      throw new IllegalArgumentException("Not a directory!");
    }
    ImageIO.cacheDirectory = cacheDirectory;
  }

  public static File getCacheDirectory() {
    return cacheDirectory;
  }

  public static ImageInputStream createImageInputStream(Object input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    Iterator<ImageInputStreamSpi> iter = registry().getServiceProviders(ImageInputStreamSpi.class, true);
    while (iter.hasNext()) {
      ImageInputStreamSpi spi = iter.next();
      if (spi.getInputClass().isInstance(input)) {
        try {
          return spi.createInputStreamInstance(input, getUseCache(), getCacheDirectory());
        } catch (IOException e) {
          throw new IIOException("Can't create cache file!", e);
        }
      }
    }
    return null;
  }

  public static ImageOutputStream createImageOutputStream(Object output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    Iterator<ImageOutputStreamSpi> iter = registry().getServiceProviders(ImageOutputStreamSpi.class, true);
    while (iter.hasNext()) {
      ImageOutputStreamSpi spi = iter.next();
      if (spi.getOutputClass().isInstance(output)) {
        try {
          return spi.createOutputStreamInstance(output, getUseCache(), getCacheDirectory());
        } catch (IOException e) {
          throw new IIOException("Can't create cache file!", e);
        }
      }
    }
    return null;
  }

  private static String[] toStringArray(Set<String> s) {
    return s.toArray(new String[0]);
  }

  public static String[] getReaderFormatNames() {
    Set<String> s = new HashSet<>();
    Iterator<ImageReaderSpi> iter = registry().getServiceProviders(ImageReaderSpi.class, true);
    while (iter.hasNext()) {
      s.addAll(Arrays.asList(iter.next().getFormatNames()));
    }
    return toStringArray(s);
  }

  public static String[] getReaderMIMETypes() {
    Set<String> s = new HashSet<>();
    Iterator<ImageReaderSpi> iter = registry().getServiceProviders(ImageReaderSpi.class, true);
    while (iter.hasNext()) {
      String[] m = iter.next().getMIMETypes();
      if (m != null) {
        s.addAll(Arrays.asList(m));
      }
    }
    return toStringArray(s);
  }

  public static String[] getReaderFileSuffixes() {
    Set<String> s = new HashSet<>();
    Iterator<ImageReaderSpi> iter = registry().getServiceProviders(ImageReaderSpi.class, true);
    while (iter.hasNext()) {
      String[] m = iter.next().getFileSuffixes();
      if (m != null) {
        s.addAll(Arrays.asList(m));
      }
    }
    return toStringArray(s);
  }

  public static String[] getWriterFormatNames() {
    Set<String> s = new HashSet<>();
    Iterator<ImageWriterSpi> iter = registry().getServiceProviders(ImageWriterSpi.class, true);
    while (iter.hasNext()) {
      s.addAll(Arrays.asList(iter.next().getFormatNames()));
    }
    return toStringArray(s);
  }

  public static String[] getWriterMIMETypes() {
    Set<String> s = new HashSet<>();
    Iterator<ImageWriterSpi> iter = registry().getServiceProviders(ImageWriterSpi.class, true);
    while (iter.hasNext()) {
      String[] m = iter.next().getMIMETypes();
      if (m != null) {
        s.addAll(Arrays.asList(m));
      }
    }
    return toStringArray(s);
  }

  public static String[] getWriterFileSuffixes() {
    Set<String> s = new HashSet<>();
    Iterator<ImageWriterSpi> iter = registry().getServiceProviders(ImageWriterSpi.class, true);
    while (iter.hasNext()) {
      String[] m = iter.next().getFileSuffixes();
      if (m != null) {
        s.addAll(Arrays.asList(m));
      }
    }
    return toStringArray(s);
  }

  private static boolean contains(String[] names, String name) {
    if (names == null) {
      return false;
    }
    for (String n : names) {
      if (name.equalsIgnoreCase(n)) {
        return true;
      }
    }
    return false;
  }

  public static Iterator<ImageReader> getImageReaders(Object input) {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    final Object in = input;
    Iterator<ImageReaderSpi> iter = registry().getServiceProviders(ImageReaderSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        try {
          return ((ImageReaderSpi) provider).canDecodeInput(in);
        } catch (IOException e) {
          return false;
        }
      }
    }, true);
    return new ReaderIterator(iter);
  }

  public static Iterator<ImageReader> getImageReadersByFormatName(String formatName) {
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    final String f = formatName;
    return new ReaderIterator(registry().getServiceProviders(ImageReaderSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageReaderSpi) provider).getFormatNames(), f);
      }
    }, true));
  }

  public static Iterator<ImageReader> getImageReadersBySuffix(String fileSuffix) {
    if (fileSuffix == null) {
      throw new IllegalArgumentException("fileSuffix == null!");
    }
    final String f = fileSuffix;
    return new ReaderIterator(registry().getServiceProviders(ImageReaderSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageReaderSpi) provider).getFileSuffixes(), f);
      }
    }, true));
  }

  public static Iterator<ImageReader> getImageReadersByMIMEType(String MIMEType) {
    if (MIMEType == null) {
      throw new IllegalArgumentException("MIMEType == null!");
    }
    final String f = MIMEType;
    return new ReaderIterator(registry().getServiceProviders(ImageReaderSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageReaderSpi) provider).getMIMETypes(), f);
      }
    }, true));
  }

  public static ImageReader getImageReader(ImageWriter writer) {
    if (writer == null) {
      throw new IllegalArgumentException("writer == null!");
    }
    ImageWriterSpi spi = writer.getOriginatingProvider();
    if (spi == null) {
      return null;
    }
    String[] names = spi.getImageReaderSpiNames();
    if (names == null) {
      return null;
    }
    Iterator<ImageReaderSpi> iter = registry().getServiceProviders(ImageReaderSpi.class, true);
    while (iter.hasNext()) {
      ImageReaderSpi r = iter.next();
      for (String n : names) {
        if (r.getClass().getName().equals(n)) {
          try {
            return r.createReaderInstance();
          } catch (IOException e) {
            return null;
          }
        }
      }
    }
    return null;
  }

  public static Iterator<ImageWriter> getImageWritersByFormatName(String formatName) {
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    final String f = formatName;
    return new WriterIterator(registry().getServiceProviders(ImageWriterSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageWriterSpi) provider).getFormatNames(), f);
      }
    }, true));
  }

  public static Iterator<ImageWriter> getImageWritersBySuffix(String fileSuffix) {
    if (fileSuffix == null) {
      throw new IllegalArgumentException("fileSuffix == null!");
    }
    final String f = fileSuffix;
    return new WriterIterator(registry().getServiceProviders(ImageWriterSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageWriterSpi) provider).getFileSuffixes(), f);
      }
    }, true));
  }

  public static Iterator<ImageWriter> getImageWritersByMIMEType(String MIMEType) {
    if (MIMEType == null) {
      throw new IllegalArgumentException("MIMEType == null!");
    }
    final String f = MIMEType;
    return new WriterIterator(registry().getServiceProviders(ImageWriterSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        return contains(((ImageWriterSpi) provider).getMIMETypes(), f);
      }
    }, true));
  }

  public static ImageWriter getImageWriter(ImageReader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("reader == null!");
    }
    ImageReaderSpi spi = reader.getOriginatingProvider();
    if (spi == null) {
      return null;
    }
    String[] names = spi.getImageWriterSpiNames();
    if (names == null) {
      return null;
    }
    Iterator<ImageWriterSpi> iter = registry().getServiceProviders(ImageWriterSpi.class, true);
    while (iter.hasNext()) {
      ImageWriterSpi w = iter.next();
      for (String n : names) {
        if (w.getClass().getName().equals(n)) {
          try {
            return w.createWriterInstance();
          } catch (IOException e) {
            return null;
          }
        }
      }
    }
    return null;
  }

  public static Iterator<ImageWriter> getImageWriters(ImageTypeSpecifier type, String formatName) {
    if (type == null) {
      throw new IllegalArgumentException("type == null!");
    }
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    final String f = formatName;
    final ImageTypeSpecifier t = type;
    return new WriterIterator(registry().getServiceProviders(ImageWriterSpi.class, new ServiceRegistry.Filter() {
      public boolean filter(Object provider) {
        ImageWriterSpi spi = (ImageWriterSpi) provider;
        return contains(spi.getFormatNames(), f) && spi.canEncodeImage(t);
      }
    }, true));
  }

  public static Iterator<ImageTranscoder> getImageTranscoders(ImageReader reader, ImageWriter writer) {
    if (reader == null) {
      throw new IllegalArgumentException("reader == null!");
    }
    if (writer == null) {
      throw new IllegalArgumentException("writer == null!");
    }
    List<ImageTranscoder> out = new ArrayList<>();
    Iterator<ImageTranscoderSpi> iter = registry().getServiceProviders(ImageTranscoderSpi.class, true);
    while (iter.hasNext()) {
      ImageTranscoderSpi spi = iter.next();
      if (spi.getReaderServiceProviderName().equals(reader.getOriginatingProvider().getClass().getName())
          && spi.getWriterServiceProviderName().equals(writer.getOriginatingProvider().getClass().getName())) {
        out.add(spi.createTranscoderInstance());
      }
    }
    return out.iterator();
  }

  private static final class ReaderIterator implements Iterator<ImageReader> {
    private final Iterator<ImageReaderSpi> iter;

    ReaderIterator(Iterator<ImageReaderSpi> iter) {
      this.iter = iter;
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public ImageReader next() {
      ImageReaderSpi spi = iter.next();
      try {
        return spi.createReaderInstance();
      } catch (IOException e) {
        return null;
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static final class WriterIterator implements Iterator<ImageWriter> {
    private final Iterator<ImageWriterSpi> iter;

    WriterIterator(Iterator<ImageWriterSpi> iter) {
      this.iter = iter;
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public ImageWriter next() {
      ImageWriterSpi spi = iter.next();
      try {
        return spi.createWriterInstance();
      } catch (IOException e) {
        return null;
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static BufferedImage read(File input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    if (!input.canRead()) {
      throw new IIOException("Can't read input file!");
    }
    ImageInputStream stream = createImageInputStream(input);
    if (stream == null) {
      throw new IIOException("Can't create an ImageInputStream!");
    }
    BufferedImage bi = read(stream);
    if (bi == null) {
      stream.close();
    }
    return bi;
  }

  public static BufferedImage read(InputStream input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    ImageInputStream stream = createImageInputStream(input);
    BufferedImage bi = read(stream);
    if (bi == null && stream != null) {
      stream.close();
    }
    return bi;
  }

  public static BufferedImage read(URL input) throws IOException {
    if (input == null) {
      throw new IllegalArgumentException("input == null!");
    }
    InputStream istream = null;
    try {
      istream = input.openStream();
    } catch (IOException e) {
      throw new IIOException("Can't get input stream from URL!", e);
    }
    ImageInputStream stream = createImageInputStream(istream);
    BufferedImage bi;
    try {
      bi = read(stream);
      if (bi == null) {
        stream.close();
      }
    } finally {
      istream.close();
    }
    return bi;
  }

  public static BufferedImage read(ImageInputStream stream) throws IOException {
    if (stream == null) {
      throw new IllegalArgumentException("stream == null!");
    }
    Iterator<ImageReader> iter = getImageReaders(stream);
    if (!iter.hasNext()) {
      return null;
    }
    ImageReader reader = iter.next();
    ImageReadParam param = reader.getDefaultReadParam();
    reader.setInput(stream, true, true);
    BufferedImage bi;
    try {
      bi = reader.read(0, param);
    } finally {
      reader.dispose();
      stream.close();
    }
    return bi;
  }

  public static boolean write(RenderedImage im, String formatName, ImageOutputStream output) throws IOException {
    if (im == null) {
      throw new IllegalArgumentException("im == null!");
    }
    if (formatName == null) {
      throw new IllegalArgumentException("formatName == null!");
    }
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    return doWrite(im, getWriter(im, formatName), output);
  }

  public static boolean write(RenderedImage im, String formatName, File output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    ImageOutputStream stream = null;
    ImageWriter writer = getWriter(im, formatName);
    if (writer == null) {
      return false;
    }
    try {
      output.delete();
      stream = createImageOutputStream(output);
    } catch (IOException e) {
      throw new IIOException("Can't create output stream!", e);
    }
    try {
      return doWrite(im, writer, stream);
    } finally {
      stream.close();
    }
  }

  public static boolean write(RenderedImage im, String formatName, OutputStream output) throws IOException {
    if (output == null) {
      throw new IllegalArgumentException("output == null!");
    }
    ImageOutputStream stream = null;
    try {
      stream = createImageOutputStream(output);
    } catch (IOException e) {
      throw new IIOException("Can't create output stream!", e);
    }
    try {
      return doWrite(im, getWriter(im, formatName), stream);
    } finally {
      stream.close();
    }
  }

  private static ImageWriter getWriter(RenderedImage im, String formatName) {
    ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(im);
    Iterator<ImageWriter> iter = getImageWriters(type, formatName);
    if (iter.hasNext()) {
      return iter.next();
    }
    return null;
  }

  private static boolean doWrite(RenderedImage im, ImageWriter writer, ImageOutputStream output) throws IOException {
    if (writer == null) {
      return false;
    }
    writer.setOutput(output);
    try {
      writer.write(im);
    } finally {
      writer.dispose();
      output.flush();
    }
    return true;
  }
}
