/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSResourceResolver;

public class DomConfiguration implements DOMConfiguration {
  private static final String[] kNames = {
    "comments", "datatype-normalization", "cdata-sections", "entities", "split-cdata-sections", "namespaces",
    "validate", "infoset", "normalize-characters", "canonical-form", "validate-if-schema",
    "check-character-normalization", "well-formed", "namespace-declarations", "element-content-whitespace",
    "error-handler", "schema-type", "schema-location", "resource-resolver",
    "http://apache.org/xml/properties/internal/entity-resolver",
        "http://apache.org/xml/properties/internal/grammar-pool",
    "http://apache.org/xml/properties/security-manager", "http://apache.org/xml/properties/internal/symbol-table",
    "http://apache.org/xml/features/validation/schema/augment-psvi"
  };

  private final DocumentImpl document;
  private final Map<String, Object> values = new HashMap<String, Object>();

  DomConfiguration(DocumentImpl document) {
    this.document = document;
    values.put("comments", Boolean.TRUE);
    values.put("datatype-normalization", Boolean.FALSE);
    values.put("cdata-sections", Boolean.TRUE);
    values.put("entities", Boolean.TRUE);
    values.put("split-cdata-sections", Boolean.TRUE);
    values.put("namespaces", Boolean.TRUE);
    values.put("validate", Boolean.FALSE);
    values.put("infoset", Boolean.FALSE);
    values.put("normalize-characters", Boolean.FALSE);
    values.put("canonical-form", Boolean.FALSE);
    values.put("validate-if-schema", Boolean.FALSE);
    values.put("check-character-normalization", Boolean.FALSE);
    values.put("well-formed", Boolean.TRUE);
    values.put("namespace-declarations", Boolean.TRUE);
    values.put("element-content-whitespace", Boolean.TRUE);
    values.put("http://apache.org/xml/features/validation/schema/augment-psvi", Boolean.TRUE);
  }

  private String key(String name) {
    String lower = name.toLowerCase();
    for (String n : kNames) {
      if (n.toLowerCase().equals(lower)) {
        return n;
      }
    }
    return null;
  }

  private static boolean isBooleanParam(String k) {
    return !(k.equals("error-handler") || k.equals("schema-type") || k.equals("schema-location")
        || k.equals("resource-resolver") || k.startsWith("http://apache.org/xml/properties/"));
  }

  public void setParameter(String name, Object value) throws DOMException {
    String k = key(name);
    if (k == null) {
      throw new DOMException(DOMException.NOT_FOUND_ERR, "FEATURE_NOT_FOUND: The parameter " + name
          + " is not recognized.");
    }
    if (isBooleanParam(k)) {
      if (!(value instanceof Boolean)) {
        throw new DOMException(DOMException.NOT_FOUND_ERR, "FEATURE_NOT_FOUND: The parameter " + name
            + " is not recognized.");
      }
      boolean on = ((Boolean) value).booleanValue();
      if (!canSetParameter(name, value)) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "FEATURE_NOT_SUPPORTED: The parameter " + name
            + " is recognized but the requested value cannot be set.");
      }
      if (k.equals("infoset")) {
        if (on) {
          values.put("namespace-declarations", Boolean.TRUE);
          values.put("validate-if-schema", Boolean.FALSE);
          values.put("entities", Boolean.FALSE);
          values.put("datatype-normalization", Boolean.FALSE);
          values.put("cdata-sections", Boolean.FALSE);
          values.put("well-formed", Boolean.TRUE);
          values.put("element-content-whitespace", Boolean.TRUE);
          values.put("comments", Boolean.TRUE);
          values.put("namespaces", Boolean.TRUE);
        }
        values.put("infoset", on);
        return;
      }
      values.put(k, on);
      return;
    }
    if (k.equals("error-handler")) {
      if (value != null && !(value instanceof DOMErrorHandler)) {
        throw DomMessages.error(DOMException.TYPE_MISMATCH_ERR);
      }
    } else if (k.equals("resource-resolver")) {
      if (value != null && !(value instanceof LSResourceResolver)) {
        throw DomMessages.error(DOMException.TYPE_MISMATCH_ERR);
      }
    } else if (k.equals("schema-type") || k.equals("schema-location")) {
      if (value != null && !(value instanceof String)) {
        throw DomMessages.error(DOMException.TYPE_MISMATCH_ERR);
      }
    }
    values.put(k, value);
  }

  public Object getParameter(String name) throws DOMException {
    String k = key(name);
    if (k == null) {
      throw new DOMException(DOMException.NOT_FOUND_ERR, "FEATURE_NOT_FOUND: The parameter " + name
          + " is not recognized.");
    }
    if (k.equals("infoset")) {
      return Boolean.valueOf(bool("namespace-declarations") && !bool("validate-if-schema") && !bool("entities")
          && !bool("datatype-normalization") && !bool("cdata-sections") && bool("well-formed")
          && bool("element-content-whitespace") && bool("comments") && bool("namespaces"));
    }
    return values.get(k);
  }

  boolean bool(String k) {
    Object v = values.get(k);
    return v instanceof Boolean && ((Boolean) v).booleanValue();
  }

  public boolean canSetParameter(String name, Object value) {
    String k = key(name);
    if (k == null) {
      return false;
    }
    if (value == null) {
      return true;
    }
    if (isBooleanParam(k)) {
      if (!(value instanceof Boolean)) {
        return false;
      }
      boolean on = ((Boolean) value).booleanValue();
      if (k.equals("canonical-form") || k.equals("check-character-normalization") || k.equals("normalize-characters")) {
        return !on;
      }
      if (k.equals("well-formed") || k.equals("split-cdata-sections") || k.equals("namespaces")
          || k.equals("element-content-whitespace") || k.equals("comments") || k.equals("cdata-sections")
          || k.equals("entities") || k.equals("namespace-declarations") || k.equals("infoset")
          || k.equals("datatype-normalization") || k.equals("validate") || k.equals("validate-if-schema")
          || k.equals("http://apache.org/xml/features/validation/schema/augment-psvi")) {
        return true;
      }
      return true;
    }
    if (k.equals("error-handler")) {
      return value instanceof DOMErrorHandler;
    }
    if (k.equals("resource-resolver")) {
      return value instanceof LSResourceResolver;
    }
    if (k.equals("schema-type")) {
      return value instanceof String && (value.equals("http://www.w3.org/2001/XMLSchema")
          || value.equals("http://www.w3.org/TR/REC-xml"));
    }
    return true;
  }

  public DOMStringList getParameterNames() {
    List<String> names = new ArrayList<String>();
    for (String n : kNames) {
      names.add(n);
    }
    return new DomStringList(names);
  }
}
