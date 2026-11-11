/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageCodecParityModern {
  static byte[] jpeg(BufferedImage im, float q) throws Exception {
    ImageWriter w = ImageIO.getImageWritersByFormatName("jpeg").next();
    ImageWriteParam p = w.getDefaultWriteParam();
    p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    p.setCompressionQuality(q);
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    w.setOutput(new MemoryCacheImageOutputStream(bo));
    w.write(null, new IIOImage(im, null, null), p);
    w.dispose();
    return bo.toByteArray();
  }

  static String decodeHash(byte[] d) throws Exception {
    BufferedImage b = ImageIO.read(new ByteArrayInputStream(d));
    long h = 7;
    for (int y = 0; y < b.getHeight(); y++) {
      for (int x = 0; x < b.getWidth(); x++) {
        h = h * 31 + b.getRGB(x, y);
      }
    }
    return Long.toString(h);
  }

  public static void main(String[] args) throws Exception {
    Random r = new Random(11);
    int[][] sizes = {{1, 1}, {8, 8}, {17, 9}, {31, 47}, {100, 77}};
    for (int[] sz : sizes) {
      for (int type : new int[] {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_BYTE_INDEXED}) {
        BufferedImage im = new BufferedImage(sz[0], sz[1], type);
        for (int y = 0; y < sz[1]; y++) {
          for (int x = 0; x < sz[0]; x++) {
            im.setRGB(x, y, 0xff000000 | ((x * 9 & 255) << 16) | (r.nextInt(256) << 8) | (y * 5 & 255));
          }
        }
        if (type != BufferedImage.TYPE_BYTE_INDEXED) {
          for (float q : new float[] {0.75f, 0.2f, 0.95f}) {
            byte[] d = jpeg(im, q);
            System.out.println("jpeg " + sz[0] + "x" + sz[1] + " t" + type + " q" + q + " " + Arrays.hashCode(d) + " "
                + decodeHash(d));
          }
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(im, "png", bo);
        System.out.println("png " + sz[0] + "x" + sz[1] + " t" + type + " " + Arrays.hashCode(bo.toByteArray()));
      }
    }
  }
}
