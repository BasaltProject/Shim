/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public final class JpegReader extends CodecReader {
  private static final class Component {
    int id;
    int h;
    int v;
    int tq;
    int blocksW;
    int blocksH;
    short[] coefs;
    int dcTable;
    int acTable;
    int pred;
  }

  private static final class Huffman {
    final int[] maxCode = new int[18];
    final int[] valPtr = new int[17];
    final int[] minCode = new int[17];
    int[] vals;

    Huffman(int[] bits, int[] vals) {
      this.vals = vals;
      int code = 0;
      int k = 0;
      for (int l = 1; l <= 16; l++) {
        valPtr[l] = k;
        minCode[l] = code;
        code += bits[l - 1];
        k += bits[l - 1];
        maxCode[l] = bits[l - 1] == 0 ? -1 : code - 1;
        code <<= 1;
      }
    }
  }

  private ImageInputStream in;
  private int bitBuf;
  private int bitCnt;
  private int marker;
  private int eobrun;
  private final int[][] quant = new int[4][];
  private final Huffman[] dcTables = new Huffman[4];
  private final Huffman[] acTables = new Huffman[4];
  private int restartInterval;
  private int width;
  private int height;
  private boolean progressive;
  private Component[] comps;
  private int hmax;
  private int vmax;
  private int adobeTransform = -1;
  private boolean jfif;

  protected void reportProgress(int rows) {
    int interval = Math.max((rows - 1) / 20, 1);
    for (int y = interval; y <= rows - 1; y += interval) {
      processImageProgress((float) y / (rows - 1) * 100.0F);
    }
  }

  public JpegReader(ImageReaderSpi spi) {
    super(spi);
  }

  public javax.imageio.ImageReadParam getDefaultReadParam() {
    return new javax.imageio.plugins.jpeg.JPEGImageReadParam();
  }

  public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return new JpegMetadata(stream());
  }

  protected String[] nativeFormat() { return new String[] {"javax_imageio_jpeg_image_1.0", "JPEG", "false"}; }

  protected int imageCount() throws IOException {
    return 1;
  }

  private int readBit() throws IOException {
    if (bitCnt == 0) {
      int b = in.read();
      if (b < 0) {
        throw new IIOException("Premature end of JPEG data");
      }
      if (b == 0xff) {
        int b2 = in.read();
        if (b2 != 0) {
          marker = b2;
          b = 0;
          bitBuf = 0;
          bitCnt = 8;
          return 0;
        }
      }
      bitBuf = b;
      bitCnt = 8;
    }
    bitCnt--;
    return (bitBuf >> bitCnt) & 1;
  }

  private int receive(int n) throws IOException {
    int v = 0;
    for (int i = 0; i < n; i++) {
      v = (v << 1) | readBit();
    }
    return v;
  }

  private static int extend(int v, int n) {
    return n == 0 ? 0 : (v < (1 << (n - 1)) ? v - (1 << n) + 1 : v);
  }

  private int decodeHuff(Huffman h) throws IOException {
    int code = 0;
    for (int l = 1; l <= 16; l++) {
      code = (code << 1) | readBit();
      if (h.maxCode[l] >= 0 && code <= h.maxCode[l] && code >= h.minCode[l]) {
        return h.vals[h.valPtr[l] + code - h.minCode[l]];
      }
    }
    throw new IIOException("Bad Huffman code");
  }

  protected BufferedImage decode(int index) throws IOException {
    in = stream();
    in.seek(0);
    bitCnt = 0;
    marker = 0;
    restartInterval = 0;
    adobeTransform = -1;
    jfif = false;
    if (in.readUnsignedByte() != 0xff || in.readUnsignedByte() != 0xd8) {
      throw new IIOException("Not a JPEG file: starts with 0x" + 0);
    }
    boolean gotFrame = false;
    while (true) {
      int m = nextMarker();
      if (m == 0xd9) {
        break;
      }
      if (m == 0xd8 || (m >= 0xd0 && m <= 0xd7) || m == 0x01) {
        continue;
      }
      int len = in.readUnsignedShort() - 2;
      switch (m) {
      case 0xe0: {
        byte[] d = new byte[len];
        in.readFully(d);
        if (len >= 5 && d[0] == 'J' && d[1] == 'F' && d[2] == 'I' && d[3] == 'F') {
          jfif = true;
        }
        break;
      }
      case 0xee: {
        byte[] d = new byte[len];
        in.readFully(d);
        if (len >= 12 && d[0] == 'A' && d[1] == 'd' && d[2] == 'o' && d[3] == 'b' && d[4] == 'e') {
          adobeTransform = d[11] & 0xff;
        }
        break;
      }
      case 0xdb:
        readQuant(len);
        break;
      case 0xc4:
        readHuffman(len);
        break;
      case 0xdd:
        restartInterval = in.readUnsignedShort();
        break;
      case 0xc0:
      case 0xc1:
      case 0xc2:
        readFrame(m == 0xc2, len);
        gotFrame = true;
        break;
      case 0xc3:
      case 0xc5:
      case 0xc6:
      case 0xc7:
      case 0xc9:
      case 0xca:
      case 0xcb:
      case 0xcd:
      case 0xce:
      case 0xcf:
        throw new IIOException("Unsupported Image Type");
      case 0xda:
        if (!gotFrame) {
          throw new IIOException("SOS without SOF");
        }
        readScan(len);
        break;
      default:
        in.skipBytes(len);
        break;
      }
    }
    if (!gotFrame) {
      throw new IIOException("No JPEG frame found");
    }
    return output();
  }

  private int nextMarker() throws IOException {
    int b;
    if (marker != 0) {
      int m = marker;
      marker = 0;
      bitCnt = 0;
      return m;
    }
    do {
      b = in.read();
      if (b < 0) {
        return 0xd9;
      }
    } while (b != 0xff);
    do {
      b = in.read();
      if (b < 0) {
        return 0xd9;
      }
    } while (b == 0xff);
    return b;
  }

  private void readQuant(int len) throws IOException {
    while (len > 0) {
      int pq = in.readUnsignedByte();
      int t = pq & 15;
      int[] q = new int[64];
      for (int i = 0; i < 64; i++) {
        q[JpegTables.kZigzag[i]] = (pq >> 4) == 0 ? in.readUnsignedByte() : in.readUnsignedShort();
      }
      quant[t & 3] = q;
      len -= 1 + 64 * ((pq >> 4) == 0 ? 1 : 2);
    }
  }

  private void readHuffman(int len) throws IOException {
    while (len > 0) {
      int tc = in.readUnsignedByte();
      int[] bits = new int[16];
      int total = 0;
      for (int i = 0; i < 16; i++) {
        bits[i] = in.readUnsignedByte();
        total += bits[i];
      }
      int[] vals = new int[total];
      for (int i = 0; i < total; i++) {
        vals[i] = in.readUnsignedByte();
      }
      Huffman h = new Huffman(bits, vals);
      if ((tc >> 4) == 0) {
        dcTables[tc & 3] = h;
      } else {
        acTables[tc & 3] = h;
      }
      len -= 17 + total;
    }
  }

  private void readFrame(boolean prog, int len) throws IOException {
    progressive = prog;
    int precision = in.readUnsignedByte();
    if (precision != 8) {
      throw new IIOException("Unsupported Image Type");
    }
    height = in.readUnsignedShort();
    width = in.readUnsignedShort();
    int n = in.readUnsignedByte();
    comps = new Component[n];
    hmax = 1;
    vmax = 1;
    for (int i = 0; i < n; i++) {
      Component c = new Component();
      c.id = in.readUnsignedByte();
      int hv = in.readUnsignedByte();
      c.h = hv >> 4;
      c.v = hv & 15;
      c.tq = in.readUnsignedByte();
      hmax = Math.max(hmax, c.h);
      vmax = Math.max(vmax, c.v);
      comps[i] = c;
    }
    if (width <= 0 || height <= 0) {
      throw new IIOException("Bad JPEG dimensions");
    }
    int mcusX = (width + 8 * hmax - 1) / (8 * hmax);
    int mcusY = (height + 8 * vmax - 1) / (8 * vmax);
    for (Component c : comps) {
      c.blocksW = mcusX * c.h;
      c.blocksH = mcusY * c.v;
      c.coefs = new short[c.blocksW * c.blocksH * 64];
    }
  }

  private void readScan(int len) throws IOException {
    int ns = in.readUnsignedByte();
    Component[] sc = new Component[ns];
    for (int i = 0; i < ns; i++) {
      int cid = in.readUnsignedByte();
      int tt = in.readUnsignedByte();
      for (Component c : comps) {
        if (c.id == cid) {
          sc[i] = c;
          c.dcTable = tt >> 4;
          c.acTable = tt & 15;
        }
      }
      if (sc[i] == null) {
        throw new IIOException("Bad component in scan");
      }
    }
    int ss = in.readUnsignedByte();
    int se = in.readUnsignedByte();
    int a = in.readUnsignedByte();
    int ah = a >> 4;
    int al = a & 15;
    bitCnt = 0;
    marker = 0;
    eobrun = 0;
    for (Component c : comps) {
      c.pred = 0;
    }
    int mcusX = (width + 8 * hmax - 1) / (8 * hmax);
    int mcusY = (height + 8 * vmax - 1) / (8 * vmax);
    int total;
    boolean single = ns == 1;
    int bw = 0;
    int bh = 0;
    if (single) {
      Component c = sc[0];
      bw = (((width * c.h + hmax - 1) / hmax) + 7) / 8;
      bh = (((height * c.v + vmax - 1) / vmax) + 7) / 8;
      total = bw * bh;
    } else {
      total = mcusX * mcusY;
    }
    int restartCount = 0;
    for (int mcu = 0; mcu < total; mcu++) {
      if (restartInterval != 0 && mcu > 0 && mcu % restartInterval == 0) {
        bitCnt = 0;
        if (marker == 0) {
          int b;
          do {
            b = in.read();
          } while (b >= 0 && b != 0xff);
          do {
            b = in.read();
          } while (b == 0xff);
          marker = b;
        }
        if (marker >= 0xd0 && marker <= 0xd7) {
          marker = 0;
        }
        for (Component c : comps) {
          c.pred = 0;
        }
        eobrun = 0;
      }
      if (single) {
        Component c = sc[0];
        int br = mcu / bw;
        int bc = mcu % bw;
        decodeBlock(c, br, bc, ss, se, ah, al);
      } else {
        int mr = mcu / mcusX;
        int mc = mcu % mcusX;
        for (Component c : sc) {
          for (int v = 0; v < c.v; v++) {
            for (int h = 0; h < c.h; h++) {
              decodeBlock(c, mr * c.v + v, mc * c.h + h, ss, se, ah, al);
            }
          }
        }
      }
    }
    bitCnt = 0;
  }

  private void decodeBlock(Component c, int br, int bc, int ss, int se, int ah, int al) throws IOException {
    int base = (br * c.blocksW + bc) * 64;
    short[] coefs = c.coefs;
    if (!progressive) {
      Huffman dc = dcTables[c.dcTable];
      Huffman ac = acTables[c.acTable];
      int t = decodeHuff(dc);
      int diff = t == 0 ? 0 : extend(receive(t), t);
      c.pred += diff;
      coefs[base] = (short) c.pred;
      int k = 1;
      while (k < 64) {
        int rs = decodeHuff(ac);
        int s = rs & 15;
        int r = rs >> 4;
        if (s == 0) {
          if (r < 15) {
            break;
          }
          k += 16;
          continue;
        }
        k += r;
        if (k > 63) {
          break;
        }
        coefs[base + JpegTables.kZigzag[k]] = (short) extend(receive(s), s);
        k++;
      }
      return;
    }
    if (ss == 0) {
      if (ah == 0) {
        int t = decodeHuff(dcTables[c.dcTable]);
        int diff = t == 0 ? 0 : extend(receive(t), t);
        c.pred += diff;
        coefs[base] = (short) (c.pred << al);
      } else if (readBit() != 0) {
        coefs[base] |= (short) (1 << al);
      }
      return;
    }
    Huffman ac = acTables[c.acTable];
    if (ah == 0) {
      if (eobrun > 0) {
        eobrun--;
        return;
      }
      int k = ss;
      while (k <= se) {
        int rs = decodeHuff(ac);
        int s = rs & 15;
        int r = rs >> 4;
        if (s == 0) {
          if (r < 15) {
            eobrun = (1 << r) - 1;
            if (r != 0) {
              eobrun += receive(r);
            }
            break;
          }
          k += 16;
          continue;
        }
        k += r;
        if (k > 63) {
          break;
        }
        coefs[base + JpegTables.kZigzag[k]] = (short) (extend(receive(s), s) * (1 << al));
        k++;
      }
      return;
    }
    int p1 = 1 << al;
    int m1 = -1 << al;
    int k = ss;
    if (eobrun <= 0) {
      for (; k <= se; k++) {
        int rs = decodeHuff(ac);
        int s = rs & 15;
        int r = rs >> 4;
        int value = 0;
        if (s != 0) {
          value = readBit() != 0 ? p1 : m1;
        } else if (r != 15) {
          eobrun = 1 << r;
          if (r != 0) {
            eobrun += receive(r);
          }
          break;
        }
        while (k <= se) {
          int idx = base + JpegTables.kZigzag[k];
          if (coefs[idx] != 0) {
            if (readBit() != 0) {
              if ((coefs[idx] & p1) == 0) {
                coefs[idx] += coefs[idx] >= 0 ? p1 : m1;
              }
            }
          } else {
            if (--r < 0) {
              break;
            }
          }
          k++;
        }
        if (value != 0 && k <= 63) {
          coefs[base + JpegTables.kZigzag[k]] = (short) value;
        }
      }
    }
    if (eobrun > 0) {
      for (; k <= se; k++) {
        int idx = base + JpegTables.kZigzag[k];
        if (coefs[idx] != 0) {
          if (readBit() != 0) {
            if ((coefs[idx] & p1) == 0) {
              coefs[idx] += coefs[idx] >= 0 ? p1 : m1;
            }
          }
        }
      }
      eobrun--;
    }
  }

  private BufferedImage output() {
    int n = comps.length;
    byte[][] planes = new byte[n][];
    int[] pw = new int[n];
    int[] block = new int[64];
    int[] samples = new int[64];
    for (int ci = 0; ci < n; ci++) {
      Component c = comps[ci];
      int stride = c.blocksW * 8;
      byte[] plane = new byte[stride * c.blocksH * 8];
      int[] q = quant[c.tq & 3];
      for (int br = 0; br < c.blocksH; br++) {
        for (int bc = 0; bc < c.blocksW; bc++) {
          int base = (br * c.blocksW + bc) * 64;
          for (int i = 0; i < 64; i++) {
            block[i] = c.coefs[base + i] * (q == null ? 1 : q[i]);
          }
          JpegTables.idct(block, samples);
          for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
              plane[(br * 8 + y) * stride + bc * 8 + x] = (byte) samples[y * 8 + x];
            }
          }
        }
      }
      planes[ci] = plane;
      pw[ci] = stride;
    }
    byte[][] full = new byte[n][];
    for (int ci = 0; ci < n; ci++) {
      full[ci] = upsample(planes[ci], pw[ci], comps[ci]);
    }
    if (n == 1) {
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
      WritableRaster r = bi.getRaster();
      int[] row = new int[width];
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          row[x] = full[0][y * width + x] & 0xff;
        }
        r.setPixels(0, y, width, 1, row);
      }
      return bi;
    }
    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    WritableRaster r = bi.getRaster();
    int[] px = new int[width * 3];
    boolean ycc = n == 3 ? (adobeTransform != 0 && !(adobeTransform < 0 && !jfif && comps[0].id == 'R'))
        : adobeTransform == 2;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int[] v = new int[n];
        for (int ci = 0; ci < n; ci++) {
          v[ci] = full[ci][y * width + x] & 0xff;
        }
        int rr;
        int gg;
        int bb;
        if (n >= 3) {
          if (ycc) {
            int cb = v[1] - 128;
            int cr = v[2] - 128;
            rr = clamp8(v[0] + ((91881 * cr + 32768) >> 16));
            gg = clamp8(v[0] + ((-22554 * cb + -46802 * cr + 32768) >> 16));
            bb = clamp8(v[0] + ((116130 * cb + 32768) >> 16));
          } else {
            rr = v[0];
            gg = v[1];
            bb = v[2];
          }
          if (n == 4) {
            int k = v[3];
            if (adobeTransform >= 0) {
              rr = rr * k / 255;
              gg = gg * k / 255;
              bb = bb * k / 255;
            } else {
              rr = (255 - rr) * (255 - k) / 255;
              gg = (255 - gg) * (255 - k) / 255;
              bb = (255 - bb) * (255 - k) / 255;
            }
          }
        } else {
          rr = gg = bb = v[0];
        }
        px[x * 3] = rr;
        px[x * 3 + 1] = gg;
        px[x * 3 + 2] = bb;
      }
      r.setPixels(0, y, width, 1, px);
    }
    return bi;
  }

  private static int clamp8(int i) { return i < 0 ? 0 : i > 255 ? 255 : i; }

  private byte[] upsample(byte[] plane, int stride, Component c) {
    int dw = (width * c.h + hmax - 1) / hmax;
    int dh = (height * c.v + vmax - 1) / vmax;
    byte[] out = new byte[width * height];
    int hx = hmax / c.h;
    int vx = vmax / c.v;
    boolean fancy = hx == 2 && (vx == 1 || vx == 2) && dw > 2 && hmax % c.h == 0 && vmax % c.v == 0;
    if (!fancy) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          out[y * width + x] = plane[(y * c.v / vmax) * stride + (x * c.h / hmax)];
        }
      }
      return out;
    }
    for (int y = 0; y < height; y++) {
      int r0 = Math.min(dh - 1, y / vx);
      int r1 = vx == 1 ? r0 : (y & 1) == 0 ? Math.max(0, r0 - 1) : Math.min(dh - 1, r0 + 1);
      for (int i = 0; i < dw; i++) {
        int x0 = 2 * i;
        int x1 = x0 + 1;
        if (vx == 1) {
          int cur = plane[r0 * stride + i] & 0xff;
          int lo = i == 0 ? cur : plane[r0 * stride + i - 1] & 0xff;
          int hi = i == dw - 1 ? cur : plane[r0 * stride + i + 1] & 0xff;
          int a = i == 0 ? cur : (3 * cur + lo + 1) >> 2;
          int b = i == dw - 1 ? cur : (3 * cur + hi + 2) >> 2;
          if (x0 < width) {
            out[y * width + x0] = (byte) a;
          }
          if (x1 < width) {
            out[y * width + x1] = (byte) b;
          }
        } else {
          int sum = 3 * (plane[r0 * stride + i] & 0xff) + (plane[r1 * stride + i] & 0xff);
          int last = i == 0 ? sum : 3 * (plane[r0 * stride + i - 1] & 0xff) + (plane[r1 * stride + i - 1] & 0xff);
          int next = i == dw - 1 ? sum : 3 * (plane[r0 * stride + i + 1] & 0xff) + (plane[r1 * stride + i + 1] & 0xff);
          int a = i == 0 ? (sum * 4 + 8) >> 4 : (sum * 3 + last + 8) >> 4;
          int b = i == dw - 1 ? (sum * 4 + 7) >> 4 : (sum * 3 + next + 7) >> 4;
          if (x0 < width) {
            out[y * width + x0] = (byte) a;
          }
          if (x1 < width) {
            out[y * width + x1] = (byte) b;
          }
        }
      }
    }
    return out;
  }

  private static int clamp(double v) {
    int i = (int) Math.round(v);
    return i < 0 ? 0 : i > 255 ? 255 : i;
  }
}
