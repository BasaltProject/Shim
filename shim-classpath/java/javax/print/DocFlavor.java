/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DocFlavor implements Serializable, Cloneable {
  private static final long serialVersionUID = -4512080796965449721L;

  public static final String hostEncoding = java.nio.charset.Charset.defaultCharset().name();

  private final String mediaType;
  private final String mediaSubtype;
  private final Map<String, String> parameters = new TreeMap<String, String>();
  private final String myClassName;
  private transient String canonical;

  public DocFlavor(String mimeType, String className) {
    if (className == null) {
      throw new NullPointerException();
    }
    String[] parts = mimeType.split(";");
    String base = parts[0].trim().toLowerCase(Locale.ENGLISH);
    int slash = base.indexOf('/');
    if (slash <= 0 || slash == base.length() - 1) {
      throw new IllegalArgumentException("Invalid mime type: " + mimeType);
    }
    mediaType = base.substring(0, slash);
    mediaSubtype = base.substring(slash + 1);
    for (int i = 1; i < parts.length; i++) {
      String p = parts[i].trim();
      if (p.isEmpty()) {
        continue;
      }
      int eq = p.indexOf('=');
      if (eq <= 0) {
        throw new IllegalArgumentException("Invalid mime type: " + mimeType);
      }
      String name = p.substring(0, eq).trim().toLowerCase(Locale.ENGLISH);
      String value = p.substring(eq + 1).trim();
      if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
        value = value.substring(1, value.length() - 1);
      }
      parameters.put(name, value);
    }
    myClassName = className;
  }

  public String getMimeType() {
    StringBuilder sb = new StringBuilder(mediaType).append('/').append(mediaSubtype);
    for (Map.Entry<String, String> e : parameters.entrySet()) {
      sb.append("; ").append(e.getKey()).append("=\"").append(e.getValue()).append('"');
    }
    return sb.toString();
  }

  public String getMediaType() { return mediaType; }

  public String getMediaSubtype() { return mediaSubtype; }

  public String getParameter(String paramName) { return parameters.get(paramName.toLowerCase(Locale.ENGLISH)); }

  public String getRepresentationClassName() { return myClassName; }

  public String toString() { return getStringValue(); }

  public int hashCode() { return getStringValue().hashCode(); }

  public boolean equals(Object obj) {
    return obj != null && obj instanceof DocFlavor && getStringValue().equals(((DocFlavor) obj).getStringValue());
  }

  private String getStringValue() {
    if (canonical == null) {
      canonical = getMimeType() + "; class=\"" + myClassName + "\"";
    }
    return canonical;
  }

  public static class SERVICE_FORMATTED extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final SERVICE_FORMATTED RENDERABLE_IMAGE = new SERVICE_FORMATTED("java.awt.image.renderable.RenderableImage");
    public static final SERVICE_FORMATTED PRINTABLE = new SERVICE_FORMATTED("java.awt.print.Printable");
    public static final SERVICE_FORMATTED PAGEABLE = new SERVICE_FORMATTED("java.awt.print.Pageable");

    public SERVICE_FORMATTED(String className) {
      super("application/x-java-jvm-local-objectref", className);
    }
  }

  public static class READER extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final READER TEXT_PLAIN = new READER("text/plain; charset=\"utf-16\"");
    public static final READER TEXT_HTML = new READER("text/html; charset=\"utf-16\"");

    public READER(String mimeType) {
      super(mimeType, "java.io.Reader");
    }
  }

  public static class STRING extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final STRING TEXT_PLAIN = new STRING("text/plain; charset=\"utf-16\"");
    public static final STRING TEXT_HTML = new STRING("text/html; charset=\"utf-16\"");

    public STRING(String mimeType) {
      super(mimeType, "java.lang.String");
    }
  }

  public static class CHAR_ARRAY extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final CHAR_ARRAY TEXT_PLAIN = new CHAR_ARRAY("text/plain; charset=\"utf-16\"");
    public static final CHAR_ARRAY TEXT_HTML = new CHAR_ARRAY("text/html; charset=\"utf-16\"");

    public CHAR_ARRAY(String mimeType) {
      super(mimeType, "[C");
    }
  }

  public static class URL extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final URL TEXT_PLAIN_HOST = new URL("text/plain; charset=\"utf-8\"");
    public static final URL TEXT_PLAIN_UTF_8 = new URL("text/plain; charset=\"utf-8\"");
    public static final URL TEXT_PLAIN_UTF_16 = new URL("text/plain; charset=\"utf-16\"");
    public static final URL TEXT_PLAIN_UTF_16BE = new URL("text/plain; charset=\"utf-16be\"");
    public static final URL TEXT_PLAIN_UTF_16LE = new URL("text/plain; charset=\"utf-16le\"");
    public static final URL TEXT_PLAIN_US_ASCII = new URL("text/plain; charset=\"us-ascii\"");
    public static final URL TEXT_HTML_HOST = new URL("text/html; charset=\"utf-8\"");
    public static final URL TEXT_HTML_UTF_8 = new URL("text/html; charset=\"utf-8\"");
    public static final URL TEXT_HTML_UTF_16 = new URL("text/html; charset=\"utf-16\"");
    public static final URL TEXT_HTML_UTF_16BE = new URL("text/html; charset=\"utf-16be\"");
    public static final URL TEXT_HTML_UTF_16LE = new URL("text/html; charset=\"utf-16le\"");
    public static final URL TEXT_HTML_US_ASCII = new URL("text/html; charset=\"us-ascii\"");
    public static final URL PDF = new URL("application/pdf");
    public static final URL POSTSCRIPT = new URL("application/postscript");
    public static final URL PCL = new URL("application/vnd.hp-pcl");
    public static final URL GIF = new URL("image/gif");
    public static final URL JPEG = new URL("image/jpeg");
    public static final URL PNG = new URL("image/png");
    public static final URL AUTOSENSE = new URL("application/octet-stream");

    public URL(String mimeType) {
      super(mimeType, "java.net.URL");
    }
  }

  public static class INPUT_STREAM extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final INPUT_STREAM TEXT_PLAIN_HOST = new INPUT_STREAM("text/plain; charset=\"utf-8\"");
    public static final INPUT_STREAM TEXT_PLAIN_UTF_8 = new INPUT_STREAM("text/plain; charset=\"utf-8\"");
    public static final INPUT_STREAM TEXT_PLAIN_UTF_16 = new INPUT_STREAM("text/plain; charset=\"utf-16\"");
    public static final INPUT_STREAM TEXT_PLAIN_UTF_16BE = new INPUT_STREAM("text/plain; charset=\"utf-16be\"");
    public static final INPUT_STREAM TEXT_PLAIN_UTF_16LE = new INPUT_STREAM("text/plain; charset=\"utf-16le\"");
    public static final INPUT_STREAM TEXT_PLAIN_US_ASCII = new INPUT_STREAM("text/plain; charset=\"us-ascii\"");
    public static final INPUT_STREAM TEXT_HTML_HOST = new INPUT_STREAM("text/html; charset=\"utf-8\"");
    public static final INPUT_STREAM TEXT_HTML_UTF_8 = new INPUT_STREAM("text/html; charset=\"utf-8\"");
    public static final INPUT_STREAM TEXT_HTML_UTF_16 = new INPUT_STREAM("text/html; charset=\"utf-16\"");
    public static final INPUT_STREAM TEXT_HTML_UTF_16BE = new INPUT_STREAM("text/html; charset=\"utf-16be\"");
    public static final INPUT_STREAM TEXT_HTML_UTF_16LE = new INPUT_STREAM("text/html; charset=\"utf-16le\"");
    public static final INPUT_STREAM TEXT_HTML_US_ASCII = new INPUT_STREAM("text/html; charset=\"us-ascii\"");
    public static final INPUT_STREAM PDF = new INPUT_STREAM("application/pdf");
    public static final INPUT_STREAM POSTSCRIPT = new INPUT_STREAM("application/postscript");
    public static final INPUT_STREAM PCL = new INPUT_STREAM("application/vnd.hp-pcl");
    public static final INPUT_STREAM GIF = new INPUT_STREAM("image/gif");
    public static final INPUT_STREAM JPEG = new INPUT_STREAM("image/jpeg");
    public static final INPUT_STREAM PNG = new INPUT_STREAM("image/png");
    public static final INPUT_STREAM AUTOSENSE = new INPUT_STREAM("application/octet-stream");

    public INPUT_STREAM(String mimeType) {
      super(mimeType, "java.io.InputStream");
    }
  }

  public static class BYTE_ARRAY extends DocFlavor {
    private static final long serialVersionUID = 1L;

    public static final BYTE_ARRAY TEXT_PLAIN_HOST = new BYTE_ARRAY("text/plain; charset=\"utf-8\"");
    public static final BYTE_ARRAY TEXT_PLAIN_UTF_8 = new BYTE_ARRAY("text/plain; charset=\"utf-8\"");
    public static final BYTE_ARRAY TEXT_PLAIN_UTF_16 = new BYTE_ARRAY("text/plain; charset=\"utf-16\"");
    public static final BYTE_ARRAY TEXT_PLAIN_UTF_16BE = new BYTE_ARRAY("text/plain; charset=\"utf-16be\"");
    public static final BYTE_ARRAY TEXT_PLAIN_UTF_16LE = new BYTE_ARRAY("text/plain; charset=\"utf-16le\"");
    public static final BYTE_ARRAY TEXT_PLAIN_US_ASCII = new BYTE_ARRAY("text/plain; charset=\"us-ascii\"");
    public static final BYTE_ARRAY TEXT_HTML_HOST = new BYTE_ARRAY("text/html; charset=\"utf-8\"");
    public static final BYTE_ARRAY TEXT_HTML_UTF_8 = new BYTE_ARRAY("text/html; charset=\"utf-8\"");
    public static final BYTE_ARRAY TEXT_HTML_UTF_16 = new BYTE_ARRAY("text/html; charset=\"utf-16\"");
    public static final BYTE_ARRAY TEXT_HTML_UTF_16BE = new BYTE_ARRAY("text/html; charset=\"utf-16be\"");
    public static final BYTE_ARRAY TEXT_HTML_UTF_16LE = new BYTE_ARRAY("text/html; charset=\"utf-16le\"");
    public static final BYTE_ARRAY TEXT_HTML_US_ASCII = new BYTE_ARRAY("text/html; charset=\"us-ascii\"");
    public static final BYTE_ARRAY PDF = new BYTE_ARRAY("application/pdf");
    public static final BYTE_ARRAY POSTSCRIPT = new BYTE_ARRAY("application/postscript");
    public static final BYTE_ARRAY PCL = new BYTE_ARRAY("application/vnd.hp-pcl");
    public static final BYTE_ARRAY GIF = new BYTE_ARRAY("image/gif");
    public static final BYTE_ARRAY JPEG = new BYTE_ARRAY("image/jpeg");
    public static final BYTE_ARRAY PNG = new BYTE_ARRAY("image/png");
    public static final BYTE_ARRAY AUTOSENSE = new BYTE_ARRAY("application/octet-stream");

    public BYTE_ARRAY(String mimeType) {
      super(mimeType, "[B");
    }
  }
}
