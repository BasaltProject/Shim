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
import java.util.Random;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class TiffCcittModern {
  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    int[][] sizes = {{37, 23}, {100, 40}, {5, 3}, {64, 64}};
    for (String type : new String[] {"CCITT RLE", "CCITT T.4", "CCITT T.6"}) {
      for (int[] s : sizes) {
        BufferedImage image = new BufferedImage(s[0], s[1], BufferedImage.TYPE_BYTE_BINARY);
        Random r = new Random(5);
        for (int y = 0; y < s[1]; y++) {
          for (int x = 0; x < s[0]; x++) {
            boolean on = ((x / (3 + (y % 4))) % 2 == 0) ^ (y % 7 == 3) ^ (r.nextInt(40) == 0);
            image.setRGB(x, y, on ? -1 : 0xff000000);
          }
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionType(type);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageOutputStream out = ImageIO.createImageOutputStream(bytes);
        writer.setOutput(out);
        writer.write(null, new IIOImage(image, null, null), param);
        out.close();
        writer.dispose();
        BufferedImage back = ImageIO.read(new ByteArrayInputStream(bytes.toByteArray()));
        int bad = 0;
        for (int y = 0; y < s[1]; y++) {
          for (int x = 0; x < s[0]; x++) {
            if (back.getRGB(x, y) != image.getRGB(x, y)) {
              bad++;
            }
          }
        }
        System.out.println(type + " " + s[0] + "x" + s[1] + " " + bytes.size() + " "
            + Arrays.hashCode(bytes.toByteArray()) + " bad=" + bad);
      }
    }
  }
}
