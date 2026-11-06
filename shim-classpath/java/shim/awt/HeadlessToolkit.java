/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;

public final class HeadlessToolkit extends Toolkit {
  private EventQueue queue;
  private Clipboard clipboard;

  public HeadlessToolkit() {
    super();
  }

  protected EventQueue getSystemEventQueueImpl() {
    synchronized (this) {
      if (queue == null) {
        queue = new EventQueue();
      }
      return queue;
    }
  }

  public Dimension getScreenSize() throws HeadlessException {
    throw new HeadlessException();
  }

  public int getScreenResolution() throws HeadlessException {
    throw new HeadlessException();
  }

  public ColorModel getColorModel() throws HeadlessException {
    throw new HeadlessException();
  }

  public String[] getFontList() {
    return Fonts.logicalFamilies();
  }

  public FontMetrics getFontMetrics(Font font) {
    return new SimpleFontMetrics(font, new FontRenderContext(null, false, false));
  }

  public void sync() { }

  private static Image load(java.io.InputStream in) {
    try {
      BufferedImage img = ImageIO.read(in);
      if (img != null) {
        return img;
      }
    } catch (IOException e) {
      // fall through
    }
    return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
  }

  public Image getImage(String filename) {
    return createImage(filename);
  }

  public Image getImage(URL url) {
    return createImage(url);
  }

  public Image createImage(String filename) {
    try {
      return load(new java.io.FileInputStream(new File(filename)));
    } catch (IOException e) {
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
  }

  public Image createImage(URL url) {
    try {
      return load(url.openStream());
    } catch (IOException e) {
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }
  }

  public Image createImage(byte[] imagedata, int imageoffset, int imagelength) {
    return load(new ByteArrayInputStream(imagedata, imageoffset, imagelength));
  }

  public Image createImage(ImageProducer producer) {
    return ImageSupport.fromProducer(producer);
  }

  public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
    return true;
  }

  public int checkImage(Image image, int width, int height, ImageObserver observer) {
    return image instanceof java.awt.image.BufferedImage ? ImageObserver.ALLBITS
        : ImageObserver.ALLBITS | ImageObserver.WIDTH | ImageObserver.HEIGHT;
  }

  public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props) {
    if (frame == null) {
      throw new NullPointerException("frame must not be null");
    }
    throw new HeadlessException();
  }

  public boolean isAlwaysOnTopSupported() {
    return false;
  }

  public void beep() {
    System.out.write(0x07);
    System.out.flush();
  }

  public Clipboard getSystemClipboard() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
    return false;
  }

  public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType modalExclusionType) {
    return false;
  }

  public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException {
    throw new HeadlessException();
  }
}
