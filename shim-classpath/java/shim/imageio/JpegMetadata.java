/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public final class JpegMetadata extends IIOMetadata {
  public static final String kFormat = "javax_imageio_jpeg_image_1.0";
  public static final String kStreamFormat = "javax_imageio_jpeg_stream_1.0";
  private final boolean stream;
  private IIOMetadataNode variety = new IIOMetadataNode("JPEGvariety");
  private IIOMetadataNode sequence = new IIOMetadataNode("markerSequence");
  private IIOMetadataNode initialVariety;
  private IIOMetadataNode initialSequence;

  public JpegMetadata(ImageInputStream in) throws IOException {
    super(true, kFormat, null, null, null);
    stream = false;
    in.seek(0);
    in.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);
    if (in.readUnsignedShort() != 0xFFD8) {
      throw new IOException("Not a JPEG file");
    }
    while (true) {
      int b = in.read();
      if (b < 0) {
        break;
      }
      if (b != 0xFF) {
        continue;
      }
      int m = in.read();
      while (m == 0xFF) {
        m = in.read();
      }
      if (m < 0 || m == 0xD9) {
        break;
      }
      if (m == 0 || m == 0x01 || (m >= 0xD0 && m <= 0xD7)) {
        continue;
      }
      int len = in.readUnsignedShort() - 2;
      byte[] d = new byte[Math.max(len, 0)];
      in.readFully(d);
      segment(m, d);
    }
    snapshot();
  }

  private JpegMetadata(boolean stream) {
    super(true, stream ? kStreamFormat : kFormat, null, null, null);
    this.stream = stream;
  }

  public static JpegMetadata createImage(IIOMetadataNode varietyNode, IIOMetadataNode sequenceNode) {
    JpegMetadata m = new JpegMetadata(false);
    m.variety = varietyNode;
    m.sequence = sequenceNode;
    m.snapshot();
    return m;
  }

  public static JpegMetadata createStream(IIOMetadataNode sequenceNode) {
    JpegMetadata m = new JpegMetadata(true);
    m.sequence = sequenceNode;
    m.snapshot();
    return m;
  }

  public JpegMetadata copy() {
    JpegMetadata m = new JpegMetadata(stream);
    m.variety = deepCopy(variety);
    m.sequence = deepCopy(sequence);
    m.snapshot();
    return m;
  }

  public boolean isStream() { return stream; }

  public IIOMetadataNode varietyNode() { return variety; }

  public IIOMetadataNode sequenceNode() { return sequence; }

  private void snapshot() {
    initialVariety = deepCopy(variety);
    initialSequence = deepCopy(sequence);
  }

  static IIOMetadataNode deepCopy(IIOMetadataNode n) {
    IIOMetadataNode c = new IIOMetadataNode(n.getNodeName());
    org.w3c.dom.NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      c.setAttribute(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
    }
    Object uo = n.getUserObject();
    if (uo instanceof byte[]) {
      uo = ((byte[]) uo).clone();
    }
    c.setUserObject(uo);
    for (Node k = n.getFirstChild(); k != null; k = k.getNextSibling()) {
      c.appendChild(deepCopy((IIOMetadataNode) k));
    }
    return c;
  }

  private static int u16(byte[] d, int o) {
    return ((d[o] & 0xff) << 8) | (d[o + 1] & 0xff);
  }

  public static IIOMetadataNode el(String name, String... kv) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    for (int i = 0; i < kv.length; i += 2) {
      n.setAttribute(kv[i], kv[i + 1]);
    }
    return n;
  }

  private static String s(int v) { return Integer.toString(v); }

  public static IIOMetadataNode qtableNode(int precision, int id, JPEGQTable table) {
    IIOMetadataNode n = el("dqtable", "elementPrecision", s(precision), "qtableId", s(id));
    n.setUserObject(table);
    return n;
  }

  public static IIOMetadataNode htableNode(int cls, int id, JPEGHuffmanTable table) {
    IIOMetadataNode n = el("dhtable", "class", s(cls), "htableId", s(id));
    n.setUserObject(table);
    return n;
  }

  private void segment(int m, byte[] d) {
    if (m == 0xE0 && d.length >= 14 && d[0] == 'J' && d[1] == 'F' && d[2] == 'I' && d[3] == 'F' && d[4] == 0) {
      variety.appendChild(el("app0JFIF", "majorVersion", s(d[5] & 0xff), "minorVersion", s(d[6] & 0xff), "resUnits",
          s(d[7] & 0xff), "Xdensity", s(u16(d, 8)), "Ydensity", s(u16(d, 10)), "thumbWidth", s(d[12] & 0xff),
          "thumbHeight", s(d[13] & 0xff)));
    } else if (m == 0xEE && d.length >= 12 && d[0] == 'A' && d[1] == 'd' && d[2] == 'o' && d[3] == 'b' && d[4] == 'e') {
      sequence.appendChild(el("app14Adobe", "version", s(u16(d, 5)), "flags0", s(u16(d, 7)), "flags1", s(u16(d, 9)),
          "transform", s(d[11] & 0xff)));
    } else if (m >= 0xE0 && m <= 0xEF) {
      IIOMetadataNode n = el("unknown", "MarkerTag", s(m));
      n.setUserObject(d);
      sequence.appendChild(n);
    } else if (m == 0xFE) {
      sequence.appendChild(el("com", "comment", new String(d, java.nio.charset.StandardCharsets.ISO_8859_1)));
    } else if (m == 0xDB) {
      IIOMetadataNode dqt = new IIOMetadataNode("dqt");
      int o = 0;
      while (o < d.length) {
        int pq = (d[o] >> 4) & 0xf;
        int[] natural = new int[64];
        for (int i = 0; i < 64; i++) {
          int at = o + 1 + (pq == 0 ? i : 2 * i);
          int v = pq == 0 ? d[at] & 0xff : u16(d, at);
          natural[JpegTables.kZigzag[i]] = v;
        }
        dqt.appendChild(qtableNode(pq, d[o] & 0xf, new JPEGQTable(natural)));
        o += 1 + 64 * (pq + 1);
      }
      sequence.appendChild(dqt);
    } else if (m == 0xC4) {
      int o = 0;
      while (o + 17 <= d.length) {
        IIOMetadataNode dht = new IIOMetadataNode("dht");
        short[] lengths = new short[16];
        int total = 0;
        for (int i = 1; i <= 16; i++) {
          lengths[i - 1] = (short) (d[o + i] & 0xff);
          total += d[o + i] & 0xff;
        }
        short[] values = new short[total];
        for (int i = 0; i < total && o + 17 + i < d.length; i++) {
          values[i] = (short) (d[o + 17 + i] & 0xff);
        }
        dht.appendChild(htableNode((d[o] >> 4) & 0xf, d[o] & 0xf, new JPEGHuffmanTable(lengths, values)));
        o += 17 + total;
        sequence.appendChild(dht);
      }
    } else if (m == 0xDD) {
      sequence.appendChild(el("dri", "interval", s(u16(d, 0))));
    } else if (m >= 0xC0 && m <= 0xCF && m != 0xC4 && m != 0xC8 && m != 0xCC) {
      int components = d[5] & 0xff;
      IIOMetadataNode sof = el("sof", "process", s(m - 0xC0), "samplePrecision", s(d[0] & 0xff), "numLines",
          s(u16(d, 1)), "samplesPerLine", s(u16(d, 3)), "numFrameComponents", s(components));
      for (int i = 0; i < components; i++) {
        int o = 6 + 3 * i;
        sof.appendChild(el("componentSpec", "componentId", s(d[o] & 0xff), "HsamplingFactor", s((d[o + 1] >> 4) & 0xf),
            "VsamplingFactor", s(d[o + 1] & 0xf), "QtableSelector", s(d[o + 2] & 0xff)));
      }
      sequence.appendChild(sof);
    } else if (m == 0xDA) {
      int n = d[0] & 0xff;
      IIOMetadataNode sos = el("sos", "numScanComponents", s(n), "startSpectralSelection", s(d[1 + 2 * n] & 0xff),
          "endSpectralSelection", s(d[2 + 2 * n] & 0xff), "approxHigh", s((d[3 + 2 * n] >> 4) & 0xf), "approxLow",
          s(d[3 + 2 * n] & 0xf));
      for (int i = 0; i < n; i++) {
        sos.appendChild(el("scanComponentSpec", "componentSelector", s(d[1 + 2 * i] & 0xff), "dcHuffTable",
            s((d[2 + 2 * i] >> 4) & 0xf), "acHuffTable", s(d[2 + 2 * i] & 0xf)));
      }
      sequence.appendChild(sos);
    }
  }

  public boolean isReadOnly() { return false; }

  public Node getAsTree(String formatName) {
    if (formatName == null) {
      throw new IllegalArgumentException("null");
    }
    if (!stream && IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (!stream && kFormat.equals(formatName)) {
      IIOMetadataNode root = new IIOMetadataNode(kFormat);
      root.appendChild(deepCopy(variety));
      root.appendChild(deepCopy(sequence));
      return root;
    }
    if (stream && kStreamFormat.equals(formatName)) {
      IIOMetadataNode root = deepCopy(sequence);
      IIOMetadataNode named = new IIOMetadataNode(kStreamFormat);
      while (root.getFirstChild() != null) {
        named.appendChild(root.removeChild(root.getFirstChild()));
      }
      return named;
    }
    throw new IllegalArgumentException("Unsupported format name: " + formatName);
  }

  private IIOMetadataNode find(IIOMetadataNode parent, String name) {
    for (Node k = parent.getFirstChild(); k != null; k = k.getNextSibling()) {
      if (k.getNodeName().equals(name)) {
        return (IIOMetadataNode) k;
      }
    }
    return null;
  }

  private static int attr(IIOMetadataNode n, String name, int fallback) {
    if (n == null || n.getAttribute(name) == null || n.getAttribute(name).isEmpty()) {
      return fallback;
    }
    return Integer.parseInt(n.getAttribute(name));
  }

  private boolean jfif() { return find(variety, "app0JFIF") != null; }

  private IIOMetadataNode sof() { return find(sequence, "sof"); }

  private int components() { return attr(sof(), "numFrameComponents", 0); }

  private int adobeTransform() { return attr(find(sequence, "app14Adobe"), "transform", -1); }

  private int scans() {
    int n = 0;
    for (Node k = sequence.getFirstChild(); k != null; k = k.getNextSibling()) {
      if (k.getNodeName().equals("sos")) {
        n++;
      }
    }
    return n;
  }

  private String colorSpace() {
    int components = components();
    if (components == 1) {
      return "GRAY";
    }
    if (components == 4) {
      return adobeTransform() == 2 ? "YCCK" : "CMYK";
    }
    if (components == 3) {
      if (jfif()) {
        return "YCbCr";
      }
      IIOMetadataNode sof = sof();
      int[] ids = new int[3];
      int i = 0;
      for (Node k = sof.getFirstChild(); k != null && i < 3; k = k.getNextSibling()) {
        ids[i++] = attr((IIOMetadataNode) k, "componentId", 0);
      }
      if (adobeTransform() == 0 || (ids[0] == 'R' && ids[1] == 'G' && ids[2] == 'B')) {
        return "RGB";
      }
      return "YCbCr";
    }
    return "Unknown";
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode c = new IIOMetadataNode("Chroma");
    c.appendChild(el("ColorSpaceType", "name", colorSpace()));
    c.appendChild(el("NumChannels", "value", s(components())));
    return c;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    c.appendChild(el("CompressionTypeName", "value", "JPEG"));
    c.appendChild(el("Lossless", "value", "FALSE"));
    IIOMetadataNode sof = sof();
    boolean progressive = sof != null && attr(sof, "process", 0) == 2;
    c.appendChild(el("NumProgressiveScans", "value", s(progressive ? scans() : 1)));
    return c;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode d = new IIOMetadataNode("Dimension");
    IIOMetadataNode j = find(variety, "app0JFIF");
    int resUnits = attr(j, "resUnits", 0);
    int xDensity = attr(j, "Xdensity", 1);
    int yDensity = attr(j, "Ydensity", 1);
    float aspect = 1.0f;
    if (j != null && resUnits == 0 && yDensity != 0) {
      aspect = (float) xDensity / yDensity;
    }
    d.appendChild(el("PixelAspectRatio", "value", Float.toString(aspect)));
    d.appendChild(el("ImageOrientation", "value", "normal"));
    if (j != null && resUnits != 0 && xDensity != 0 && yDensity != 0) {
      float unit = resUnits == 1 ? 25.4f : 10.0f;
      d.appendChild(el("HorizontalPixelSize", "value", Float.toString(unit / xDensity)));
      d.appendChild(el("VerticalPixelSize", "value", Float.toString(unit / yDensity)));
    }
    return d;
  }

  private static final java.util.Set<String> kMarkerNames = new java.util.HashSet<String>(
      java.util.Arrays.asList("dqt", "dht", "dri", "com", "unknown", "app14Adobe", "sof", "sos"));

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    apply(formatName, root, false);
  }

  public void setFromTree(String formatName, Node root) throws IIOInvalidTreeException {
    apply(formatName, root, true);
  }

  private void apply(String formatName, Node root, boolean clear) throws IIOInvalidTreeException {
    if (formatName == null) {
      throw new IllegalArgumentException("null");
    }
    String expected = stream ? kStreamFormat : kFormat;
    if (!expected.equals(formatName)) {
      if (!stream && IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
        throw new IIOInvalidTreeException("Standard tree merging is not supported", root);
      }
      throw new IllegalArgumentException("Unsupported format name: " + formatName);
    }
    if (root == null) {
      throw new IllegalArgumentException("root == null!");
    }
    if (!root.getNodeName().equals(expected)) {
      throw new IIOInvalidTreeException("Invalid root node name: " + root.getNodeName(), root);
    }
    IIOMetadataNode newVariety = clear ? new IIOMetadataNode("JPEGvariety") : deepCopy(variety);
    IIOMetadataNode newSequence = clear ? new IIOMetadataNode("markerSequence") : deepCopy(sequence);
    java.util.List<Node> markers = new java.util.ArrayList<Node>();
    for (Node k = root.getFirstChild(); k != null; k = k.getNextSibling()) {
      String name = k.getNodeName();
      if (!stream && name.equals("JPEGvariety")) {
        for (Node v = k.getFirstChild(); v != null; v = v.getNextSibling()) {
          if (!v.getNodeName().equals("app0JFIF")) {
            throw new IIOInvalidTreeException("Invalid variety node: " + v.getNodeName(), v);
          }
          IIOMetadataNode old = find(newVariety, "app0JFIF");
          if (old != null) {
            newVariety.removeChild(old);
          }
          newVariety.appendChild(deepCopy((IIOMetadataNode) v));
        }
      } else if (!stream && name.equals("markerSequence")) {
        for (Node v = k.getFirstChild(); v != null; v = v.getNextSibling()) {
          markers.add(v);
        }
      } else if (stream) {
        markers.add(k);
      } else {
        throw new IIOInvalidTreeException("Invalid node: " + name, k);
      }
    }
    for (Node v : markers) {
      if (!kMarkerNames.contains(v.getNodeName())) {
        throw new IIOInvalidTreeException("Invalid node: " + v.getNodeName(), v);
      }
      IIOMetadataNode copy = deepCopy((IIOMetadataNode) v);
      String name = copy.getNodeName();
      IIOMetadataNode old = clear || name.equals("com") || name.equals("unknown") ? null : find(newSequence, name);
      if (old != null && !name.equals("dqt") && !name.equals("dht")) {
        newSequence.replaceChild(copy, old);
      } else {
        newSequence.appendChild(copy);
      }
    }
    variety = newVariety;
    sequence = newSequence;
  }

  public void reset() {
    variety = deepCopy(initialVariety);
    sequence = deepCopy(initialSequence);
  }
}
