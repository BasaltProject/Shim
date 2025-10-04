/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

final class DefaultFileSystem extends FileSystem {
  static final DefaultFileSystem INSTANCE = new DefaultFileSystem();

  private DefaultFileSystem() {}

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getSeparator() {
    return File.separator;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return Collections.singletonList(new BasePath(File.separator));
  }

  @Override
  public Path getPath(String first, String... more) {
    Objects.requireNonNull(first, "first");
    if (more == null || more.length == 0) {
      return new BasePath(first);
    }
    StringBuilder sb = new StringBuilder(first);
    for (String s : more) {
      Objects.requireNonNull(s, "more element");
      if (!s.isEmpty()) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != File.separatorChar) {
          sb.append(File.separatorChar);
        }
        sb.append(s);
      }
    }
    return new BasePath(sb.toString());
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    Objects.requireNonNull(syntaxAndPattern, "syntaxAndPattern");
    int colon = syntaxAndPattern.indexOf(':');
    if (colon <= 0) {
      throw new IllegalArgumentException("Syntax and pattern must be in 'syntax:pattern' form");
    }
    String syntax = syntaxAndPattern.substring(0, colon);
    String pattern = syntaxAndPattern.substring(colon + 1);
    Pattern regex;
    if (syntax.equalsIgnoreCase("glob")) {
      regex = Pattern.compile(globToRegex(pattern));
    } else if (syntax.equalsIgnoreCase("regex")) {
      regex = Pattern.compile(pattern);
    } else {
      throw new UnsupportedOperationException("Unsupported syntax: " + syntax);
    }
    return path -> regex.matcher(path.toString()).matches();
  }

  private static String globToRegex(String glob) {
    StringBuilder sb = new StringBuilder("^");
    int len = glob.length();
    int i = 0;
    boolean inGroup = false;
    while (i < len) {
      char c = glob.charAt(i++);
      switch (c) {
        case '{':
          if (inGroup) {
            throw new java.util.regex.PatternSyntaxException("Cannot nest groups", glob, i - 1);
          }
          inGroup = true;
          sb.append("(?:(?:");
          break;
        case '}':
          if (inGroup) {
            sb.append("))");
            inGroup = false;
          } else {
            sb.append("\\}");
          }
          break;
        case ',':
          sb.append(inGroup ? ")|(?:" : ",");
          break;
        case '[': {
          sb.append("[[^/]&&[");
          if (i < len && glob.charAt(i) == '!') {
            sb.append('^');
            i++;
          }
          boolean closed = false;
          while (i < len) {
            char b = glob.charAt(i++);
            if (b == ']') {
              closed = true;
              break;
            }
            if (b == '\\' || b == '[' || b == '&') {
              sb.append('\\');
            }
            sb.append(b);
          }
          if (!closed) {
            throw new java.util.regex.PatternSyntaxException("Missing ']", glob, i - 1);
          }
          sb.append("]]");
          break;
        }
        case ']':
          sb.append("\\]");
          break;
        case '*':
          if (i < len && glob.charAt(i) == '*') {
            i++;
            sb.append(".*");
          } else {
            sb.append("[^/]*");
          }
          break;
        case '?':
          sb.append("[^/]");
          break;
        case '.':
        case '(':
        case ')':
        case '+':
        case '|':
        case '^':
        case '$':
        case '@':
        case '%':
          sb.append('\\').append(c);
          break;
        case '\\':
          if (i < len) {
            sb.append('\\').append(glob.charAt(i++));
          }
          break;
        default:
          sb.append(c);
          break;
      }
    }
    if (inGroup) {
      throw new java.util.regex.PatternSyntaxException("Missing '}", glob, len - 1);
    }
    return sb.append('$').toString();
  }

  static final class BasePath implements Path {
    private final String rawPath;

    BasePath(String rawPath) {
      this.rawPath = Objects.requireNonNull(rawPath, "path");
    }

    @Override
    public FileSystem getFileSystem() {
      return INSTANCE;
    }

    @Override
    public boolean isAbsolute() {
      return rawPath.startsWith("/") || (rawPath.length() >= 3 && Character.isLetter(rawPath.charAt(0)) && rawPath.charAt(1) == ':' && (rawPath.charAt(2) == '\\' || rawPath.charAt(2) == '/'));
    }

    @Override
    public Path getRoot() {
      if (isAbsolute()) {
        if (rawPath.startsWith("/")) {
          return new BasePath("/");
        }
        int colon = rawPath.indexOf(':');
        if (colon > 0 && colon + 1 < rawPath.length()) {
          return new BasePath(rawPath.substring(0, colon + 2));
        }
      }
      return null;
    }

    private List<String> segments() {
      List<String> list = new ArrayList<String>();
      int start = 0;
      int len = rawPath.length();
      if (isAbsolute() && rawPath.startsWith("/")) {
        start = 1;
      }
      for (int i = start; i < len; ++i) {
        char c = rawPath.charAt(i);
        if (c == '/' || c == '\\') {
          if (i > start) {
            list.add(rawPath.substring(start, i));
          }
          start = i + 1;
        }
      }
      if (start < len) {
        list.add(rawPath.substring(start));
      } else if (list.isEmpty() && !isAbsolute()) {
        list.add("");
      }
      return list;
    }

    @Override
    public Path getFileName() {
      if (rawPath.isEmpty()) {
        return this;
      }
      if (rawPath.equals("/") || rawPath.equals("\\")) {
        return null;
      }
      List<String> segs = segments();
      if (segs.isEmpty()) {
        return null;
      }
      return new BasePath(segs.get(segs.size() - 1));
    }

    @Override
    public Path getParent() {
      if (rawPath.isEmpty() || rawPath.equals("/") || rawPath.equals("\\")) {
        return null;
      }
      int lastSlash = -1;
      for (int i = rawPath.length() - 1; i >= 0; --i) {
        char c = rawPath.charAt(i);
        if (c == '/' || c == '\\') {
          if (i == rawPath.length() - 1) {
            continue;
          }
          lastSlash = i;
          break;
        }
      }
      if (lastSlash < 0) {
        return isAbsolute() ? getRoot() : null;
      }
      if (lastSlash == 0 && isAbsolute()) {
        return getRoot();
      }
      return new BasePath(rawPath.substring(0, lastSlash));
    }

    @Override
    public int getNameCount() {
      if (rawPath.equals("/") || rawPath.equals("\\")) {
        return 0;
      }
      return segments().size();
    }

    @Override
    public Path getName(int index) {
      List<String> segs = segments();
      if (index < 0 || index >= segs.size()) {
        throw new IllegalArgumentException("Index out of bounds: " + index);
      }
      return new BasePath(segs.get(index));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
      List<String> segs = segments();
      if (beginIndex < 0 || endIndex > segs.size() || beginIndex >= endIndex) {
        throw new IllegalArgumentException("Invalid subpath range: " + beginIndex + " to " + endIndex);
      }
      StringBuilder sb = new StringBuilder();
      for (int i = beginIndex; i < endIndex; ++i) {
        if (sb.length() > 0) sb.append(File.separatorChar);
        sb.append(segs.get(i));
      }
      return new BasePath(sb.toString());
    }

    @Override
    public boolean startsWith(Path other) {
      Objects.requireNonNull(other, "other");
      if (isAbsolute() != other.isAbsolute()) {
        return false;
      }
      List<String> thisSegs = segments();
      List<String> otherSegs = ((BasePath) other).segments();
      if (otherSegs.size() > thisSegs.size()) {
        return false;
      }
      for (int i = 0; i < otherSegs.size(); ++i) {
        if (!thisSegs.get(i).equals(otherSegs.get(i))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean endsWith(Path other) {
      Objects.requireNonNull(other, "other");
      if (other.isAbsolute()) {
        return equals(other);
      }
      List<String> thisSegs = segments();
      List<String> otherSegs = ((BasePath) other).segments();
      if (otherSegs.size() > thisSegs.size()) {
        return false;
      }
      int offset = thisSegs.size() - otherSegs.size();
      for (int i = 0; i < otherSegs.size(); ++i) {
        if (!thisSegs.get(offset + i).equals(otherSegs.get(i))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public Path normalize() {
      List<String> segs = segments();
      List<String> normalized = new ArrayList<String>();
      for (String s : segs) {
        if (s.equals(".") || s.isEmpty()) {
          continue;
        }
        if (s.equals("..")) {
          if (!normalized.isEmpty() && !normalized.get(normalized.size() - 1).equals("..")) {
            normalized.remove(normalized.size() - 1);
          } else if (!isAbsolute()) {
            normalized.add("..");
          }
        } else {
          normalized.add(s);
        }
      }
      StringBuilder sb = new StringBuilder();
      if (isAbsolute()) {
        sb.append(File.separatorChar);
      }
      for (int i = 0; i < normalized.size(); ++i) {
        if (i > 0 || (isAbsolute() && sb.length() > 1)) {
          sb.append(File.separatorChar);
        }
        sb.append(normalized.get(i));
      }
      if (sb.length() == 0) {
        return new BasePath("");
      }
      return new BasePath(sb.toString());
    }

    @Override
    public Path resolve(Path other) {
      Objects.requireNonNull(other, "other");
      if (other.isAbsolute()) {
        return other;
      }
      String oStr = other.toString();
      if (oStr.isEmpty()) {
        return this;
      }
      if (rawPath.isEmpty()) {
        return other;
      }
      if (rawPath.endsWith("/") || rawPath.endsWith("\\")) {
        return new BasePath(rawPath + oStr);
      }
      return new BasePath(rawPath + File.separatorChar + oStr);
    }

    @Override
    public Path relativize(Path other) {
      Objects.requireNonNull(other, "other");
      if (isAbsolute() != other.isAbsolute()) {
        throw new IllegalArgumentException("Both paths must be absolute or relative");
      }
      List<String> thisSegs = segments();
      List<String> otherSegs = ((BasePath) other).segments();
      int common = 0;
      while (common < thisSegs.size() && common < otherSegs.size() && thisSegs.get(common).equals(otherSegs.get(common))) {
        common++;
      }
      StringBuilder sb = new StringBuilder();
      for (int i = common; i < thisSegs.size(); ++i) {
        if (sb.length() > 0) sb.append(File.separatorChar);
        sb.append("..");
      }
      for (int i = common; i < otherSegs.size(); ++i) {
        if (sb.length() > 0) sb.append(File.separatorChar);
        sb.append(otherSegs.get(i));
      }
      if (sb.length() == 0) {
        return new BasePath("");
      }
      return new BasePath(sb.toString());
    }

    @Override
    public URI toUri() {
      String p = toAbsolutePath().normalize().toString();
      if (!p.startsWith("/")) {
        p = "/" + p;
      }
      if (!p.endsWith("/") && new java.io.File(p).isDirectory()) {
        p = p + "/";
      }
      try {
        return new URI("file", "", p, null);
      } catch (java.net.URISyntaxException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    public Path toAbsolutePath() {
      if (isAbsolute()) {
        return this;
      }
      String cwd = System.getProperty("user.dir");
      return new BasePath(cwd).resolve(this);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
      File f = toFile();
      if (!f.exists()) {
        throw new NoSuchFileException(rawPath);
      }
      return new BasePath(f.getCanonicalPath());
    }

    @Override
    public int compareTo(Path other) {
      return rawPath.compareTo(other.toString());
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) return true;
      if (other instanceof Path) {
        return rawPath.equals(other.toString());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return rawPath.hashCode();
    }

    @Override
    public String toString() {
      return rawPath;
    }
  }
}
