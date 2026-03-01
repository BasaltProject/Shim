/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;

public class ImageBasicsModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  static final String[][] kVectors = {
      {"rgb8", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAIAAADJUWIXAAAAS0lEQVR4nAFAAL//AE3HByBRFZoPifLG2srjRAG7MRKKzF0/4itT5hn5sfkCU15BYjj9BLKlSTFEXTp2A8sG4dqZma1hjpMjBwFmRpG9G8IJEZu1AAAAAElFTkSuQmCC"},
      {"rgba8", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAYAAABGM/VAAAAAX0lEQVR4nAFUAKv/ANx5mt+Em60F1KEKwEQequ60tI76AQsfCr11yo7m2tHG/WPNOyF4hqrdAmZqcOrf9ZmRSvAUQJmhE66xfTCfA9kdZApWD8JoqStHwqEyNRjtNG5rSMYpit90iMUAAAAASUVORK5CYII="},
      {"gray8", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAAAAABjWKqcAAAAIUlEQVR4nGMoFHg5eR1jybqC1Q5Mi99fnj+H+ffrCacTAYpUDHGdDJKeAAAAAElFTkSuQmCC"},
      {"gray4", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAEBAAAAACmqEedAAAAGElEQVR4nGOQtlrH6OBYzHTfuJY5bNMzACQsBXuAn5vTAAAAAElFTkSuQmCC"},
      {"pal8", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAMAAABx7QVyAAAAMFBMVEVXeoxBA/nMGYp/idga8qUAHEAXPxkj9xAs+qFQoSSzxcebuIdhqNs/QQHCKFsVv+ukQKGtAAAABHRSTlMAgP8KIVdpdgAAACBJREFUeJxj4Ofi5eVjZGD/z/SLiZeB/c8/ZhbmP395ATaSBmV0J4PHAAAAAElFTkSuQmCC"},
      {"rgb16", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAEEAIAAACZwb5UAAAAh0lEQVR4nAF8AIP/AFwne1x/6MmBvMuz1irAeNNS1PdPzUxTMf734l9FiAFlS6F2l9MjJPyV8iI797ut8a80434970sXrU/XrgACEC2C+euo2RWpVImNk7FIpV6KNPNZkiwjqPGHN/i8A2+jscHmLnUXvPghFn9uyWD21JYwsuT9FfNpM2fTJgrSQXtDAZclAAAAAElFTkSuQmCC"},
      {"gray8a", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAQAAADsOj3LAAAAN0lEQVR4nAEsANP/AJlf6vafWiM2XMgBtzPRVzyRgfqwdQLHgiJEIQhv6UTDA0vZn7nPMkwZusDHbhRnCXCj3QAAAABJRU5ErkJggg=="},
      {"rgb8i", "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAECAIAAAG+VlKBAAAAT0lEQVR4nAFEALv/AMtjVwChnv4AvZYPADmRThkNuAmtpQBbZ4Dj0MQBRe8tMjjSAJn3D2EBN3f7WOtlY2wS4wGosESBYeSGWPi3dlGSwsbN0B609eWDNAAAAABJRU5ErkJggg=="}
  };

  static String hex(int v) {
    return Integer.toHexString(v);
  }

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    for (String[] v : kVectors) {
      t("decode-" + v[0], () -> {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(v[1])));
        StringBuilder sb = new StringBuilder();
        sb.append(img.getType()).append(' ').append(img.getWidth()).append('x').append(img.getHeight()).append(':');
        for (int y = 0; y < img.getHeight(); y++) {
          for (int x = 0; x < img.getWidth(); x++) {
            sb.append(' ').append(hex(img.getRGB(x, y)));
          }
        }
        return sb;
      });
    }
    t("types", () -> {
      StringBuilder sb = new StringBuilder();
      for (int ty : new int[] {1, 2, 3, 4, 5, 6, 7, 10}) {
        BufferedImage i = new BufferedImage(2, 2, ty);
        i.setRGB(0, 0, 0x80336699);
        i.setRGB(1, 1, 0xffabcdef);
        sb.append(ty).append('=').append(hex(i.getRGB(0, 0))).append('/').append(hex(i.getRGB(1, 0))).append('/')
            .append(hex(i.getRGB(1, 1))).append('/').append(i.getTransparency()).append(' ');
      }
      return sb;
    });
    t("fresh-pixel", () -> {
      StringBuilder sb = new StringBuilder();
      for (int ty : new int[] {1, 2, 3, 4, 5, 6, 7, 10}) {
        sb.append(hex(new BufferedImage(1, 1, ty).getRGB(0, 0))).append(' ');
      }
      return sb;
    });
    t("gray-roundtrip", () -> {
      BufferedImage g = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
      StringBuilder sb = new StringBuilder();
      for (int v = 0; v < 256; v += 5) {
        g.setRGB(0, 0, 0xff000000 | (v << 16) | (v << 8) | v);
        sb.append(hex(g.getRGB(0, 0) & 0xff)).append(' ');
      }
      return sb;
    });
    t("gray-from-color", () -> {
      BufferedImage g = new BufferedImage(2, 1, BufferedImage.TYPE_BYTE_GRAY);
      g.setRGB(0, 0, 0xffff0000);
      g.setRGB(1, 0, 0xff00ff00);
      return hex(g.getRGB(0, 0)) + " " + hex(g.getRGB(1, 0));
    });
    t("bulk", () -> {
      BufferedImage i = new BufferedImage(4, 3, BufferedImage.TYPE_INT_ARGB);
      int[] px = new int[12];
      for (int k = 0; k < 12; k++) {
        px[k] = 0xff000000 | (k * 20 << 16) | (k * 10 << 8) | k;
      }
      i.setRGB(0, 0, 4, 3, px, 0, 4);
      int[] out = i.getRGB(1, 1, 2, 2, null, 0, 2);
      int[] out2 = i.getRGB(0, 0, 4, 3, new int[20], 3, 4);
      return Arrays.toString(Arrays.stream(out).mapToObj(Integer::toHexString).toArray()) + " " + out2.length + " "
          + hex(out2[3]) + " " + hex(out2[14]);
    });
    t("subimage", () -> {
      BufferedImage i = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
      BufferedImage s = i.getSubimage(1, 1, 2, 2);
      s.setRGB(0, 0, 0x123456);
      return hex(i.getRGB(1, 1)) + " " + s.getWidth() + "x" + s.getHeight() + " " + hex(s.getRGB(0, 0)) + " "
          + hex(s.getSubimage(1, 1, 1, 1).getRGB(0, 0));
    });
    t("sub-errors", () -> {
      BufferedImage i = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
      StringBuilder sb = new StringBuilder();
      int[][] args2 = {{-1, 0, 1, 1}, {0, -1, 1, 1}, {0, 0, 0, 1}, {0, 0, 1, 0}, {3, 0, 2, 1}, {0, 3, 1, 2}, {1, 1, 5, 5}};
      for (int[] a : args2) {
        try {
          i.getSubimage(a[0], a[1], a[2], a[3]);
          sb.append("ok ");
        } catch (Exception e) {
          sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(" | ");
        }
      }
      return sb;
    });
    t("bounds", () -> {
      BufferedImage i = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
      StringBuilder sb = new StringBuilder();
      for (int[] a : new int[][] {{-1, 0}, {0, -1}, {2, 0}, {0, 2}}) {
        try {
          i.getRGB(a[0], a[1]);
          sb.append("ok ");
        } catch (Exception e) {
          sb.append(e.getMessage()).append(" | ");
        }
        try {
          i.setRGB(a[0], a[1], 0);
          sb.append("ok ");
        } catch (Exception e) {
          sb.append(e.getMessage()).append(" | ");
        }
      }
      return sb;
    });
    t("ctor-errors", () -> {
      StringBuilder sb = new StringBuilder();
      for (int[] a : new int[][] {{0, 1, 1}, {1, 0, 1}, {-1, -1, 1}, {1, 1, 0}, {1, 1, 99}, {1, 1, -1}}) {
        try {
          new BufferedImage(a[0], a[1], a[2]);
          sb.append("ok ");
        } catch (Exception e) {
          sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(" | ");
        }
      }
      return sb;
    });
    t("png-roundtrip", () -> {
      StringBuilder sb = new StringBuilder();
      for (int ty : new int[] {1, 2, 4, 5, 6, 10}) {
        BufferedImage i = new BufferedImage(7, 5, ty);
        for (int y = 0; y < 5; y++) {
          for (int x = 0; x < 7; x++) {
            i.setRGB(x, y, ((x * 36 + y) << 24) | (x * 30 << 16) | (y * 50 << 8) | (x * y * 7));
          }
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        boolean ok = ImageIO.write(i, "png", bo);
        BufferedImage r = ImageIO.read(new ByteArrayInputStream(bo.toByteArray()));
        boolean same = r.getWidth() == 7 && r.getHeight() == 5;
        for (int y = 0; y < 5 && same; y++) {
          for (int x = 0; x < 7; x++) {
            if (r.getRGB(x, y) != i.getRGB(x, y)) {
              same = false;
            }
          }
        }
        sb.append(ty).append(':').append(ok).append('/').append(r.getType()).append('/').append(same).append(' ');
      }
      return sb;
    });
    t("write-formats", () -> {
      BufferedImage i = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      return ImageIO.write(i, "png", bo) + " " + ImageIO.write(i, "PNG", bo) + " " + ImageIO.write(i, "nope", bo);
    });
    t("write-nulls", () -> {
      StringBuilder sb = new StringBuilder();
      BufferedImage i = new BufferedImage(1, 1, 1);
      try {
        ImageIO.write(null, "png", new ByteArrayOutputStream());
      } catch (Exception e) {
        sb.append(e.getMessage()).append(" | ");
      }
      try {
        ImageIO.write(i, null, new ByteArrayOutputStream());
      } catch (Exception e) {
        sb.append(e.getMessage()).append(" | ");
      }
      try {
        ImageIO.write(i, "png", (java.io.OutputStream) null);
      } catch (Exception e) {
        sb.append(e.getMessage()).append(" | ");
      }
      return sb;
    });
    t("read-nulls", () -> {
      StringBuilder sb = new StringBuilder();
      try {
        ImageIO.read((java.io.InputStream) null);
      } catch (Exception e) {
        sb.append(e.getMessage()).append(" | ");
      }
      try {
        ImageIO.read((java.io.File) null);
      } catch (Exception e) {
        sb.append(e.getMessage()).append(" | ");
      }
      try {
        ImageIO.read(new java.io.File("/nonexistent/x.png"));
      } catch (Exception e) {
        sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(" | ");
      }
      return sb;
    });
    t("read-garbage", () -> ImageIO.read(new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})));
    t("read-truncated", () -> ImageIO.read(new ByteArrayInputStream(Arrays.copyOf(Base64.getDecoder().decode(kVectors[0][1]),
        60))));
    t("image-misc", () -> {
      BufferedImage i = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
      return i.getWidth(null) + " " + i.getHeight(null) + " " + i.getMinX() + " " + i.getMinY() + " "
          + (i.getProperty("x") == java.awt.Image.UndefinedProperty) + " " + i.isAlphaPremultiplied() + " "
          + new BufferedImage(1, 1, 3).isAlphaPremultiplied();
    });
    t("raster", () -> {
      BufferedImage i = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
      i.setRGB(1, 1, 0x80102030);
      var r = i.getRaster();
      return r.getNumBands() + " " + r.getWidth() + " " + r.getSample(1, 1, 0) + " " + r.getSample(1, 1, 1) + " "
          + r.getSample(1, 1, 2) + " " + r.getSample(1, 1, 3);
    });
  }
}
