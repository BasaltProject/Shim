/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DataFlavor implements Externalizable, Cloneable {
  private static final long serialVersionUID = 8367026044764648243L;

  static final Class<InputStream> ioInputStreamClass = InputStream.class;

  public static final String javaSerializedObjectMimeType = "application/x-java-serialized-object";
  public static final String javaJVMLocalObjectMimeType = "application/x-java-jvm-local-objectref";
  public static final String javaRemoteObjectMimeType = "application/x-java-remote-object";

  public static final DataFlavor stringFlavor = createConstant(String.class, "Unicode String");
  public static final DataFlavor imageFlavor = createConstant("image/x-java-image; class=java.awt.Image", "Image");
  @Deprecated
  public static final DataFlavor plainTextFlavor = createConstant("text/plain; charset=unicode; class=java.io.InputStream", "Plain Text");
  public static final DataFlavor javaFileListFlavor = createConstant("application/x-java-file-list;class=java.util.List", null);
  public static final DataFlavor selectionHtmlFlavor = initHtmlDataFlavor("selection");
  public static final DataFlavor fragmentHtmlFlavor = initHtmlDataFlavor("fragment");
  public static final DataFlavor allHtmlFlavor = initHtmlDataFlavor("all");

  private static DataFlavor createConstant(Class<?> rc, String prn) {
    try {
      return new DataFlavor(rc, prn);
    } catch (Exception e) {
      return null;
    }
  }

  private static DataFlavor createConstant(String mt, String prn) {
    try {
      return new DataFlavor(mt, prn);
    } catch (Exception e) {
      return null;
    }
  }

  private static DataFlavor initHtmlDataFlavor(String htmlFlavorType) {
    try {
      return new DataFlavor("text/html; class=java.lang.String;document=" + htmlFlavorType + ";charset=Unicode");
    } catch (Exception e) {
      return null;
    }
  }

  private String primaryType;
  private String subType;
  private final Map<String, String> params = new LinkedHashMap<>();
  private String humanPresentableName;
  private Class<?> representationClass;

  public DataFlavor() {
    super();
  }

  private DataFlavor(String primary, String sub, Map<String, String> p, Class<?> representationClass, String humanPresentableName) {
    this.primaryType = primary;
    this.subType = sub;
    if (p != null) {
      params.putAll(p);
    }
    this.representationClass = representationClass;
    this.humanPresentableName = humanPresentableName;
    params.remove("class");
  }

  public DataFlavor(Class<?> representationClass, String humanPresentableName) {
    this("application", "x-java-serialized-object", null, representationClass, humanPresentableName);
    if (representationClass == null) {
      throw new NullPointerException("representationClass");
    }
  }

  public DataFlavor(String mimeType, String humanPresentableName) {
    super();
    if (mimeType == null) {
      throw new NullPointerException("mimeType");
    }
    try {
      initialize(mimeType, humanPresentableName, this.getClass().getClassLoader());
    } catch (ClassNotFoundException cnfe) {
      throw new IllegalArgumentException("failed to parse:" + mimeType, cnfe);
    }
  }

  public DataFlavor(String mimeType, String humanPresentableName, ClassLoader classLoader) throws ClassNotFoundException {
    super();
    if (mimeType == null) {
      throw new NullPointerException("mimeType");
    }
    initialize(mimeType, humanPresentableName, classLoader);
  }

  public DataFlavor(String mimeType) throws ClassNotFoundException {
    super();
    if (mimeType == null) {
      throw new NullPointerException("mimeType");
    }
    initialize(mimeType, null, this.getClass().getClassLoader());
  }

  private void initialize(String mimeType, String humanPresentableName, ClassLoader classLoader)
      throws ClassNotFoundException {
    if (mimeType == null) {
      throw new NullPointerException("mimeType");
    }
    String normalized = mimeType.trim();
    int slash = normalized.indexOf('/');
    if (slash <= 0) {
      throw new IllegalArgumentException("failed to parse:" + mimeType);
    }
    int semi = normalized.indexOf(';');
    String type = normalized.substring(0, slash).trim().toLowerCase(java.util.Locale.ROOT);
    String sub = (semi < 0 ? normalized.substring(slash + 1) : normalized.substring(slash + 1, semi)).trim()
        .toLowerCase(java.util.Locale.ROOT);
    if (type.isEmpty() || sub.isEmpty()) {
      throw new IllegalArgumentException("failed to parse:" + mimeType);
    }
    primaryType = type;
    subType = sub;
    String cls = null;
    if (semi >= 0) {
      for (String part : normalized.substring(semi + 1).split(";")) {
        int eq = part.indexOf('=');
        if (eq < 0) {
          if (!part.trim().isEmpty()) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
          }
          continue;
        }
        String key = part.substring(0, eq).trim().toLowerCase(java.util.Locale.ROOT);
        String value = part.substring(eq + 1).trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
          value = value.substring(1, value.length() - 1);
        }
        if (key.equals("class")) {
          cls = value;
        } else {
          params.put(key, value);
        }
      }
    }
    if (cls == null) {
      representationClass = InputStream.class;
    } else {
      representationClass = tryToLoadClass(cls, classLoader);
    }
    this.humanPresentableName = humanPresentableName != null ? humanPresentableName : (primaryType + "/" + subType);
  }

  public String toString() {
    String string = getClass().getName();
    string += "[" + paramString() + "]";
    return string;
  }

  private String paramString() {
    String params = "";
    params += "mimetype=";
    if (primaryType == null) {
      params += "null";
    } else {
      params += getMimeType0();
    }
    params += ";representationclass=";
    if (representationClass == null) {
      params += "null";
    } else {
      params += representationClass.getName();
    }
    if ("text".equals(primaryType) && (isRepresentationClassInputStream() || representationClass == byte[].class
        || ByteBuffer.class.isAssignableFrom(representationClass))) {
      String cs = this.params.get("charset");
      params += ";charset=" + (cs != null ? cs : java.nio.charset.Charset.defaultCharset().name());
    }
    return params;
  }

  private String getMimeType0() {
    return primaryType + "/" + subType;
  }

  public static final DataFlavor getTextPlainUnicodeFlavor() {
    return new DataFlavor("text/plain;charset=" + java.nio.charset.Charset.defaultCharset().name()
        + ";class=java.io.InputStream", "Plain Text");
  }

  public static final DataFlavor selectBestTextFlavor(DataFlavor[] availableFlavors) {
    if (availableFlavors == null || availableFlavors.length == 0) {
      return null;
    }
    DataFlavor best = null;
    for (DataFlavor f : availableFlavors) {
      if (f.isFlavorTextType()) {
        if (best == null || (f.representationClass == String.class && best.representationClass != String.class)) {
          best = f;
        }
      }
    }
    return best;
  }

  public Reader getReaderForText(Transferable transferable) throws UnsupportedFlavorException, IOException {
    Object transferObject = transferable.getTransferData(this);
    if (transferObject == null) {
      throw new IllegalArgumentException("getTransferData() returned null");
    }
    if (transferObject instanceof Reader) {
      return (Reader) transferObject;
    } else if (transferObject instanceof String) {
      return new StringReader((String) transferObject);
    } else if (transferObject instanceof CharBuffer) {
      return new StringReader(transferObject.toString());
    } else if (transferObject instanceof char[]) {
      return new StringReader(new String((char[]) transferObject));
    }
    String charset = getParameter("charset");
    java.nio.charset.Charset cs = charset == null ? java.nio.charset.Charset.defaultCharset()
        : java.nio.charset.Charset.forName(charset);
    ByteBuffer bb = null;
    if (transferObject instanceof InputStream) {
      return new InputStreamReader((InputStream) transferObject, cs);
    } else if (transferObject instanceof byte[]) {
      return new InputStreamReader(new ByteArrayInputStream((byte[]) transferObject), cs);
    } else if (((bb = transferObject instanceof ByteBuffer ? (ByteBuffer) transferObject : null) != null)) {
      byte[] bytes = new byte[bb.remaining()];
      bb.get(bytes);
      return new InputStreamReader(new ByteArrayInputStream(bytes), cs);
    }
    throw new IllegalArgumentException("transfer data is not Reader, String, CharBuffer, char array, InputStream, ByteBuffer, or byte array");
  }

  public String getMimeType() {
    if (primaryType == null) {
      return null;
    }
    java.util.Hashtable<String, String> all = new java.util.Hashtable<String, String>(params);
    if (representationClass != null) {
      all.put("class", representationClass.getName());
    }
    StringBuilder sb = new StringBuilder(getMimeType0());
    for (java.util.Enumeration<String> keys = all.keys(); keys.hasMoreElements();) {
      String k = keys.nextElement();
      sb.append("; ").append(k).append('=').append(all.get(k));
    }
    return sb.toString();
  }

  public Class<?> getRepresentationClass() {
    return representationClass;
  }

  public String getHumanPresentableName() {
    return humanPresentableName;
  }

  public String getPrimaryType() {
    return primaryType;
  }

  public String getSubType() {
    return subType;
  }

  public String getParameter(String paramName) {
    if (paramName.equals("humanPresentableName")) {
      return humanPresentableName;
    }
    return params.get(paramName);
  }

  public void setHumanPresentableName(String humanPresentableName) {
    this.humanPresentableName = humanPresentableName;
  }

  public boolean equals(Object o) {
    return o instanceof DataFlavor && equals((DataFlavor) o);
  }

  public boolean equals(DataFlavor that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (!Objects.equals(this.getRepresentationClass(), that.getRepresentationClass())) {
      return false;
    }
    if (primaryType == null ? that.primaryType != null : !primaryType.equals(that.primaryType)) {
      return false;
    }
    if (subType == null ? that.subType != null : !subType.equals(that.subType)) {
      return false;
    }
    if (!"text".equals(primaryType) || isUnicodeFlavor() == that.isUnicodeFlavor()) {
      if (!params.equals(that.params) && !(isUnicodeFlavor() && that.isUnicodeFlavor())) {
        return false;
      }
    }
    return true;
  }

  private boolean isUnicodeFlavor() {
    String cs = params.get("charset");
    return cs != null && (cs.equalsIgnoreCase("unicode") || cs.equalsIgnoreCase("utf-16"));
  }

  @Deprecated
  public boolean equals(String s) {
    if (s == null || primaryType == null) {
      return false;
    }
    return isMimeTypeEqual(s);
  }

  public int hashCode() {
    int total = 0;
    if (representationClass != null) {
      total += representationClass.hashCode();
    }
    if (primaryType != null) {
      total += primaryType.hashCode();
      if (subType != null) {
        total += subType.hashCode();
      }
    }
    return total;
  }

  public boolean match(DataFlavor that) {
    return equals(that);
  }

  public boolean isMimeTypeEqual(String mimeType) {
    if (mimeType == null) {
      return false;
    }
    int slash = mimeType.indexOf('/');
    if (slash < 0 || primaryType == null) {
      return false;
    }
    int semi = mimeType.indexOf(';');
    String t = mimeType.substring(0, slash).trim().toLowerCase(java.util.Locale.ROOT);
    String s = (semi < 0 ? mimeType.substring(slash + 1) : mimeType.substring(slash + 1, semi)).trim()
        .toLowerCase(java.util.Locale.ROOT);
    return t.equals(primaryType) && s.equals(subType);
  }

  public final boolean isMimeTypeEqual(DataFlavor dataFlavor) {
    return isMimeTypeEqual(dataFlavor.getMimeType());
  }

  public boolean isMimeTypeSerializedObject() {
    return isMimeTypeEqual(javaSerializedObjectMimeType);
  }

  public final Class<?> getDefaultRepresentationClass() {
    return ioInputStreamClass;
  }

  public final String getDefaultRepresentationClassAsString() {
    return getDefaultRepresentationClass().getName();
  }

  public boolean isRepresentationClassInputStream() {
    return ioInputStreamClass.isAssignableFrom(representationClass);
  }

  public boolean isRepresentationClassReader() {
    return java.io.Reader.class.isAssignableFrom(representationClass);
  }

  public boolean isRepresentationClassCharBuffer() {
    return java.nio.CharBuffer.class.isAssignableFrom(representationClass);
  }

  public boolean isRepresentationClassByteBuffer() {
    return java.nio.ByteBuffer.class.isAssignableFrom(representationClass);
  }

  public boolean isRepresentationClassSerializable() {
    return java.io.Serializable.class.isAssignableFrom(representationClass);
  }

  public boolean isRepresentationClassRemote() {
    return false;
  }

  public boolean isFlavorSerializedObjectType() {
    return isRepresentationClassSerializable() && isMimeTypeEqual(javaSerializedObjectMimeType);
  }

  public boolean isFlavorRemoteObjectType() {
    return isRepresentationClassRemote() && isRepresentationClassSerializable() && isMimeTypeEqual(javaRemoteObjectMimeType);
  }

  public boolean isFlavorJavaFileListType() {
    return java.util.List.class.isAssignableFrom(representationClass) && isMimeTypeEqual(javaFileListFlavor);
  }

  public boolean isFlavorTextType() {
    return "text".equals(primaryType) || this == stringFlavor || equals(stringFlavor)
        || (representationClass != null && (Reader.class.isAssignableFrom(representationClass)
        || representationClass == String.class && "text".equals(primaryType)));
  }

  public synchronized void writeExternal(ObjectOutput os) throws IOException {
    os.writeObject(humanPresentableName);
    os.writeObject(getMimeType());
  }

  public synchronized void readExternal(ObjectInput is) throws IOException, ClassNotFoundException {
    String name = (String) is.readObject();
    String mime = (String) is.readObject();
    initialize(mime == null ? "application/x-java-serialized-object" : mime, name, this.getClass().getClassLoader());
  }

  public Object clone() throws CloneNotSupportedException {
    DataFlavor ret = (DataFlavor) super.clone();
    return ret;
  }

  protected String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
    return parameterValue;
  }

  protected String normalizeMimeType(String mimeType) {
    return mimeType;
  }

  protected static final Class<?> tryToLoadClass(String className, ClassLoader fallback) throws ClassNotFoundException {
    try {
      return Class.forName(className, true, ClassLoader.getSystemClassLoader());
    } catch (ClassNotFoundException e) {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      if (contextClassLoader != null) {
        try {
          return Class.forName(className, true, contextClassLoader);
        } catch (ClassNotFoundException e2) {
          // fall through
        }
      }
      return Class.forName(className, true, fallback);
    }
  }
}
