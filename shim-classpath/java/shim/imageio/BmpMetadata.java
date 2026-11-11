/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public final class BmpMetadata extends IIOMetadata {
  public static final String kFormat = "javax_imageio_bmp_1.0";
  private static final String[] kCompressions = {"BI_RGB", "BI_RLE8", "BI_RLE4", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
  private String version;
  private int width;
  private int height;
  private int bits;
  private int compression;
  private int imageSize;
  private int xPels;
  private int yPels;
  private int colorsUsed;
  private int colorsImportant;
  private byte[] palette;
  private int paletteEntrySize;
  private int[] masks;

  public BmpMetadata(ImageInputStream in) throws IOException {
    super(true, kFormat, null, null, null);
    in.seek(0);
    ByteOrder old = in.getByteOrder();
    in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    try {
      in.skipBytes(14);
      long headerSize = in.readUnsignedInt();
      if (headerSize == 12) {
        version = "BMP v. 2.x";
        width = in.readUnsignedShort();
        height = in.readUnsignedShort();
        in.readUnsignedShort();
        bits = in.readUnsignedShort();
        paletteEntrySize = 3;
      } else {
        version = headerSize == 40 ? "BMP v. 3.x" : headerSize == 108 ? "BMP v. 4.x" : headerSize == 124
            ? "BMP v. 5.x" : "BMP v. 3.x";
        width = in.readInt();
        height = in.readInt();
        in.readUnsignedShort();
        bits = in.readUnsignedShort();
        compression = (int) in.readUnsignedInt();
        imageSize = (int) in.readUnsignedInt();
        xPels = in.readInt();
        yPels = in.readInt();
        colorsUsed = (int) in.readUnsignedInt();
        colorsImportant = (int) in.readUnsignedInt();
        paletteEntrySize = 4;
        if (compression == 3 && headerSize == 40) {
          masks = new int[] {in.readInt(), in.readInt(), in.readInt()};
        } else if (headerSize >= 52 && compression == 3) {
          masks = new int[] {in.readInt(), in.readInt(), in.readInt()};
        }
      }
      if (bits <= 8) {
        int n = colorsUsed != 0 ? colorsUsed : 1 << bits;
        in.seek(14 + headerSize);
        palette = new byte[n * 4];
        for (int i = 0; i < n; i++) {
          byte b = in.readByte();
          byte g = in.readByte();
          byte r = in.readByte();
          byte a = paletteEntrySize == 4 ? in.readByte() : (byte) 0xff;
          palette[4 * i] = b;
          palette[4 * i + 1] = g;
          palette[4 * i + 2] = r;
          palette[4 * i + 3] = a;
        }
      }
    } finally {
      in.setByteOrder(old);
    }
  }

  private boolean fresh;

  public BmpMetadata(int bitsPerPixel) {
    super(true, kFormat, null, null, null);
    version = "BMP v. 3.x";
    bits = bitsPerPixel;
    fresh = true;
  }

  public boolean isReadOnly() { return true; }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (kFormat.equals(formatName)) {
      return nativeTree();
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  private static void add(IIOMetadataNode parent, String name, Object value) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    if (value != null) {
      n.setUserObject(value);
      n.setNodeValue(value.toString());
    }
    parent.appendChild(n);
  }

  private Node nativeTree() {
    IIOMetadataNode root = new IIOMetadataNode(kFormat);
    add(root, "BMPVersion", version);
    add(root, "Width", Integer.valueOf(width));
    add(root, "Height", Integer.valueOf(height));
    add(root, "BitsPerPixel", Short.valueOf((short) bits));
    add(root, "Compression", Integer.valueOf(compression));
    add(root, "ImageSize", Integer.valueOf(imageSize));
    IIOMetadataNode ppm = new IIOMetadataNode("PixelsPerMeter");
    add(ppm, "X", Integer.valueOf(xPels));
    add(ppm, "Y", Integer.valueOf(yPels));
    root.appendChild(ppm);
    add(root, "ColorsUsed", Integer.valueOf(colorsUsed));
    add(root, "ColorsImportant", Integer.valueOf(colorsImportant));
    if (masks != null) {
      IIOMetadataNode m = new IIOMetadataNode("Mask");
      add(m, "Red", Integer.valueOf(masks[0]));
      add(m, "Green", Integer.valueOf(masks[1]));
      add(m, "Blue", Integer.valueOf(masks[2]));
      root.appendChild(m);
    }
    if (palette != null) {
      IIOMetadataNode p = new IIOMetadataNode("Palette");
      for (int i = 0; i < palette.length / 4; i++) {
        IIOMetadataNode e = new IIOMetadataNode("PaletteEntry");
        add(e, "Red", Byte.valueOf(palette[4 * i]));
        add(e, "Green", Byte.valueOf(palette[4 * i + 1]));
        add(e, "Blue", Byte.valueOf(palette[4 * i + 2]));
        add(e, "Alpha", Byte.valueOf(palette[4 * i + 3]));
        p.appendChild(e);
      }
      root.appendChild(p);
    }
    return root;
  }

  protected IIOMetadataNode getStandardChromaNode() {
    if (palette == null) {
      return null;
    }
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    IIOMetadataNode p = new IIOMetadataNode("Palette");
    for (int i = 0; i < palette.length / 4; i++) {
      IIOMetadataNode e = new IIOMetadataNode("PaletteEntry");
      e.setAttribute("index", Integer.toString(i));
      e.setAttribute("red", Integer.toString(palette[4 * i] & 0xff));
      e.setAttribute("green", Integer.toString(palette[4 * i + 1] & 0xff));
      e.setAttribute("blue", Integer.toString(palette[4 * i + 2] & 0xff));
      e.setAttribute("alpha", Integer.toString(palette[4 * i + 3]));
      p.appendChild(e);
    }
    chroma.appendChild(p);
    return chroma;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    IIOMetadataNode n = new IIOMetadataNode("CompressionTypeName");
    n.setAttribute("value", compression >= 0 && compression < kCompressions.length ? kCompressions[compression]
        : "BI_RGB");
    c.appendChild(n);
    return c;
  }

  protected IIOMetadataNode getStandardDataNode() {
    IIOMetadataNode d = new IIOMetadataNode("Data");
    IIOMetadataNode n = new IIOMetadataNode("BitsPerSample");
    String v = "";
    if (fresh) {
      v = " ";
    } else if (palette == null) {
      if (bits == 16 && compression != 3) {
        v = "5 5 5 ";
      } else if (bits == 16 || bits == 32) {
        v = bits == 16 ? "5 6 5 " : "8 8 8 ";
      } else {
        v = "8 8 8 ";
      }
    }
    n.setAttribute("value", v);
    d.appendChild(n);
    return d;
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    throw new IllegalStateException("Metadata is read-only.");
  }

  public void reset() {
    throw new IllegalStateException("Metadata is read-only.");
  }
}
