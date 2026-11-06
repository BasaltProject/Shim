/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ColorConvertOp implements BufferedImageOp, RasterOp {
  ICC_Profile[] profileList;
  ColorSpace[] CSList;
  RenderingHints hints;
  boolean gotProfiles;

  public ColorConvertOp(RenderingHints hints) {
    profileList = new ICC_Profile[0];
    this.hints = hints;
  }

  public ColorConvertOp(ColorSpace cspace, RenderingHints hints) {
    if (cspace == null) {
      throw new NullPointerException("ColorSpace cannot be null");
    }
    if (cspace instanceof java.awt.color.ICC_ColorSpace) {
      profileList = new ICC_Profile[1];
      profileList[0] = ((java.awt.color.ICC_ColorSpace) cspace).getProfile();
    } else {
      CSList = new ColorSpace[1];
      CSList[0] = cspace;
    }
    this.hints = hints;
  }

  public ColorConvertOp(ColorSpace srcCspace, ColorSpace dstCspace, RenderingHints hints) {
    if ((srcCspace == null) || (dstCspace == null)) {
      throw new NullPointerException("ColorSpaces cannot be null");
    }
    if ((srcCspace instanceof java.awt.color.ICC_ColorSpace) && (dstCspace instanceof java.awt.color.ICC_ColorSpace)) {
      profileList = new ICC_Profile[2];
      profileList[0] = ((java.awt.color.ICC_ColorSpace) srcCspace).getProfile();
      profileList[1] = ((java.awt.color.ICC_ColorSpace) dstCspace).getProfile();
    } else {
      CSList = new ColorSpace[2];
      CSList[0] = srcCspace;
      CSList[1] = dstCspace;
    }
    this.hints = hints;
  }

  public ColorConvertOp(ICC_Profile[] profiles, RenderingHints hints) {
    if (profiles == null) {
      throw new NullPointerException("Profiles cannot be null");
    }
    gotProfiles = true;
    profileList = new ICC_Profile[profiles.length];
    for (int i1 = 0; i1 < profiles.length; i1++) {
      profileList[i1] = profiles[i1];
    }
    this.hints = hints;
  }

  public final ICC_Profile[] getICC_Profiles() {
    if (gotProfiles) {
      ICC_Profile[] profiles = new ICC_Profile[profileList.length];
      for (int i1 = 0; i1 < profileList.length; i1++) {
        profiles[i1] = profileList[i1];
      }
      return profiles;
    }
    return null;
  }

  private ColorSpace destSpace() {
    if (CSList != null && CSList.length > 0) {
      return CSList[CSList.length - 1];
    }
    if (profileList != null && profileList.length > 0) {
      return new java.awt.color.ICC_ColorSpace(profileList[profileList.length - 1]);
    }
    return null;
  }

  public final BufferedImage filter(BufferedImage src, BufferedImage dst) {
    BufferedImage out = dst;
    if (out == null) {
      ColorSpace cs = destSpace();
      if (cs == null) {
        throw new IllegalArgumentException("Destination ColorSpace is undefined");
      }
      ColorModel cm = new ComponentColorModel(cs, src.getColorModel().hasAlpha(), false,
          src.getColorModel().hasAlpha() ? java.awt.Transparency.TRANSLUCENT : java.awt.Transparency.OPAQUE,
          DataBuffer.TYPE_BYTE);
      out = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), false, null);
    }
    ColorSpace scs = src.getColorModel().getColorSpace();
    ColorSpace dcs = out.getColorModel().getColorSpace();
    if (CSList != null && CSList.length == 2) {
      scs = CSList[0];
      dcs = CSList[1];
    }
    if (dcs.getType() == ColorSpace.TYPE_GRAY && dcs.getNumComponents() == 1
        && out.getColorModel().getNumComponents() == 1) {
      ColorModel dm = out.getColorModel();
      float[] lin = new float[256];
      for (int i = 0; i < 256; i++) {
        float c = i / 255f;
        lin[i] = c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055) / 1.055, 2.4);
      }
      float[] norm = new float[1];
      for (int y = 0; y < src.getHeight(); y++) {
        for (int x = 0; x < src.getWidth(); x++) {
          int p = src.getRGB(x, y);
          double lum = 0.2225045 * lin[(p >> 16) & 0xff] + 0.7168786 * lin[(p >> 8) & 0xff]
              + 0.0606169 * lin[p & 0xff];
          norm[0] = (float) Math.min(1.0, lum);
          out.getRaster().setDataElements(x, y, dm.getDataElements(norm, 0, null));
        }
      }
      return out;
    }
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        out.setRGB(x, y, src.getRGB(x, y));
      }
    }
    return out;
  }

  public final WritableRaster filter(Raster src, WritableRaster dst) {
    if (dst == null) {
      dst = createCompatibleDestRaster(src);
    }
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        for (int b = 0; b < Math.min(src.getNumBands(), dst.getNumBands()); b++) {
          dst.setSample(dst.getMinX() + x, dst.getMinY() + y, b, src.getSample(src.getMinX() + x, src.getMinY() + y, b));
        }
      }
    }
    return dst;
  }

  public final Rectangle2D getBounds2D(BufferedImage src) { return getBounds2D(src.getRaster()); }

  public final Rectangle2D getBounds2D(Raster src) { return src.getBounds(); }

  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
    ColorModel cm = destCM;
    if (cm == null) {
      ColorSpace cs = destSpace();
      if (cs == null) {
        cm = ColorModel.getRGBdefault();
      } else {
        cm = new ComponentColorModel(cs, src.getColorModel().hasAlpha(), false,
            src.getColorModel().hasAlpha() ? java.awt.Transparency.TRANSLUCENT : java.awt.Transparency.OPAQUE,
            DataBuffer.TYPE_BYTE);
      }
    }
    return new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
        cm.isAlphaPremultiplied(), null);
  }

  public WritableRaster createCompatibleDestRaster(Raster src) {
    return src.createCompatibleWritableRaster();
  }

  public final Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return shim.awt.OpSupport.point(srcPt, dstPt); }

  public final RenderingHints getRenderingHints() { return hints; }
}
