/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGQTable;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class JpegWriter extends CodecWriter {
  private ImageOutputStream out;
  private int bitBuf;
  private int bitCnt;
  private final int[][] dcCode = new int[4][16];
  private final int[][] dcLen = new int[4][16];
  private final int[][] acCode = new int[4][256];
  private final int[][] acLen = new int[4][256];

  public JpegWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public ImageWriteParam getDefaultWriteParam() {
    return new javax.imageio.plugins.jpeg.JPEGImageWriteParam(getLocale());
  }

  private static void build(int[] bits, int[] vals, int[] code, int[] len) {
    int c = 0;
    int k = 0;
    for (int l = 1; l <= 16; l++) {
      for (int i = 0; i < bits[l - 1]; i++) {
        code[vals[k]] = c++;
        len[vals[k]] = l;
        k++;
      }
      c <<= 1;
    }
  }

  private void put(int code, int len) throws IOException {
    if (len == 0) {
      return;
    }
    bitBuf = (bitBuf << len) | (code & ((1 << len) - 1));
    bitCnt += len;
    while (bitCnt >= 8) {
      int b = (bitBuf >> (bitCnt - 8)) & 0xff;
      out.write(b);
      if (b == 0xff) {
        out.write(0);
      }
      bitCnt -= 8;
    }
    bitBuf &= (1 << bitCnt) - 1;
  }

  private void flushBitsToByte() throws IOException {
    if (bitCnt > 0) {
      put(0x7f, 8 - bitCnt);
    }
  }

  private static final class Comp {
    int id;
    int h;
    int v;
    int tq;
    int td;
    int ta;
    int blocksW;
    int blocksH;
    int[][][] coef;
  }

  private static int attr(IIOMetadataNode n, String name, int fallback) {
    String v = n == null ? null : n.getAttribute(name);
    return v == null || v.isEmpty() ? fallback : Integer.parseInt(v);
  }

  private static IIOMetadataNode child(IIOMetadataNode parent, String name) {
    for (org.w3c.dom.Node k = parent.getFirstChild(); k != null; k = k.getNextSibling()) {
      if (k.getNodeName().equals(name)) {
        return (IIOMetadataNode) k;
      }
    }
    return null;
  }

  private static float qualityOf(ImageWriteParam param) {
    if (param != null && param.canWriteCompressed() && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
      return param.getCompressionQuality();
    }
    return 0.75f;
  }

  static int[][] quantTables(float quality) {
    float linear = quality < 0.5f ? 0.5f / quality : 2.0f - 2.0f * quality;
    int[][] qt = new int[2][64];
    for (int i = 0; i < 64; i++) {
      qt[0][i] = Math.max(1, Math.min(255, (int) (JpegTables.kLumaQuant[i] * linear + 0.5f)));
      qt[1][i] = Math.max(1, Math.min(255, (int) (JpegTables.kChromaQuant[i] * linear + 0.5f)));
    }
    return qt;
  }

  private static JpegMetadata defaultTree(boolean gray, float quality) {
    return defaultTree(gray, quantTables(quality));
  }

  static byte[] encodeWithoutJfif(BufferedImage image) throws IOException {
    java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
    javax.imageio.stream.MemoryCacheImageOutputStream sink = new javax.imageio.stream.MemoryCacheImageOutputStream(bytes);
    JpegWriter writer = new JpegWriter(null);
    writer.setOutput(sink);
    int[][] ones = new int[2][64];
    java.util.Arrays.fill(ones[0], 1);
    java.util.Arrays.fill(ones[1], 1);
    JpegMetadata tree = defaultTree(false, ones);
    IIOMetadataNode variety = tree.varietyNode();
    while (variety.getFirstChild() != null) {
      variety.removeChild(variety.getFirstChild());
    }
    writer.write(null, new IIOImage(image, null, tree), null);
    sink.close();
    return bytes.toByteArray();
  }

  private static int[][] metadataTables(ImageWriteParam param) {
    boolean explicit = param != null && param.canWriteCompressed()
        && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT;
    if (!explicit) {
      return new int[][] {JPEGQTable.K1Div2Luminance.getTable(), JPEGQTable.K2Div2Chrominance.getTable()};
    }
    float q = Math.max(0.01f, Math.min(1.0f, param.getCompressionQuality()));
    float linear = q < 0.5f ? 0.5f / q : 2.0f - q * 2.0f;
    return new int[][] {JPEGQTable.K1Luminance.getScaledInstance(linear, true).getTable(),
        JPEGQTable.K2Div2Chrominance.getScaledInstance(linear, true).getTable()};
  }

  private static JpegMetadata defaultTree(boolean gray, int[][] qt) {
    IIOMetadataNode variety = new IIOMetadataNode("JPEGvariety");
    variety.appendChild(JpegMetadata.el("app0JFIF", "majorVersion", "1", "minorVersion", "2", "resUnits", "0",
        "Xdensity", "1", "Ydensity", "1", "thumbWidth", "0", "thumbHeight", "0"));
    IIOMetadataNode seq = new IIOMetadataNode("markerSequence");
    seq.appendChild(tables("dqt", gray, qt, null));
    seq.appendChild(tables("dht", gray, null, null));
    IIOMetadataNode sof = JpegMetadata.el("sof", "process", "0", "samplePrecision", "8", "numLines", "0",
        "samplesPerLine", "0", "numFrameComponents", gray ? "1" : "3");
    IIOMetadataNode sos = JpegMetadata.el("sos", "numScanComponents", gray ? "1" : "3", "startSpectralSelection", "0",
        "endSpectralSelection", "63", "approxHigh", "0", "approxLow", "0");
    for (int i = 0; i < (gray ? 1 : 3); i++) {
      sof.appendChild(JpegMetadata.el("componentSpec", "componentId", Integer.toString(i + 1), "HsamplingFactor",
          gray || i > 0 ? "1" : "2", "VsamplingFactor", gray || i > 0 ? "1" : "2", "QtableSelector",
          i == 0 ? "0" : "1"));
      sos.appendChild(JpegMetadata.el("scanComponentSpec", "componentSelector", Integer.toString(i + 1), "dcHuffTable",
          i == 0 ? "0" : "1", "acHuffTable", i == 0 ? "0" : "1"));
    }
    seq.appendChild(sof);
    seq.appendChild(sos);
    return JpegMetadata.createImage(variety, seq);
  }

  private static IIOMetadataNode tables(String name, boolean gray, int[][] qt, Object unused) {
    IIOMetadataNode node = new IIOMetadataNode(name);
    int n = gray ? 1 : 2;
    for (int t = 0; t < n; t++) {
      if (qt != null) {
        node.appendChild(JpegMetadata.qtableNode(0, t, new JPEGQTable(qt[t])));
      }
    }
    if (qt == null) {
      for (int cls = 0; cls < 2; cls++) {
        for (int t = 0; t < n; t++) {
          int[] bits = cls == 0 ? (t == 0 ? JpegTables.kDcLumaBits : JpegTables.kDcChromaBits)
              : (t == 0 ? JpegTables.kAcLumaBits : JpegTables.kAcChromaBits);
          int[] vals = cls == 0 ? (t == 0 ? JpegTables.kDcLumaVals : JpegTables.kDcChromaVals)
              : (t == 0 ? JpegTables.kAcLumaVals : JpegTables.kAcChromaVals);
          node.appendChild(JpegMetadata.htableNode(cls, t, new JPEGHuffmanTable(toShorts(bits), toShorts(vals))));
        }
      }
    }
    return node;
  }

  private static short[] toShorts(int[] v) {
    short[] r = new short[v.length];
    for (int i = 0; i < v.length; i++) {
      r[i] = (short) v[i];
    }
    return r;
  }

  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
    IIOMetadataNode seq = new IIOMetadataNode("markerSequence");
    int[][] qt = metadataTables(param);
    seq.appendChild(tables("dqt", false, qt, null));
    seq.appendChild(tables("dht", false, null, null));
    IIOMetadataNode root = new IIOMetadataNode("x");
    while (seq.getFirstChild() != null) {
      root.appendChild(seq.removeChild(seq.getFirstChild()));
    }
    return JpegMetadata.createStream(root);
  }

  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
    int bands = imageType.getNumBands();
    boolean gray = bands == 1 || imageType.getColorModel() instanceof java.awt.image.IndexColorModel;
    if (!gray && bands != 3) {
      return null;
    }
    return defaultTree(gray, metadataTables(param));
  }

  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
    if (inData instanceof JpegMetadata && !((JpegMetadata) inData).isStream()) {
      return ((JpegMetadata) inData).copy();
    }
    return null;
  }

  public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
    if (inData instanceof JpegMetadata && ((JpegMetadata) inData).isStream()) {
      return ((JpegMetadata) inData).copy();
    }
    return null;
  }

  private void marker(int m, byte[] payload) throws IOException {
    out.write(0xff);
    out.write(m);
    out.writeShort(payload.length + 2);
    out.write(payload);
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    out = stream();
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    int w = bi.getWidth();
    int h = bi.getHeight();
    for (int bits : bi.getColorModel().getComponentSize()) {
      if (bits > 8) {
        throw new IIOException("Illegal band size: should be 0 < size <= 8");
      }
    }
    if (bi.getColorModel().hasAlpha()) {
      throw new IIOException("Bogus input colorspace");
    }
    boolean gray = bi.getColorModel().getNumColorComponents() == 1;
    JpegMetadata meta = image.getMetadata() instanceof JpegMetadata && !((JpegMetadata) image.getMetadata()).isStream()
        ? (JpegMetadata) image.getMetadata() : defaultTree(gray, qualityOf(param));
    IIOMetadataNode sofNode = child(meta.sequenceNode(), "sof");
    IIOMetadataNode sosNode = child(meta.sequenceNode(), "sos");
    if (sofNode == null || sosNode == null || attr(sofNode, "numFrameComponents", 0) != (gray ? 1 : 3)) {
      meta = defaultTree(gray, qualityOf(param));
      sofNode = child(meta.sequenceNode(), "sof");
      sosNode = child(meta.sequenceNode(), "sos");
    }
    int nc = gray ? 1 : 3;
    Comp[] comps = new Comp[nc];
    int i = 0;
    for (org.w3c.dom.Node k = sofNode.getFirstChild(); k != null && i < nc; k = k.getNextSibling()) {
      IIOMetadataNode cs = (IIOMetadataNode) k;
      Comp c = new Comp();
      c.id = attr(cs, "componentId", i + 1);
      c.h = attr(cs, "HsamplingFactor", 1);
      c.v = attr(cs, "VsamplingFactor", 1);
      c.tq = attr(cs, "QtableSelector", 0);
      comps[i++] = c;
    }
    i = 0;
    for (org.w3c.dom.Node k = sosNode.getFirstChild(); k != null && i < nc; k = k.getNextSibling()) {
      IIOMetadataNode sc = (IIOMetadataNode) k;
      comps[i].td = attr(sc, "dcHuffTable", 0);
      comps[i].ta = attr(sc, "acHuffTable", 0);
      i++;
    }
    int[][] qtables = new int[4][];
    int[][][] huff = new int[2][4][];
    int[][][] huffVals = new int[2][4][];
    List<IIOMetadataNode> dqtNodes = new ArrayList<IIOMetadataNode>();
    List<IIOMetadataNode> dhtNodes = new ArrayList<IIOMetadataNode>();
    int restart = 0;
    for (org.w3c.dom.Node k = meta.sequenceNode().getFirstChild(); k != null; k = k.getNextSibling()) {
      IIOMetadataNode n = (IIOMetadataNode) k;
      if (n.getNodeName().equals("dqt")) {
        for (org.w3c.dom.Node t = n.getFirstChild(); t != null; t = t.getNextSibling()) {
          IIOMetadataNode tn = (IIOMetadataNode) t;
          if (tn.getUserObject() instanceof JPEGQTable) {
            qtables[attr(tn, "qtableId", 0) & 3] = ((JPEGQTable) tn.getUserObject()).getTable();
          }
        }
      } else if (n.getNodeName().equals("dht")) {
        for (org.w3c.dom.Node t = n.getFirstChild(); t != null; t = t.getNextSibling()) {
          IIOMetadataNode tn = (IIOMetadataNode) t;
          if (tn.getUserObject() instanceof JPEGHuffmanTable) {
            JPEGHuffmanTable ht = (JPEGHuffmanTable) tn.getUserObject();
            int cls = attr(tn, "class", 0) & 1;
            int id = attr(tn, "htableId", 0) & 3;
            huff[cls][id] = toInts(ht.getLengths());
            huffVals[cls][id] = toInts(ht.getValues());
          }
        }
      } else if (n.getNodeName().equals("dri")) {
        restart = attr(n, "interval", 0);
      }
    }
    int[][] fallback = quantTables(qualityOf(param));
    for (Comp c : comps) {
      if (qtables[c.tq & 3] == null) {
        qtables[c.tq & 3] = fallback[c.tq == 0 ? 0 : 1];
      }
      if (huff[0][c.td & 3] == null) {
        huff[0][c.td & 3] = c.td == 0 ? JpegTables.kDcLumaBits : JpegTables.kDcChromaBits;
        huffVals[0][c.td & 3] = c.td == 0 ? JpegTables.kDcLumaVals : JpegTables.kDcChromaVals;
      }
      if (huff[1][c.ta & 3] == null) {
        huff[1][c.ta & 3] = c.ta == 0 ? JpegTables.kAcLumaBits : JpegTables.kAcChromaBits;
        huffVals[1][c.ta & 3] = c.ta == 0 ? JpegTables.kAcLumaVals : JpegTables.kAcChromaVals;
      }
    }
    boolean optimize = param instanceof javax.imageio.plugins.jpeg.JPEGImageWriteParam
        && ((javax.imageio.plugins.jpeg.JPEGImageWriteParam) param).getOptimizeHuffmanTables();
    processImageStarted(0);
    out.write(0xff);
    out.write(0xd8);
    IIOMetadataNode jfif = child(meta.varietyNode(), "app0JFIF");
    if (jfif != null) {
      marker(0xe0, new byte[] {'J', 'F', 'I', 'F', 0, (byte) attr(jfif, "majorVersion", 1),
          (byte) attr(jfif, "minorVersion", 2), (byte) attr(jfif, "resUnits", 0),
          (byte) (attr(jfif, "Xdensity", 1) >> 8), (byte) attr(jfif, "Xdensity", 1),
          (byte) (attr(jfif, "Ydensity", 1) >> 8), (byte) attr(jfif, "Ydensity", 1), 0, 0});
    }
    for (org.w3c.dom.Node k = meta.sequenceNode().getFirstChild(); k != null; k = k.getNextSibling()) {
      IIOMetadataNode n = (IIOMetadataNode) k;
      String name = n.getNodeName();
      if (name.equals("com")) {
        marker(0xfe, n.getAttribute("comment").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
      } else if (name.equals("unknown")) {
        Object uo = n.getUserObject();
        marker(attr(n, "MarkerTag", 0xe1) & 0xff, uo instanceof byte[] ? (byte[]) uo : new byte[0]);
      } else if (name.equals("app14Adobe")) {
        marker(0xee, new byte[] {'A', 'd', 'o', 'b', 'e', (byte) (attr(n, "version", 100) >> 8),
            (byte) attr(n, "version", 100), (byte) (attr(n, "flags0", 0) >> 8), (byte) attr(n, "flags0", 0),
            (byte) (attr(n, "flags1", 0) >> 8), (byte) attr(n, "flags1", 0), (byte) attr(n, "transform", 1)});
      }
    }
    boolean[] qWritten = new boolean[4];
    for (Comp c : comps) {
      int t = c.tq & 3;
      if (!qWritten[t]) {
        qWritten[t] = true;
        out.write(0xff);
        out.write(0xdb);
        out.writeShort(67);
        out.write(t);
        for (int q = 0; q < 64; q++) {
          out.write(qtables[t][JpegTables.kZigzag[q]]);
        }
      }
    }
    boolean progressive = param != null && param.canWriteProgressive()
        && param.getProgressiveMode() == ImageWriteParam.MODE_DEFAULT;
    out.write(0xff);
    out.write(progressive ? 0xc2 : 0xc0);
    out.writeShort(8 + 3 * nc);
    out.write(8);
    out.writeShort(h);
    out.writeShort(w);
    out.write(nc);
    for (Comp c : comps) {
      out.write(c.id);
      out.write((c.h << 4) | c.v);
      out.write(c.tq);
    }
    if (restart > 0) {
      out.write(0xff);
      out.write(0xdd);
      out.writeShort(4);
      out.writeShort(restart);
    }
    int hmax = 1;
    int vmax = 1;
    if (nc > 1) {
      for (Comp c : comps) {
        hmax = Math.max(hmax, c.h);
        vmax = Math.max(vmax, c.v);
      }
    }
    int mcuW = nc > 1 ? hmax * 8 : 8;
    int mcuH = nc > 1 ? vmax * 8 : 8;
    int mcusX = (w + mcuW - 1) / mcuW;
    int mcusY = (h + mcuH - 1) / mcuH;
    int cw = mcusX * mcuW;
    int ch = mcusY * mcuH;
    int[] argb;
    if (gray && !(bi.getColorModel() instanceof java.awt.image.IndexColorModel)) {
      argb = new int[w * h];
      int shift = bi.getColorModel().getComponentSize(0) > 8 ? bi.getColorModel().getComponentSize(0) - 8 : 0;
      int[] px = new int[bi.getRaster().getNumBands()];
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          bi.getRaster().getPixel(x, y, px);
          int v = px[0] >> shift;
          argb[y * w + x] = 0xff000000 | v * 0x010101;
        }
      }
    } else {
      argb = bi.getRGB(0, 0, w, h, null, 0, w);
    }
    int[][] full = new int[nc][cw * ch];
    for (int py = 0; py < ch; py++) {
      for (int px = 0; px < cw; px++) {
        int p = argb[Math.min(h - 1, py) * w + Math.min(w - 1, px)];
        int o = py * cw + px;
        if (gray) {
          full[0][o] = p & 0xff;
        } else {
          int r = (p >> 16) & 0xff;
          int g = (p >> 8) & 0xff;
          int b = p & 0xff;
          full[0][o] = (19595 * r + 38470 * g + 7471 * b + 32768) >> 16;
          full[1][o] = (-11059 * r - 21709 * g + 32768 * b + (128 << 16) + 32767) >> 16;
          full[2][o] = (32768 * r - 27439 * g - 5329 * b + (128 << 16) + 32767) >> 16;
        }
      }
    }
    int[] samples = new int[64];
    for (int ci = 0; ci < nc; ci++) {
      Comp c = comps[ci];
      int ch1 = nc > 1 ? c.h : 1;
      int cv1 = nc > 1 ? c.v : 1;
      int fx = hmax / ch1;
      int fy = vmax / cv1;
      int pw = mcusX * ch1 * 8;
      int ph = mcusY * cv1 * 8;
      int[] plane = fx == 1 && fy == 1 ? full[ci] : downsample(full[ci], cw, pw, ph, fx, fy, h);
      int stride = fx == 1 && fy == 1 ? cw : pw;
      int compW = (w * ch1 + hmax - 1) / hmax;
      int compH = (h * cv1 + vmax - 1) / vmax;
      c.blocksW = (compW + 7) / 8;
      c.blocksH = (compH + 7) / 8;
      c.coef = new int[mcusY * cv1][mcusX * ch1][];
      for (int by = 0; by < mcusY * cv1; by++) {
        for (int bx = 0; bx < mcusX * ch1; bx++) {
          if (bx >= c.blocksW || by >= c.blocksH) {
            continue;
          }
          grab(plane, stride, bx * 8, by * 8, samples);
          c.coef[by][bx] = quantize(samples, qtables[c.tq & 3]);
        }
      }
    }
    if (progressive) {
      progressiveScans(comps, nc, mcusX, mcusY);
      out.write(0xff);
      out.write(0xd9);
      out.flush();
      for (int y = 8; y < h; y += 8) {
        processImageProgress((float) y / h * 100.0F);
      }
      processImageComplete();
      return;
    }
    if (optimize) {
      long[][][] freq = new long[2][4][257];
      scan(comps, nc, mcusX, mcusY, restart, null, freq, null, null);
      for (int cls = 0; cls < 2; cls++) {
        for (int t = 0; t < 4; t++) {
          boolean used = false;
          for (int s = 0; s < 257; s++) {
            used |= freq[cls][t][s] != 0;
          }
          if (used) {
            int[][] g = optimal(freq[cls][t]);
            huff[cls][t] = g[0];
            huffVals[cls][t] = g[1];
          }
        }
      }
    }
    boolean[][] dhtWritten = new boolean[2][4];
    for (Comp c : comps) {
      for (int cls = 0; cls < 2; cls++) {
        int t = cls == 0 ? c.td & 3 : c.ta & 3;
        if (!dhtWritten[cls][t]) {
          dhtWritten[cls][t] = true;
          huffTable((cls << 4) | t, huff[cls][t], huffVals[cls][t]);
        }
      }
    }
    out.write(0xff);
    out.write(0xda);
    out.writeShort(6 + 2 * nc);
    out.write(nc);
    for (Comp c : comps) {
      out.write(c.id);
      out.write(((c.td & 15) << 4) | (c.ta & 15));
    }
    out.write(0);
    out.write(63);
    out.write(0);
    for (int cls = 0; cls < 2; cls++) {
      for (int t = 0; t < 4; t++) {
        if (huff[cls][t] != null) {
          if (cls == 0) {
            build(huff[cls][t], huffVals[cls][t], dcCode[t], dcLen[t]);
          } else {
            build(huff[cls][t], huffVals[cls][t], acCode[t], acLen[t]);
          }
        }
      }
    }
    scan(comps, nc, mcusX, mcusY, restart, this, null, null, null);
    flushBitsToByte();
    out.write(0xff);
    out.write(0xd9);
    out.flush();
    for (int y = 8; y < h; y += 8) {
      processImageProgress((float) y / h * 100.0F);
    }
    processImageComplete();
  }

  private long[][] progCounts;
  private boolean progEmit;
  private int eobRun;
  private int pendingBits;
  private final int[] bitBuffer = new int[1000];

  private void symbol(int cls, int tbl, int sym) throws IOException {
    if (progEmit) {
      if (cls == 0) {
        put(dcCode[tbl][sym], dcLen[tbl][sym]);
      } else {
        put(acCode[tbl][sym], acLen[tbl][sym]);
      }
    } else {
      progCounts[cls * 4 + tbl][sym]++;
    }
  }

  private void bits(int value, int n) throws IOException {
    if (progEmit && n > 0) {
      put(value, n);
    }
  }

  private void flushEobRun(int ta) throws IOException {
    if (eobRun > 0) {
      int nb = 31 - Integer.numberOfLeadingZeros(eobRun);
      symbol(1, ta, nb << 4);
      if (nb > 0) {
        bits(eobRun, nb);
      }
      eobRun = 0;
      for (int i = 0; i < pendingBits; i++) {
        bits(bitBuffer[i], 1);
      }
      pendingBits = 0;
    }
  }

  private void progressiveScans(Comp[] comps, int nc, int mcusX, int mcusY) throws IOException {
    int[][] script;
    if (nc == 3) {
      script = new int[][] {{-1, 0, 0, 0, 1}, {0, 1, 5, 0, 2}, {2, 1, 63, 0, 1}, {1, 1, 63, 0, 1}, {0, 6, 63, 0, 2},
          {0, 1, 63, 2, 1}, {-1, 0, 0, 1, 0}, {2, 1, 63, 1, 0}, {1, 1, 63, 1, 0}, {0, 1, 63, 1, 0}};
    } else {
      script = new int[][] {{-1, 0, 0, 0, 1}, {0, 1, 5, 0, 2}, {0, 6, 63, 0, 2}, {0, 1, 63, 2, 1}, {-1, 0, 0, 1, 0},
          {0, 1, 63, 1, 0}};
    }
    for (int[] sc : script) {
      boolean dc = sc[0] < 0;
      int[] members = dc ? (nc == 3 ? new int[] {0, 1, 2} : new int[] {0}) : new int[] {sc[0]};
      int ss = sc[1];
      int se = sc[2];
      int ah = sc[3];
      int al = sc[4];
      int[][] hbits = new int[8][];
      int[][] hvals = new int[8][];
      progCounts = new long[8][257];
      progEmit = false;
      runScan(comps, members, mcusX, mcusY, dc, ss, se, ah, al);
      boolean[] sent = new boolean[8];
      List<int[]> dht = new ArrayList<int[]>();
      for (int ci : members) {
        boolean tableDc = dc && ah == 0;
        boolean tableAc = !dc;
        if (tableDc || tableAc) {
          int cls = tableDc ? 0 : 1;
          int tbl = tableDc ? comps[ci].td & 3 : comps[ci].ta & 3;
          int key = cls * 4 + tbl;
          if (!sent[key]) {
            sent[key] = true;
            int[][] g = optimal(progCounts[key]);
            hbits[key] = g[0];
            hvals[key] = g[1];
            if (cls == 0) {
              build(g[0], g[1], dcCode[tbl], dcLen[tbl]);
            } else {
              build(g[0], g[1], acCode[tbl], acLen[tbl]);
            }
            huffTable((cls << 4) | tbl, g[0], g[1]);
          }
        }
      }
      out.write(0xff);
      out.write(0xda);
      out.writeShort(6 + 2 * members.length);
      out.write(members.length);
      for (int ci : members) {
        int td = comps[ci].td & 15;
        int ta = comps[ci].ta & 15;
        if (ss == 0) {
          ta = 0;
          if (ah != 0) {
            td = 0;
          }
        } else {
          td = 0;
        }
        out.write(comps[ci].id);
        out.write((td << 4) | ta);
      }
      out.write(ss);
      out.write(se);
      out.write((ah << 4) | al);
      progEmit = true;
      runScan(comps, members, mcusX, mcusY, dc, ss, se, ah, al);
      flushBitsToByte();
    }
  }

  private void runScan(Comp[] comps, int[] members, int mcusX, int mcusY, boolean dc, int ss, int se, int ah, int al)
      throws IOException {
    eobRun = 0;
    pendingBits = 0;
    int nc = comps.length;
    if (dc) {
      int[] pred = new int[nc];
      int[] lastRaw = new int[nc];
      for (int my = 0; my < mcusY; my++) {
        for (int mx = 0; mx < mcusX; mx++) {
          for (int ci : members) {
            Comp c = comps[ci];
            int hh = nc > 1 ? c.h : 1;
            int vv = nc > 1 ? c.v : 1;
            for (int by = 0; by < vv; by++) {
              for (int bx = 0; bx < hh; bx++) {
                int[] zz = c.coef[my * vv + by][mx * hh + bx];
                int raw = zz == null ? lastRaw[ci] : zz[0];
                lastRaw[ci] = raw;
                if (ah == 0) {
                  int v = raw >> al;
                  int diff = v - pred[ci];
                  pred[ci] = v;
                  int mag = diff == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(Math.abs(diff));
                  symbol(0, c.td & 3, mag);
                  if (mag > 0) {
                    bits(diff < 0 ? diff - 1 : diff, mag);
                  }
                } else {
                  bits(raw >> al, 1);
                }
              }
            }
          }
        }
      }
      return;
    }
    Comp c = comps[members[0]];
    int ta = c.ta & 3;
    for (int by = 0; by < c.blocksH; by++) {
      for (int bx = 0; bx < c.blocksW; bx++) {
        int[] zz = c.coef[by][bx];
        if (ah == 0) {
          acFirst(zz, ta, ss, se, al);
        } else {
          acRefine(zz, ta, ss, se, al);
        }
      }
    }
    flushEobRun(ta);
  }

  private void acFirst(int[] zz, int ta, int ss, int se, int al) throws IOException {
    int r = 0;
    for (int k = ss; k <= se; k++) {
      int temp = zz[k];
      if (temp == 0) {
        r++;
        continue;
      }
      int temp2;
      if (temp < 0) {
        temp = -temp;
        temp >>= al;
        temp2 = ~temp;
      } else {
        temp >>= al;
        temp2 = temp;
      }
      if (temp == 0) {
        r++;
        continue;
      }
      if (eobRun > 0) {
        flushEobRun(ta);
      }
      while (r > 15) {
        symbol(1, ta, 0xf0);
        r -= 16;
      }
      int nb = 32 - Integer.numberOfLeadingZeros(temp);
      symbol(1, ta, (r << 4) + nb);
      bits(temp2, nb);
      r = 0;
    }
    if (r > 0) {
      eobRun++;
      if (eobRun == 0x7fff) {
        flushEobRun(ta);
      }
    }
  }

  private void acRefine(int[] zz, int ta, int ss, int se, int al) throws IOException {
    int[] abs = new int[64];
    int eob = 0;
    for (int k = ss; k <= se; k++) {
      int temp = Math.abs(zz[k]) >> al;
      abs[k] = temp;
      if (temp == 1) {
        eob = k;
      }
    }
    int r = 0;
    int[] br = new int[64];
    int brCount = 0;
    for (int k = ss; k <= se; k++) {
      int temp = abs[k];
      if (temp == 0) {
        r++;
        continue;
      }
      while (r > 15 && k <= eob) {
        flushEobRun(ta);
        symbol(1, ta, 0xf0);
        r -= 16;
        for (int i = 0; i < brCount; i++) {
          bits(br[i], 1);
        }
        brCount = 0;
      }
      if (temp > 1) {
        br[brCount++] = temp & 1;
        continue;
      }
      flushEobRun(ta);
      symbol(1, ta, (r << 4) + 1);
      bits(zz[k] < 0 ? 0 : 1, 1);
      for (int i = 0; i < brCount; i++) {
        bits(br[i], 1);
      }
      brCount = 0;
      r = 0;
    }
    if (r > 0 || brCount > 0) {
      eobRun++;
      for (int i = 0; i < brCount; i++) {
        bitBuffer[pendingBits++] = br[i];
      }
      if (eobRun == 0x7fff || pendingBits > 1000 - 64 + 1) {
        flushEobRun(ta);
      }
    }
  }

  private static int[] toInts(short[] v) {
    int[] r = new int[v.length];
    for (int i = 0; i < v.length; i++) {
      r[i] = v[i];
    }
    return r;
  }

  private static int[] downsample(int[] src, int srcStride, int pw, int ph, int fx, int fy, int imageHeight) {
    int[] dst = new int[pw * ph];
    int srcRows = src.length / srcStride;
    for (int cy = 0; cy < ph; cy++) {
      int bias2 = 1;
      for (int cx = 0; cx < pw; cx++) {
        int value;
        if (fx == 2 && fy == 2) {
          int bias = (cx & 1) == 0 ? 1 : 2;
          int o = 2 * Math.min(cy, (imageHeight - 1) / 2) * srcStride + 2 * cx;
          value = (src[o] + src[o + 1] + src[o + srcStride] + src[o + srcStride + 1] + bias) >> 2;
        } else if (fx == 2 && fy == 1) {
          int o = Math.min(cy, srcRows - 1) * srcStride + 2 * cx;
          value = (src[o] + src[o + 1] + ((cx & 1) == 0 ? 0 : 1)) >> 1;
        } else {
          int sum = 0;
          for (int yy = 0; yy < fy; yy++) {
            for (int xx = 0; xx < fx; xx++) {
              sum += src[Math.min(cy * fy + yy, srcRows - 1) * srcStride + cx * fx + xx];
            }
          }
          int n = fx * fy;
          value = (sum + n / 2) / n;
        }
        dst[cy * pw + cx] = value;
      }
    }
    return dst;
  }

  private int[] quantize(int[] samples, int[] q) {
    int[] f = new int[64];
    JpegTables.fdctInt(samples, f);
    int[] zz = new int[64];
    for (int i = 0; i < 64; i++) {
      int idx = JpegTables.kZigzag[i];
      int div = q[idx] << 3;
      int t = f[idx];
      if (t < 0) {
        zz[i] = -((-t + (div >> 1)) / div);
      } else {
        zz[i] = (t + (div >> 1)) / div;
      }
    }
    return zz;
  }

  private void scan(Comp[] comps, int nc, int mcusX, int mcusY, int restart, JpegWriter emit, long[][][] freq,
      Object unused1, Object unused2) throws IOException {
    int[] pred = new int[nc];
    int mcuCount = 0;
    int rst = 0;
    for (int my = 0; my < mcusY; my++) {
      for (int mx = 0; mx < mcusX; mx++) {
        if (restart > 0 && mcuCount > 0 && mcuCount % restart == 0) {
          if (emit != null) {
            emit.flushBitsToByte();
            emit.out.write(0xff);
            emit.out.write(0xd0 + (rst & 7));
          }
          rst++;
          java.util.Arrays.fill(pred, 0);
        }
        mcuCount++;
        for (int ci = 0; ci < nc; ci++) {
          Comp c = comps[ci];
          int hh = nc > 1 ? c.h : 1;
          int vv = nc > 1 ? c.v : 1;
          for (int by = 0; by < vv; by++) {
            for (int bx = 0; bx < hh; bx++) {
              int[] zz = c.coef[my * vv + by][mx * hh + bx];
              pred[ci] = encodeBlock(zz, c.td & 3, c.ta & 3, pred[ci], emit, freq);
            }
          }
        }
      }
    }
  }

  private int encodeBlock(int[] zz, int td, int ta, int pred, JpegWriter emit, long[][][] freq) throws IOException {
    if (zz == null) {
      if (emit != null) {
        emit.put(emit.dcCode[td][0], emit.dcLen[td][0]);
        emit.put(emit.acCode[ta][0], emit.acLen[ta][0]);
      } else {
        freq[0][td][0]++;
        freq[1][ta][0]++;
      }
      return pred;
    }
    int diff = zz[0] - pred;
    int mag = diff == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(Math.abs(diff));
    if (emit != null) {
      emit.put(emit.dcCode[td][mag], emit.dcLen[td][mag]);
      if (mag > 0) {
        emit.put(diff < 0 ? diff - 1 : diff, mag);
      }
    } else {
      freq[0][td][mag]++;
    }
    int run = 0;
    for (int i = 1; i < 64; i++) {
      int v = zz[i];
      if (v == 0) {
        run++;
        continue;
      }
      while (run > 15) {
        if (emit != null) {
          emit.put(emit.acCode[ta][0xf0], emit.acLen[ta][0xf0]);
        } else {
          freq[1][ta][0xf0]++;
        }
        run -= 16;
      }
      int m = 32 - Integer.numberOfLeadingZeros(Math.abs(v));
      int sym = (run << 4) | m;
      if (emit != null) {
        emit.put(emit.acCode[ta][sym], emit.acLen[ta][sym]);
        emit.put(v < 0 ? v - 1 : v, m);
      } else {
        freq[1][ta][sym]++;
      }
      run = 0;
    }
    if (run > 0) {
      if (emit != null) {
        emit.put(emit.acCode[ta][0], emit.acLen[ta][0]);
      } else {
        freq[1][ta][0]++;
      }
    }
    return zz[0];
  }

  private static int[][] optimal(long[] frequency) {
    long[] freq = frequency.clone();
    freq[256] = 1;
    int[] codesize = new int[257];
    int[] others = new int[257];
    java.util.Arrays.fill(others, -1);
    while (true) {
      int c1 = -1;
      long v = Long.MAX_VALUE;
      for (int i = 0; i <= 256; i++) {
        if (freq[i] != 0 && freq[i] <= v) {
          v = freq[i];
          c1 = i;
        }
      }
      int c2 = -1;
      v = Long.MAX_VALUE;
      for (int i = 0; i <= 256; i++) {
        if (freq[i] != 0 && freq[i] <= v && i != c1) {
          v = freq[i];
          c2 = i;
        }
      }
      if (c2 < 0) {
        break;
      }
      freq[c1] += freq[c2];
      freq[c2] = 0;
      codesize[c1]++;
      while (others[c1] >= 0) {
        c1 = others[c1];
        codesize[c1]++;
      }
      others[c1] = c2;
      codesize[c2]++;
      while (others[c2] >= 0) {
        c2 = others[c2];
        codesize[c2]++;
      }
    }
    int[] bits = new int[33];
    for (int i = 0; i <= 256; i++) {
      if (codesize[i] > 0) {
        bits[codesize[i]]++;
      }
    }
    for (int i = 32; i > 16; i--) {
      while (bits[i] > 0) {
        int j = i - 2;
        while (bits[j] == 0) {
          j--;
        }
        bits[i] -= 2;
        bits[i - 1]++;
        bits[j + 1] += 2;
        bits[j]--;
      }
    }
    int i = 16;
    while (bits[i] == 0) {
      i--;
    }
    bits[i]--;
    int[] lengths = new int[16];
    int total = 0;
    for (int l = 1; l <= 16; l++) {
      lengths[l - 1] = bits[l];
      total += bits[l];
    }
    int[] vals = new int[total];
    int p = 0;
    for (int l = 1; l <= 32; l++) {
      for (int j = 0; j < 256; j++) {
        if (codesize[j] == l) {
          vals[p++] = j;
        }
      }
    }
    return new int[][] {lengths, vals};
  }

  private void huffTable(int id, int[] bits, int[] vals) throws IOException {
    out.write(0xff);
    out.write(0xc4);
    out.writeShort(19 + vals.length);
    out.write(id);
    for (int b : bits) {
      out.write(b);
    }
    for (int v : vals) {
      out.write(v);
    }
  }

  private static void grab(int[] plane, int stride, int x0, int y0, int[] out) {
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        out[y * 8 + x] = plane[(y0 + y) * stride + x0 + x] - 128;
      }
    }
  }

}
