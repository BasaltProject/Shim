/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public final class PngNative {
  private static final String[] kColorTypes = {"Grayscale", null, "RGB", "Palette", "GrayAlpha", null, "RGBAlpha"};
  private static final String[] kIntents = {"Perceptual", "Relative colorimetric", "Saturation",
      "Absolute colorimetric"};

  private PngNative() { }

  private static IIOMetadataNode el(String name, String... kv) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    for (int i = 0; i < kv.length; i += 2) {
      n.setAttribute(kv[i], kv[i + 1]);
    }
    return n;
  }

  private static String s(int v) { return Integer.toString(v); }

  private static int i32(byte[] d, int o) {
    return ((d[o] & 0xff) << 24) | ((d[o + 1] & 0xff) << 16) | ((d[o + 2] & 0xff) << 8) | (d[o + 3] & 0xff);
  }

  private static int zero(byte[] d, int from) {
    int i = from;
    while (i < d.length && d[i] != 0) {
      i++;
    }
    return i;
  }

  public static Node read(ImageInputStream in) throws IOException {
    in.seek(8);
    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
    IIOMetadataNode text = new IIOMetadataNode("tEXt");
    while (true) {
      int len;
      try {
        len = in.readInt();
      } catch (java.io.EOFException e) {
        break;
      }
      byte[] t = new byte[4];
      in.readFully(t);
      String type = new String(t, java.nio.charset.StandardCharsets.ISO_8859_1);
      byte[] d = new byte[len];
      in.readFully(d);
      in.skipBytes(4);
      if (type.equals("IDAT") || type.equals("IEND")) {
        if (type.equals("IEND")) {
          break;
        }
        continue;
      }
      switch (type) {
        case "IHDR": {
          int ct = d[9] & 0xff;
          root.appendChild(el("IHDR", "width", s(i32(d, 0)), "height", s(i32(d, 4)), "bitDepth", s(d[8] & 0xff),
              "colorType", ct < kColorTypes.length && kColorTypes[ct] != null ? kColorTypes[ct] : "RGB",
              "compressionMethod", "deflate", "filterMethod", "adaptive", "interlaceMethod",
              d[12] == 0 ? "none" : "adam7"));
          break;
        }
        case "PLTE": {
          IIOMetadataNode p = new IIOMetadataNode("PLTE");
          for (int i = 0; i < d.length / 3; i++) {
            p.appendChild(el("PLTEEntry", "index", s(i), "red", s(d[3 * i] & 0xff), "green", s(d[3 * i + 1] & 0xff),
                "blue", s(d[3 * i + 2] & 0xff)));
          }
          root.appendChild(p);
          break;
        }
        case "gAMA":
          root.appendChild(el("gAMA", "value", s(i32(d, 0))));
          break;
        case "pHYs":
          root.appendChild(el("pHYs", "pixelsPerUnitXAxis", s(i32(d, 0)), "pixelsPerUnitYAxis", s(i32(d, 4)),
              "unitSpecifier", d[8] == 1 ? "meter" : "unknown"));
          break;
        case "sRGB":
          root.appendChild(el("sRGB", "renderingIntent", kIntents[Math.min(d[0] & 0xff, 3)]));
          break;
        case "tIME":
          root.appendChild(el("tIME", "year", s(((d[0] & 0xff) << 8) | (d[1] & 0xff)), "month", s(d[2] & 0xff), "day",
              s(d[3] & 0xff), "hour", s(d[4] & 0xff), "minute", s(d[5] & 0xff), "second", s(d[6] & 0xff)));
          break;
        case "tEXt": {
          int z = zero(d, 0);
          text.appendChild(el("tEXtEntry", "keyword", new String(d, 0, z, java.nio.charset.StandardCharsets.ISO_8859_1),
              "value", z + 1 <= d.length ? new String(d, z + 1, d.length - z - 1,
                  java.nio.charset.StandardCharsets.ISO_8859_1) : ""));
          break;
        }
        default:
          break;
      }
    }
    if (text.hasChildNodes()) {
      root.appendChild(text);
    }
    return root;
  }
}
