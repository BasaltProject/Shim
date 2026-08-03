/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.rtf;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

public class RTFEditorKit extends StyledEditorKit {
  public RTFEditorKit() { }

  public String getContentType() { return "text/rtf"; }

  public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
    read(new InputStreamReader(in, "ISO-8859-1"), doc, pos);
  }

  public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = in.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    new RtfReader(sb.toString(), doc, pos).run();
  }

  public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
    out.write(RtfWriter.generate(doc, pos, len).getBytes("ISO-8859-1"));
    out.flush();
  }

  public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
    throw new IOException("RTF is an 8-bit format");
  }

  static Color black() { return Color.black; }
}
