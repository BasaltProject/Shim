/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class XmlDtd {
  public static final int kEmpty = 0;
  public static final int kAny = 1;
  public static final int kMixed = 2;
  public static final int kChildren = 3;

  public static final class EntityDecl {
    public String name;
    public boolean parameter;
    public String value;
    public String publicId;
    public String systemId;
    public String expandedSystemId;
    public String notation;
    public String baseUri;
    public boolean external;
    public boolean declaredExternally;
  }

  public static final class AttDef {
    public String element;
    public String name;
    public String type;
    public String mode;
    public String value;
    public boolean declaredExternally;
  }

  public static final class ElementDecl {
    public String name;
    public String model;
    public int kind;
    public boolean declaredExternally;
  }

  public static final class NotationDecl {
    public String name;
    public String publicId;
    public String systemId;
    public String expandedSystemId;
  }

  public String rootName;
  public String rawText;
  public int endLine;
  public int endCol;
  public int endOffset;
  public String publicId;
  public String systemId;
  public String internalSubset;
  public boolean hasExternalParameterEntity;
  public final Map<String, EntityDecl> generalEntities = new LinkedHashMap<String, EntityDecl>();
  public final Map<String, EntityDecl> parameterEntities = new LinkedHashMap<String, EntityDecl>();
  public final Map<String, List<AttDef>> attributes = new LinkedHashMap<String, List<AttDef>>();
  public final Map<String, ElementDecl> elements = new LinkedHashMap<String, ElementDecl>();
  public final Map<String, NotationDecl> notations = new LinkedHashMap<String, NotationDecl>();

  public AttDef findAttribute(String element, String name) {
    List<AttDef> list = attributes.get(element);
    if (list == null) {
      return null;
    }
    for (AttDef def : list) {
      if (def.name.equals(name)) {
        return def;
      }
    }
    return null;
  }

  public List<AttDef> attributesOf(String element) {
    List<AttDef> list = attributes.get(element);
    return list == null ? new ArrayList<AttDef>() : list;
  }
}
