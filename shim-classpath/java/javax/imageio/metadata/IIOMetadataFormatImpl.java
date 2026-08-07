/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.imageio.ImageTypeSpecifier;

public abstract class IIOMetadataFormatImpl implements IIOMetadataFormat {
  public static final String standardMetadataFormatName = "javax_imageio_1.0";

  private static IIOMetadataFormat standardFormat;

  private String resourceBaseName = this.getClass().getName() + "Resources";
  private String rootName;
  private final Map<String, Element> elementMap = new HashMap<String, Element>();

  private static final class Attribute {
    final String attrName;
    final int valueType;
    final int dataType;
    final boolean required;
    final String defaultValue;
    List<String> enumeration;
    String minValue;
    String maxValue;
    int listMinLength;
    int listMaxLength;

    Attribute(String attrName, int dataType, boolean required, String defaultValue, int valueType) {
      this.attrName = attrName;
      this.dataType = dataType;
      this.required = required;
      this.defaultValue = defaultValue;
      this.valueType = valueType;
    }
  }

  private static final class ObjectValue {
    int valueType = VALUE_NONE;
    Class<?> classType;
    Object defaultValue;
    List<?> enumeration;
    Comparable<?> minValue;
    Comparable<?> maxValue;
    int arrayMinLength;
    int arrayMaxLength;
  }

  private static final class Element {
    final String elementName;
    final int childPolicy;
    int minChildren;
    int maxChildren;
    final List<String> childList = new ArrayList<String>();
    final List<String> parentList = new ArrayList<String>();
    final List<String> attrList = new ArrayList<String>();
    final Map<String, Attribute> attrMap = new HashMap<String, Attribute>();
    ObjectValue objectValue;

    Element(String elementName, int childPolicy) {
      this.elementName = elementName;
      this.childPolicy = childPolicy;
    }
  }

  public IIOMetadataFormatImpl(String rootName, int childPolicy) {
    if (rootName == null) {
      throw new IllegalArgumentException("rootName == null!");
    }
    if (childPolicy < CHILD_POLICY_EMPTY || childPolicy > CHILD_POLICY_MAX || childPolicy == CHILD_POLICY_REPEAT) {
      throw new IllegalArgumentException("childPolicy is not one of the predefined constants!");
    }
    this.rootName = rootName;
    elementMap.put(rootName, new Element(rootName, childPolicy));
  }

  public IIOMetadataFormatImpl(String rootName, int minChildren, int maxChildren) {
    if (rootName == null) {
      throw new IllegalArgumentException("rootName == null!");
    }
    if (minChildren < 0) {
      throw new IllegalArgumentException("minChildren < 0!");
    }
    if (minChildren > maxChildren) {
      throw new IllegalArgumentException("minChildren > maxChildren!");
    }
    Element root = new Element(rootName, CHILD_POLICY_REPEAT);
    root.minChildren = minChildren;
    root.maxChildren = maxChildren;
    this.rootName = rootName;
    elementMap.put(rootName, root);
  }

  protected void setResourceBaseName(String resourceBaseName) {
    if (resourceBaseName == null) {
      throw new IllegalArgumentException("resourceBaseName == null!");
    }
    this.resourceBaseName = resourceBaseName;
  }

  protected String getResourceBaseName() { return resourceBaseName; }

  private Element getElement(String elementName, boolean mustAppear) {
    if (mustAppear && (elementName == null)) {
      throw new IllegalArgumentException("element name is null!");
    }
    Element element = elementMap.get(elementName);
    if (mustAppear && (element == null)) {
      throw new IllegalArgumentException("No such element: " + elementName);
    }
    return element;
  }

  private Element getElement(String elementName) { return getElement(elementName, true); }

  private Attribute getAttribute(String elementName, String attrName) {
    Element element = getElement(elementName);
    Attribute attr = element.attrMap.get(attrName);
    if (attr == null) {
      throw new IllegalArgumentException("No such attribute \"" + attrName + "\"!");
    }
    return attr;
  }

  protected void addElement(String elementName, String parentName, int childPolicy) {
    Element parent = getElement(parentName);
    if (childPolicy < CHILD_POLICY_EMPTY || childPolicy > CHILD_POLICY_MAX || childPolicy == CHILD_POLICY_REPEAT) {
      throw new IllegalArgumentException("childPolicy is not one of the predefined constants!");
    }
    Element element = new Element(elementName, childPolicy);
    parent.childList.add(elementName);
    element.parentList.add(parentName);
    elementMap.put(elementName, element);
  }

  protected void addElement(String elementName, String parentName, int minChildren, int maxChildren) {
    Element parent = getElement(parentName);
    if (minChildren < 0) {
      throw new IllegalArgumentException("minChildren < 0!");
    }
    if (minChildren > maxChildren) {
      throw new IllegalArgumentException("minChildren > maxChildren!");
    }
    Element element = new Element(elementName, CHILD_POLICY_REPEAT);
    element.minChildren = minChildren;
    element.maxChildren = maxChildren;
    parent.childList.add(elementName);
    element.parentList.add(parentName);
    elementMap.put(elementName, element);
  }

  protected void addChildElement(String elementName, String parentName) {
    Element parent = getElement(parentName);
    Element element = getElement(elementName);
    parent.childList.add(elementName);
    element.parentList.add(parentName);
  }

  protected void removeElement(String elementName) {
    Element element = getElement(elementName, false);
    if (element != null) {
      for (String parentName : new ArrayList<String>(element.parentList)) {
        Element parent = getElement(parentName, false);
        if (parent != null) {
          parent.childList.remove(elementName);
        }
      }
      elementMap.remove(elementName);
    }
  }

  protected void addAttribute(String elementName, String attrName, int dataType, boolean required,
      String defaultValue) {
    Element element = getElement(elementName);
    if (attrName == null) {
      throw new IllegalArgumentException("attrName == null!");
    }
    if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
      throw new IllegalArgumentException("Invalid value for dataType!");
    }
    Attribute attr = new Attribute(attrName, dataType, required, defaultValue, VALUE_ARBITRARY);
    element.attrList.add(attrName);
    element.attrMap.put(attrName, attr);
  }

  protected void addAttribute(String elementName, String attrName, int dataType, boolean required,
      String defaultValue, List<String> enumeratedValues) {
    Element element = getElement(elementName);
    if (attrName == null) {
      throw new IllegalArgumentException("attrName == null!");
    }
    if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
      throw new IllegalArgumentException("Invalid value for dataType!");
    }
    if (enumeratedValues == null) {
      throw new IllegalArgumentException("enumeratedValues == null!");
    }
    if (enumeratedValues.size() == 0) {
      throw new IllegalArgumentException("enumeratedValues is empty!");
    }
    for (Object o : enumeratedValues) {
      if (o == null) {
        throw new IllegalArgumentException("enumeratedValues contains a null!");
      }
      if (!(o instanceof String)) {
        throw new IllegalArgumentException("enumeratedValues contains a non-String value!");
      }
    }
    Attribute attr = new Attribute(attrName, dataType, required, defaultValue, VALUE_ENUMERATION);
    attr.enumeration = new ArrayList<String>(enumeratedValues);
    element.attrList.add(attrName);
    element.attrMap.put(attrName, attr);
  }

  protected void addAttribute(String elementName, String attrName, int dataType, boolean required,
      String defaultValue, String minValue, String maxValue, boolean minInclusive, boolean maxInclusive) {
    Element element = getElement(elementName);
    if (attrName == null) {
      throw new IllegalArgumentException("attrName == null!");
    }
    if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
      throw new IllegalArgumentException("Invalid value for dataType!");
    }
    int valueType = VALUE_RANGE;
    if (minInclusive) {
      valueType |= VALUE_RANGE_MIN_INCLUSIVE_MASK;
    }
    if (maxInclusive) {
      valueType |= VALUE_RANGE_MAX_INCLUSIVE_MASK;
    }
    Attribute attr = new Attribute(attrName, dataType, required, defaultValue, valueType);
    attr.minValue = minValue;
    attr.maxValue = maxValue;
    element.attrList.add(attrName);
    element.attrMap.put(attrName, attr);
  }

  protected void addAttribute(String elementName, String attrName, int dataType, boolean required,
      int listMinLength, int listMaxLength) {
    Element element = getElement(elementName);
    if (attrName == null) {
      throw new IllegalArgumentException("attrName == null!");
    }
    if (dataType < DATATYPE_STRING || dataType > DATATYPE_DOUBLE) {
      throw new IllegalArgumentException("Invalid value for dataType!");
    }
    if (listMinLength < 0 || listMinLength > listMaxLength) {
      throw new IllegalArgumentException("Invalid list bounds!");
    }
    Attribute attr = new Attribute(attrName, dataType, required, null, VALUE_LIST);
    attr.listMinLength = listMinLength;
    attr.listMaxLength = listMaxLength;
    element.attrList.add(attrName);
    element.attrMap.put(attrName, attr);
  }

  protected void addBooleanAttribute(String elementName, String attrName, boolean hasDefaultValue,
      boolean defaultValue) {
    List<String> values = new ArrayList<String>();
    values.add("TRUE");
    values.add("FALSE");
    String dval = null;
    if (hasDefaultValue) {
      dval = defaultValue ? "TRUE" : "FALSE";
    }
    addAttribute(elementName, attrName, DATATYPE_BOOLEAN, true, dval, values);
  }

  protected void removeAttribute(String elementName, String attrName) {
    Element element = getElement(elementName);
    element.attrList.remove(attrName);
    element.attrMap.remove(attrName);
  }

  protected <T> void addObjectValue(String elementName, Class<T> classType, boolean required, T defaultValue) {
    Element element = getElement(elementName);
    ObjectValue obj = new ObjectValue();
    obj.valueType = VALUE_ARBITRARY;
    obj.classType = classType;
    obj.defaultValue = defaultValue;
    element.objectValue = obj;
  }

  protected <T> void addObjectValue(String elementName, Class<T> classType, boolean required, T defaultValue,
      List<? extends T> enumeratedValues) {
    Element element = getElement(elementName);
    if (enumeratedValues == null) {
      throw new IllegalArgumentException("enumeratedValues == null!");
    }
    if (enumeratedValues.size() == 0) {
      throw new IllegalArgumentException("enumeratedValues is empty!");
    }
    for (Object o : enumeratedValues) {
      if (o == null) {
        throw new IllegalArgumentException("enumeratedValues contains a null!");
      }
      if (!classType.isInstance(o)) {
        throw new IllegalArgumentException("enumeratedValues contains a value not of class classType!");
      }
    }
    ObjectValue obj = new ObjectValue();
    obj.valueType = VALUE_ENUMERATION;
    obj.classType = classType;
    obj.defaultValue = defaultValue;
    obj.enumeration = new ArrayList<Object>(enumeratedValues);
    element.objectValue = obj;
  }

  protected <T extends Comparable<? super T>> void addObjectValue(String elementName, Class<T> classType,
      T defaultValue, Comparable<? super T> minValue, Comparable<? super T> maxValue, boolean minInclusive,
      boolean maxInclusive) {
    Element element = getElement(elementName);
    ObjectValue obj = new ObjectValue();
    obj.valueType = VALUE_RANGE;
    if (minInclusive) {
      obj.valueType |= VALUE_RANGE_MIN_INCLUSIVE_MASK;
    }
    if (maxInclusive) {
      obj.valueType |= VALUE_RANGE_MAX_INCLUSIVE_MASK;
    }
    obj.classType = classType;
    obj.defaultValue = defaultValue;
    obj.minValue = minValue;
    obj.maxValue = maxValue;
    element.objectValue = obj;
  }

  protected void addObjectValue(String elementName, Class<?> classType, int arrayMinLength, int arrayMaxLength) {
    Element element = getElement(elementName);
    ObjectValue obj = new ObjectValue();
    obj.valueType = VALUE_LIST;
    obj.classType = classType;
    obj.arrayMinLength = arrayMinLength;
    obj.arrayMaxLength = arrayMaxLength;
    element.objectValue = obj;
  }

  protected void removeObjectValue(String elementName) {
    Element element = getElement(elementName);
    element.objectValue = null;
  }

  public String getRootName() { return rootName; }

  public abstract boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType);

  public int getElementMinChildren(String elementName) {
    Element element = getElement(elementName);
    if (element.childPolicy != CHILD_POLICY_REPEAT) {
      throw new IllegalArgumentException("Child policy not CHILD_POLICY_REPEAT!");
    }
    return element.minChildren;
  }

  public int getElementMaxChildren(String elementName) {
    Element element = getElement(elementName);
    if (element.childPolicy != CHILD_POLICY_REPEAT) {
      throw new IllegalArgumentException("Child policy not CHILD_POLICY_REPEAT!");
    }
    return element.maxChildren;
  }

  private String getResource(String key, Locale locale) {
    if (locale == null) {
      locale = Locale.getDefault();
    }
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(resourceBaseName, locale);
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      return null;
    }
  }

  public String getElementDescription(String elementName, Locale locale) {
    Element element = getElement(elementName);
    return getResource(elementName, locale);
  }

  public int getChildPolicy(String elementName) { return getElement(elementName).childPolicy; }

  public String[] getChildNames(String elementName) {
    Element element = getElement(elementName);
    if (element.childPolicy == CHILD_POLICY_EMPTY) {
      return null;
    }
    return element.childList.toArray(new String[0]);
  }

  public String[] getAttributeNames(String elementName) {
    Element element = getElement(elementName);
    return element.attrList.toArray(new String[0]);
  }

  public int getAttributeValueType(String elementName, String attrName) {
    return getAttribute(elementName, attrName).valueType;
  }

  public int getAttributeDataType(String elementName, String attrName) {
    return getAttribute(elementName, attrName).dataType;
  }

  public boolean isAttributeRequired(String elementName, String attrName) {
    return getAttribute(elementName, attrName).required;
  }

  public String getAttributeDefaultValue(String elementName, String attrName) {
    return getAttribute(elementName, attrName).defaultValue;
  }

  public String[] getAttributeEnumerations(String elementName, String attrName) {
    Attribute attr = getAttribute(elementName, attrName);
    if (attr.valueType != VALUE_ENUMERATION) {
      throw new IllegalArgumentException("Attribute not an enumeration!");
    }
    return attr.enumeration.toArray(new String[0]);
  }

  public String getAttributeMinValue(String elementName, String attrName) {
    Attribute attr = getAttribute(elementName, attrName);
    int vt = attr.valueType & ~(VALUE_RANGE_MIN_INCLUSIVE_MASK | VALUE_RANGE_MAX_INCLUSIVE_MASK);
    if (vt != VALUE_RANGE) {
      throw new IllegalArgumentException("Attribute not a range!");
    }
    return attr.minValue;
  }

  public String getAttributeMaxValue(String elementName, String attrName) {
    Attribute attr = getAttribute(elementName, attrName);
    int vt = attr.valueType & ~(VALUE_RANGE_MIN_INCLUSIVE_MASK | VALUE_RANGE_MAX_INCLUSIVE_MASK);
    if (vt != VALUE_RANGE) {
      throw new IllegalArgumentException("Attribute not a range!");
    }
    return attr.maxValue;
  }

  public int getAttributeListMinLength(String elementName, String attrName) {
    Attribute attr = getAttribute(elementName, attrName);
    if (attr.valueType != VALUE_LIST) {
      throw new IllegalArgumentException("Attribute not a list!");
    }
    return attr.listMinLength;
  }

  public int getAttributeListMaxLength(String elementName, String attrName) {
    Attribute attr = getAttribute(elementName, attrName);
    if (attr.valueType != VALUE_LIST) {
      throw new IllegalArgumentException("Attribute not a list!");
    }
    return attr.listMaxLength;
  }

  public String getAttributeDescription(String elementName, String attrName, Locale locale) {
    Element element = getElement(elementName);
    if (attrName == null) {
      throw new IllegalArgumentException("attrName == null!");
    }
    Attribute attr = element.attrMap.get(attrName);
    if (attr == null) {
      throw new IllegalArgumentException("No such attribute!");
    }
    return getResource(elementName + "/" + attrName, locale);
  }

  private ObjectValue getObjectValue(String elementName) {
    Element element = getElement(elementName);
    ObjectValue objv = element.objectValue;
    if (objv == null) {
      throw new IllegalArgumentException("No object within element " + elementName + "!");
    }
    return objv;
  }

  public int getObjectValueType(String elementName) {
    Element element = getElement(elementName);
    if (element.objectValue == null) {
      return VALUE_NONE;
    }
    return element.objectValue.valueType;
  }

  public Class<?> getObjectClass(String elementName) { return getObjectValue(elementName).classType; }

  public Object getObjectDefaultValue(String elementName) { return getObjectValue(elementName).defaultValue; }

  public Object[] getObjectEnumerations(String elementName) {
    ObjectValue objv = getObjectValue(elementName);
    if (objv.valueType != VALUE_ENUMERATION) {
      throw new IllegalArgumentException("Not an enumeration!");
    }
    return objv.enumeration.toArray();
  }

  public Comparable<?> getObjectMinValue(String elementName) {
    ObjectValue objv = getObjectValue(elementName);
    if ((objv.valueType & VALUE_RANGE) != VALUE_RANGE) {
      throw new IllegalArgumentException("Not a range!");
    }
    return objv.minValue;
  }

  public Comparable<?> getObjectMaxValue(String elementName) {
    ObjectValue objv = getObjectValue(elementName);
    if ((objv.valueType & VALUE_RANGE) != VALUE_RANGE) {
      throw new IllegalArgumentException("Not a range!");
    }
    return objv.maxValue;
  }

  public int getObjectArrayMinLength(String elementName) {
    ObjectValue objv = getObjectValue(elementName);
    if (objv.valueType != VALUE_LIST) {
      throw new IllegalArgumentException("Not a list!");
    }
    return objv.arrayMinLength;
  }

  public int getObjectArrayMaxLength(String elementName) {
    ObjectValue objv = getObjectValue(elementName);
    if (objv.valueType != VALUE_LIST) {
      throw new IllegalArgumentException("Not a list!");
    }
    return objv.arrayMaxLength;
  }

  public static synchronized IIOMetadataFormat getStandardFormatInstance() {
    if (standardFormat == null) {
      standardFormat = new StandardFormat();
    }
    return standardFormat;
  }

  private static final class StandardFormat extends IIOMetadataFormatImpl {
    StandardFormat() {
      super(standardMetadataFormatName, CHILD_POLICY_SOME);
      List<String> values;
      addElement("Chroma", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("ColorSpaceType", "Chroma", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      for (String s : new String[] {"XYZ", "Lab", "Luv", "YCbCr", "Yxy", "YCCK", "PhotoYCC", "RGB", "GRAY", "HSV",
          "HLS", "CMYK", "CMY", "2CLR", "3CLR", "4CLR", "5CLR", "6CLR", "7CLR", "8CLR", "9CLR", "ACLR", "BCLR",
          "CCLR", "DCLR", "ECLR", "FCLR"}) {
        values.add(s);
      }
      addAttribute("ColorSpaceType", "name", DATATYPE_STRING, true, null, values);
      addElement("NumChannels", "Chroma", CHILD_POLICY_EMPTY);
      addAttribute("NumChannels", "value", DATATYPE_INTEGER, true, 0, Integer.MAX_VALUE);
      addElement("Gamma", "Chroma", CHILD_POLICY_EMPTY);
      addAttribute("Gamma", "value", DATATYPE_FLOAT, true, null);
      addElement("BlackIsZero", "Chroma", CHILD_POLICY_EMPTY);
      addBooleanAttribute("BlackIsZero", "value", true, true);
      addElement("Palette", "Chroma", 0, 256);
      addElement("PaletteEntry", "Palette", CHILD_POLICY_EMPTY);
      addAttribute("PaletteEntry", "index", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("PaletteEntry", "red", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("PaletteEntry", "green", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("PaletteEntry", "blue", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("PaletteEntry", "alpha", DATATYPE_INTEGER, false, "255", "0", "255", true, true);
      addElement("BackgroundIndex", "Chroma", CHILD_POLICY_EMPTY);
      addAttribute("BackgroundIndex", "value", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addElement("BackgroundColor", "Chroma", CHILD_POLICY_EMPTY);
      addAttribute("BackgroundColor", "red", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("BackgroundColor", "green", DATATYPE_INTEGER, true, null, "0", "255", true, true);
      addAttribute("BackgroundColor", "blue", DATATYPE_INTEGER, true, null, "0", "255", true, true);

      addElement("Compression", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("CompressionTypeName", "Compression", CHILD_POLICY_EMPTY);
      addAttribute("CompressionTypeName", "value", DATATYPE_STRING, true, null);
      addElement("Lossless", "Compression", CHILD_POLICY_EMPTY);
      addBooleanAttribute("Lossless", "value", true, true);
      addElement("NumProgressiveScans", "Compression", CHILD_POLICY_EMPTY);
      addAttribute("NumProgressiveScans", "value", DATATYPE_INTEGER, true, null);
      addElement("BitRate", "Compression", CHILD_POLICY_EMPTY);
      addAttribute("BitRate", "value", DATATYPE_FLOAT, true, null);

      addElement("Data", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("PlanarConfiguration", "Data", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      values.add("PixelInterleaved");
      values.add("PlaneInterleaved");
      values.add("LineInterleaved");
      values.add("TileInterleaved");
      addAttribute("PlanarConfiguration", "value", DATATYPE_STRING, true, null, values);
      addElement("SampleFormat", "Data", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      values.add("SignedIntegral");
      values.add("UnsignedIntegral");
      values.add("Real");
      values.add("Index");
      addAttribute("SampleFormat", "value", DATATYPE_STRING, true, null, values);
      addElement("BitsPerSample", "Data", CHILD_POLICY_EMPTY);
      addAttribute("BitsPerSample", "value", DATATYPE_INTEGER, true, 1, Integer.MAX_VALUE);
      addElement("SignificantBitsPerSample", "Data", CHILD_POLICY_EMPTY);
      addAttribute("SignificantBitsPerSample", "value", DATATYPE_INTEGER, true, 1, Integer.MAX_VALUE);
      addElement("SampleMSB", "Data", CHILD_POLICY_EMPTY);
      addAttribute("SampleMSB", "value", DATATYPE_INTEGER, true, 1, Integer.MAX_VALUE);

      addElement("Dimension", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("PixelAspectRatio", "Dimension", CHILD_POLICY_EMPTY);
      addAttribute("PixelAspectRatio", "value", DATATYPE_FLOAT, true, null);
      addElement("ImageOrientation", "Dimension", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      for (String s : new String[] {"Normal", "Rotate90", "Rotate180", "Rotate270", "FlipH", "FlipV",
          "FlipHRotate90", "FlipVRotate90"}) {
        values.add(s);
      }
      addAttribute("ImageOrientation", "value", DATATYPE_STRING, true, null, values);
      for (String n : new String[] {"HorizontalPixelSize", "VerticalPixelSize", "HorizontalPhysicalPixelSpacing",
          "VerticalPhysicalPixelSpacing", "HorizontalPosition", "VerticalPosition", "HorizontalPixelOffset",
          "VerticalPixelOffset", "HorizontalScreenSize", "VerticalScreenSize"}) {
        addElement(n, "Dimension", CHILD_POLICY_EMPTY);
        addAttribute(n, "value", n.endsWith("PixelOffset") || n.endsWith("ScreenSize") ? DATATYPE_INTEGER
            : DATATYPE_FLOAT, true, null);
      }

      addElement("Document", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("FormatVersion", "Document", CHILD_POLICY_EMPTY);
      addAttribute("FormatVersion", "value", DATATYPE_STRING, true, null);
      addElement("SubimageInterpretation", "Document", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      for (String s : new String[] {"Standalone", "SinglePage", "FullResolution", "ReducedResolution", "PyramidLayer",
          "Preview", "VolumeSlice", "ObjectView", "Panorama", "AnimationFrame", "TransparencyMask", "CompositingLayer",
          "SpectralSlice", "Unknown"}) {
        values.add(s);
      }
      addAttribute("SubimageInterpretation", "value", DATATYPE_STRING, true, null, values);
      addElement("ImageCreationTime", "Document", CHILD_POLICY_EMPTY);
      addAttribute("ImageCreationTime", "year", DATATYPE_INTEGER, true, null);
      addAttribute("ImageCreationTime", "month", DATATYPE_INTEGER, true, null, "1", "12", true, true);
      addAttribute("ImageCreationTime", "day", DATATYPE_INTEGER, true, null, "1", "31", true, true);
      addAttribute("ImageCreationTime", "hour", DATATYPE_INTEGER, false, "0", "0", "23", true, true);
      addAttribute("ImageCreationTime", "minute", DATATYPE_INTEGER, false, "0", "0", "59", true, true);
      addAttribute("ImageCreationTime", "second", DATATYPE_INTEGER, false, "0", "0", "60", true, true);
      addElement("ImageModificationTime", "Document", CHILD_POLICY_EMPTY);
      addAttribute("ImageModificationTime", "year", DATATYPE_INTEGER, true, null);
      addAttribute("ImageModificationTime", "month", DATATYPE_INTEGER, true, null, "1", "12", true, true);
      addAttribute("ImageModificationTime", "day", DATATYPE_INTEGER, true, null, "1", "31", true, true);
      addAttribute("ImageModificationTime", "hour", DATATYPE_INTEGER, false, "0", "0", "23", true, true);
      addAttribute("ImageModificationTime", "minute", DATATYPE_INTEGER, false, "0", "0", "59", true, true);
      addAttribute("ImageModificationTime", "second", DATATYPE_INTEGER, false, "0", "0", "60", true, true);

      addElement("Text", standardMetadataFormatName, CHILD_POLICY_SEQUENCE);
      addElement("TextEntry", "Text", CHILD_POLICY_EMPTY);
      addAttribute("TextEntry", "keyword", DATATYPE_STRING, false, null);
      addAttribute("TextEntry", "value", DATATYPE_STRING, true, null);
      addAttribute("TextEntry", "language", DATATYPE_STRING, false, null);
      addAttribute("TextEntry", "encoding", DATATYPE_STRING, false, null);
      values = new ArrayList<String>();
      values.add("none");
      values.add("lzw");
      values.add("zip");
      values.add("bzip");
      values.add("other");
      addAttribute("TextEntry", "compression", DATATYPE_STRING, false, "none", values);

      addElement("Transparency", standardMetadataFormatName, CHILD_POLICY_SOME);
      addElement("Alpha", "Transparency", CHILD_POLICY_EMPTY);
      values = new ArrayList<String>();
      values.add("none");
      values.add("premultiplied");
      values.add("nonpremultiplied");
      addAttribute("Alpha", "value", DATATYPE_STRING, false, "none", values);
      addElement("TransparentIndex", "Transparency", CHILD_POLICY_EMPTY);
      addAttribute("TransparentIndex", "value", DATATYPE_INTEGER, true, null);
      addElement("TransparentColor", "Transparency", CHILD_POLICY_EMPTY);
      addAttribute("TransparentColor", "value", DATATYPE_INTEGER, true, 0, Integer.MAX_VALUE);
      addElement("TileTransparencies", "Transparency", CHILD_POLICY_SEQUENCE);
      addElement("TransparentTile", "TileTransparencies", CHILD_POLICY_EMPTY);
      addAttribute("TransparentTile", "x", DATATYPE_INTEGER, true, null);
      addAttribute("TransparentTile", "y", DATATYPE_INTEGER, true, null);
      addElement("TileOpacities", "Transparency", CHILD_POLICY_SEQUENCE);
      addElement("OpaqueTile", "TileOpacities", CHILD_POLICY_EMPTY);
      addAttribute("OpaqueTile", "x", DATATYPE_INTEGER, true, null);
      addAttribute("OpaqueTile", "y", DATATYPE_INTEGER, true, null);
    }

    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) { return true; }
  }
}
