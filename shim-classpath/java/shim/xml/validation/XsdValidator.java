/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import shim.xml.validation.XsdModel.AttributeUse;
import shim.xml.validation.XsdModel.Complex;

/** Validates a stream of SAX events against a schema, reporting errors in the reference implementation's words. */
final class XsdValidator implements ContentHandler {
  private static final String kXsi = "http://www.w3.org/2001/XMLSchema-instance";

  private final XsdSchema schema;
  private ErrorHandler errorHandler;
  private ContentHandler forward;
  private Locator locator;
  private final List<Frame> stack = new ArrayList<Frame>();
  private final List<String[]> mappings = new ArrayList<String[]>();
  private int pendingStart = -1;
  private Set<String> ids = new HashSet<String>();
  private List<String> references = new ArrayList<String>();

  private static final class Frame {
    String qName;
    String uri;
    String local;
    XsdModel.Element decl;
    Complex complex;
    XsdSimple simple;
    XsdContent content;
    XsdContent.State state;
    StringBuilder text = new StringBuilder();
    boolean skip;
    boolean sawChild;
    boolean sawText;
    boolean contentFailed;
    boolean nilled;
    int mappingStart;
  }

  XsdValidator(XsdSchema schema) {
    this.schema = schema;
  }

  void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }

  ErrorHandler errorHandler() {
    return errorHandler;
  }

  void setForward(ContentHandler handler) {
    this.forward = handler;
  }

  ContentHandler forward() {
    return forward;
  }

  void reset() {
    stack.clear();
    mappings.clear();
    pendingStart = -1;
    ids = new HashSet<String>();
    references = new ArrayList<String>();
  }

  private void error(String message) throws SAXException {
    SAXParseException e = new SAXParseException(message, locator == null ? null : locator.getPublicId(),
        locator == null ? null : locator.getSystemId(), locator == null ? -1 : locator.getLineNumber(),
        locator == null ? -1 : locator.getColumnNumber());
    if (errorHandler == null) {
      throw e;
    }
    errorHandler.error(e);
  }

  private final XsdSimple.Ctx context = new XsdSimple.Ctx() {
    public String resolvePrefix(String prefix) {
      for (int i = mappings.size() - 1; i >= 0; i--) {
        if (mappings.get(i)[0].equals(prefix)) {
          return mappings.get(i)[1];
        }
      }
      return prefix.equals("xml") ? "http://www.w3.org/XML/1998/namespace" : null;
    }

    public boolean declareId(String id) {
      return ids.add(id);
    }

    public void referenceId(String id) {
      references.add(id);
    }

    public String owner() {
      return currentOwner;
    }
  };

  private String pendingError;
  private String currentOwner = "";

  private void flushPending() throws SAXException {
    if (pendingError != null) {
      String m = pendingError;
      pendingError = null;
      error(m);
    }
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
    if (forward != null) {
      forward.setDocumentLocator(locator);
    }
  }

  public void startDocument() throws SAXException {
    reset();
    if (forward != null) {
      forward.startDocument();
    }
  }

  public void endDocument() throws SAXException {
    if (forward != null) {
      forward.endDocument();
    }
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (pendingStart < 0) {
      pendingStart = mappings.size();
    }
    mappings.add(new String[] {prefix, uri});
    if (forward != null) {
      forward.startPrefixMapping(prefix, uri);
    }
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    if (forward != null) {
      forward.endPrefixMapping(prefix);
    }
  }

  private Frame top() {
    return stack.isEmpty() ? null : stack.get(stack.size() - 1);
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    String qn = qName == null || qName.isEmpty() ? localName : qName;
    Frame f = new Frame();
    f.qName = qn;
    f.uri = uri == null ? "" : uri;
    f.local = localName == null || localName.isEmpty() ? qn : localName;
    f.mappingStart = pendingStart >= 0 ? pendingStart : mappings.size();
    pendingStart = -1;
    Frame parent = top();
    XsdModel.Element decl = null;
    if (parent == null) {
      decl = schema.element(XsdSchema.key(f.uri, f.local));
      if (decl == null) {
        error("cvc-elt.1.a: Cannot find the declaration of element '" + qn + "'.");
        f.skip = true;
      }
    } else if (parent.skip) {
      f.skip = true;
    } else {
      parent.sawChild = true;
      if (parent.complex == null || parent.content == null) {
        f.skip = true;
      } else {
        XsdContent.Match m = parent.content.step(parent.state, f.uri, f.local);
        if (m == null) {
          if (!parent.contentFailed) {
            parent.contentFailed = true;
            error(parent.content.unexpected(parent.state, f.uri, f.local, shown(f)));
          }
          f.skip = true;
        } else if (m.decl != null) {
          decl = m.decl;
        } else {
          XsdModel.Element global = schema.element(XsdSchema.key(f.uri, f.local));
          if (global != null && !m.wildcard.process.equals("skip")) {
            decl = global;
          } else if (m.wildcard.process.equals("strict")) {
            error("cvc-complex-type.2.4.c: The matching wildcard is strict, but no declaration can be found for element '"
                + qn + "'.");
            f.skip = true;
          } else {
            f.skip = true;
          }
        }
      }
    }
    if (!f.skip && decl != null) {
      f.decl = decl;
      Object type = schema.typeOf(decl);
      String xsiType = atts == null ? null : atts.getValue(kXsi, "type");
      if (xsiType != null) {
        Object t = schema.type(resolveQName(xsiType));
        if (t != null) {
          type = t;
        } else {
          error("cvc-elt.4.2: Cannot resolve '" + xsiType + "' to a type definition for element '" + qn + "'.");
        }
      }
      if (type instanceof XsdSimple) {
        f.simple = (XsdSimple) type;
      } else {
        f.complex = (Complex) type;
        if (f.complex.content == XsdModel.kSimple) {
          f.simple = f.complex.simple;
        }
      }
      String nil = atts == null ? null : atts.getValue(kXsi, "nil");
      if (nil != null && (nil.trim().equals("true") || nil.trim().equals("1"))) {
        if (!decl.nillable) {
          error("cvc-elt.3.1: Attribute '" + kXsi + ",nil' must not appear on element '" + qn
              + "', because the {nillable} property of '" + qn + "' is false.");
        } else {
          f.nilled = true;
        }
      }
      checkAttributes(f, atts);
      if (f.complex != null && f.complex.particle != null && f.complex.content != XsdModel.kSimple) {
        if (f.complex.compiled == null) {
          f.complex.compiled = new XsdContent(f.complex.particle, schema);
        }
        f.content = f.complex.compiled;
        f.state = f.content.begin();
      }
    } else if (!f.skip) {
      f.skip = true;
    }
    stack.add(f);
    if (forward != null) {
      forward.startElement(uri, localName, qName, atts);
    }
  }

  private static String shown(Frame f) {
    return f.uri.isEmpty() ? f.qName : "{\"" + f.uri + "\":" + f.local + "}";
  }

  private String resolveQName(String value) {
    value = value.trim();
    int colon = value.indexOf(':');
    String prefix = colon < 0 ? "" : value.substring(0, colon);
    String local = colon < 0 ? value : value.substring(colon + 1);
    String ns = context.resolvePrefix(prefix);
    return XsdSchema.key(ns == null ? "" : ns, local);
  }

  private void checkAttributes(Frame f, Attributes atts) throws SAXException {
    List<AttributeUse> uses = f.complex == null ? new ArrayList<AttributeUse>() : f.complex.attributes;
    Set<AttributeUse> seen = new HashSet<AttributeUse>();
    int count = atts == null ? 0 : atts.getLength();
    for (int i = 0; i < count; i++) {
      String aq = atts.getQName(i);
      String au = atts.getURI(i);
      String al = atts.getLocalName(i);
      if (al == null || al.isEmpty()) {
        al = aq;
      }
      if (aq.equals("xmlns") || aq.startsWith("xmlns:") || kXsi.equals(au)) {
        continue;
      }
      AttributeUse use = null;
      for (AttributeUse u : uses) {
        if (u.decl.name.equals(al) && u.decl.namespace.equals(au == null ? "" : au)) {
          use = u;
          break;
        }
      }
      if (use == null) {
        if (f.complex == null && f.simple != null) {
          error("cvc-type.3.1.1: Element '" + f.qName + "' is a simple type, so it cannot have attributes, excepting "
              + "those whose namespace name is identical to 'http://www.w3.org/2001/XMLSchema-instance' and whose "
              + "[local name] is one of 'type', 'nil', 'schemaLocation' or 'noNamespaceSchemaLocation'. However, "
              + "the attribute, '" + aq + "' was found.");
          continue;
        }
        if (f.complex != null && f.complex.anyAttribute != null && f.complex.anyAttribute.allows(au)) {
          continue;
        }
        error("cvc-complex-type.3.2.2: Attribute '" + aq + "' is not allowed to appear in element '" + f.qName + "'.");
        continue;
      }
      seen.add(use);
      if (use.use.equals("prohibited")) {
        error("cvc-complex-type.3.2.2: Attribute '" + aq + "' is not allowed to appear in element '" + f.qName + "'.");
        continue;
      }
      String value = atts.getValue(i);
      currentOwner = "attribute '" + aq + "'";
      XsdSimple.Outcome o = use.type.check(value, context);
      flushPending();
      if (o.error != null) {
        error(o.error);
        error("cvc-attribute.3: The value '" + value + "' of attribute '" + aq + "' on element '" + f.qName
            + "' is not valid with respect to its type, '" + use.type.displayName() + "'.");
      } else if (use.fixed != null && !XsdLexical.equalValues(use.type.lexical, XsdSimple.normalize(use.fixed,
          use.type.effectiveWhiteSpace()), o.canonical == null ? value : o.canonical)) {
        error("cvc-complex-type.3.1: Value '" + value + "' of attribute '" + aq + "' of element '" + f.qName
            + "' is not valid with respect to the corresponding attribute use. Attribute '" + aq
            + "' has a fixed value of '" + use.fixed + "'.");
      }
    }
    for (AttributeUse u : uses) {
      if (u.use.equals("required") && !seen.contains(u)) {
        error("cvc-complex-type.4: Attribute '" + u.decl.name + "' must appear on element '" + f.qName + "'.");
      }
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    Frame f = top();
    if (f != null && !f.skip) {
      f.text.append(ch, start, length);
      if (!f.sawText) {
        for (int i = start; i < start + length; i++) {
          char c = ch[i];
          if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
            f.sawText = true;
            break;
          }
        }
      }
    }
    if (forward != null) {
      forward.characters(ch, start, length);
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    characters(ch, start, length);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (forward != null) {
      forward.processingInstruction(target, data);
    }
  }

  public void skippedEntity(String name) throws SAXException {
    if (forward != null) {
      forward.skippedEntity(name);
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    Frame f = stack.remove(stack.size() - 1);
    if (!f.skip && f.decl != null) {
      finish(f);
    }
    while (mappings.size() > f.mappingStart) {
      mappings.remove(mappings.size() - 1);
    }
    if (stack.isEmpty()) {
      for (String ref : references) {
        if (!ids.contains(ref)) {
          error("cvc-id.1: There is no ID/IDREF binding for IDREF '" + ref + "'.");
        }
      }
    }
    if (forward != null) {
      forward.endElement(uri, localName, qName);
    }
  }

  private void finish(Frame f) throws SAXException {
    String text = f.text.toString();
    if (f.nilled) {
      if (f.sawChild || !text.isEmpty()) {
        error("cvc-elt.3.2.1: Element '" + f.qName + "' cannot have character or element information [children], "
            + "because '" + kXsi + ",nil' is specified.");
      }
      return;
    }
    if (f.complex == null || f.complex.content == XsdModel.kSimple) {
      if (f.complex == null && f.sawChild) {
        error("cvc-type.3.1.2: Element '" + f.qName
            + "' is a simple type, so it must have no element information item [children].");
        return;
      }
      if (f.complex != null && f.sawChild) {
        error("cvc-complex-type.2.2: Element '" + f.qName
            + "' must have no element [children], and the value must be valid.");
      }
      XsdSimple type = f.simple == null ? XsdSimple.builtin("anySimpleType") : f.simple;
      String value = text;
      if (value.isEmpty() && f.decl.defaultValue != null) {
        value = f.decl.defaultValue;
      }
      if (value.isEmpty() && f.decl.fixed != null) {
        value = f.decl.fixed;
      }
      currentOwner = "element '" + f.qName + "'";
      XsdSimple.Outcome o = type.check(value, context);
      flushPending();
      if (o.error != null) {
        error(o.error);
        if (f.complex != null) {
          error("cvc-complex-type.2.2: Element '" + f.qName
              + "' must have no element [children], and the value must be valid.");
        } else {
          error("cvc-type.3.1.3: The value '" + value + "' of element '" + f.qName + "' is not valid.");
        }
      } else if (f.decl.fixed != null && !XsdLexical.equalValues(type.lexical, XsdSimple.normalize(f.decl.fixed,
          type.effectiveWhiteSpace()), o.canonical == null ? value : o.canonical)) {
        error("cvc-elt.5.2.2.2.2: The value '" + value + "' of element '" + f.qName
            + "' does not match the {value constraint} value '" + f.decl.fixed + "'.");
      }
      return;
    }
    Complex c = f.complex;
    if (c.content == XsdModel.kEmpty) {
      if (f.sawChild || f.text.length() > 0) {
        error("cvc-complex-type.2.1: Element '" + f.qName
            + "' must have no character or element information item [children], because the type's content type is empty.");
      }
      return;
    }
    if (c.content == XsdModel.kElementOnly && f.sawText) {
      error("cvc-complex-type.2.3: Element '" + f.qName
          + "' cannot have character [children], because the type's content type is element-only.");
    }
    if (f.content != null && !f.contentFailed && !f.content.canEnd(f.state)) {
      error(f.content.incomplete(f.state, f.qName));
    }
  }
}
