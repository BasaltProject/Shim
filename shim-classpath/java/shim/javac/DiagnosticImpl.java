/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public final class DiagnosticImpl implements Diagnostic<JavaFileObject> {
  private final Kind kind;
  private final JavaFileObject source;
  private final long pos;
  private final long start;
  private final long end;
  private final String code;
  private final String message;
  private final LineMapImpl lines;

  public DiagnosticImpl(Kind kind, JavaFileObject source, LineMapImpl lines, long pos, long start, long end,
      String code, String message) {
    this.kind = kind;
    this.source = source;
    this.lines = lines;
    this.pos = pos;
    this.start = start;
    this.end = end;
    this.code = code;
    this.message = message;
  }

  public Kind getKind() {
    return kind;
  }

  public JavaFileObject getSource() {
    return source;
  }

  public long getPosition() {
    return pos;
  }

  public long getStartPosition() {
    return start;
  }

  public long getEndPosition() {
    return end;
  }

  public long getLineNumber() {
    return source == null || pos < 0 || lines == null ? NOPOS : lines.getLineNumber(pos);
  }

  public long getColumnNumber() {
    return source == null || pos < 0 || lines == null ? NOPOS : lines.getColumnNumber(pos);
  }

  public String getCode() {
    return code;
  }

  public String getMessage(Locale locale) {
    return message;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    String prefix;
    switch (kind) {
      case ERROR:
        prefix = "error: ";
        break;
      case WARNING:
      case MANDATORY_WARNING:
        prefix = "warning: ";
        break;
      case NOTE:
        prefix = "Note: ";
        break;
      default:
        prefix = "";
        break;
    }
    if (source != null) {
      sb.append(source.getName());
      if (pos >= 0 && lines != null) {
        sb.append(':').append(lines.getLineNumber(pos));
      }
      sb.append(": ");
    }
    sb.append(prefix).append(message);
    if (source != null && pos >= 0 && lines != null) {
      String text = lines.lineText(pos);
      sb.append('\n').append(text).append('\n');
      long col = lines.getColumnNumber(pos) - 1;
      StringBuilder caret = new StringBuilder();
      int ci = 0;
      for (int i = 0; ci < col && i < text.length(); i++) {
        if (text.charAt(i) == '\t') {
          caret.append('\t');
          ci = (ci / 8 + 1) * 8;
        } else {
          caret.append(' ');
          ci++;
        }
      }
      while (ci < col) {
        caret.append(' ');
        ci++;
      }
      sb.append(caret).append('^');
    }
    return sb.toString();
  }
}
