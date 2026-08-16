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

public final class GifMetadata extends IIOMetadata {
  public static final String kImageFormat = "javax_imageio_gif_image_1.0";
  public static final String kStreamFormat = "javax_imageio_gif_stream_1.0";
  private static final String[] kDisposal = {"none", "doNotDispose", "restoreToBackgroundColor", "restoreToPrevious",
      "undefinedDisposalMethod4", "undefinedDisposalMethod5", "undefinedDisposalMethod6", "undefinedDisposalMethod7"};
  private final boolean stream;
  private String version;
  private int screenW;
  private int screenH;
  private int colorResolution;
  private int aspect;
  private byte[] globalTable;
  private int backgroundIndex;
  private boolean globalSorted;
  private int left;
  private int top;
  private int width;
  private int height;
  private boolean interlaced;
  private byte[] localTable;
  private boolean localSorted;
  private boolean hasGce;
  private int disposal;
  private boolean userInput;
  private boolean transparent;
  private int delay;
  private int transparentIndex;

  public GifMetadata(ImageInputStream in, boolean stream, int imageIndex) throws IOException {
    super(true, stream ? kStreamFormat : kImageFormat, null, null, null);
    this.stream = stream;
    ByteOrder old = in.getByteOrder();
    in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    try {
      parse(in, imageIndex);
    } finally {
      in.setByteOrder(old);
    }
  }

  private static byte[] table(ImageInputStream in, int packed) throws IOException {
    byte[] t = new byte[3 << ((packed & 7) + 1)];
    in.readFully(t);
    return t;
  }

  private static void skipBlocks(ImageInputStream in) throws IOException {
    int n;
    while ((n = in.read()) > 0) {
      in.skipBytes(n);
    }
  }

  private void parse(ImageInputStream in, int imageIndex) throws IOException {
    in.seek(0);
    byte[] sig = new byte[6];
    in.readFully(sig);
    version = new String(sig, 3, 3, java.nio.charset.StandardCharsets.ISO_8859_1);
    screenW = in.readUnsignedShort();
    screenH = in.readUnsignedShort();
    int packed = in.readUnsignedByte();
    backgroundIndex = in.readUnsignedByte();
    aspect = in.readUnsignedByte();
    colorResolution = ((packed >> 4) & 7) + 1;
    globalSorted = (packed & 8) != 0;
    if ((packed & 0x80) != 0) {
      globalTable = table(in, packed);
    }
    if (stream) {
      return;
    }
    int index = 0;
    boolean gce = false;
    while (true) {
      int b = in.read();
      if (b == 0x21) {
        int label = in.readUnsignedByte();
        if (label == 0xF9) {
          in.readUnsignedByte();
          int p = in.readUnsignedByte();
          if (index == imageIndex) {
            hasGce = true;
            disposal = (p >> 2) & 7;
            userInput = (p & 2) != 0;
            transparent = (p & 1) != 0;
            delay = in.readUnsignedShort();
            transparentIndex = in.readUnsignedByte();
          } else {
            in.skipBytes(3);
          }
          in.readUnsignedByte();
        } else {
          skipBlocks(in);
        }
      } else if (b == 0x2C) {
        left = in.readUnsignedShort();
        top = in.readUnsignedShort();
        width = in.readUnsignedShort();
        height = in.readUnsignedShort();
        int p = in.readUnsignedByte();
        interlaced = (p & 0x40) != 0;
        if ((p & 0x80) != 0) {
          localSorted = (p & 0x20) != 0;
          byte[] t = table(in, p);
          if (index == imageIndex) {
            localTable = t;
          }
        }
        if (index == imageIndex) {
          return;
        }
        in.readUnsignedByte();
        skipBlocks(in);
        index++;
        gce = false;
      } else {
        throw new java.io.EOFException("No image " + imageIndex);
      }
    }
  }

  public boolean isReadOnly() { return true; }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if ((stream ? kStreamFormat : kImageFormat).equals(formatName)) {
      return stream ? streamTree() : imageTree();
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  private static IIOMetadataNode node(String name, String attr, String value) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    n.setAttribute(attr, value);
    return n;
  }

  private static String bool(boolean b) { return b ? "TRUE" : "FALSE"; }

  private static void entries(IIOMetadataNode parent, String name, byte[] t) {
    for (int i = 0; i < t.length / 3; i++) {
      IIOMetadataNode e = new IIOMetadataNode(name);
      e.setAttribute("index", Integer.toString(i));
      e.setAttribute("red", Integer.toString(t[3 * i] & 0xff));
      e.setAttribute("green", Integer.toString(t[3 * i + 1] & 0xff));
      e.setAttribute("blue", Integer.toString(t[3 * i + 2] & 0xff));
      parent.appendChild(e);
    }
  }

  private Node streamTree() {
    IIOMetadataNode root = new IIOMetadataNode(kStreamFormat);
    root.appendChild(node("Version", "value", version));
    IIOMetadataNode lsd = new IIOMetadataNode("LogicalScreenDescriptor");
    lsd.setAttribute("logicalScreenWidth", Integer.toString(screenW));
    lsd.setAttribute("logicalScreenHeight", Integer.toString(screenH));
    lsd.setAttribute("colorResolution", Integer.toString(colorResolution));
    lsd.setAttribute("pixelAspectRatio", Integer.toString(aspect));
    root.appendChild(lsd);
    if (globalTable != null) {
      IIOMetadataNode g = new IIOMetadataNode("GlobalColorTable");
      g.setAttribute("sizeOfGlobalColorTable", Integer.toString(globalTable.length / 3));
      g.setAttribute("backgroundColorIndex", Integer.toString(backgroundIndex));
      g.setAttribute("sortFlag", bool(globalSorted));
      entries(g, "ColorTableEntry", globalTable);
      root.appendChild(g);
    }
    return root;
  }

  private Node imageTree() {
    IIOMetadataNode root = new IIOMetadataNode(kImageFormat);
    IIOMetadataNode d = new IIOMetadataNode("ImageDescriptor");
    d.setAttribute("imageLeftPosition", Integer.toString(left));
    d.setAttribute("imageTopPosition", Integer.toString(top));
    d.setAttribute("imageWidth", Integer.toString(width));
    d.setAttribute("imageHeight", Integer.toString(height));
    d.setAttribute("interlaceFlag", bool(interlaced));
    root.appendChild(d);
    if (localTable != null) {
      IIOMetadataNode l = new IIOMetadataNode("LocalColorTable");
      l.setAttribute("sizeOfLocalColorTable", Integer.toString(localTable.length / 3));
      l.setAttribute("sortFlag", bool(localSorted));
      entries(l, "ColorTableEntry", localTable);
      root.appendChild(l);
    }
    if (hasGce) {
      IIOMetadataNode g = new IIOMetadataNode("GraphicControlExtension");
      g.setAttribute("disposalMethod", kDisposal[disposal]);
      g.setAttribute("userInputFlag", bool(userInput));
      g.setAttribute("transparentColorFlag", bool(transparent));
      g.setAttribute("delayTime", Integer.toString(delay));
      g.setAttribute("transparentColorIndex", Integer.toString(transparentIndex));
      root.appendChild(g);
    }
    return root;
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    chroma.appendChild(node("ColorSpaceType", "name", "RGB"));
    if (!stream) {
      chroma.appendChild(node("NumChannels", "value", hasGce && transparent ? "4" : "3"));
    }
    chroma.appendChild(node("BlackIsZero", "value", "TRUE"));
    byte[] t = stream ? globalTable : localTable;
    if (t != null) {
      IIOMetadataNode p = new IIOMetadataNode("Palette");
      entries(p, "PaletteEntry", t);
      chroma.appendChild(p);
    }
    if (stream && globalTable != null) {
      chroma.appendChild(node("BackgroundIndex", "value", Integer.toString(backgroundIndex)));
    }
    return chroma;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    c.appendChild(node("CompressionTypeName", "value", "lzw"));
    c.appendChild(node("Lossless", "value", "TRUE"));
    if (stream) {
      return c;
    }
    c.appendChild(node("NumProgressiveScans", "value", interlaced ? "4" : "1"));
    return c;
  }

  protected IIOMetadataNode getStandardDataNode() {
    IIOMetadataNode d = new IIOMetadataNode("Data");
    d.appendChild(node("SampleFormat", "value", "Index"));
    if (stream) {
      d.appendChild(node("BitsPerSample", "value", Integer.toString(colorResolution)));
    }
    return d;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode d = new IIOMetadataNode("Dimension");
    if (stream) {
      d.appendChild(node("PixelAspectRatio", "value", Float.toString(aspect == 0 ? 1.0f : (aspect + 15) / 64.0f)));
      d.appendChild(node("ImageOrientation", "value", "Normal"));
      d.appendChild(node("HorizontalScreenSize", "value", Integer.toString(screenW)));
      d.appendChild(node("VerticalScreenSize", "value", Integer.toString(screenH)));
    } else {
      d.appendChild(node("ImageOrientation", "value", "Normal"));
      d.appendChild(node("HorizontalPixelOffset", "value", Integer.toString(left)));
      d.appendChild(node("VerticalPixelOffset", "value", Integer.toString(top)));
    }
    return d;
  }

  protected IIOMetadataNode getStandardDocumentNode() {
    if (!stream) {
      return null;
    }
    return new IIOMetadataNode("Document") {
      {
        appendChild(node("FormatVersion", "value", version));
      }
    };
  }

  protected IIOMetadataNode getStandardTransparencyNode() {
    if (stream || !hasGce || !transparent) {
      return null;
    }
    IIOMetadataNode t = new IIOMetadataNode("Transparency");
    t.appendChild(node("TransparentIndex", "value", Integer.toString(transparentIndex)));
    return t;
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    throw new IllegalStateException("Metadata is read-only!");
  }

  public void reset() {
    throw new IllegalStateException("Metadata is read-only!");
  }
}
