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
import java.util.ArrayList;
import java.util.List;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import javax.imageio.ImageTypeSpecifier;
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
  private final List<Object[]> applications = new ArrayList<>();
  private final List<String> comments = new ArrayList<>();

  public GifMetadata(boolean stream) {
    super(true, stream ? kStreamFormat : kImageFormat, null, null, null);
    this.stream = stream;
    reset();
  }

  public static GifMetadata defaultImage(ImageTypeSpecifier type) {
    GifMetadata m = new GifMetadata(false);
    if (type != null) {
      m.width = type.getSampleModel().getWidth();
      m.height = type.getSampleModel().getHeight();
      ColorModel cm = type.getColorModel();
      IndexColorModel icm = cm instanceof IndexColorModel ? (IndexColorModel) cm : defaultPalette();
      m.localTable = tableOf(icm);
      m.hasGce = true;
      int t = icm.getTransparentPixel();
      if (t >= 0 && cm instanceof IndexColorModel) {
        m.transparent = true;
        m.transparentIndex = t;
      }
    }
    return m;
  }

  private static IndexColorModel defaultPalette() {
    return (IndexColorModel) new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_BYTE_INDEXED)
        .getColorModel();
  }

  public static GifMetadata defaultStream(ImageTypeSpecifier type) {
    GifMetadata m = new GifMetadata(true);
    return m;
  }

  static byte[] tableOf(IndexColorModel icm) {
    int n = icm.getMapSize();
    int size = 2;
    while (size < n) {
      size <<= 1;
    }
    byte[] t = new byte[size * 3];
    for (int i = 0; i < n; i++) {
      t[3 * i] = (byte) icm.getRed(i);
      t[3 * i + 1] = (byte) icm.getGreen(i);
      t[3 * i + 2] = (byte) icm.getBlue(i);
    }
    return t;
  }

  public GifMetadata(ImageInputStream in, boolean stream, int imageIndex) throws IOException {
    super(true, stream ? kStreamFormat : kImageFormat, null, null, null);
    this.stream = stream;
    version = "89a";
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

  private static byte[] readBlocks(ImageInputStream in) throws IOException {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    int n;
    while ((n = in.read()) > 0) {
      byte[] b = new byte[n];
      in.readFully(b);
      out.write(b, 0, n);
    }
    return out.toByteArray();
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
    hasGce = true;
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
        } else if (label == 0xFF && index == imageIndex) {
          int n = in.readUnsignedByte();
          byte[] head = new byte[n];
          in.readFully(head);
          String id = new String(head, 0, Math.min(8, n), java.nio.charset.StandardCharsets.ISO_8859_1);
          String auth = n > 8 ? new String(head, 8, Math.min(3, n - 8), java.nio.charset.StandardCharsets.ISO_8859_1) : "";
          applications.add(new Object[] {id, auth, readBlocks(in)});
        } else if (label == 0xFE && index == imageIndex) {
          comments.add(new String(readBlocks(in), java.nio.charset.StandardCharsets.ISO_8859_1));
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

  public boolean isReadOnly() { return false; }

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
    if (!applications.isEmpty()) {
      IIOMetadataNode apps = new IIOMetadataNode("ApplicationExtensions");
      for (Object[] a : applications) {
        IIOMetadataNode e = new IIOMetadataNode("ApplicationExtension");
        e.setAttribute("applicationID", (String) a[0]);
        e.setAttribute("authenticationCode", (String) a[1]);
        e.setUserObject(((byte[]) a[2]).clone());
        apps.appendChild(e);
      }
      root.appendChild(apps);
    }
    if (!comments.isEmpty()) {
      IIOMetadataNode cs = new IIOMetadataNode("CommentExtensions");
      for (String c : comments) {
        cs.appendChild(node("CommentExtension", "value", c));
      }
      root.appendChild(cs);
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
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      mergeStandard(root);
      return;
    }
    if (!(stream ? kStreamFormat : kImageFormat).equals(formatName)) {
      throw new IllegalArgumentException("Not a recognized format!");
    }
    if (root == null || !formatName.equals(root.getNodeName())) {
      throw new IIOInvalidTreeException("Root must be " + formatName, root);
    }
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      String name = c.getNodeName();
      if (stream && name.equals("Version")) {
        version = attr(c, "value", version);
      } else if (stream && name.equals("LogicalScreenDescriptor")) {
        screenW = intAttr(c, "logicalScreenWidth", screenW);
        screenH = intAttr(c, "logicalScreenHeight", screenH);
        colorResolution = intAttr(c, "colorResolution", colorResolution);
        aspect = intAttr(c, "pixelAspectRatio", aspect);
      } else if (stream && name.equals("GlobalColorTable")) {
        backgroundIndex = intAttr(c, "backgroundColorIndex", backgroundIndex);
        globalSorted = boolAttr(c, "sortFlag", globalSorted);
        globalTable = readTable(c, intAttr(c, "sizeOfGlobalColorTable", 0));
      } else if (!stream && name.equals("ImageDescriptor")) {
        left = intAttr(c, "imageLeftPosition", left);
        top = intAttr(c, "imageTopPosition", top);
        width = intAttr(c, "imageWidth", width);
        height = intAttr(c, "imageHeight", height);
        interlaced = boolAttr(c, "interlaceFlag", interlaced);
      } else if (!stream && name.equals("LocalColorTable")) {
        localSorted = boolAttr(c, "sortFlag", localSorted);
        localTable = readTable(c, intAttr(c, "sizeOfLocalColorTable", 0));
      } else if (!stream && name.equals("GraphicControlExtension")) {
        hasGce = true;
        String d = attr(c, "disposalMethod", kDisposal[disposal]);
        for (int i = 0; i < kDisposal.length; i++) {
          if (kDisposal[i].equals(d)) {
            disposal = i;
          }
        }
        userInput = boolAttr(c, "userInputFlag", userInput);
        transparent = boolAttr(c, "transparentColorFlag", transparent);
        delay = intAttr(c, "delayTime", delay);
        transparentIndex = intAttr(c, "transparentColorIndex", transparentIndex);
      } else if (!stream && name.equals("ApplicationExtensions")) {
        applications.clear();
        for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
          Object user = e instanceof IIOMetadataNode ? ((IIOMetadataNode) e).getUserObject() : null;
          if (!(user instanceof byte[])) {
            throw new IIOInvalidTreeException("Application extension needs a byte[] user object", e);
          }
          applications.add(new Object[] {attr(e, "applicationID", ""), attr(e, "authenticationCode", ""),
              ((byte[]) user).clone()});
        }
      } else if (!stream && name.equals("CommentExtensions")) {
        comments.clear();
        for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
          comments.add(attr(e, "value", ""));
        }
      }
    }
  }

  private void mergeStandard(Node root) throws IIOInvalidTreeException {
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeName().equals("Chroma")) {
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("Palette")) {
            int n = 0;
            for (Node e = p.getFirstChild(); e != null; e = e.getNextSibling()) {
              n = Math.max(n, intAttr(e, "index", 0) + 1);
            }
            byte[] t = readTable(p, n);
            if (stream) {
              globalTable = t;
            } else {
              localTable = t;
            }
          } else if (p.getNodeName().equals("BackgroundIndex") && stream) {
            backgroundIndex = intAttr(p, "value", 0);
          }
        }
      } else if (c.getNodeName().equals("Transparency") && !stream) {
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("TransparentIndex")) {
            hasGce = true;
            transparent = true;
            transparentIndex = intAttr(p, "value", 0);
          }
        }
      } else if (c.getNodeName().equals("Dimension") && !stream) {
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("HorizontalPixelOffset")) {
            left = intAttr(p, "value", 0);
          } else if (p.getNodeName().equals("VerticalPixelOffset")) {
            top = intAttr(p, "value", 0);
          }
        }
      } else if (c.getNodeName().equals("Text") && !stream) {
        comments.clear();
        for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
          comments.add(attr(e, "value", ""));
        }
      }
    }
  }

  private static String attr(Node n, String name, String fallback) {
    Node a = n.getAttributes() == null ? null : n.getAttributes().getNamedItem(name);
    return a == null || a.getNodeValue().isEmpty() ? fallback : a.getNodeValue();
  }

  private static int intAttr(Node n, String name, int fallback) throws IIOInvalidTreeException {
    String v = attr(n, name, null);
    if (v == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(v.trim());
    } catch (NumberFormatException e) {
      throw new IIOInvalidTreeException("Bad value for " + name + " attribute!", n);
    }
  }

  private static boolean boolAttr(Node n, String name, boolean fallback) throws IIOInvalidTreeException {
    String v = attr(n, name, null);
    if (v == null) {
      return fallback;
    }
    if (v.equalsIgnoreCase("true")) {
      return true;
    }
    if (v.equalsIgnoreCase("false")) {
      return false;
    }
    throw new IIOInvalidTreeException("Bad value for " + name + " attribute!", n);
  }

  private static byte[] readTable(Node node, int size) throws IIOInvalidTreeException {
    int n = size;
    for (Node e = node.getFirstChild(); e != null; e = e.getNextSibling()) {
      n = Math.max(n, intAttr(e, "index", 0) + 1);
    }
    byte[] t = new byte[n * 3];
    for (Node e = node.getFirstChild(); e != null; e = e.getNextSibling()) {
      int i = intAttr(e, "index", 0);
      t[3 * i] = (byte) intAttr(e, "red", 0);
      t[3 * i + 1] = (byte) intAttr(e, "green", 0);
      t[3 * i + 2] = (byte) intAttr(e, "blue", 0);
    }
    return t;
  }

  public void reset() {
    version = "89a";
    screenW = 0;
    screenH = 0;
    colorResolution = 0;
    aspect = 0;
    globalTable = null;
    backgroundIndex = 0;
    globalSorted = false;
    left = 0;
    top = 0;
    width = 0;
    height = 0;
    interlaced = true;
    localTable = null;
    localSorted = false;
    hasGce = false;
    disposal = 0;
    userInput = false;
    transparent = false;
    delay = 0;
    transparentIndex = 0;
    applications.clear();
    comments.clear();
  }

  boolean isStream() { return stream; }

  int screenWidth() { return screenW; }

  int screenHeight() { return screenH; }

  int colorResolutionBits() { return colorResolution; }

  int pixelAspect() { return aspect; }

  String versionName() { return version; }

  byte[] globalTableBytes() { return globalTable; }

  int backgroundColorIndex() { return backgroundIndex; }

  boolean globalSortFlag() { return globalSorted; }

  int imageLeft() { return left; }

  int imageTop() { return top; }

  boolean interlaceFlag() { return interlaced; }

  byte[] localTableBytes() { return localTable; }

  boolean localSortFlag() { return localSorted; }

  boolean hasGraphicControl() { return hasGce; }

  int disposalMethod() { return disposal; }

  boolean userInputFlag() { return userInput; }

  boolean transparentFlag() { return transparent; }

  int delayTime() { return delay; }

  int transparentColorIndex() { return transparentIndex; }

  List<Object[]> applicationExtensions() { return applications; }

  List<String> commentExtensions() { return comments; }
}
