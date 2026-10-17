/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSResourceResolver;

public final class DomParamConfig implements DOMConfiguration {
  private static final int kBoth = 0;
  private static final int kTrueOnly = 1;
  private static final int kFalseOnly = 2;
  private static final int kHandler = 3;
  private static final int kResolver = 4;
  private static final int kString = 5;

  private final boolean serializer;
  private final Map<String, Integer> rules = new LinkedHashMap<String, Integer>();
  private final Map<String, Object> values = new LinkedHashMap<String, Object>();

  private DomParamConfig(boolean serializer) {
    this.serializer = serializer;
  }

  private void add(String name, int rule, Object value) {
    rules.put(name, Integer.valueOf(rule));
    values.put(name, value);
  }

  public static DomParamConfig forSerializer() {
    DomParamConfig c = new DomParamConfig(true);
    c.add("canonical-form", kFalseOnly, Boolean.FALSE);
    c.add("cdata-sections", kBoth, Boolean.TRUE);
    c.add("check-character-normalization", kFalseOnly, Boolean.FALSE);
    c.add("comments", kBoth, Boolean.TRUE);
    c.add("datatype-normalization", kFalseOnly, Boolean.FALSE);
    c.add("discard-default-content", kBoth, Boolean.TRUE);
    c.add("element-content-whitespace", kBoth, Boolean.TRUE);
    c.add("entities", kBoth, Boolean.TRUE);
    c.add("error-handler", kHandler, null);
    c.add("format-pretty-print", kBoth, Boolean.FALSE);
    c.add("http://www.oracle.com/xml/jaxp/properties/isStandalone", kBoth, Boolean.FALSE);
    c.add("ignore-unknown-character-denormalizations", kTrueOnly, Boolean.TRUE);
    c.add("infoset", kBoth, Boolean.FALSE);
    c.add("jdk.xml.isStandalone", kBoth, Boolean.FALSE);
    c.add("namespace-declarations", kBoth, Boolean.TRUE);
    c.add("namespaces", kBoth, Boolean.TRUE);
    c.add("split-cdata-sections", kBoth, Boolean.TRUE);
    c.add("validate", kFalseOnly, Boolean.FALSE);
    c.add("validate-if-schema", kFalseOnly, Boolean.FALSE);
    c.add("well-formed", kBoth, Boolean.TRUE);
    c.add("xml-declaration", kBoth, Boolean.TRUE);
    return c;
  }

  public static DomParamConfig forParser() {
    DomParamConfig c = new DomParamConfig(false);
    c.add("canonical-form", kFalseOnly, Boolean.FALSE);
    c.add("cdata-sections", kBoth, Boolean.FALSE);
    c.add("charset-overrides-xml-encoding", kBoth, Boolean.TRUE);
    c.add("check-character-normalization", kFalseOnly, Boolean.FALSE);
    c.add("comments", kBoth, Boolean.TRUE);
    c.add("datatype-normalization", kBoth, Boolean.FALSE);
    c.add("disallow-doctype", kBoth, Boolean.FALSE);
    c.add("element-content-whitespace", kBoth, Boolean.TRUE);
    c.add("entities", kBoth, Boolean.FALSE);
    c.add("error-handler", kHandler, null);
    c.add("ignore-unknown-character-denormalizations", kTrueOnly, Boolean.TRUE);
    c.add("infoset", kBoth, Boolean.FALSE);
    c.add("namespace-declarations", kBoth, Boolean.TRUE);
    c.add("namespaces", kBoth, Boolean.TRUE);
    c.add("normalize-characters", kFalseOnly, Boolean.FALSE);
    c.add("resource-resolver", kResolver, null);
    c.add("schema-location", kString, null);
    c.add("schema-type", kString, null);
    c.add("split-cdata-sections", kBoth, Boolean.TRUE);
    c.add("supported-media-types-only", kFalseOnly, Boolean.FALSE);
    c.add("validate", kBoth, Boolean.FALSE);
    c.add("validate-if-schema", kBoth, Boolean.FALSE);
    c.add("well-formed", kTrueOnly, Boolean.TRUE);
    c.add("http://apache.org/xml/features/disallow-doctype-decl", kBoth, Boolean.FALSE);
    return c;
  }

  private DOMException notRecognized(String name, int code) {
    String text = serializer ? "The parameter '" + name + "' is not recognized."
        : "FEATURE_NOT_FOUND: The parameter " + name + " is not recognized.";
    return new DOMException((short) code, text);
  }

  private DOMException notSettable(String name) {
    String text = serializer ? "The parameter '" + name + "' is recognized but the requested value cannot be set."
        : "FEATURE_NOT_SUPPORTED: The parameter " + name + " is recognized but the requested value cannot be set.";
    return new DOMException(DOMException.NOT_SUPPORTED_ERR, text);
  }

  private DOMException mismatch() {
    String text = serializer ? "The value type for this parameter name is incompatible with the expected value type."
        : "TYPE_MISMATCH_ERR: The value type for this parameter name is incompatible with the expected value type.";
    return new DOMException(DOMException.TYPE_MISMATCH_ERR, text);
  }

  private String key(String name) {
    String lower = name.toLowerCase();
    for (String k : rules.keySet()) {
      if (k.toLowerCase().equals(lower)) {
        return k;
      }
    }
    return null;
  }

  public boolean bool(String name) {
    Object v = values.get(name);
    return v instanceof Boolean && ((Boolean) v).booleanValue();
  }

  public Object value(String name) {
    return values.get(name);
  }

  public void setParameter(String name, Object value) throws DOMException {
    if (name == null) {
      throw new NullPointerException("Cannot invoke \"String.equalsIgnoreCase(String)\" because \"name\" is null");
    }
    String k = key(name);
    if (serializer) {
      setSerializer(name, k, value);
    } else {
      setParser(name, k, value);
    }
  }

  private void setSerializer(String name, String k, Object value) {
    String lower = name.toLowerCase();
    if (value instanceof Boolean) {
      boolean on = ((Boolean) value).booleanValue();
      if (lower.equals("normalize-characters")) {
        throw notSettable(name);
      }
      if (k == null) {
        return;
      }
      int rule = rules.get(k).intValue();
      if ((rule == kFalseOnly && on && !k.equals("well-formed")) || (rule == kTrueOnly && !on)) {
        throw notSettable(name);
      }
      if (rule == kHandler) {
        return;
      }
      if (k.equals("infoset")) {
        if (on) {
          applyInfoset();
        }
        return;
      }
      values.put(k, Boolean.valueOf(on));
      return;
    }
    if (k != null && rules.get(k).intValue() == kHandler) {
      if (value == null || value instanceof DOMErrorHandler) {
        values.put(k, value);
        return;
      }
      throw mismatch();
    }
    if (lower.equals("schema-type") || lower.equals("schema-location") || lower.equals("normalize-characters")
        && value != null) {
      throw notSettable(name);
    }
    throw notRecognized(name, DOMException.NOT_FOUND_ERR);
  }

  private void applyInfoset() {
    put("namespace-declarations", true);
    put("validate-if-schema", false);
    put("entities", false);
    put("datatype-normalization", false);
    put("cdata-sections", false);
    put("well-formed", true);
    put("element-content-whitespace", true);
    put("comments", true);
    put("namespaces", true);
  }

  private void setParser(String name, String k, Object value) {
    if (k == null) {
      throw notRecognized(name, DOMException.NOT_FOUND_ERR);
    }
    int rule = rules.get(k).intValue();
    switch (rule) {
      case kHandler:
      case kResolver:
        if (value == null) {
          values.put(k, null);
        } else if (value instanceof Boolean) {
          throw notRecognized(name, DOMException.NOT_FOUND_ERR);
        } else if ((rule == kHandler && value instanceof DOMErrorHandler)
            || (rule == kResolver && value instanceof LSResourceResolver)) {
          values.put(k, value);
        } else {
          throw mismatch();
        }
        return;
      case kString:
        if (value instanceof Boolean) {
          throw notRecognized(name, DOMException.NOT_FOUND_ERR);
        }
        if (value != null && !(value instanceof String)) {
          throw mismatch();
        }
        if (k.equals("schema-type") && !"http://www.w3.org/2001/XMLSchema".equals(value)
            && !"http://www.w3.org/TR/REC-xml".equals(value)) {
          return;
        }
        values.put(k, value);
        return;
      default:
        break;
    }
    if (!(value instanceof Boolean)) {
      boolean strictType = k.equals("well-formed") || k.equals("canonical-form") || k.equals("infoset")
          || k.equals("supported-media-types-only") || k.equals("check-character-normalization")
          || k.equals("ignore-unknown-character-denormalizations");
      if (strictType) {
        throw mismatch();
      }
      throw notRecognized(name, DOMException.NOT_FOUND_ERR);
    }
    boolean on = ((Boolean) value).booleanValue();
    if ((rule == kFalseOnly && on) || (rule == kTrueOnly && !on)) {
      throw notSettable(name);
    }
    if (k.equals("infoset")) {
      if (on) {
        applyInfoset();
      }
      return;
    }
    values.put(k, Boolean.valueOf(on));
  }

  private void put(String name, boolean v) {
    if (values.containsKey(name)) {
      values.put(name, Boolean.valueOf(v));
    }
  }

  private boolean derivedInfoset() {
    return bool("namespace-declarations") && !bool("validate-if-schema") && !bool("entities")
        && !bool("datatype-normalization") && !bool("cdata-sections") && bool("well-formed")
        && bool("element-content-whitespace") && bool("comments") && bool("namespaces");
  }

  public Object getParameter(String name) throws DOMException {
    String k = key(name);
    if (k == null) {
      if (serializer && name.equalsIgnoreCase("normalize-characters")) {
        return null;
      }
      throw notRecognized(name, DOMException.NOT_FOUND_ERR);
    }
    if (k.equals("infoset")) {
      return Boolean.valueOf(derivedInfoset());
    }
    return values.get(k);
  }

  public boolean canSetParameter(String name, Object value) {
    String k = key(name);
    if (k == null) {
      return !serializer && name.equalsIgnoreCase("xml-declaration") && value instanceof Boolean;
    }
    int rule = rules.get(k).intValue();
    switch (rule) {
      case kHandler:
        return value == null || (!serializer && value instanceof DOMErrorHandler)
            || (serializer && value instanceof DOMErrorHandler);
      case kResolver:
        return value == null || value instanceof LSResourceResolver;
      case kString:
        return value == null || value instanceof String;
      default:
        break;
    }
    if (!(value instanceof Boolean)) {
      return false;
    }
    boolean on = ((Boolean) value).booleanValue();
    if (rule == kTrueOnly) {
      return on;
    }
    if (rule == kFalseOnly) {
      return !on;
    }
    return true;
  }

  public DOMStringList getParameterNames() {
    List<String> names = new ArrayList<String>();
    for (String n : rules.keySet()) {
      if (!n.startsWith("http://apache.org/")) {
        names.add(n);
      }
    }
    Collections.sort(names);
    return new DomStringList(names);
  }
}
