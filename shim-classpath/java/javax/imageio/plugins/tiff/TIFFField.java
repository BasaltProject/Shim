/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.tiff;

import java.util.StringTokenizer;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class TIFFField implements Cloneable {
  private static final String[] kTypeNames = {null, "Byte", "Ascii", "Short", "Long", "Rational", "SByte", "Undefined",
      "SShort", "SLong", "SRational", "Float", "Double", "IFDPointer"};
  private static final long kMaxUnsigned32 = 0xffffffffL;

  private final TIFFTag tag;
  private final int tagNumber;
  private final int type;
  private final int count;
  private final Object data;
  private TIFFDirectory dir;

  public static TIFFField createFromMetadataNode(TIFFTagSet tagSet, Node node) {
    if (node == null) {
      throw new NullPointerException("node == null!");
    }
    String name = node.getNodeName();
    if (!name.equals("TIFFField")) {
      throw new IllegalArgumentException("!name.equals(\"TIFFField\")");
    }
    int tagNumber = getIntAttribute(node, "number", 0, 65535, true);
    TIFFTag tag = null;
    if (tagSet != null) {
      tag = tagSet.getTag(tagNumber);
    }
    Node child = node.getFirstChild();
    if (child == null) {
      throw new IllegalArgumentException("A TIFFField node must have a child node.");
    }
    String typeName = child.getNodeName();
    int type = -1;
    for (int i = TIFFTag.MIN_DATATYPE; i <= TIFFTag.MAX_DATATYPE; i++) {
      String tn = "TIFF" + kTypeNames[i];
      if (typeName.equals(i == TIFFTag.TIFF_UNDEFINED ? tn : tn + "s") || (i == TIFFTag.TIFF_IFD_POINTER
          && typeName.equals("TIFFIFD"))) {
        type = i;
        break;
      }
    }
    if (type < 0) {
      throw new IllegalArgumentException("Unknown field type \"" + typeName + "\".");
    }
    if (tag == null) {
      String tn = node instanceof org.w3c.dom.Element ? ((org.w3c.dom.Element) node).getAttribute("name") : "";
      tag = new TIFFTag(tn == null || tn.isEmpty() ? TIFFTag.UNKNOWN_TAG_NAME : tn, tagNumber, 1 << type);
    }
    Object data;
    int count;
    if (type == TIFFTag.TIFF_UNDEFINED) {
      String v = ((org.w3c.dom.Element) child).getAttribute("value");
      StringTokenizer st = new StringTokenizer(v, ",");
      byte[] b = new byte[st.countTokens()];
      for (int i = 0; i < b.length; i++) {
        b[i] = (byte) Integer.parseInt(st.nextToken().trim());
      }
      data = b;
      count = b.length;
    } else if (type == TIFFTag.TIFF_IFD_POINTER) {
      data = new long[] {0L};
      count = 1;
    } else {
      java.util.List<String> values = new java.util.ArrayList<String>();
      for (Node c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
        values.add(((org.w3c.dom.Element) c).getAttribute("value"));
      }
      count = values.size();
      data = parseValues(type, values);
    }
    return new TIFFField(tag, type, count, data);
  }

  private static int getIntAttribute(Node node, String attrName, int minValue, int maxValue, boolean required) {
    NamedNodeMap attrs = node.getAttributes();
    Node attr = attrs == null ? null : attrs.getNamedItem(attrName);
    if (attr == null) {
      if (required) {
        throw new IllegalArgumentException("Missing attribute \"" + attrName + "\" in node " + node.getNodeName());
      }
      return -1;
    }
    try {
      int v = Integer.parseInt(attr.getNodeValue());
      if (v < minValue || v > maxValue) {
        throw new IllegalArgumentException("Attribute \"" + attrName + "\" out of range");
      }
      return v;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Attribute \"" + attrName + "\" is not an integer");
    }
  }

  private static Object parseValues(int type, java.util.List<String> v) {
    int n = v.size();
    switch (type) {
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_SBYTE: {
        byte[] a = new byte[n];
        for (int i = 0; i < n; i++) {
          a[i] = (byte) Integer.parseInt(v.get(i));
        }
        return a;
      }
      case TIFFTag.TIFF_ASCII:
        return v.toArray(new String[0]);
      case TIFFTag.TIFF_SHORT: {
        char[] a = new char[n];
        for (int i = 0; i < n; i++) {
          a[i] = (char) Integer.parseInt(v.get(i));
        }
        return a;
      }
      case TIFFTag.TIFF_SSHORT: {
        short[] a = new short[n];
        for (int i = 0; i < n; i++) {
          a[i] = (short) Integer.parseInt(v.get(i));
        }
        return a;
      }
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER: {
        long[] a = new long[n];
        for (int i = 0; i < n; i++) {
          a[i] = Long.parseLong(v.get(i));
        }
        return a;
      }
      case TIFFTag.TIFF_SLONG: {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
          a[i] = Integer.parseInt(v.get(i));
        }
        return a;
      }
      case TIFFTag.TIFF_RATIONAL: {
        long[][] a = new long[n][2];
        for (int i = 0; i < n; i++) {
          String s = v.get(i);
          int slash = s.indexOf('/');
          a[i][0] = Long.parseLong(s.substring(0, slash));
          a[i][1] = Long.parseLong(s.substring(slash + 1));
        }
        return a;
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[][] a = new int[n][2];
        for (int i = 0; i < n; i++) {
          String s = v.get(i);
          int slash = s.indexOf('/');
          a[i][0] = Integer.parseInt(s.substring(0, slash));
          a[i][1] = Integer.parseInt(s.substring(slash + 1));
        }
        return a;
      }
      case TIFFTag.TIFF_FLOAT: {
        float[] a = new float[n];
        for (int i = 0; i < n; i++) {
          a[i] = Float.parseFloat(v.get(i));
        }
        return a;
      }
      default: {
        double[] a = new double[n];
        for (int i = 0; i < n; i++) {
          a[i] = Double.parseDouble(v.get(i));
        }
        return a;
      }
    }
  }

  public TIFFField(TIFFTag tag, int type, int count, Object data) {
    if (tag == null) {
      throw new NullPointerException("tag == null!");
    }
    if ((type < TIFFTag.MIN_DATATYPE) || (type > TIFFTag.MAX_DATATYPE)) {
      throw new IllegalArgumentException("Unknown data type " + type);
    }
    if (count < 0) {
      throw new IllegalArgumentException("count < 0!");
    }
    if (data == null) {
      throw new NullPointerException("data == null!");
    }
    if (!tag.isDataTypeOK(type)) {
      throw new IllegalArgumentException("Illegal data type " + type + " for " + tag.getName() + " tag");
    }
    boolean ok;
    switch (type) {
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_SBYTE:
      case TIFFTag.TIFF_UNDEFINED:
        ok = data instanceof byte[] && ((byte[]) data).length == count;
        break;
      case TIFFTag.TIFF_ASCII:
        ok = data instanceof String[] && ((String[]) data).length == count;
        break;
      case TIFFTag.TIFF_SHORT:
        ok = data instanceof char[] && ((char[]) data).length == count;
        break;
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER:
        ok = data instanceof long[] && ((long[]) data).length == count;
        break;
      case TIFFTag.TIFF_RATIONAL:
        ok = data instanceof long[][] && ((long[][]) data).length == count;
        break;
      case TIFFTag.TIFF_SSHORT:
        ok = data instanceof short[] && ((short[]) data).length == count;
        break;
      case TIFFTag.TIFF_SLONG:
        ok = data instanceof int[] && ((int[]) data).length == count;
        break;
      case TIFFTag.TIFF_SRATIONAL:
        ok = data instanceof int[][] && ((int[][]) data).length == count;
        break;
      case TIFFTag.TIFF_FLOAT:
        ok = data instanceof float[] && ((float[]) data).length == count;
        break;
      default:
        ok = data instanceof double[] && ((double[]) data).length == count;
        break;
    }
    if (!ok) {
      throw new IllegalArgumentException("Illegal class or length for data array");
    }
    this.tag = tag;
    this.tagNumber = tag.getNumber();
    this.type = type;
    this.count = count;
    this.data = data;
  }

  public TIFFField(TIFFTag tag, int type, int count) {
    this(tag, type, count, createArrayForType(type, count));
  }

  public TIFFField(TIFFTag tag, long value) {
    if (tag == null) {
      throw new NullPointerException("tag == null!");
    }
    if (value < 0) {
      throw new IllegalArgumentException("value < 0!");
    }
    if (value > kMaxUnsigned32) {
      throw new IllegalArgumentException("value > 0xffffffff!");
    }
    this.tag = tag;
    this.tagNumber = tag.getNumber();
    this.count = 1;
    if (value < 65536) {
      if (!tag.isDataTypeOK(TIFFTag.TIFF_SHORT)) {
        throw new IllegalArgumentException("Illegal data type " + getTypeName(TIFFTag.TIFF_SHORT) + " for tag \""
            + tag.getName() + "\"");
      }
      this.type = TIFFTag.TIFF_SHORT;
      this.data = new char[] {(char) value};
    } else {
      if (!tag.isDataTypeOK(TIFFTag.TIFF_LONG)) {
        throw new IllegalArgumentException("Illegal data type " + getTypeName(TIFFTag.TIFF_LONG) + " for tag \""
            + tag.getName() + "\"");
      }
      this.type = TIFFTag.TIFF_LONG;
      this.data = new long[] {value};
    }
  }

  public TIFFField(TIFFTag tag, int type, long offset, TIFFDirectory dir) {
    this(tag, type, 1, new long[] {offset});
    if (dir == null) {
      throw new NullPointerException("dir == null!");
    }
    this.dir = dir;
  }

  public TIFFTag getTag() { return tag; }

  public int getTagNumber() { return tagNumber; }

  public int getType() { return type; }

  public static String getTypeName(int dataType) {
    if (dataType < TIFFTag.MIN_DATATYPE || dataType > TIFFTag.MAX_DATATYPE) {
      throw new IllegalArgumentException("Unknown data type " + dataType);
    }
    return kTypeNames[dataType];
  }

  public static int getTypeByName(String typeName) {
    for (int i = TIFFTag.MIN_DATATYPE; i <= TIFFTag.MAX_DATATYPE; i++) {
      if (typeName.equals(kTypeNames[i])) {
        return i;
      }
    }
    return -1;
  }

  public static Object createArrayForType(int dataType, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count < 0!");
    }
    switch (dataType) {
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_SBYTE:
      case TIFFTag.TIFF_UNDEFINED:
        return new byte[count];
      case TIFFTag.TIFF_ASCII:
        return new String[count];
      case TIFFTag.TIFF_SHORT:
        return new char[count];
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER:
        return new long[count];
      case TIFFTag.TIFF_RATIONAL:
        return new long[count][2];
      case TIFFTag.TIFF_SSHORT:
        return new short[count];
      case TIFFTag.TIFF_SLONG:
        return new int[count];
      case TIFFTag.TIFF_SRATIONAL:
        return new int[count][2];
      case TIFFTag.TIFF_FLOAT:
        return new float[count];
      case TIFFTag.TIFF_DOUBLE:
        return new double[count];
      default:
        throw new IllegalArgumentException("Unknown data type " + dataType);
    }
  }

  public Node getAsNativeNode() {
    IIOMetadataNode root = new IIOMetadataNode("TIFFField");
    root.setAttribute("number", Integer.toString(getTagNumber()));
    if (tag != null && tag.getName() != null) {
      root.setAttribute("name", tag.getName());
    }
    String typeName = "TIFF" + getTypeName(type);
    if (type == TIFFTag.TIFF_UNDEFINED) {
      IIOMetadataNode node = new IIOMetadataNode(typeName);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < count; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(getAsInt(i));
      }
      node.setAttribute("value", sb.toString());
      root.appendChild(node);
    } else if (type == TIFFTag.TIFF_IFD_POINTER) {
      IIOMetadataNode node = new IIOMetadataNode("TIFFIFD");
      root.appendChild(node);
    } else {
      IIOMetadataNode list = new IIOMetadataNode(typeName + "s");
      for (int i = 0; i < count; i++) {
        IIOMetadataNode node = new IIOMetadataNode(typeName);
        node.setAttribute("value", getValueAsString(i));
        if (type == TIFFTag.TIFF_SHORT || type == TIFFTag.TIFF_LONG || type == TIFFTag.TIFF_BYTE) {
          if (tag != null && tag.hasValueNames()) {
            String desc = tag.getValueName(getAsInt(i));
            if (desc != null) {
              node.setAttribute("description", desc);
            }
          }
        }
        list.appendChild(node);
      }
      root.appendChild(list);
    }
    return root;
  }

  public boolean isIntegral() {
    switch (type) {
      case TIFFTag.TIFF_RATIONAL:
      case TIFFTag.TIFF_SRATIONAL:
      case TIFFTag.TIFF_FLOAT:
      case TIFFTag.TIFF_DOUBLE:
      case TIFFTag.TIFF_ASCII:
        return false;
      default:
        return true;
    }
  }

  public int getCount() { return count; }

  public Object getData() { return data; }

  public byte[] getAsBytes() { return (byte[]) data; }

  public char[] getAsChars() { return (char[]) data; }

  public short[] getAsShorts() { return (short[]) data; }

  public int[] getAsInts() {
    if (data instanceof int[]) {
      return (int[]) data;
    } else if (data instanceof char[]) {
      char[] cdata = (char[]) data;
      int[] idata = new int[cdata.length];
      for (int i = 0; i < cdata.length; i++) {
        idata[i] = cdata[i] & 0xffff;
      }
      return idata;
    } else if (data instanceof short[]) {
      short[] sdata = (short[]) data;
      int[] idata = new int[sdata.length];
      for (int i = 0; i < sdata.length; i++) {
        idata[i] = sdata[i];
      }
      return idata;
    } else {
      throw new ClassCastException("Data not char[], short[], or int[]!");
    }
  }

  public long[] getAsLongs() { return (long[]) data; }

  public float[] getAsFloats() { return (float[]) data; }

  public double[] getAsDoubles() { return (double[]) data; }

  public int[][] getAsSRationals() { return (int[][]) data; }

  public long[][] getAsRationals() { return (long[][]) data; }

  public int getAsInt(int index) {
    switch (type) {
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_UNDEFINED:
        return ((byte[]) data)[index] & 0xff;
      case TIFFTag.TIFF_SBYTE:
        return ((byte[]) data)[index];
      case TIFFTag.TIFF_SHORT:
        return ((char[]) data)[index] & 0xffff;
      case TIFFTag.TIFF_SSHORT:
        return ((short[]) data)[index];
      case TIFFTag.TIFF_SLONG:
        return ((int[]) data)[index];
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER:
        return (int) ((long[]) data)[index];
      case TIFFTag.TIFF_RATIONAL: {
        long[] pair = ((long[][]) data)[index];
        return (int) (pair[0] / pair[1]);
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[] pair = ((int[][]) data)[index];
        return pair[0] / pair[1];
      }
      case TIFFTag.TIFF_FLOAT:
        return (int) ((float[]) data)[index];
      case TIFFTag.TIFF_DOUBLE:
        return (int) ((double[]) data)[index];
      default:
        return Integer.parseInt(((String[]) data)[index]);
    }
  }

  public long getAsLong(int index) {
    switch (type) {
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_UNDEFINED:
        return ((byte[]) data)[index] & 0xffL;
      case TIFFTag.TIFF_SBYTE:
        return ((byte[]) data)[index];
      case TIFFTag.TIFF_SHORT:
        return ((char[]) data)[index] & 0xffffL;
      case TIFFTag.TIFF_SSHORT:
        return ((short[]) data)[index];
      case TIFFTag.TIFF_SLONG:
        return ((int[]) data)[index];
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER:
        return ((long[]) data)[index];
      case TIFFTag.TIFF_RATIONAL: {
        long[] pair = ((long[][]) data)[index];
        return pair[0] / pair[1];
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[] pair = ((int[][]) data)[index];
        return (long) pair[0] / pair[1];
      }
      case TIFFTag.TIFF_FLOAT:
        return (long) ((float[]) data)[index];
      case TIFFTag.TIFF_DOUBLE:
        return (long) ((double[]) data)[index];
      default:
        return Long.parseLong(((String[]) data)[index]);
    }
  }

  public float getAsFloat(int index) {
    switch (type) {
      case TIFFTag.TIFF_RATIONAL: {
        long[] pair = ((long[][]) data)[index];
        return (float) ((double) pair[0] / pair[1]);
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[] pair = ((int[][]) data)[index];
        return (float) ((double) pair[0] / pair[1]);
      }
      case TIFFTag.TIFF_FLOAT:
        return ((float[]) data)[index];
      case TIFFTag.TIFF_DOUBLE:
        return (float) ((double[]) data)[index];
      case TIFFTag.TIFF_ASCII:
        return Float.parseFloat(((String[]) data)[index]);
      default:
        return (float) getAsLong(index);
    }
  }

  public double getAsDouble(int index) {
    switch (type) {
      case TIFFTag.TIFF_RATIONAL: {
        long[] pair = ((long[][]) data)[index];
        return (double) pair[0] / pair[1];
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[] pair = ((int[][]) data)[index];
        return (double) pair[0] / pair[1];
      }
      case TIFFTag.TIFF_FLOAT:
        return ((float[]) data)[index];
      case TIFFTag.TIFF_DOUBLE:
        return ((double[]) data)[index];
      case TIFFTag.TIFF_ASCII:
        return Double.parseDouble(((String[]) data)[index]);
      default:
        return (double) getAsLong(index);
    }
  }

  public String getAsString(int index) { return ((String[]) data)[index]; }

  public int[] getAsSRational(int index) {
    int[] pair = ((int[][]) data)[index];
    return new int[] {pair[0], pair[1]};
  }

  public long[] getAsRational(int index) {
    long[] pair = ((long[][]) data)[index];
    return new long[] {pair[0], pair[1]};
  }

  public String getValueAsString(int index) {
    switch (type) {
      case TIFFTag.TIFF_ASCII:
        return ((String[]) data)[index];
      case TIFFTag.TIFF_BYTE:
      case TIFFTag.TIFF_UNDEFINED:
        return Integer.toString(((byte[]) data)[index] & 0xff);
      case TIFFTag.TIFF_SBYTE:
        return Integer.toString(((byte[]) data)[index]);
      case TIFFTag.TIFF_SHORT:
        return Integer.toString(((char[]) data)[index] & 0xffff);
      case TIFFTag.TIFF_SSHORT:
        return Integer.toString(((short[]) data)[index]);
      case TIFFTag.TIFF_SLONG:
        return Integer.toString(((int[]) data)[index]);
      case TIFFTag.TIFF_LONG:
      case TIFFTag.TIFF_IFD_POINTER:
        return Long.toString(((long[]) data)[index]);
      case TIFFTag.TIFF_RATIONAL: {
        long[] pair = ((long[][]) data)[index];
        return Long.toString(pair[0]) + "/" + Long.toString(pair[1]);
      }
      case TIFFTag.TIFF_SRATIONAL: {
        int[] pair = ((int[][]) data)[index];
        return Integer.toString(pair[0]) + "/" + Integer.toString(pair[1]);
      }
      case TIFFTag.TIFF_FLOAT:
        return Float.toString(((float[]) data)[index]);
      case TIFFTag.TIFF_DOUBLE:
        return Double.toString(((double[]) data)[index]);
      default:
        throw new ClassCastException("Unknown data type");
    }
  }

  public boolean hasDirectory() { return getDirectory() != null; }

  public TIFFDirectory getDirectory() { return dir; }

  public TIFFField clone() throws CloneNotSupportedException {
    TIFFField that = (TIFFField) super.clone();
    Object newData;
    if (data instanceof byte[]) {
      newData = ((byte[]) data).clone();
    } else if (data instanceof char[]) {
      newData = ((char[]) data).clone();
    } else if (data instanceof short[]) {
      newData = ((short[]) data).clone();
    } else if (data instanceof int[]) {
      newData = ((int[]) data).clone();
    } else if (data instanceof long[]) {
      newData = ((long[]) data).clone();
    } else if (data instanceof float[]) {
      newData = ((float[]) data).clone();
    } else if (data instanceof double[]) {
      newData = ((double[]) data).clone();
    } else if (data instanceof String[]) {
      newData = ((String[]) data).clone();
    } else if (data instanceof int[][]) {
      int[][] src = (int[][]) data;
      int[][] dst = new int[src.length][];
      for (int i = 0; i < src.length; i++) {
        dst[i] = src[i].clone();
      }
      newData = dst;
    } else if (data instanceof long[][]) {
      long[][] src = (long[][]) data;
      long[][] dst = new long[src.length][];
      for (int i = 0; i < src.length; i++) {
        dst[i] = src[i].clone();
      }
      newData = dst;
    } else {
      newData = data;
    }
    return new TIFFField(that.tag, that.type, that.count, newData);
  }
}
