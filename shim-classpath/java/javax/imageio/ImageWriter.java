/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public abstract class ImageWriter implements ImageTranscoder {
  protected ImageWriterSpi originatingProvider = null;
  protected Object output = null;
  protected Locale[] availableLocales = null;
  protected Locale locale = null;
  protected List<IIOWriteWarningListener> warningListeners = null;
  protected List<Locale> warningLocales = null;
  protected List<IIOWriteProgressListener> progressListeners = null;

  private boolean abortFlag = false;

  protected ImageWriter(ImageWriterSpi originatingProvider) {
    this.originatingProvider = originatingProvider;
  }

  public ImageWriterSpi getOriginatingProvider() { return originatingProvider; }

  public void setOutput(Object output) {
    if (output != null) {
      ImageWriterSpi spi = getOriginatingProvider();
      if (spi != null) {
        Class<?>[] classes = spi.getOutputTypes();
        boolean found = false;
        for (int i = 0; i < classes.length; i++) {
          if (classes[i].isInstance(output)) {
            found = true;
            break;
          }
        }
        if (!found) {
          throw new IllegalArgumentException("Illegal output type!");
        }
      }
    }
    this.output = output;
  }

  public Object getOutput() { return output; }

  public Locale[] getAvailableLocales() { return availableLocales == null ? null : availableLocales.clone(); }

  public void setLocale(Locale locale) {
    if (locale != null) {
      Locale[] locales = getAvailableLocales();
      boolean found = false;
      if (locales != null) {
        for (int i = 0; i < locales.length; i++) {
          if (locale.equals(locales[i])) {
            found = true;
            break;
          }
        }
      }
      if (!found) {
        throw new IllegalArgumentException("Invalid locale!");
      }
    }
    this.locale = locale;
  }

  public Locale getLocale() { return locale; }

  public ImageWriteParam getDefaultWriteParam() { return new ImageWriteParam(getLocale()); }

  public abstract IIOMetadata getDefaultStreamMetadata(ImageWriteParam param);

  public abstract IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param);

  public abstract IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param);

  public abstract IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType,
      ImageWriteParam param);

  public int getNumThumbnailsSupported(ImageTypeSpecifier imageType, ImageWriteParam param,
      IIOMetadata streamMetadata, IIOMetadata imageMetadata) {
    return 0;
  }

  public Dimension[] getPreferredThumbnailSizes(ImageTypeSpecifier imageType, ImageWriteParam param,
      IIOMetadata streamMetadata, IIOMetadata imageMetadata) {
    return null;
  }

  public boolean canWriteRasters() { return false; }

  public abstract void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException;

  public void write(IIOImage image) throws IOException {
    write(null, image, null);
  }

  public void write(RenderedImage image) throws IOException {
    write(null, new IIOImage(image, null, null), null);
  }

  public boolean canWriteSequence() { return false; }

  public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
    unsupported();
  }

  public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public void endWriteSequence() throws IOException {
    unsupported();
  }

  public boolean canReplaceStreamMetadata() throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void replaceStreamMetadata(IIOMetadata streamMetadata) throws IOException {
    unsupported();
  }

  public boolean canReplaceImageMetadata(int imageIndex) throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void replaceImageMetadata(int imageIndex, IIOMetadata imageMetadata) throws IOException {
    unsupported();
  }

  public boolean canInsertImage(int imageIndex) throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void writeInsert(int imageIndex, IIOImage image, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public boolean canRemoveImage(int imageIndex) throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void removeImage(int imageIndex) throws IOException {
    unsupported();
  }

  public boolean canWriteEmpty() throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void prepareWriteEmpty(IIOMetadata streamMetadata, ImageTypeSpecifier imageType, int width, int height,
      IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public void endWriteEmpty() throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    throw new IllegalStateException("No call to prepareWriteEmpty!");
  }

  public boolean canInsertEmpty(int imageIndex) throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void prepareInsertEmpty(int imageIndex, ImageTypeSpecifier imageType, int width, int height,
      IIOMetadata imageMetadata, List<? extends BufferedImage> thumbnails, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public void endInsertEmpty() throws IOException {
    unsupported();
  }

  public boolean canReplacePixels(int imageIndex) throws IOException {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    return false;
  }

  public void prepareReplacePixels(int imageIndex, Rectangle region) throws IOException {
    unsupported();
  }

  public void replacePixels(RenderedImage image, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public void replacePixels(Raster raster, ImageWriteParam param) throws IOException {
    unsupported();
  }

  public void endReplacePixels() throws IOException {
    unsupported();
  }

  private void unsupported() {
    if (getOutput() == null) {
      throw new IllegalStateException("getOutput() == null!");
    }
    throw new UnsupportedOperationException("Unsupported write variant!");
  }

  public void abort() { this.abortFlag = true; }

  protected boolean abortRequested() { return this.abortFlag; }

  protected void clearAbortRequest() { this.abortFlag = false; }

  public void addIIOWriteWarningListener(IIOWriteWarningListener listener) {
    if (listener == null) {
      return;
    }
    if (warningListeners == null) {
      warningListeners = new ArrayList<>();
      warningLocales = new ArrayList<>();
    }
    warningListeners.add(listener);
    warningLocales.add(getLocale());
  }

  public void removeIIOWriteWarningListener(IIOWriteWarningListener listener) {
    if (listener == null || warningListeners == null) {
      return;
    }
    int index = warningListeners.indexOf(listener);
    if (index != -1) {
      warningListeners.remove(index);
      warningLocales.remove(index);
      if (warningListeners.size() == 0) {
        warningListeners = null;
        warningLocales = null;
      }
    }
  }

  public void removeAllIIOWriteWarningListeners() {
    warningListeners = null;
    warningLocales = null;
  }

  public void addIIOWriteProgressListener(IIOWriteProgressListener listener) {
    if (listener == null) {
      return;
    }
    if (progressListeners == null) {
      progressListeners = new ArrayList<>();
    }
    progressListeners.add(listener);
  }

  public void removeIIOWriteProgressListener(IIOWriteProgressListener listener) {
    if (listener == null || progressListeners == null) {
      return;
    }
    progressListeners.remove(listener);
    if (progressListeners.size() == 0) {
      progressListeners = null;
    }
  }

  public void removeAllIIOWriteProgressListeners() { progressListeners = null; }

  protected void processImageStarted(int imageIndex) {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.imageStarted(this, imageIndex);
    }
  }

  protected void processImageProgress(float percentageDone) {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.imageProgress(this, percentageDone);
    }
  }

  protected void processImageComplete() {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.imageComplete(this);
    }
  }

  protected void processThumbnailStarted(int imageIndex, int thumbnailIndex) {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.thumbnailStarted(this, imageIndex, thumbnailIndex);
    }
  }

  protected void processThumbnailProgress(float percentageDone) {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.thumbnailProgress(this, percentageDone);
    }
  }

  protected void processThumbnailComplete() {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.thumbnailComplete(this);
    }
  }

  protected void processWriteAborted() {
    if (progressListeners == null) {
      return;
    }
    for (IIOWriteProgressListener l : new ArrayList<>(progressListeners)) {
      l.writeAborted(this);
    }
  }

  protected void processWarningOccurred(int imageIndex, String warning) {
    if (warningListeners == null) {
      return;
    }
    if (warning == null) {
      throw new IllegalArgumentException("warning == null!");
    }
    for (IIOWriteWarningListener l : new ArrayList<>(warningListeners)) {
      l.warningOccurred(this, imageIndex, warning);
    }
  }

  protected void processWarningOccurred(int imageIndex, String baseName, String keyword) {
    processWarningOccurred(imageIndex, keyword);
  }

  public void reset() {
    setOutput(null);
    setLocale(null);
    removeAllIIOWriteWarningListeners();
    removeAllIIOWriteProgressListeners();
    clearAbortRequest();
  }

  public void dispose() { }
}
