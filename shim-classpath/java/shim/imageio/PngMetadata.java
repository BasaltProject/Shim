/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

public final class PngMetadata extends IIOMetadata {
  public static final String kFormat = "javax_imageio_png_1.0";
  private static final String[] kColorTypes = {"Grayscale", null, "RGB", "Palette", "GrayAlpha", null, "RGBAlpha"};
  private static final String[] kIntents = {"Perceptual", "Relative colorimetric", "Saturation",
      "Absolute colorimetric"};
  private static final String[] kOrder = {"IHDR", "PLTE", "bKGD", "cHRM", "gAMA", "hIST", "iCCP", "iTXt", "pHYs",
      "sBIT", "sPLT", "sRGB", "tEXt", "tIME", "tRNS", "zTXt", "UnknownChunks"};
  private final ImageTypeSpecifier defaults;
  private IIOMetadataNode root;

  private PngMetadata(IIOMetadataNode root, ImageTypeSpecifier defaults) {
    super(true, kFormat, null, null, null);
    this.root = root;
    this.defaults = defaults;
  }

  public static PngMetadata defaultFor(ImageTypeSpecifier type) {
    return new PngMetadata(defaultTree(type), type);
  }

  private static IIOMetadataNode el(String name, String... kv) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    for (int i = 0; i < kv.length; i += 2) {
      n.setAttribute(kv[i], kv[i + 1]);
    }
    return n;
  }

  private static String s(int v) { return Integer.toString(v); }

  private static IIOMetadataNode defaultTree(ImageTypeSpecifier type) {
    IIOMetadataNode r = new IIOMetadataNode(kFormat);
    String colorType = "RGB";
    int depth = 8;
    IndexColorModel palette = null;
    if (type != null) {
      ColorModel cm = type.getColorModel();
      boolean gray = cm.getColorSpace().getType() == java.awt.color.ColorSpace.TYPE_GRAY;
      if (cm instanceof IndexColorModel) {
        palette = (IndexColorModel) cm;
        colorType = "Palette";
        int bits = palette.getPixelSize();
        depth = bits <= 1 ? 1 : bits <= 2 ? 2 : bits <= 4 ? 4 : 8;
        if (isGrayRamp(palette)) {
          palette = null;
          colorType = "Grayscale";
        }
      } else if (gray) {
        colorType = cm.hasAlpha() ? "GrayAlpha" : "Grayscale";
        depth = cm.getComponentSize(0) >= 16 ? 16 : 8;
      } else {
        colorType = cm.hasAlpha() ? "RGBAlpha" : "RGB";
        depth = cm.getComponentSize(0) >= 16 ? 16 : 8;
      }
    }
    r.appendChild(el("IHDR", "width", "0", "height", "0", "bitDepth", s(depth), "colorType", colorType,
        "compressionMethod", "deflate", "filterMethod", "adaptive", "interlaceMethod", "none"));
    if (palette != null) {
      IIOMetadataNode p = new IIOMetadataNode("PLTE");
      for (int i = 0; i < palette.getMapSize(); i++) {
        p.appendChild(el("PLTEEntry", "index", s(i), "red", s(palette.getRed(i)), "green", s(palette.getGreen(i)),
            "blue", s(palette.getBlue(i))));
      }
      r.appendChild(p);
    }
    return r;
  }

  private static boolean isGrayRamp(IndexColorModel icm) {
    int n = icm.getMapSize();
    if (n != 1 << icm.getPixelSize() || icm.hasAlpha() || n > 256) {
      return false;
    }
    for (int i = 0; i < n; i++) {
      int v = i * 255 / (n - 1);
      if (icm.getRed(i) != v || icm.getGreen(i) != v || icm.getBlue(i) != v) {
        return false;
      }
    }
    return true;
  }

  public boolean isReadOnly() { return false; }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (kFormat.equals(formatName)) {
      return root.cloneNode(true);
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  public void reset() {
    root = defaultTree(defaults);
  }

  private IIOMetadataNode child(String name) {
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (c.getNodeName().equals(name)) {
        return (IIOMetadataNode) c;
      }
    }
    return null;
  }

  private void put(IIOMetadataNode node) {
    IIOMetadataNode old = child(node.getNodeName());
    if (old != null) {
      root.replaceChild(node, old);
      return;
    }
    int rank = rank(node.getNodeName());
    for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
      if (rank(c.getNodeName()) > rank) {
        root.insertBefore(node, c);
        return;
      }
    }
    root.appendChild(node);
  }

  private static int rank(String name) {
    for (int i = 0; i < kOrder.length; i++) {
      if (kOrder[i].equals(name)) {
        return i;
      }
    }
    return kOrder.length;
  }

  private static String attr(Node n, String name) {
    Node a = n.getAttributes() == null ? null : n.getAttributes().getNamedItem(name);
    return a == null ? null : a.getNodeValue();
  }

  private static String need(Node n, String name) throws IIOInvalidTreeException {
    String v = attr(n, name);
    if (v == null) {
      throw new IIOInvalidTreeException("Required attribute " + name + " not present!", n);
    }
    return v;
  }

  private static int needInt(Node n, String name, int lo, int hi) throws IIOInvalidTreeException {
    try {
      int v = Integer.parseInt(need(n, name).trim());
      if (v < lo || v > hi) {
        throw new IIOInvalidTreeException(name + " attribute out of range!", n);
      }
      return v;
    } catch (NumberFormatException e) {
      throw new IIOInvalidTreeException("Bad value for " + name + " attribute!", n);
    }
  }

  public void mergeTree(String formatName, Node r) throws IIOInvalidTreeException {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      mergeStandard(r);
      return;
    }
    if (!kFormat.equals(formatName)) {
      throw new IllegalArgumentException("Not a recognized format!");
    }
    if (r == null || !kFormat.equals(r.getNodeName())) {
      throw new IIOInvalidTreeException("Root must be " + kFormat, r);
    }
    for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling()) {
      String name = c.getNodeName();
      if (rank(name) == kOrder.length) {
        throw new IIOInvalidTreeException("Unknown child of root node!", c);
      }
      validate(c);
      IIOMetadataNode copy = (IIOMetadataNode) deepCopy(c);
      if (name.equals("tRNS") && copy.getFirstChild() != null
          && copy.getFirstChild().getNodeName().equals("tRNS_Palette")) {
        copy = (IIOMetadataNode) fillPaletteAlpha(copy);
      }
      if (name.equals("tEXt") || name.equals("iTXt") || name.equals("zTXt")) {
        IIOMetadataNode old = child(name);
        if (old != null) {
          for (Node e = copy.getFirstChild(); e != null; e = e.getNextSibling()) {
            old.appendChild(deepCopy(e));
          }
          continue;
        }
      }
      put(copy);
    }
  }

  private static Node fillPaletteAlpha(IIOMetadataNode trns) {
    Node pal = trns.getFirstChild();
    int max = -1;
    for (Node e = pal.getFirstChild(); e != null; e = e.getNextSibling()) {
      max = Math.max(max, Integer.parseInt(attr(e, "index")));
    }
    String[] alpha = new String[max + 1];
    for (Node e = pal.getFirstChild(); e != null; e = e.getNextSibling()) {
      alpha[Integer.parseInt(attr(e, "index"))] = attr(e, "alpha");
    }
    IIOMetadataNode out = new IIOMetadataNode("tRNS");
    IIOMetadataNode p = new IIOMetadataNode("tRNS_Palette");
    for (int i = 0; i <= max; i++) {
      p.appendChild(el("tRNS_PaletteEntry", "index", s(i), "alpha", alpha[i] == null ? "0" : alpha[i]));
    }
    out.appendChild(p);
    return out;
  }

  private static Node deepCopy(Node n) {
    IIOMetadataNode out = new IIOMetadataNode(n.getNodeName());
    if (n.getAttributes() != null) {
      for (int i = 0; i < n.getAttributes().getLength(); i++) {
        out.setAttribute(n.getAttributes().item(i).getNodeName(), n.getAttributes().item(i).getNodeValue());
      }
    }
    if (n instanceof IIOMetadataNode) {
      out.setUserObject(((IIOMetadataNode) n).getUserObject());
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
      out.appendChild(deepCopy(c));
    }
    return out;
  }

  private void validate(Node c) throws IIOInvalidTreeException {
    switch (c.getNodeName()) {
    case "IHDR":
      need(c, "width");
      need(c, "height");
      need(c, "bitDepth");
      need(c, "colorType");
      break;
    case "gAMA":
      needInt(c, "value", 0, Integer.MAX_VALUE);
      break;
    case "pHYs":
      needInt(c, "pixelsPerUnitXAxis", 0, Integer.MAX_VALUE);
      needInt(c, "pixelsPerUnitYAxis", 0, Integer.MAX_VALUE);
      need(c, "unitSpecifier");
      break;
    case "tIME":
      needInt(c, "year", 0, 65535);
      needInt(c, "month", 1, 12);
      needInt(c, "day", 1, 31);
      needInt(c, "hour", 0, 23);
      needInt(c, "minute", 0, 59);
      needInt(c, "second", 0, 60);
      break;
    case "sRGB":
      need(c, "renderingIntent");
      break;
    case "tEXt":
      for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
        need(e, "keyword");
        need(e, "value");
      }
      break;
    case "zTXt":
      for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
        need(e, "keyword");
        need(e, "text");
      }
      break;
    case "iTXt":
      for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
        need(e, "keyword");
        need(e, "text");
      }
      break;
    default:
      break;
    }
  }

  private void mergeStandard(Node r) throws IIOInvalidTreeException {
    for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling()) {
      switch (c.getNodeName()) {
      case "Chroma":
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("Gamma")) {
            put(el("gAMA", "value", s(Math.round(Float.parseFloat(need(p, "value")) * 100000f))));
          } else if (p.getNodeName().equals("Palette")) {
            IIOMetadataNode plte = new IIOMetadataNode("PLTE");
            IIOMetadataNode alpha = new IIOMetadataNode("tRNS_Palette");
            boolean anyAlpha = false;
            for (Node e = p.getFirstChild(); e != null; e = e.getNextSibling()) {
              plte.appendChild(el("PLTEEntry", "index", need(e, "index"), "red", need(e, "red"), "green",
                  need(e, "green"), "blue", need(e, "blue")));
              String a = attr(e, "alpha");
              if (a != null) {
                anyAlpha |= !a.equals("255");
                alpha.appendChild(el("tRNS_PaletteEntry", "index", need(e, "index"), "alpha", a));
              }
            }
            put(plte);
            if (anyAlpha) {
              IIOMetadataNode t = new IIOMetadataNode("tRNS");
              t.appendChild(alpha);
              put(t);
            }
          }
        }
        break;
      case "Dimension":
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("HorizontalPixelSize") || p.getNodeName().equals("VerticalPixelSize")) {
            String h = null;
            String v = null;
            for (Node q = c.getFirstChild(); q != null; q = q.getNextSibling()) {
              if (q.getNodeName().equals("HorizontalPixelSize")) {
                h = need(q, "value");
              } else if (q.getNodeName().equals("VerticalPixelSize")) {
                v = need(q, "value");
              }
            }
            if (h != null && v != null) {
              put(el("pHYs", "pixelsPerUnitXAxis", s(Math.round(1000f * Float.parseFloat(h))),
                  "pixelsPerUnitYAxis", s(Math.round(1000f * Float.parseFloat(v))), "unitSpecifier", "meter"));
            }
            break;
          }
        }
        break;
      case "Document":
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("ImageModificationTime")) {
            put(el("tIME", "year", need(p, "year"), "month", need(p, "month"), "day", need(p, "day"), "hour",
                attr(p, "hour") == null ? "0" : attr(p, "hour"), "minute",
                attr(p, "minute") == null ? "0" : attr(p, "minute"), "second",
                attr(p, "second") == null ? "0" : attr(p, "second")));
          }
        }
        break;
      case "Text":
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          String keyword = need(p, "keyword");
          String value = need(p, "value");
          String comp = attr(p, "compression");
          String lang = attr(p, "language");
          boolean zip = "zip".equals(comp);
          IIOMetadataNode e;
          String chunk;
          if (lang != null && !lang.isEmpty() || (attr(p, "encoding") != null
              && !attr(p, "encoding").equalsIgnoreCase("ISO-8859-1") && !zip)) {
            chunk = "iTXt";
            e = el("iTXtEntry", "keyword", keyword, "compressionFlag", zip ? "TRUE" : "FALSE", "compressionMethod",
                "0", "languageTag", lang == null ? "" : lang, "translatedKeyword", "", "text", value);
          } else if (zip) {
            chunk = "zTXt";
            e = el("zTXtEntry", "keyword", keyword, "compressionMethod", "deflate", "text", value);
          } else {
            chunk = "tEXt";
            e = el("tEXtEntry", "keyword", keyword, "value", value);
          }
          IIOMetadataNode holder = child(chunk);
          if (holder == null) {
            holder = new IIOMetadataNode(chunk);
            put(holder);
          }
          holder.appendChild(e);
        }
        break;
      case "Transparency":
        for (Node p = c.getFirstChild(); p != null; p = p.getNextSibling()) {
          if (p.getNodeName().equals("TransparentColor")) {
            String[] v = need(p, "value").trim().split("\\s+");
            IIOMetadataNode t = new IIOMetadataNode("tRNS");
            if (v.length == 1) {
              t.appendChild(el("tRNS_Grayscale", "gray", v[0]));
            } else if (v.length == 3) {
              t.appendChild(el("tRNS_RGB", "red", v[0], "green", v[1], "blue", v[2]));
            }
            put(t);
          }
        }
        break;
      default:
        break;
      }
    }
  }

  private String ihdr(String name) {
    IIOMetadataNode h = child("IHDR");
    String v = h == null ? null : attr(h, name);
    return v == null ? "" : v;
  }

  public String colorTypeName() { return ihdr("colorType"); }

  public boolean interlaced() { return ihdr("interlaceMethod").equals("adam7"); }

  public void setInterlaced(boolean on) {
    IIOMetadataNode h = child("IHDR");
    if (h != null) {
      h.setAttribute("interlaceMethod", on ? "adam7" : "none");
    }
  }

  public static PngMetadata parse(ImageInputStream in) throws IOException {
    in.seek(8);
    IIOMetadataNode r = new IIOMetadataNode(kFormat);
    PngMetadata m = new PngMetadata(r, null);
    IIOMetadataNode text = new IIOMetadataNode("tEXt");
    IIOMetadataNode itxt = new IIOMetadataNode("iTXt");
    IIOMetadataNode ztxt = new IIOMetadataNode("zTXt");
    IIOMetadataNode unknown = new IIOMetadataNode("UnknownChunks");
    String colorType = "RGB";
    int paletteSize = 0;
    List<Object[]> deferred = new ArrayList<>();
    while (true) {
      int len;
      try {
        len = in.readInt();
      } catch (java.io.EOFException e) {
        break;
      }
      byte[] t = new byte[4];
      in.readFully(t);
      String type = new String(t, StandardCharsets.ISO_8859_1);
      byte[] d = new byte[len];
      in.readFully(d);
      in.skipBytes(4);
      if (type.equals("IEND")) {
        break;
      }
      switch (type) {
      case "IDAT":
        break;
      case "IHDR": {
        int ct = d[9] & 0xff;
        colorType = ct < kColorTypes.length && kColorTypes[ct] != null ? kColorTypes[ct] : "RGB";
        m.put(el("IHDR", "width", s(i32(d, 0)), "height", s(i32(d, 4)), "bitDepth", s(d[8] & 0xff), "colorType",
            colorType, "compressionMethod", "deflate", "filterMethod", "adaptive", "interlaceMethod",
            d[12] == 0 ? "none" : "adam7"));
        break;
      }
      case "PLTE": {
        IIOMetadataNode p = new IIOMetadataNode("PLTE");
        paletteSize = d.length / 3;
        for (int i = 0; i < paletteSize; i++) {
          p.appendChild(el("PLTEEntry", "index", s(i), "red", s(d[3 * i] & 0xff), "green", s(d[3 * i + 1] & 0xff),
              "blue", s(d[3 * i + 2] & 0xff)));
        }
        m.put(p);
        break;
      }
      case "gAMA":
        m.put(el("gAMA", "value", s(i32(d, 0))));
        break;
      case "cHRM":
        m.put(el("cHRM", "whitePointX", s(i32(d, 0)), "whitePointY", s(i32(d, 4)), "redX", s(i32(d, 8)), "redY",
            s(i32(d, 12)), "greenX", s(i32(d, 16)), "greenY", s(i32(d, 20)), "blueX", s(i32(d, 24)), "blueY",
            s(i32(d, 28))));
        break;
      case "pHYs":
        m.put(el("pHYs", "pixelsPerUnitXAxis", s(i32(d, 0)), "pixelsPerUnitYAxis", s(i32(d, 4)), "unitSpecifier",
            d[8] == 1 ? "meter" : "unknown"));
        break;
      case "sRGB":
        m.put(el("sRGB", "renderingIntent", kIntents[Math.min(d[0] & 0xff, 3)]));
        break;
      case "tIME":
        m.put(el("tIME", "year", s(((d[0] & 0xff) << 8) | (d[1] & 0xff)), "month", s(d[2] & 0xff), "day",
            s(d[3] & 0xff), "hour", s(d[4] & 0xff), "minute", s(d[5] & 0xff), "second", s(d[6] & 0xff)));
        break;
      case "bKGD":
      case "tRNS":
      case "sBIT":
        deferred.add(new Object[] {type, d});
        break;
      case "tEXt": {
        int z = zero(d, 0);
        text.appendChild(el("tEXtEntry", "keyword", latin(d, 0, z), "value", z + 1 <= d.length
            ? latin(d, z + 1, d.length - z - 1) : ""));
        break;
      }
      case "zTXt": {
        int z = zero(d, 0);
        String value = z + 2 <= d.length ? latin(inflate(d, z + 2, d.length - z - 2), 0, -1) : "";
        ztxt.appendChild(el("zTXtEntry", "keyword", latin(d, 0, z), "compressionMethod", "deflate", "text", value));
        break;
      }
      case "iTXt": {
        int z = zero(d, 0);
        boolean compressed = z + 1 < d.length && d[z + 1] != 0;
        int lang = z + 3;
        int langEnd = zero(d, lang);
        int transEnd = zero(d, langEnd + 1);
        byte[] body = transEnd + 1 <= d.length ? java.util.Arrays.copyOfRange(d, transEnd + 1, d.length) : new byte[0];
        if (compressed) {
          body = inflate(body, 0, body.length);
        }
        itxt.appendChild(el("iTXtEntry", "keyword", latin(d, 0, z), "compressionFlag", compressed ? "TRUE" : "FALSE",
            "compressionMethod", "0", "languageTag", latin(d, lang, langEnd - lang), "translatedKeyword",
            new String(d, langEnd + 1, Math.max(0, transEnd - langEnd - 1), StandardCharsets.UTF_8), "text",
            new String(body, StandardCharsets.UTF_8)));
        break;
      }
      case "iCCP": {
        int z = zero(d, 0);
        IIOMetadataNode n = el("iCCP", "profileName", latin(d, 0, z), "compressionMethod", "deflate");
        n.setUserObject(z + 2 <= d.length ? inflate(d, z + 2, d.length - z - 2) : new byte[0]);
        m.put(n);
        break;
      }
      case "hIST": {
        IIOMetadataNode n = new IIOMetadataNode("hIST");
        for (int i = 0; i + 1 < d.length; i += 2) {
          n.appendChild(el("hISTEntry", "index", s(i / 2), "value", s(((d[i] & 0xff) << 8) | (d[i + 1] & 0xff))));
        }
        m.put(n);
        break;
      }
      default: {
        if ((t[0] & 0x20) == 0) {
          break;
        }
        IIOMetadataNode u = el("UnknownChunk", "type", type);
        u.setUserObject(d);
        unknown.appendChild(u);
        break;
      }
      }
    }
    for (Object[] o : deferred) {
      m.put(deferredChunk((String) o[0], (byte[]) o[1], colorType, paletteSize));
    }
    if (text.hasChildNodes()) {
      m.put(text);
    }
    if (itxt.hasChildNodes()) {
      m.put(itxt);
    }
    if (ztxt.hasChildNodes()) {
      m.put(ztxt);
    }
    if (unknown.hasChildNodes()) {
      m.put(unknown);
    }
    return m;
  }

  private static IIOMetadataNode deferredChunk(String type, byte[] d, String colorType, int paletteSize) {
    IIOMetadataNode n = new IIOMetadataNode(type);
    switch (type) {
    case "bKGD":
      if (colorType.equals("Palette")) {
        n.appendChild(el("bKGD_Palette", "index", s(d[0] & 0xff)));
      } else if (colorType.startsWith("Gray")) {
        n.appendChild(el("bKGD_Grayscale", "gray", s(u16(d, 0))));
      } else {
        n.appendChild(el("bKGD_RGB", "red", s(u16(d, 0)), "green", s(u16(d, 2)), "blue", s(u16(d, 4))));
      }
      break;
    case "tRNS":
      if (colorType.equals("Palette")) {
        IIOMetadataNode p = new IIOMetadataNode("tRNS_Palette");
        for (int i = 0; i < d.length; i++) {
          p.appendChild(el("tRNS_PaletteEntry", "index", s(i), "alpha", s(d[i] & 0xff)));
        }
        n.appendChild(p);
      } else if (colorType.startsWith("Gray")) {
        n.appendChild(el("tRNS_Grayscale", "gray", s(u16(d, 0))));
      } else {
        n.appendChild(el("tRNS_RGB", "red", s(u16(d, 0)), "green", s(u16(d, 2)), "blue", s(u16(d, 4))));
      }
      break;
    default:
      switch (colorType) {
      case "Grayscale":
        n.appendChild(el("sBIT_Grayscale", "gray", s(d[0] & 0xff)));
        break;
      case "GrayAlpha":
        n.appendChild(el("sBIT_GrayAlpha", "gray", s(d[0] & 0xff), "alpha", s(d[1] & 0xff)));
        break;
      case "RGBAlpha":
        n.appendChild(el("sBIT_RGBAlpha", "red", s(d[0] & 0xff), "green", s(d[1] & 0xff), "blue", s(d[2] & 0xff),
            "alpha", s(d[3] & 0xff)));
        break;
      default:
        n.appendChild(el(colorType.equals("Palette") ? "sBIT_Palette" : "sBIT_RGB", "red", s(d[0] & 0xff), "green",
            s(d[1] & 0xff), "blue", s(d[2] & 0xff)));
        break;
      }
      break;
    }
    return n;
  }

  private static int u16(byte[] d, int o) { return ((d[o] & 0xff) << 8) | (d[o + 1] & 0xff); }

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

  private static String latin(byte[] d, int off, int len) {
    return new String(d, off, len < 0 ? d.length - off : len, StandardCharsets.ISO_8859_1);
  }

  private static byte[] inflate(byte[] d, int off, int len) throws IOException {
    try (InflaterInputStream in = new InflaterInputStream(new java.io.ByteArrayInputStream(d, off, len))) {
      return in.readAllBytes();
    }
  }

  private static byte[] deflate(byte[] d) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (DeflaterOutputStream z = new DeflaterOutputStream(out)) {
      z.write(d);
    }
    return out.toByteArray();
  }

  public interface ChunkSink {
    void chunk(String type, byte[] body) throws IOException;
  }

  public void writeEarly(ChunkSink sink, String colorType) throws IOException {
    IIOMetadataNode n;
    if ((n = child("cHRM")) != null) {
      byte[] b = new byte[32];
      String[] k = {"whitePointX", "whitePointY", "redX", "redY", "greenX", "greenY", "blueX", "blueY"};
      for (int i = 0; i < 8; i++) {
        put32(b, 4 * i, Integer.parseInt(attr(n, k[i])));
      }
      sink.chunk("cHRM", b);
    }
    if ((n = child("gAMA")) != null) {
      byte[] b = new byte[4];
      put32(b, 0, Integer.parseInt(attr(n, "value")));
      sink.chunk("gAMA", b);
    }
    if ((n = child("iCCP")) != null && n.getUserObject() instanceof byte[]) {
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      o.write(attr(n, "profileName").getBytes(StandardCharsets.ISO_8859_1));
      o.write(0);
      o.write(0);
      o.write(deflate((byte[]) n.getUserObject()));
      sink.chunk("iCCP", o.toByteArray());
    }
    if ((n = child("sBIT")) != null && n.getFirstChild() != null) {
      Node c = n.getFirstChild();
      String[] keys = {"gray", "alpha", "red", "green", "blue"};
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      String[] order = c.getNodeName().equals("sBIT_GrayAlpha") ? new String[] {"gray", "alpha"}
          : c.getNodeName().equals("sBIT_Grayscale") ? new String[] {"gray"}
          : c.getNodeName().equals("sBIT_RGBAlpha") ? new String[] {"red", "green", "blue", "alpha"}
          : new String[] {"red", "green", "blue"};
      for (String key : order) {
        o.write(Integer.parseInt(attr(c, key)));
      }
      sink.chunk("sBIT", o.toByteArray());
    }
    if ((n = child("sRGB")) != null) {
      int idx = 0;
      for (int i = 0; i < kIntents.length; i++) {
        if (kIntents[i].equals(attr(n, "renderingIntent"))) {
          idx = i;
        }
      }
      sink.chunk("sRGB", new byte[] {(byte) idx});
    }
  }

  public void writeLate(ChunkSink sink, String colorType, boolean skipTrns) throws IOException {
    IIOMetadataNode n;
    if ((n = child("hIST")) != null) {
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
        int v = Integer.parseInt(attr(c, "value"));
        o.write(v >> 8);
        o.write(v);
      }
      sink.chunk("hIST", o.toByteArray());
    }
    if (!skipTrns && (n = child("tRNS")) != null && n.getFirstChild() != null) {
      Node c = n.getFirstChild();
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      if (c.getNodeName().equals("tRNS_Palette")) {
        int max = -1;
        for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
          max = Math.max(max, Integer.parseInt(attr(e, "index")));
        }
        byte[] a = new byte[max + 1];
        java.util.Arrays.fill(a, (byte) 0);
        for (Node e = c.getFirstChild(); e != null; e = e.getNextSibling()) {
          a[Integer.parseInt(attr(e, "index"))] = (byte) Integer.parseInt(attr(e, "alpha"));
        }
        o.write(a);
      } else if (c.getNodeName().equals("tRNS_Grayscale")) {
        put16(o, Integer.parseInt(attr(c, "gray")));
      } else {
        put16(o, Integer.parseInt(attr(c, "red")));
        put16(o, Integer.parseInt(attr(c, "green")));
        put16(o, Integer.parseInt(attr(c, "blue")));
      }
      sink.chunk("tRNS", o.toByteArray());
    }
    if ((n = child("bKGD")) != null && n.getFirstChild() != null) {
      Node c = n.getFirstChild();
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      if (c.getNodeName().equals("bKGD_Palette")) {
        o.write(Integer.parseInt(attr(c, "index")));
      } else if (c.getNodeName().equals("bKGD_Grayscale")) {
        put16(o, Integer.parseInt(attr(c, "gray")));
      } else {
        put16(o, Integer.parseInt(attr(c, "red")));
        put16(o, Integer.parseInt(attr(c, "green")));
        put16(o, Integer.parseInt(attr(c, "blue")));
      }
      sink.chunk("bKGD", o.toByteArray());
    }
    if ((n = child("pHYs")) != null) {
      byte[] b = new byte[9];
      put32(b, 0, Integer.parseInt(attr(n, "pixelsPerUnitXAxis")));
      put32(b, 4, Integer.parseInt(attr(n, "pixelsPerUnitYAxis")));
      b[8] = (byte) ("meter".equals(attr(n, "unitSpecifier")) ? 1 : 0);
      sink.chunk("pHYs", b);
    }
    if ((n = child("tIME")) != null) {
      int y = Integer.parseInt(attr(n, "year"));
      sink.chunk("tIME", new byte[] {(byte) (y >> 8), (byte) y, (byte) Integer.parseInt(attr(n, "month")),
          (byte) Integer.parseInt(attr(n, "day")), (byte) Integer.parseInt(attr(n, "hour")),
          (byte) Integer.parseInt(attr(n, "minute")), (byte) Integer.parseInt(attr(n, "second"))});
    }
    if ((n = child("tEXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(attr(e, "keyword").getBytes(StandardCharsets.ISO_8859_1));
        o.write(0);
        o.write(attr(e, "value").getBytes(StandardCharsets.ISO_8859_1));
        sink.chunk("tEXt", o.toByteArray());
      }
    }
    if ((n = child("iTXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        boolean comp = "TRUE".equalsIgnoreCase(attr(e, "compressionFlag"));
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(attr(e, "keyword").getBytes(StandardCharsets.ISO_8859_1));
        o.write(0);
        o.write(comp ? 1 : 0);
        o.write(0);
        String lang = attr(e, "languageTag");
        o.write((lang == null ? "" : lang).getBytes(StandardCharsets.ISO_8859_1));
        o.write(0);
        String tr = attr(e, "translatedKeyword");
        o.write((tr == null ? "" : tr).getBytes(StandardCharsets.UTF_8));
        o.write(0);
        byte[] text = attr(e, "text").getBytes(StandardCharsets.UTF_8);
        o.write(comp ? deflate(text) : text);
        sink.chunk("iTXt", o.toByteArray());
      }
    }
    if ((n = child("zTXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(attr(e, "keyword").getBytes(StandardCharsets.ISO_8859_1));
        o.write(0);
        o.write(0);
        o.write(deflate(attr(e, "text").getBytes(StandardCharsets.ISO_8859_1)));
        sink.chunk("zTXt", o.toByteArray());
      }
    }
    if ((n = child("UnknownChunks")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        Object data = ((IIOMetadataNode) e).getUserObject();
        if (data instanceof byte[]) {
          sink.chunk(attr(e, "type"), (byte[]) data);
        }
      }
    }
  }

  private static void put32(byte[] b, int o, int v) {
    b[o] = (byte) (v >>> 24);
    b[o + 1] = (byte) (v >>> 16);
    b[o + 2] = (byte) (v >>> 8);
    b[o + 3] = (byte) v;
  }

  private static void put16(ByteArrayOutputStream o, int v) {
    o.write(v >> 8);
    o.write(v);
  }

  private int channels() {
    switch (colorTypeName()) {
    case "Grayscale":
    case "Palette":
      return 1;
    case "GrayAlpha":
      return 2;
    case "RGBAlpha":
      return 4;
    default:
      return 3;
    }
  }

  private boolean paletteHasAlpha() {
    IIOMetadataNode t = child("tRNS");
    return t != null && t.getFirstChild() != null && t.getFirstChild().getNodeName().equals("tRNS_Palette");
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    String ct = colorTypeName();
    boolean gray = ct.startsWith("Gray");
    chroma.appendChild(el("ColorSpaceType", "name", gray ? "GRAY" : "RGB"));
    int n = channels();
    if (ct.equals("Palette") && paletteHasAlpha()) {
      n = 4;
    } else if (ct.equals("Palette")) {
      n = 3;
    }
    chroma.appendChild(el("NumChannels", "value", s(n)));
    IIOMetadataNode g = child("gAMA");
    if (g != null) {
      chroma.appendChild(el("Gamma", "value", Float.toString(Integer.parseInt(attr(g, "value")) / 100000.0f)));
    }
    chroma.appendChild(el("BlackIsZero", "value", "TRUE"));
    IIOMetadataNode plte = child("PLTE");
    if (plte != null && ct.equals("Palette")) {
      IIOMetadataNode p = new IIOMetadataNode("Palette");
      IIOMetadataNode trns = child("tRNS");
      for (Node e = plte.getFirstChild(); e != null; e = e.getNextSibling()) {
        String alpha = "255";
        if (trns != null && trns.getFirstChild() != null && trns.getFirstChild().getNodeName().equals("tRNS_Palette")) {
          for (Node a = trns.getFirstChild().getFirstChild(); a != null; a = a.getNextSibling()) {
            if (attr(a, "index").equals(attr(e, "index"))) {
              alpha = attr(a, "alpha");
            }
          }
        }
        IIOMetadataNode pe = el("PaletteEntry", "index", attr(e, "index"), "red", attr(e, "red"), "green",
            attr(e, "green"), "blue", attr(e, "blue"));
        if (trns != null) {
          pe.setAttribute("alpha", alpha);
        }
        p.appendChild(pe);
      }
      chroma.appendChild(p);
    }
    IIOMetadataNode bk = child("bKGD");
    if (bk != null && bk.getFirstChild() != null) {
      Node c = bk.getFirstChild();
      if (c.getNodeName().equals("bKGD_Palette")) {
        chroma.appendChild(el("BackgroundIndex", "value", attr(c, "index")));
      } else if (c.getNodeName().equals("bKGD_Grayscale")) {
        chroma.appendChild(el("BackgroundColor", "red", attr(c, "gray"), "green", attr(c, "gray"), "blue",
            attr(c, "gray")));
      } else {
        chroma.appendChild(el("BackgroundColor", "red", attr(c, "red"), "green", attr(c, "green"), "blue",
            attr(c, "blue")));
      }
    }
    return chroma;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    c.appendChild(el("CompressionTypeName", "value", "deflate"));
    c.appendChild(el("Lossless", "value", "TRUE"));
    c.appendChild(el("NumProgressiveScans", "value", interlaced() ? "7" : "1"));
    return c;
  }

  protected IIOMetadataNode getStandardDataNode() {
    IIOMetadataNode d = new IIOMetadataNode("Data");
    d.appendChild(el("PlanarConfiguration", "value", "PixelInterleaved"));
    String ct = colorTypeName();
    d.appendChild(el("SampleFormat", "value", ct.equals("Palette") ? "Index" : "UnsignedIntegral"));
    int n = ct.equals("Palette") ? (paletteHasAlpha() ? 4 : 3) : channels();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.append(i == 0 ? "" : " ").append(ihdr("bitDepth"));
    }
    d.appendChild(el("BitsPerSample", "value", sb.toString()));
    IIOMetadataNode sbit = child("sBIT");
    if (sbit != null && sbit.getFirstChild() != null) {
      Node c = sbit.getFirstChild();
      String[] order = c.getNodeName().equals("sBIT_GrayAlpha") ? new String[] {"gray", "alpha"}
          : c.getNodeName().equals("sBIT_Grayscale") ? new String[] {"gray"}
          : c.getNodeName().equals("sBIT_RGBAlpha") ? new String[] {"red", "green", "blue", "alpha"}
          : new String[] {"red", "green", "blue"};
      StringBuilder sig = new StringBuilder();
      for (int i = 0; i < order.length; i++) {
        sig.append(i == 0 ? "" : " ").append(attr(c, order[i]));
      }
      d.appendChild(el("SignificantBitsPerSample", "value", sig.toString()));
    }
    return d;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode d = new IIOMetadataNode("Dimension");
    IIOMetadataNode p = child("pHYs");
    float ratio = 1.0f;
    if (p != null) {
      int x = Integer.parseInt(attr(p, "pixelsPerUnitXAxis"));
      int y = Integer.parseInt(attr(p, "pixelsPerUnitYAxis"));
      if (x != 0) {
        ratio = (float) y / x;
      }
    }
    d.appendChild(el("PixelAspectRatio", "value", Float.toString(ratio)));
    d.appendChild(el("ImageOrientation", "value", "Normal"));
    if (p != null && "meter".equals(attr(p, "unitSpecifier"))) {
      d.appendChild(el("HorizontalPixelSize", "value",
          Float.toString(1000.0f / Integer.parseInt(attr(p, "pixelsPerUnitXAxis")))));
      d.appendChild(el("VerticalPixelSize", "value",
          Float.toString(1000.0f / Integer.parseInt(attr(p, "pixelsPerUnitYAxis")))));
    }
    return d;
  }

  protected IIOMetadataNode getStandardDocumentNode() {
    IIOMetadataNode t = child("tIME");
    if (t == null) {
      return null;
    }
    IIOMetadataNode d = new IIOMetadataNode("Document");
    d.appendChild(el("ImageModificationTime", "year", attr(t, "year"), "month", attr(t, "month"), "day",
        attr(t, "day"), "hour", attr(t, "hour"), "minute", attr(t, "minute"), "second", attr(t, "second")));
    return d;
  }

  protected IIOMetadataNode getStandardTextNode() {
    IIOMetadataNode text = new IIOMetadataNode("Text");
    boolean any = false;
    IIOMetadataNode n;
    if ((n = child("tEXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        text.appendChild(el("TextEntry", "keyword", attr(e, "keyword"), "value", attr(e, "value"), "encoding",
            "ISO-8859-1", "compression", "none"));
        any = true;
      }
    }
    if ((n = child("iTXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        text.appendChild(el("TextEntry", "keyword", attr(e, "keyword"), "value", attr(e, "text"), "language",
            attr(e, "languageTag"), "compression", "TRUE".equalsIgnoreCase(attr(e, "compressionFlag")) ? "deflate"
                : "none"));
        any = true;
      }
    }
    if ((n = child("zTXt")) != null) {
      for (Node e = n.getFirstChild(); e != null; e = e.getNextSibling()) {
        text.appendChild(el("TextEntry", "keyword", attr(e, "keyword"), "value", attr(e, "text"), "compression",
            "zip"));
        any = true;
      }
    }
    return any ? text : null;
  }

  protected IIOMetadataNode getStandardTransparencyNode() {
    IIOMetadataNode t = new IIOMetadataNode("Transparency");
    String ct = colorTypeName();
    boolean alpha = ct.equals("GrayAlpha") || ct.equals("RGBAlpha") || (ct.equals("Palette") && paletteHasAlpha());
    t.appendChild(el("Alpha", "value", alpha ? "nonpremultipled" : "none"));
    IIOMetadataNode trns = child("tRNS");
    if (trns != null && trns.getFirstChild() != null) {
      Node c = trns.getFirstChild();
      if (c.getNodeName().equals("tRNS_Palette")) {
        t.appendChild(new IIOMetadataNode("TransparentColor"));
      } else if (c.getNodeName().equals("tRNS_Grayscale")) {
        t.appendChild(el("TransparentColor", "value", attr(c, "gray")));
      } else {
        t.appendChild(el("TransparentColor", "value", attr(c, "red") + " " + attr(c, "green") + " " + attr(c, "blue")));
      }
    }
    return t;
  }
}
