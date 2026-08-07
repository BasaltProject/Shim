/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.print;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class PSDocument {
  private final OutputStream out;
  private final String title;
  private final int copies;
  private int pages;
  private boolean open;

  PSDocument(OutputStream out, String title, int copies) {
    this.out = out;
    this.title = title;
    this.copies = copies;
  }

  void write(String s) throws IOException {
    out.write(s.getBytes(StandardCharsets.ISO_8859_1));
  }

  void begin() throws IOException {
    write("%!PS-Adobe-3.0\n%%Creator: Basalt Shim\n%%Title: " + title.replace('\n', ' ') + "\n"
        + "%%DocumentData: Clean7Bit\n%%LanguageLevel: 2\n%%EndComments\n"
        + "%%BeginProlog\n/m {moveto} bind def\n/l {lineto} bind def\n/c {curveto} bind def\n"
        + "/h {closepath} bind def\n/n {newpath} bind def\n/f {fill} bind def\n/e {eofill} bind def\n"
        + "/rg {setrgbcolor} bind def\n/q {gsave} bind def\n/Q {grestore} bind def\n%%EndProlog\n");
    if (copies > 1) {
      write("/#copies " + copies + " def\n");
    }
    open = true;
  }

  void beginPage(double width, double height, double deviceScale) throws IOException {
    pages++;
    write("%%Page: " + pages + " " + pages + "\n%%BeginPageSetup\n<< /PageSize [" + num(width) + " " + num(height)
        + "] >> setpagedevice\n0 " + num(height) + " translate " + num(1 / deviceScale) + " " + num(-1 / deviceScale)
        + " scale\n%%EndPageSetup\n");
  }

  void endPage() throws IOException {
    write("showpage\n");
  }

  void end() throws IOException {
    if (open) {
      write("%%Trailer\n%%EOF\n");
      out.flush();
      open = false;
    }
  }

  int pages() {
    return pages;
  }

  static String num(double v) {
    if (v == Math.rint(v) && Math.abs(v) < 1e9) {
      return Long.toString((long) v);
    }
    String s = String.format(java.util.Locale.ROOT, "%.3f", v);
    int end = s.length();
    while (s.charAt(end - 1) == '0') {
      end--;
    }
    if (s.charAt(end - 1) == '.') {
      end--;
    }
    return s.substring(0, end);
  }
}
