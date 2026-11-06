/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.imageio.plugins.tiff;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;
import org.w3c.dom.Node;

public class TIFFImageMetadata extends IIOMetadata {
  public static final String kNativeMetadataFormatName = "javax_imageio_tiff_image_1.0";
  public static final String kNativeMetadataFormatClassName = "com.sun.imageio.plugins.tiff.TIFFImageMetadataFormat";

  private TIFFDirectory root;

  public TIFFImageMetadata(TIFFDirectory root) {
    super(true, kNativeMetadataFormatName, kNativeMetadataFormatClassName, null, null);
    this.root = root;
  }

  public TIFFImageMetadata() {
    this(new TIFFDirectory(new TIFFTagSet[] {BaselineTIFFTagSet.getInstance()}, null));
  }

  public TIFFDirectory getRootIFD() { return root; }

  public boolean isReadOnly() { return false; }

  public Node getAsTree(String formatName) {
    if (formatName.equals(kNativeMetadataFormatName)) {
      IIOMetadataNode r = new IIOMetadataNode(kNativeMetadataFormatName);
      r.appendChild(directoryNode(root));
      return r;
    } else if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
      return getStandardTree();
    } else {
      throw new IllegalArgumentException("Not a recognized format!");
    }
  }

  private static IIOMetadataNode directoryNode(TIFFDirectory dir) {
    IIOMetadataNode ifd = new IIOMetadataNode("TIFFIFD");
    TIFFTagSet[] sets = dir.getTagSets();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sets.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(sets[i].getClass().getName());
    }
    ifd.setAttribute("tagSets", sb.toString());
    for (TIFFField f : dir.getTIFFFields()) {
      ifd.appendChild(f.getAsNativeNode());
    }
    return ifd;
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    if (formatName.equals(kNativeMetadataFormatName)) {
      if (root == null) {
        throw new NullPointerException("root == null!");
      }
      if (!root.getNodeName().equals(kNativeMetadataFormatName)) {
        throw new IIOInvalidTreeException("Root must be " + kNativeMetadataFormatName, root);
      }
      Node ifd = root.getFirstChild();
      if (ifd == null || !ifd.getNodeName().equals("TIFFIFD")) {
        throw new IIOInvalidTreeException("Root must have a TIFFIFD child", root);
      }
      TIFFTagSet[] sets = this.root.getTagSets();
      TIFFTagSet merged = sets.length > 0 ? sets[0] : null;
      for (Node n = ifd.getFirstChild(); n != null; n = n.getNextSibling()) {
        TIFFField field = null;
        for (TIFFTagSet s : sets) {
          try {
            field = TIFFField.createFromMetadataNode(s, n);
            if (s.getTag(field.getTagNumber()) != null) {
              break;
            }
          } catch (IllegalArgumentException e) {
            throw new IIOInvalidTreeException(e.getMessage(), e, n);
          }
        }
        if (field == null) {
          try {
            field = TIFFField.createFromMetadataNode(merged, n);
          } catch (IllegalArgumentException e) {
            throw new IIOInvalidTreeException(e.getMessage(), e, n);
          }
        }
        this.root.addTIFFField(field);
      }
    } else if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
      throw new IIOInvalidTreeException("Merging the standard format is not supported", root);
    } else {
      throw new IllegalArgumentException("Not a recognized format!");
    }
  }

  public void reset() { root.removeTIFFFields(); }

  private long intField(int tag, long def) {
    TIFFField f = root.getTIFFField(tag);
    return f == null ? def : f.getAsLong(0);
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    TIFFField pi = root.getTIFFField(BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION);
    if (pi != null) {
      int v = pi.getAsInt(0);
      String cs;
      switch (v) {
        case 0:
        case 1:
          cs = "GRAY";
          break;
        case 5:
          cs = "CMYK";
          break;
        case 6:
          cs = "YCbCr";
          break;
        case 8:
          cs = "Lab";
          break;
        default:
          cs = "RGB";
          break;
      }
      IIOMetadataNode n = new IIOMetadataNode("ColorSpaceType");
      n.setAttribute("name", cs);
      chroma.appendChild(n);
      n = new IIOMetadataNode("BlackIsZero");
      n.setAttribute("value", v == 0 ? "FALSE" : "TRUE");
      chroma.appendChild(n);
      n = new IIOMetadataNode("NumChannels");
      n.setAttribute("value", Long.toString(v == 3 ? 3 : intField(BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL,
          cs.equals("GRAY") ? 1 : cs.equals("CMYK") ? 4 : 3)));
      chroma.appendChild(n);
      TIFFField map = root.getTIFFField(BaselineTIFFTagSet.TAG_COLOR_MAP);
      if (v == 3 && map != null) {
        int size = map.getCount() / 3;
        IIOMetadataNode palette = new IIOMetadataNode("Palette");
        for (int i = 0; i < size; i++) {
          IIOMetadataNode e = new IIOMetadataNode("PaletteEntry");
          e.setAttribute("index", Integer.toString(i));
          e.setAttribute("red", Integer.toString(map.getAsInt(i) * 255 / 65535));
          e.setAttribute("green", Integer.toString(map.getAsInt(size + i) * 255 / 65535));
          e.setAttribute("blue", Integer.toString(map.getAsInt(2 * size + i) * 255 / 65535));
          palette.appendChild(e);
        }
        chroma.appendChild(palette);
      }
    }
    return chroma;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode compression = new IIOMetadataNode("Compression");
    TIFFField c = root.getTIFFField(BaselineTIFFTagSet.TAG_COMPRESSION);
    if (c != null) {
      int v = c.getAsInt(0);
      String name;
      boolean lossless = true;
      switch (v) {
        case 1:
          name = "None";
          break;
        case 2:
          name = "CCITT RLE";
          break;
        case 3:
          name = "CCITT T.4";
          break;
        case 4:
          name = "CCITT T.6";
          break;
        case 5:
          name = "LZW";
          break;
        case 6:
        case 7:
          name = "JPEG";
          lossless = false;
          break;
        case 8:
        case 32946:
          name = "ZLib";
          break;
        case 32773:
          name = "PackBits";
          break;
        default:
          name = null;
          break;
      }
      if (name != null) {
        IIOMetadataNode n = new IIOMetadataNode("CompressionTypeName");
        n.setAttribute("value", name);
        compression.appendChild(n);
        n = new IIOMetadataNode("Lossless");
        n.setAttribute("value", lossless ? "TRUE" : "FALSE");
        compression.appendChild(n);
      }
    }
    IIOMetadataNode n = new IIOMetadataNode("NumProgressiveScans");
    n.setAttribute("value", "1");
    compression.appendChild(n);
    return compression;
  }

  protected IIOMetadataNode getStandardDataNode() {
    IIOMetadataNode data = new IIOMetadataNode("Data");
    IIOMetadataNode n = new IIOMetadataNode("PlanarConfiguration");
    n.setAttribute("value", intField(BaselineTIFFTagSet.TAG_PLANAR_CONFIGURATION, 1) == 2 ? "PlaneInterleaved"
        : "PixelInterleaved");
    data.appendChild(n);
    TIFFField bps = root.getTIFFField(BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE);
    StringBuilder bits = new StringBuilder();
    StringBuilder msb = new StringBuilder();
    int count = intField(BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION, 1) == 3 ? 3
        : bps == null ? 1 : bps.getCount();
    for (int i = 0; i < count; i++) {
      int b = bps == null ? 1 : bps.getAsInt(Math.min(i, bps.getCount() - 1));
      if (i > 0) {
        bits.append(' ');
        msb.append(' ');
      }
      bits.append(b);
      msb.append(((b + 7) / 8) * 8 - 1);
    }
    n = new IIOMetadataNode("SampleFormat");
    n.setAttribute("value", intField(339, 1) == 3 ? "Real" : intField(339, 1) == 2 ? "SignedIntegral"
        : intField(262, 1) == 3 ? "Index" : "UnsignedIntegral");
    data.appendChild(n);
    n = new IIOMetadataNode("BitsPerSample");
    n.setAttribute("value", bits.toString());
    data.appendChild(n);
    n = new IIOMetadataNode("SampleMSB");
    n.setAttribute("value", msb.toString());
    data.appendChild(n);
    return data;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
    IIOMetadataNode n = new IIOMetadataNode("PixelAspectRatio");
    n.setAttribute("value", "1.0");
    dim.appendChild(n);
    return dim;
  }

  protected IIOMetadataNode getStandardDocumentNode() {
    IIOMetadataNode document = new IIOMetadataNode("Document");
    IIOMetadataNode n = new IIOMetadataNode("FormatVersion");
    n.setAttribute("value", "6.0");
    document.appendChild(n);
    return document;
  }

  protected IIOMetadataNode getStandardTextNode() {
    IIOMetadataNode text = null;
    for (int tag : new int[] {269, 270, 271, 272, 285, 305, 306, 315, 316, 33432}) {
      TIFFField f = root.getTIFFField(tag);
      if (f == null || f.getType() != 2) {
        continue;
      }
      for (int i = 0; i < f.getCount(); i++) {
        if (text == null) {
          text = new IIOMetadataNode("Text");
        }
        IIOMetadataNode e = new IIOMetadataNode("TextEntry");
        e.setAttribute("keyword", f.getTag().getName());
        e.setAttribute("value", f.getAsString(i));
        text.appendChild(e);
      }
    }
    return text;
  }

  protected IIOMetadataNode getStandardTransparencyNode() {
    IIOMetadataNode transparency = new IIOMetadataNode("Transparency");
    IIOMetadataNode n = new IIOMetadataNode("Alpha");
    long extra = intField(338, -1);
    n.setAttribute("value", extra == 1 ? "premultiplied" : extra == 2 ? "nonpremultiplied" : "none");
    transparency.appendChild(n);
    return transparency;
  }
}
