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
      n = new IIOMetadataNode("NumChannels");
      n.setAttribute("value", Long.toString(intField(BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL,
          cs.equals("GRAY") ? 1 : cs.equals("CMYK") ? 4 : 3)));
      chroma.appendChild(n);
      if (v == 0 || v == 1) {
        n = new IIOMetadataNode("BlackIsZero");
        n.setAttribute("value", v == 1 ? "TRUE" : "FALSE");
        chroma.appendChild(n);
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
    int count = bps == null ? 1 : bps.getCount();
    for (int i = 0; i < count; i++) {
      int b = bps == null ? 1 : bps.getAsInt(i);
      if (i > 0) {
        bits.append(' ');
        msb.append(' ');
      }
      bits.append(b);
      msb.append(((b + 7) / 8) * 8 - 1);
    }
    n = new IIOMetadataNode("BitsPerSample");
    n.setAttribute("value", bits.toString());
    data.appendChild(n);
    n = new IIOMetadataNode("SampleMSB");
    n.setAttribute("value", msb.toString());
    data.appendChild(n);
    return data;
  }

  protected IIOMetadataNode getStandardDimensionNode() { return new IIOMetadataNode("Dimension"); }

  protected IIOMetadataNode getStandardDocumentNode() {
    IIOMetadataNode document = new IIOMetadataNode("Document");
    IIOMetadataNode n = new IIOMetadataNode("FormatVersion");
    n.setAttribute("value", "6.0");
    document.appendChild(n);
    return document;
  }

  protected IIOMetadataNode getStandardTransparencyNode() {
    IIOMetadataNode transparency = new IIOMetadataNode("Transparency");
    IIOMetadataNode n = new IIOMetadataNode("Alpha");
    n.setAttribute("value", "none");
    transparency.appendChild(n);
    return transparency;
  }
}
