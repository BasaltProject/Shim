/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image.renderable;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.ArrayList;

public class RenderableImageProducer implements ImageProducer, Runnable {
  private RenderableImage rdblImage;
  private RenderContext rc;
  private ArrayList<ImageConsumer> ics = new ArrayList<>();

  public RenderableImageProducer(RenderableImage rdblImage, RenderContext rc) {
    this.rdblImage = rdblImage;
    this.rc = rc;
  }

  public synchronized void setRenderContext(RenderContext rc) { this.rc = rc; }

  public synchronized void addConsumer(ImageConsumer ic) {
    if (!ics.contains(ic)) {
      ics.add(ic);
    }
  }

  public synchronized boolean isConsumer(ImageConsumer ic) { return ics.contains(ic); }

  public synchronized void removeConsumer(ImageConsumer ic) { ics.remove(ic); }

  public synchronized void startProduction(ImageConsumer ic) {
    addConsumer(ic);
    Thread thread = new Thread(this, "RenderableImageProducer Thread");
    thread.start();
  }

  public void requestTopDownLeftRightResend(ImageConsumer ic) { }

  public void run() {
    RenderedImage rdrdImage;
    if (rc != null) {
      rdrdImage = rdblImage.createRendering(rc);
    } else {
      rdrdImage = rdblImage.createDefaultRendering();
    }
    ColorModel colorModel = rdrdImage.getColorModel();
    Raster raster = rdrdImage.getData();
    java.awt.image.SampleModel sampleModel = raster.getSampleModel();
    java.awt.image.DataBuffer dataBuffer = raster.getDataBuffer();
    if (colorModel == null) {
      colorModel = ColorModel.getRGBdefault();
    }
    int minX = raster.getMinX();
    int minY = raster.getMinY();
    int width = raster.getWidth();
    int height = raster.getHeight();
    ImageConsumer[] copy;
    synchronized (this) {
      copy = ics.toArray(new ImageConsumer[0]);
    }
    for (ImageConsumer ic : copy) {
      ic.setDimensions(width, height);
      ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES | ImageConsumer.SINGLEPASS
          | ImageConsumer.SINGLEFRAME);
    }
    int[] pix = new int[width];
    for (int y = 0; y < height; y++) {
      int[] samples = new int[width * sampleModel.getNumBands()];
      raster.getPixels(minX, minY + y, width, 1, samples);
      int nb = sampleModel.getNumBands();
      for (int x = 0; x < width; x++) {
        Object o = raster.getDataElements(minX + x, minY + y, null);
        pix[x] = colorModel.getRGB(o);
      }
      for (ImageConsumer ic : copy) {
        ic.setPixels(0, y, width, 1, ColorModel.getRGBdefault(), pix, 0, width);
      }
    }
    for (ImageConsumer ic : copy) {
      ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }
  }
}
