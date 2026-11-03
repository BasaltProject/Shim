/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.util.Hashtable;

public class ImageFilter implements ImageConsumer, Cloneable {
  protected ImageConsumer consumer;

  public ImageFilter() { }

  public ImageFilter getFilterInstance(ImageConsumer ic) {
    ImageFilter instance = (ImageFilter) clone();
    instance.consumer = ic;
    return instance;
  }

  public void setDimensions(int width, int height) {
    consumer.setDimensions(width, height);
  }

  public void setProperties(Hashtable<?, ?> props) {
    Hashtable<Object, Object> p = new Hashtable<>();
    if (props != null) {
      p.putAll(props);
    }
    p.put("filterpriority", Integer.valueOf(0));
    consumer.setProperties(p);
  }

  public void setColorModel(ColorModel model) {
    consumer.setColorModel(model);
  }

  public void setHints(int hints) {
    consumer.setHints(hints);
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
    consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
    consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
  }

  public void imageComplete(int status) {
    consumer.imageComplete(status);
  }

  public void resendTopDownLeftRight(ImageProducer ip) {
    ip.requestTopDownLeftRightResend(this);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
}
