/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public final class JpegMetadata extends IIOMetadata {
  public static final String kFormat = "javax_imageio_jpeg_image_1.0";
  private final IIOMetadataNode variety = new IIOMetadataNode("JPEGvariety");
  private final IIOMetadataNode sequence = new IIOMetadataNode("markerSequence");
  private boolean jfif;
  private int adobeTransform = -1;
  private int components;
  private int[] componentIds = new int[0];
  private int scans;
  private boolean progressive;
  private int resUnits;
  private int xDensity = 1;
  private int yDensity = 1;

  public JpegMetadata(ImageInputStream in) throws IOException {
    super(true, kFormat, null, null, null);
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
  }

  private static int u16(byte[] d, int o) {
    return ((d[o] & 0xff) << 8) | (d[o + 1] & 0xff);
  }

  private static IIOMetadataNode el(String name, String... kv) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    for (int i = 0; i < kv.length; i += 2) {
      n.setAttribute(kv[i], kv[i + 1]);
    }
    return n;
  }

  private static String s(int v) { return Integer.toString(v); }

  private void segment(int m, byte[] d) {
    if (m == 0xE0 && d.length >= 14 && d[0] == 'J' && d[1] == 'F' && d[2] == 'I' && d[3] == 'F' && d[4] == 0) {
      jfif = true;
      resUnits = d[7] & 0xff;
      xDensity = u16(d, 8);
      yDensity = u16(d, 10);
      variety.appendChild(el("app0JFIF", "majorVersion", s(d[5] & 0xff), "minorVersion", s(d[6] & 0xff), "resUnits",
          s(resUnits), "Xdensity", s(xDensity), "Ydensity", s(yDensity), "thumbWidth", s(d[12] & 0xff), "thumbHeight",
          s(d[13] & 0xff)));
    } else if (m == 0xEE && d.length >= 12 && d[0] == 'A' && d[1] == 'd' && d[2] == 'o' && d[3] == 'b' && d[4] == 'e') {
      adobeTransform = d[11] & 0xff;
      sequence.appendChild(el("app14Adobe", "version", s(u16(d, 5)), "flags0", s(u16(d, 7)), "flags1", s(u16(d, 9)),
          "transform", s(adobeTransform)));
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
        dqt.appendChild(el("dqtable", "elementPrecision", s(pq), "qtableId", s(d[o] & 0xf)));
        o += 1 + 64 * (pq + 1);
      }
      sequence.appendChild(dqt);
    } else if (m == 0xC4) {
      int o = 0;
      while (o + 17 <= d.length) {
        IIOMetadataNode dht = new IIOMetadataNode("dht");
        dht.appendChild(el("dhtable", "class", s((d[o] >> 4) & 0xf), "htableId", s(d[o] & 0xf)));
        int total = 0;
        for (int i = 1; i <= 16; i++) {
          total += d[o + i] & 0xff;
        }
        o += 17 + total;
        sequence.appendChild(dht);
      }
    } else if (m == 0xDD) {
      sequence.appendChild(el("dri", "interval", s(u16(d, 0))));
    } else if (m >= 0xC0 && m <= 0xCF && m != 0xC4 && m != 0xC8 && m != 0xCC) {
      progressive = m == 0xC2;
      components = d[5] & 0xff;
      componentIds = new int[components];
      IIOMetadataNode sof = el("sof", "process", s(m - 0xC0), "samplePrecision", s(d[0] & 0xff), "numLines",
          s(u16(d, 1)), "samplesPerLine", s(u16(d, 3)), "numFrameComponents", s(components));
      for (int i = 0; i < components; i++) {
        int o = 6 + 3 * i;
        componentIds[i] = d[o] & 0xff;
        sof.appendChild(el("componentSpec", "componentId", s(d[o] & 0xff), "HsamplingFactor", s((d[o + 1] >> 4) & 0xf),
            "VsamplingFactor", s(d[o + 1] & 0xf), "QtableSelector", s(d[o + 2] & 0xff)));
      }
      sequence.appendChild(sof);
    } else if (m == 0xDA) {
      scans++;
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

  public boolean isReadOnly() { return true; }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (kFormat.equals(formatName)) {
      IIOMetadataNode root = new IIOMetadataNode(kFormat);
      root.appendChild(variety.cloneNode(true));
      root.appendChild(sequence.cloneNode(true));
      return root;
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  private String colorSpace() {
    if (components == 1) {
      return "GRAY";
    }
    if (components == 4) {
      return adobeTransform == 2 ? "YCCK" : "CMYK";
    }
    if (components == 3) {
      if (jfif) {
        return "YCbCr";
      }
      if (adobeTransform == 0 || (componentIds[0] == 'R' && componentIds[1] == 'G' && componentIds[2] == 'B')) {
        return "RGB";
      }
      return "YCbCr";
    }
    return "Unknown";
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode c = new IIOMetadataNode("Chroma");
    c.appendChild(el("ColorSpaceType", "name", colorSpace()));
    c.appendChild(el("NumChannels", "value", s(components)));
    return c;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    c.appendChild(el("CompressionTypeName", "value", "JPEG"));
    c.appendChild(el("Lossless", "value", "FALSE"));
    c.appendChild(el("NumProgressiveScans", "value", s(progressive ? scans : 1)));
    return c;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode d = new IIOMetadataNode("Dimension");
    float aspect = 1.0f;
    if (jfif && resUnits == 0 && yDensity != 0) {
      aspect = (float) xDensity / yDensity;
    }
    d.appendChild(el("PixelAspectRatio", "value", Float.toString(aspect)));
    d.appendChild(el("ImageOrientation", "value", "normal"));
    if (jfif && resUnits != 0 && xDensity != 0 && yDensity != 0) {
      float unit = resUnits == 1 ? 25.4f : 10.0f;
      d.appendChild(el("HorizontalPixelSize", "value", Float.toString(unit / xDensity)));
      d.appendChild(el("VerticalPixelSize", "value", Float.toString(unit / yDensity)));
    }
    return d;
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    throw new IllegalStateException("Metadata is read-only!");
  }

  public void reset() {
    throw new IllegalStateException("Metadata is read-only!");
  }
}
