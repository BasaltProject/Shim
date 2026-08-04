/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;

public class ImageIcon implements javax.swing.Icon, java.io.Serializable, javax.accessibility.Accessible {
  transient Image image;
  transient int loadStatus = 0;
  ImageObserver imageObserver;
  String description = null;
  int width = -1;
  int height = -1;
  protected static final java.awt.Component component = null;
  protected static final java.awt.MediaTracker tracker = null;
  public ImageIcon(java.net.URL p0) {
    this.image = Toolkit.getDefaultToolkit().getImage(p0);
    this.description = p0.toExternalForm();
    loadImage(image);
  }
  public ImageIcon(java.awt.Image p0, java.lang.String p1) {
    this(p0);
    description = p1;
  }
  public ImageIcon() {
    
  }
  public ImageIcon(byte[] p0, java.lang.String p1) {
    this.image = Toolkit.getDefaultToolkit().createImage(p0);
    if (image == null) {
      return;
    }
    description = p1;
    loadImage(image);
  }
  public ImageIcon(byte[] p0) {
    this.image = Toolkit.getDefaultToolkit().createImage(p0);
    if (image == null) {
      return;
    }
    Object o = image.getProperty("comment", imageObserver);
    if (o instanceof String) {
      description = (String) o;
    }
    loadImage(image);
  }
  public ImageIcon(java.awt.Image p0) {
    this.image = p0;
    Object o = p0 == null ? null : p0.getProperty("comment", imageObserver);
    if (o instanceof String) {
      description = (String) o;
    }
    loadImage(image);
  }
  public ImageIcon(java.lang.String p0, java.lang.String p1) {
    this.image = Toolkit.getDefaultToolkit().getImage(p0);
    this.description = p1;
    loadImage(image);
  }
  public ImageIcon(java.lang.String p0) {
    this.image = Toolkit.getDefaultToolkit().getImage(p0);
    this.description = p0;
    loadImage(image);
  }
  public ImageIcon(java.net.URL p0, java.lang.String p1) {
    this.image = Toolkit.getDefaultToolkit().getImage(p0);
    this.description = p1;
    loadImage(image);
  }
  public java.lang.String toString() {
    if (description != null) {
      return description;
    }
    return super.toString();
  }
  public int getImageLoadStatus() {
    return loadStatus;
  }
  public void setDescription(java.lang.String p0) {
    description = p0;
  }
  public void setImageObserver(java.awt.image.ImageObserver p0) {
    imageObserver = p0;
  }
  public java.awt.image.ImageObserver getImageObserver() {
    return imageObserver;
  }
  public java.lang.String getDescription() {
    return description;
  }
  protected void loadImage(java.awt.Image p0) {
    if (p0 != null) {
      width = p0.getWidth(imageObserver);
      height = p0.getHeight(imageObserver);
      loadStatus = MediaTracker.COMPLETE;
    }
  }
  public void setImage(java.awt.Image p0) {
    image = p0;
    loadImage(p0);
  }
  public void paintIcon(java.awt.Component p0, java.awt.Graphics p1, int p2, int p3) {
    if (imageObserver == null) {
      p1.drawImage(image, p2, p3, p0);
    } else {
      p1.drawImage(image, p2, p3, imageObserver);
    }
  }
  public int getIconWidth() {
    return width;
  }
  public int getIconHeight() {
    return height;
  }
  public java.awt.Image getImage() {
    return image;
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    return null;
  }
}
