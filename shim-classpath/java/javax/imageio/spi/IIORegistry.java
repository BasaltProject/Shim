/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public final class IIORegistry extends ServiceRegistry {
  private static IIORegistry instance;

  private IIORegistry() {
    super(Arrays.<Class<?>>asList(ImageReaderSpi.class, ImageWriterSpi.class, ImageTranscoderSpi.class,
        ImageInputStreamSpi.class, ImageOutputStreamSpi.class).iterator());
    registerStandardSpis();
    registerApplicationClasspathSpis();
  }

  public static synchronized IIORegistry getDefaultInstance() {
    if (instance == null) {
      instance = new IIORegistry();
    }
    return instance;
  }

  private void registerStandardSpis() {
    registerServiceProvider(new StreamSpi.FileIn());
    registerServiceProvider(new StreamSpi.StreamIn());
    registerServiceProvider(new StreamSpi.FileOut());
    registerServiceProvider(new StreamSpi.StreamOut());
    registerServiceProvider(new shim.imageio.PngSpi.Reader());
    registerServiceProvider(new shim.imageio.PngSpi.Writer());
    registerServiceProvider(new shim.imageio.BmpSpi.Reader());
    registerServiceProvider(new shim.imageio.BmpSpi.Writer());
    registerServiceProvider(new shim.imageio.GifSpi.Reader());
    registerServiceProvider(new shim.imageio.GifSpi.Writer());
    registerServiceProvider(new shim.imageio.TiffSpi.Reader());
    registerServiceProvider(new shim.imageio.TiffSpi.Writer());
    registerServiceProvider(new shim.imageio.WbmpSpi.Reader());
    registerServiceProvider(new shim.imageio.WbmpSpi.Writer());
    registerServiceProvider(new shim.imageio.JpegSpi.Reader());
    registerServiceProvider(new shim.imageio.JpegSpi.Writer());
  }

  public void registerApplicationClasspathSpis() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }
    for (Class<?> c : new Class<?>[] {ImageReaderSpi.class, ImageWriterSpi.class, ImageTranscoderSpi.class,
        ImageInputStreamSpi.class, ImageOutputStreamSpi.class}) {
      try {
        java.util.Iterator<?> it = lookupProviders(c, loader);
        while (it.hasNext()) {
          try {
            registerServiceProvider(it.next());
          } catch (Throwable t) {
            // ignore broken providers
          }
        }
      } catch (Throwable t) {
        // ignore missing service loader support
      }
    }
  }

  private static final class StreamSpi {
    static final class FileIn extends ImageInputStreamSpi {
      FileIn() {
        super("Oracle Corporation", "1.0", File.class);
      }

      public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileImageInputStream from a File";
      }

      public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir)
          throws IOException {
        if (input instanceof File) {
          return new FileImageInputStream((File) input);
        }
        throw new IllegalArgumentException();
      }
    }

    static final class StreamIn extends ImageInputStreamSpi {
      StreamIn() {
        super("Oracle Corporation", "1.0", InputStream.class);
      }

      public String getDescription(Locale locale) {
        return "Service provider that instantiates a cached ImageInputStream from an InputStream";
      }

      public boolean canUseCacheFile() {
        return true;
      }

      public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir)
          throws IOException {
        if (input instanceof InputStream) {
          return useCache ? new FileCacheImageInputStream((InputStream) input, cacheDir)
              : new MemoryCacheImageInputStream((InputStream) input);
        }
        throw new IllegalArgumentException();
      }
    }

    static final class FileOut extends ImageOutputStreamSpi {
      FileOut() {
        super("Oracle Corporation", "1.0", File.class);
      }

      public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileImageOutputStream from a File";
      }

      public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir)
          throws IOException {
        if (output instanceof File) {
          File f = (File) output;
          try {
            if (f.exists()) {
              f.delete();
            }
            return new FileImageOutputStream(f);
          } catch (Exception e) {
            throw new IIOExceptionBridge(e);
          }
        }
        throw new IllegalArgumentException();
      }
    }

    static final class StreamOut extends ImageOutputStreamSpi {
      StreamOut() {
        super("Oracle Corporation", "1.0", OutputStream.class);
      }

      public String getDescription(Locale locale) {
        return "Service provider that instantiates an OutputStreamImageOutputStream from an OutputStream";
      }

      public boolean canUseCacheFile() {
        return true;
      }

      public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir)
          throws IOException {
        if (output instanceof OutputStream) {
          return useCache ? new FileCacheImageOutputStream((OutputStream) output, cacheDir)
              : new MemoryCacheImageOutputStream((OutputStream) output);
        }
        throw new IllegalArgumentException();
      }
    }

    static final class IIOExceptionBridge extends IOException {
      IIOExceptionBridge(Throwable cause) {
        super(cause);
      }
    }
  }
}
