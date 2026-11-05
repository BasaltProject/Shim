/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

abstract class Wrap {
  public abstract String wrapped();

  public abstract int wrapIndexToSnippetIndex(int wi);

  public abstract int snippetIndexToWrapIndex(int si);

  public abstract int firstSnippetIndex();

  public abstract int lastSnippetIndex();

  public abstract int snippetLineToWrapLine(int snline);

  public abstract int wrapLineToSnippetLine(int wline);

  public abstract int firstSnippetLine();

  public abstract int lastSnippetLine();

  static final class Range {
    final int begin;
    final int end;

    Range(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    @Override
    public String toString() {
      return "Range[" + begin + "," + end + ")";
    }
  }

  static final class SnippetText extends Wrap {
    private final String text;
    private final Range range;

    SnippetText(String text, Range range) {
      this.text = text;
      this.range = range;
    }

    @Override
    public String wrapped() {
      return text;
    }

    @Override
    public int wrapIndexToSnippetIndex(int wi) {
      return wi >= 0 && wi < text.length() ? wi + range.begin : -1;
    }

    @Override
    public int snippetIndexToWrapIndex(int si) {
      return si >= range.begin && si < range.end ? si - range.begin : -1;
    }

    @Override
    public int firstSnippetIndex() {
      return range.begin;
    }

    @Override
    public int lastSnippetIndex() {
      return range.end - 1;
    }

    @Override
    public int snippetLineToWrapLine(int snline) {
      return snline;
    }

    @Override
    public int wrapLineToSnippetLine(int wline) {
      return wline;
    }

    @Override
    public int firstSnippetLine() {
      return 0;
    }

    @Override
    public int lastSnippetLine() {
      int lines = 0;
      for (int i = 0; i < text.length(); i++) {
        if (text.charAt(i) == '\n') {
          lines++;
        }
      }
      return lines;
    }
  }

  static class CompoundWrap extends Wrap {
    private final Object[] parts;

    CompoundWrap(Object... parts) {
      this.parts = parts.clone();
    }

    private static String textOf(Object part) {
      return part instanceof Wrap ? ((Wrap) part).wrapped() : String.valueOf(part);
    }

    @Override
    public String toString() {
      return "CompoundWrap(" + wrapped() + ")";
    }

    @Override
    public String wrapped() {
      StringBuilder sb = new StringBuilder();
      for (Object part : parts) {
        sb.append(textOf(part));
      }
      return sb.toString();
    }

    @Override
    public int wrapIndexToSnippetIndex(int wi) {
      int offset = 0;
      for (Object part : parts) {
        int length = textOf(part).length();
        if (wi >= offset && wi < offset + length) {
          return part instanceof Wrap ? ((Wrap) part).wrapIndexToSnippetIndex(wi - offset) : -1;
        }
        offset += length;
      }
      return -1;
    }

    @Override
    public int snippetIndexToWrapIndex(int si) {
      int offset = 0;
      for (Object part : parts) {
        if (part instanceof Wrap) {
          int wi = ((Wrap) part).snippetIndexToWrapIndex(si);
          if (wi >= 0) {
            return offset + wi;
          }
        }
        offset += textOf(part).length();
      }
      return -1;
    }

    @Override
    public int firstSnippetIndex() {
      for (Object part : parts) {
        if (part instanceof Wrap) {
          int index = ((Wrap) part).firstSnippetIndex();
          if (index >= 0) {
            return index;
          }
        }
      }
      return -1;
    }

    @Override
    public int lastSnippetIndex() {
      for (int i = parts.length - 1; i >= 0; i--) {
        if (parts[i] instanceof Wrap) {
          int index = ((Wrap) parts[i]).lastSnippetIndex();
          if (index >= 0) {
            return index;
          }
        }
      }
      return -1;
    }

    private static int lines(String text) {
      int lines = 0;
      for (int i = 0; i < text.length(); i++) {
        if (text.charAt(i) == '\n') {
          lines++;
        }
      }
      return lines;
    }

    @Override
    public int snippetLineToWrapLine(int snline) {
      int line = 0;
      for (Object part : parts) {
        if (part instanceof Wrap) {
          Wrap wrap = (Wrap) part;
          if (snline >= wrap.firstSnippetLine() && snline <= wrap.lastSnippetLine()) {
            return line + wrap.snippetLineToWrapLine(snline);
          }
        }
        line += lines(textOf(part));
      }
      return -1;
    }

    @Override
    public int wrapLineToSnippetLine(int wline) {
      int line = 0;
      for (Object part : parts) {
        int count = lines(textOf(part));
        if (wline >= line && wline <= line + count) {
          return part instanceof Wrap ? ((Wrap) part).wrapLineToSnippetLine(wline - line) : -1;
        }
        line += count;
      }
      return -1;
    }

    @Override
    public int firstSnippetLine() {
      for (Object part : parts) {
        if (part instanceof Wrap) {
          return ((Wrap) part).firstSnippetLine();
        }
      }
      return -1;
    }

    @Override
    public int lastSnippetLine() {
      for (int i = parts.length - 1; i >= 0; i--) {
        if (parts[i] instanceof Wrap) {
          return ((Wrap) parts[i]).lastSnippetLine();
        }
      }
      return -1;
    }
  }
}
