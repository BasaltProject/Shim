/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class BufferCapabilities implements Cloneable {
  private ImageCapabilities frontBufferCaps;
  private ImageCapabilities backBufferCaps;
  private FlipContents flipContents;

  public BufferCapabilities(ImageCapabilities frontCaps, ImageCapabilities backCaps, FlipContents flipContents) {
    if (frontCaps == null || backCaps == null) {
      throw new IllegalArgumentException("Image capabilities specified cannot be null");
    }
    this.frontBufferCaps = frontCaps;
    this.backBufferCaps = backCaps;
    this.flipContents = flipContents;
  }

  public ImageCapabilities getFrontBufferCapabilities() { return frontBufferCaps; }

  public ImageCapabilities getBackBufferCapabilities() { return backBufferCaps; }

  public boolean isPageFlipping() { return (getFlipContents() != null); }

  public FlipContents getFlipContents() { return flipContents; }

  public boolean isFullScreenRequired() { return false; }

  public boolean isMultiBufferAvailable() { return false; }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public static final class FlipContents extends AttributeValue {
    private static final String[] kNames = {"undefined", "background", "prior", "copied"};

    public static final FlipContents UNDEFINED = new FlipContents(0);
    public static final FlipContents BACKGROUND = new FlipContents(1);
    public static final FlipContents PRIOR = new FlipContents(2);
    public static final FlipContents COPIED = new FlipContents(3);

    private FlipContents(int type) { super(type, kNames); }
  }
}
