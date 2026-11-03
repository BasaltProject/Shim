/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.Image;
import java.util.Hashtable;

public class PixelGrabber implements ImageConsumer {
  ImageProducer producer;
  int dstX;
  int dstY;
  int dstW;
  int dstH;
  ColorModel imageModel;
  byte[] bytePixels;
  int[] intPixels;
  int dstOff;
  int dstScan;
  private boolean grabbing;
  private int flags;
  private boolean forceRGB;
  private int status;
  private int width = -1;
  private int height = -1;
  private static final int GRABBEDBITS = (ImageObserver.FRAMEBITS | ImageObserver.ALLBITS);
  private static final int DONEBITS = (GRABBEDBITS | ImageObserver.ERROR);

  public PixelGrabber(Image img, int x, int y, int w, int h, int[] pix, int off, int scansize) {
    this(img.getSource(), x, y, w, h, pix, off, scansize);
  }

  public PixelGrabber(ImageProducer ip, int x, int y, int w, int h, int[] pix, int off, int scansize) {
    producer = ip;
    dstX = x;
    dstY = y;
    dstW = w;
    dstH = h;
    dstOff = off;
    dstScan = scansize;
    intPixels = pix;
    imageModel = ColorModel.getRGBdefault();
  }

  public PixelGrabber(Image img, int x, int y, int w, int h, boolean forceRGB) {
    producer = img.getSource();
    dstX = x;
    dstY = y;
    dstW = w;
    dstH = h;
    this.forceRGB = forceRGB;
  }

  public synchronized void startGrabbing() {
    if ((flags & DONEBITS) != 0) {
      return;
    }
    if (!grabbing) {
      grabbing = true;
      flags &= ~(ImageObserver.ABORT);
      producer.startProduction(this);
    }
  }

  public synchronized void abortGrabbing() {
    imageComplete(IMAGEABORTED);
  }

  public boolean grabPixels() throws InterruptedException {
    return grabPixels(0);
  }

  public synchronized boolean grabPixels(long ms) throws InterruptedException {
    if ((flags & DONEBITS) != 0) {
      return (flags & GRABBEDBITS) != 0;
    }
    long end = ms + System.currentTimeMillis();
    if (!grabbing) {
      grabbing = true;
      flags &= ~(ImageObserver.ABORT);
      producer.startProduction(this);
    }
    while (grabbing) {
      long timeout;
      if (ms == 0) {
        timeout = 0;
      } else {
        timeout = end - System.currentTimeMillis();
        if (timeout <= 0) {
          break;
        }
      }
      wait(timeout);
    }
    return (flags & GRABBEDBITS) != 0;
  }

  public synchronized int getStatus() {
    return flags;
  }

  public synchronized int status() {
    return getStatus();
  }

  public synchronized int getWidth() {
    if (dstW < 0) {
      return -1;
    }
    return dstW;
  }

  public synchronized int getHeight() {
    if (dstH < 0) {
      return -1;
    }
    return dstH;
  }

  public synchronized Object getPixels() {
    return bytePixels == null ? ((Object) intPixels) : ((Object) bytePixels);
  }

  public synchronized ColorModel getColorModel() {
    return imageModel;
  }

  public void setDimensions(int width, int height) {
    if (dstW < 0) {
      dstW = width - dstX;
    }
    if (dstH < 0) {
      dstH = height - dstY;
    }
    if (dstW <= 0 || dstH <= 0) {
      imageComplete(STATICIMAGEDONE);
    } else if (intPixels == null && bytePixels == null) {
      intPixels = new int[dstW * dstH];
      dstScan = dstW;
      dstOff = 0;
    }
    this.width = width;
    this.height = height;
    flags |= (ImageObserver.WIDTH | ImageObserver.HEIGHT);
  }

  public void setHints(int hints) { }

  public void setProperties(Hashtable<?, ?> props) { }

  public void setColorModel(ColorModel model) { }

  public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, byte[] pixels, int srcOff,
      int srcScan) {
    for (int j = 0; j < srcH; j++) {
      int y = srcY + j;
      if (y < dstY || y >= dstY + dstH) {
        continue;
      }
      for (int i = 0; i < srcW; i++) {
        int x = srcX + i;
        if (x < dstX || x >= dstX + dstW) {
          continue;
        }
        int rgb = model.getRGB(pixels[srcOff + j * srcScan + i] & 0xff);
        intPixels[dstOff + (y - dstY) * dstScan + (x - dstX)] = rgb;
      }
    }
    flags |= ImageObserver.SOMEBITS;
  }

  public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, int[] pixels, int srcOff,
      int srcScan) {
    for (int j = 0; j < srcH; j++) {
      int y = srcY + j;
      if (y < dstY || y >= dstY + dstH) {
        continue;
      }
      for (int i = 0; i < srcW; i++) {
        int x = srcX + i;
        if (x < dstX || x >= dstX + dstW) {
          continue;
        }
        int rgb = model.getRGB(pixels[srcOff + j * srcScan + i]);
        intPixels[dstOff + (y - dstY) * dstScan + (x - dstX)] = rgb;
      }
    }
    flags |= ImageObserver.SOMEBITS;
  }

  public synchronized void imageComplete(int status) {
    grabbing = false;
    switch (status) {
    default:
    case IMAGEERROR:
      flags |= ImageObserver.ERROR | ImageObserver.ABORT;
      break;
    case IMAGEABORTED:
      flags |= ImageObserver.ABORT;
      break;
    case STATICIMAGEDONE:
      flags |= ImageObserver.ALLBITS;
      break;
    case SINGLEFRAMEDONE:
      flags |= ImageObserver.FRAMEBITS;
      break;
    }
    producer.removeConsumer(this);
    notifyAll();
  }
}
