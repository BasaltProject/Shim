/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class XmlDtdScanner {
  private final XmlPullParser p;
  private final XmlScanner sc;
  private XmlDtd dtd;
  private boolean external;
  private int conditionalDepth;

  XmlDtdScanner(XmlPullParser parser, XmlScanner scanner) {
    this.p = parser;
    this.sc = scanner;
  }

  private void emit(XmlPullParser.Ev e) {
    p.enqueue(e);
  }

  private XmlPullParser.Ev ev(int kind) {
    return p.newEvent(kind);
  }

  private int phase;
  private int externalBase;
  private boolean externalPeUsed;

  boolean usedExternalParameterEntity() {
    return externalPeUsed;
  }

  void start() throws IOException, XmlException {
    sc.skipString("<!DOCTYPE");
    if (!sc.skipSpaces()) {
      throw p.error("White space is required after \"<!DOCTYPE\" in the document type declaration.");
    }
    String root = sc.scanName();
    if (root == null) {
      if (sc.peek() < 0) {
        throw p.error(XmlMessages.kMarkupEntityMismatch);
      }
      throw p.error("The root element type must appear after \"<!DOCTYPE\" in the document type declaration.");
    }
    dtd = new XmlDtd();
    dtd.rootName = root;
    boolean space = sc.skipSpaces();
    if (sc.peek() < 0) {
      throw p.error(XmlMessages.kMarkupEntityMismatch);
    }
    if (sc.lookingAt("SYSTEM") || sc.lookingAt("PUBLIC")) {
      if (!space) {
        throw p.error("White space is required after the document type name.");
      }
      String[] ids = externalId(false, "<!DOCTYPE");
      dtd.publicId = ids[0];
      dtd.systemId = ids[1];
      sc.skipSpaces();
    }
    XmlPullParser.Ev start = ev(XmlPullParser.kDtdStart);
    start.a = root;
    start.b = dtd.publicId;
    start.c = dtd.systemId;
    emit(start);
    p.setDtd(dtd);
    if (sc.peek() == '[') {
      sc.read();
      phase = 1;
    } else {
      phase = 6;
    }
  }

  private void closeHeader() throws IOException, XmlException {
    if (sc.peek() != '>') {
      if (sc.peek() < 0) {
        throw p.error(XmlMessages.kMarkupEntityMismatch);
      }
      throw p.error(XmlMessages.doctypeUnterminated(dtd.rootName));
    }
    sc.read();
    phase = 2;
  }

  boolean step() throws IOException, XmlException {
    switch (phase) {
    case 1: {
      external = false;
      sc.skipSpaces();
      if (sc.peek() == ']' && sc.depth() == 0) {
        sc.read();
        sc.skipSpaces();
        closeHeader();
        return true;
      }
      declaration(false, 0);
      return true;
    }
    case 6:
      closeHeader();
      return true;
    case 2: {
      if (dtd.systemId != null && p.loadExternalDtd) {
        if (loadExternal()) {
          phase = 3;
          return true;
        }
      }
      emit(ev(XmlPullParser.kDtdEnd));
      phase = 4;
      return false;
    }
    case 3: {
      external = true;
      if (declaration(true, externalBase)) {
        sc.pop();
        external = false;
        XmlPullParser.Ev endEntity = ev(XmlPullParser.kEntityEnd);
        endEntity.a = "[dtd]";
        emit(endEntity);
        emit(ev(XmlPullParser.kDtdEnd));
        phase = 4;
        return false;
      }
      return true;
    }
    default:
      return false;
    }
  }

  private XmlException eofOrError(String message) throws IOException {
    if (sc.peek() < 0) {
      return p.errorAfterPop(XmlMessages.kPrematureEof);
    }
    return p.error(message);
  }

  private boolean loadExternal() throws IOException, XmlException {
    XmlPullParser.Resolver resolver = p.resolver();
    if (resolver == null) {
      return false;
    }
    XmlInput in = resolver.resolve("[dtd]", dtd.publicId, dtd.systemId, p.input().baseUri);
    if (in == null) {
      return false;
    }
    XmlPullParser.Ev startEntity = ev(XmlPullParser.kEntityStart);
    startEntity.a = "[dtd]";
    emit(startEntity);
    sc.pushReader("[dtd]", in.reader, in.systemId, in.publicId, in.baseUri);
    skipTextDecl();
    externalBase = sc.depth();
    return true;
  }

  private void skipTextDecl() throws IOException, XmlException {
    if (sc.lookingAt("<?xml") && sc.peek(5) >= 0 && XmlChars.isSpace(sc.peek(5))) {
      p.scanTextDecl();
    }
  }

  private boolean declaration(boolean isExternal, int base) throws IOException, XmlException {
    sc.skipSpaces();
    int c = sc.peek();
    if (c == XmlScanner.kEndOfEntity) {
      if (sc.depth() > base) {
        XmlScanner.Frame f = sc.current();
        String name = f.name;
        sc.pop();
        if (name != null && name.startsWith("%")) {
          XmlPullParser.Ev end = ev(XmlPullParser.kEntityEnd);
          end.a = name;
          emit(end);
        }
        return false;
      }
      return isExternal;
    }
    if (c == XmlScanner.kEof) {
      if (isExternal) {
        return true;
      }
      throw p.errorAfterPop(XmlMessages.kPrematureEof);
    }
    if (c == ']' && isExternal && conditionalDepth > 0 && sc.lookingAt("]]>")) {
      sc.skipString("]]>");
      conditionalDepth--;
      return false;
    }
    if (c == '%') {
      paramEntityRef(true);
      return false;
    }
    if (c != '<') {
      throw p.error(XmlMessages.kMarkupNotRecognizedInDtd);
    }
    if (sc.lookingAt("<!--")) {
      dtdComment();
    } else if (sc.lookingAt("<?")) {
      dtdPi();
    } else if (sc.lookingAt("<!ELEMENT")) {
      elementDecl();
    } else if (sc.lookingAt("<!ATTLIST")) {
      attlistDecl();
    } else if (sc.lookingAt("<!ENTITY")) {
      entityDecl();
    } else if (sc.lookingAt("<!NOTATION")) {
      notationDecl();
    } else if (isExternal && sc.lookingAt("<![")) {
      conditionalSection();
    } else {
      sc.read();
      sc.read();
      if (sc.peek() == '[') {
        sc.read();
      }
      throw p.error(XmlMessages.kMarkupNotRecognizedInDtd);
    }
    return false;
  }

  private void conditionalSection() throws IOException, XmlException {
    sc.skipString("<![");
    spaces();
    String kind = sc.scanName();
    spaces();
    if (sc.peek() != '[') {
      throw p.error(XmlMessages.kMarkupNotRecognizedInDtd);
    }
    sc.read();
    if ("INCLUDE".equals(kind)) {
      conditionalDepth++;
    } else if ("IGNORE".equals(kind)) {
      int depth = 1;
      while (depth > 0) {
        if (sc.lookingAt("<![")) {
          sc.skipString("<![");
          depth++;
        } else if (sc.lookingAt("]]>")) {
          sc.skipString("]]>");
          depth--;
        } else if (sc.read() < 0) {
          throw p.error("The excluded conditional section must end with \"]]>\".");
        }
      }
    } else {
      throw p.error(XmlMessages.kMarkupNotRecognizedInDtd);
    }
  }

  private void paramEntityRef(boolean emitEvents) throws IOException, XmlException {
    sc.read();
    String name = sc.scanName();
    if (name == null) {
      throw p.error(XmlMessages.kNameRequiredInPeReference);
    }
    if (sc.peek() != ';') {
      throw p.error(XmlMessages.semicolonRequiredInPeReference(name));
    }
    sc.read();
    XmlDtd.EntityDecl decl = dtd.parameterEntities.get(name);
    if (decl == null) {
      return;
    }
    if (sc.isActiveEntity("%" + name)) {
      throw p.error("Recursive parameter entity reference \"%" + name + ";\". (Reference path: "
          + sc.entityPath("%" + name) + "),");
    }
    if (emitEvents) {
      XmlPullParser.Ev start = ev(XmlPullParser.kEntityStart);
      start.a = "%" + name;
      emit(start);
    }
    if (!decl.external) {
      sc.pushInternal(emitEvents ? "%" + name : null, " " + decl.value + " ", decl.baseUri);
    } else {
      externalPeUsed = true;
      dtd.hasExternalParameterEntity = true;
      XmlPullParser.Resolver resolver = p.resolver();
      XmlInput in = resolver == null ? null : resolver.resolve("%" + name, decl.publicId, decl.systemId, decl.baseUri);
      if (in != null) {
        sc.pushReader(emitEvents ? "%" + name : null, in.reader, in.systemId, in.publicId, in.baseUri);
        skipTextDecl();
      }
    }
  }

  private boolean spaces() throws IOException, XmlException {
    boolean any = false;
    while (true) {
      if (sc.skipSpaces()) {
        any = true;
      }
      int c = sc.peek();
      if (c == '%' && (external || sc.depth() > 1) && sc.peek(1) >= 0 && XmlChars.isNameStart(sc.peek(1))) {
        paramEntityRef(false);
        any = true;
        continue;
      }
      if (c == XmlScanner.kEndOfEntity && sc.current() != null && sc.current().name == null && !sc.isTopFrame()) {
        sc.pop();
        any = true;
        continue;
      }
      return any;
    }
  }

  private void dtdComment() throws IOException, XmlException {
    sc.skipString("<!--");
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw p.errorAfterPop(XmlMessages.kPrematureEof);
      }
      if (c == '-' && sc.peek(1) == '-') {
        sc.read();
        sc.read();
        if (sc.peek() != '>') {
          throw p.error(XmlMessages.kDashDashInComment);
        }
        sc.read();
        break;
      }
      sb.append((char) sc.read());
    }
    XmlPullParser.Ev e = ev(XmlPullParser.kComment);
    e.a = sb.toString();
    emit(e);
  }

  private void dtdPi() throws IOException, XmlException {
    sc.skipString("<?");
    String target = sc.scanName();
    if (target == null) {
      throw p.error(XmlMessages.kPiTargetRequired);
    }
    if (target.equalsIgnoreCase("xml")) {
      throw p.error(XmlMessages.kReservedPiTarget);
    }
    StringBuilder data = new StringBuilder();
    if (!sc.skipString("?>")) {
      if (!sc.skipSpaces()) {
        throw p.error(XmlMessages.kSpaceRequiredInPi);
      }
      while (true) {
        int c = sc.peek();
        if (c < 0) {
          throw p.errorAfterPop(XmlMessages.kPrematureEof);
        }
        if (c == '?' && sc.peek(1) == '>') {
          sc.read();
          sc.read();
          break;
        }
        data.append((char) sc.read());
      }
    }
  }

  private String[] externalId(boolean notation, String owner) throws IOException, XmlException {
    String pub = null;
    String sys = null;
    if (sc.skipString("SYSTEM")) {
      if (!sc.skipSpaces()) {
        throw p.error("White space is required after keyword SYSTEM in DOCTYPE decl.");
      }
      sys = systemLiteral();
    } else if (sc.skipString("PUBLIC")) {
      if (!sc.skipSpaces()) {
        throw p.error("White spaces are required after keyword PUBLIC in DOCTYPE decl.");
      }
      pub = pubidLiteral();
      boolean space = sc.skipSpaces();
      int c = sc.peek();
      if (c == '"' || c == '\'') {
        if (!space) {
          throw p.error("White spaces are required between publicId and systemId.");
        }
        sys = systemLiteral();
      } else if (!notation) {
        if (!space) {
          throw p.error("White spaces are required between publicId and systemId.");
        }
        throw p.error("The system identifier must begin with either a single or double quote character.");
      }
    } else {
      throw p.error("The external entity declaration must begin with either \"SYSTEM\" or \"PUBLIC\".");
    }
    return new String[] {pub, sys};
  }

  private String systemLiteral() throws IOException, XmlException {
    int q = sc.peek();
    if (q != '"' && q != '\'') {
      throw p.error("The system identifier must begin with either a single or double quote character.");
    }
    sc.read();
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw p.error("The system identifier must end with the matching quote character.");
      }
      if (c == q) {
        sc.read();
        return sb.toString();
      }
      sb.append((char) sc.read());
    }
  }

  private String pubidLiteral() throws IOException, XmlException {
    int q = sc.peek();
    if (q != '"' && q != '\'') {
      throw p.error("The public identifier must begin with either a single or double quote character.");
    }
    sc.read();
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw p.error("The public identifier must end with the matching quote character.");
      }
      if (c == q) {
        sc.read();
        break;
      }
      if (!XmlChars.isPubidChar(c)) {
        sc.read();
        throw p.error(XmlMessages.invalidChar("the public identifier", c));
      }
      sb.append((char) sc.read());
    }
    String raw = sb.toString();
    StringBuilder norm = new StringBuilder();
    boolean pending = false;
    for (int i = 0; i < raw.length(); i++) {
      char ch = raw.charAt(i);
      if (ch == ' ' || ch == '\n' || ch == '\r') {
        pending = norm.length() > 0;
      } else {
        if (pending) {
          norm.append(' ');
          pending = false;
        }
        norm.append(ch);
      }
    }
    return norm.toString();
  }

  private void elementDecl() throws IOException, XmlException {
    sc.skipString("<!ELEMENT");
    if (!spaces()) {
      throw p.error("White space is required after \"<!ELEMENT\" in the element type declaration.");
    }
    String name = sc.scanName();
    if (name == null) {
      throw p.error("The element type is required in the element type declaration.");
    }
    if (!spaces()) {
      throw p.error("White space is required after the element type \"" + name + "\" in the element type declaration.");
    }
    String model;
    int kind;
    if (sc.skipString("EMPTY")) {
      model = "EMPTY";
      kind = XmlDtd.kEmpty;
    } else if (sc.skipString("ANY")) {
      model = "ANY";
      kind = XmlDtd.kAny;
    } else if (sc.peek() == '(') {
      StringBuilder sb = new StringBuilder();
      int depth = 0;
      while (true) {
        int c = sc.peek();
        if (c < 0) {
          throw p.error("The declaration for element type \"" + name + "\" must end with '>'.");
        }
        if (c == '%' && (external || sc.depth() > 1)) {
          paramEntityRef(false);
          continue;
        }
        if (c == XmlScanner.kEndOfEntity) {
          sc.pop();
          continue;
        }
        if (XmlChars.isSpace(c)) {
          sc.read();
          continue;
        }
        sb.append((char) sc.read());
        if (c == '(') {
          depth++;
        } else if (c == ')') {
          depth--;
          if (depth == 0) {
            int q = sc.peek();
            if (q == '*' || q == '+' || q == '?') {
              sb.append((char) sc.read());
            }
            break;
          }
        }
      }
      model = sb.toString();
      kind = model.startsWith("(#PCDATA") ? XmlDtd.kMixed : XmlDtd.kChildren;
    } else {
      throw p.error("The constraint is required after the element type \"" + name + "\" in the element type declaration.");
    }
    spaces();
    if (sc.peek() != '>') {
      throw p.error("The declaration for element type \"" + name + "\" must end with '>'.");
    }
    sc.read();
    if (!dtd.elements.containsKey(name)) {
      XmlDtd.ElementDecl decl = new XmlDtd.ElementDecl();
      decl.name = name;
      decl.model = model;
      decl.kind = kind;
      decl.declaredExternally = external;
      dtd.elements.put(name, decl);
    }
    XmlPullParser.Ev e = ev(XmlPullParser.kElementDecl);
    e.a = name;
    e.b = model;
    emit(e);
  }

  private void attlistDecl() throws IOException, XmlException {
    sc.skipString("<!ATTLIST");
    if (!spaces()) {
      throw p.error("White space is required after \"<!ATTLIST\" in the attribute-list declaration.");
    }
    String element = sc.scanName();
    if (element == null) {
      throw p.error("The element type is required in the attribute-list declaration.");
    }
    while (true) {
      boolean space = spaces();
      if (sc.peek() == '>') {
        sc.read();
        return;
      }
      if (!space) {
        throw p.error("White space is required before the attribute name in the attribute-list declaration for element \""
            + element + "\".");
      }
      String attr = sc.scanName();
      if (attr == null) {
        throw p.error("The attribute name must be specified in the attribute-list declaration for element \"" + element
            + "\".");
      }
      if (!spaces()) {
        throw p.error("White space is required before the attribute type in the declaration of attribute \"" + attr
            + "\" for element \"" + element + "\".");
      }
      String type = attributeType(element, attr);
      if (!spaces()) {
        throw p.error("White space is required before the attribute default in the declaration of attribute \"" + attr
            + "\" for element \"" + element + "\".");
      }
      String mode = null;
      String value = null;
      if (sc.skipString("#REQUIRED")) {
        mode = "#REQUIRED";
      } else if (sc.skipString("#IMPLIED")) {
        mode = "#IMPLIED";
      } else {
        if (sc.skipString("#FIXED")) {
          mode = "#FIXED";
          if (!spaces()) {
            throw p.error("White space must appear after \"FIXED\" in the \"" + attr + "\" attribute declaration.");
          }
        }
        int q = sc.peek();
        if (q != '"' && q != '\'') {
          throw p.error("The attribute default must begin with a quote or be one of #REQUIRED, #IMPLIED or #FIXED.");
        }
        sc.read();
        value = p.attributeValue(element, attr, q);
        if (!type.equals("CDATA")) {
          value = value.trim();
        }
      }
      if (dtd.findAttribute(element, attr) == null) {
        XmlDtd.AttDef def = new XmlDtd.AttDef();
        def.element = element;
        def.name = attr;
        def.type = type;
        def.mode = mode;
        def.value = value;
        def.declaredExternally = external;
        List<XmlDtd.AttDef> list = dtd.attributes.get(element);
        if (list == null) {
          list = new ArrayList<XmlDtd.AttDef>();
          dtd.attributes.put(element, list);
        }
        list.add(def);
        XmlPullParser.Ev e = ev(XmlPullParser.kAttributeDecl);
        e.a = element;
        e.b = attr;
        e.c = type;
        e.d = mode;
        e.e = value;
        emit(e);
      }
    }
  }

  private String attributeType(String element, String attr) throws IOException, XmlException {
    String[] simple = {"CDATA", "IDREFS", "IDREF", "ID", "ENTITIES", "ENTITY", "NMTOKENS", "NMTOKEN"};
    for (String s : simple) {
      if (sc.lookingAt(s)) {
        int after = sc.peek(s.length());
        if (after >= 0 && XmlChars.isName(after)) {
          continue;
        }
        sc.skipString(s);
        return s;
      }
    }
    if (sc.skipString("NOTATION")) {
      if (!spaces()) {
        throw p.error("White space must appear after \"NOTATION\" in the \"" + attr + "\" attribute declaration.");
      }
      if (sc.peek() != '(') {
        throw p.error("The '(' character must follow \"NOTATION\" in the \"" + attr + "\" attribute declaration.");
      }
      return "NOTATION " + enumeration(attr, true);
    }
    if (sc.peek() == '(') {
      return enumeration(attr, false);
    }
    sc.read();
    throw p.error("The attribute type is required in the declaration of attribute \"" + attr + "\" for element \""
        + element + "\".");
  }

  private String enumeration(String attr, boolean notation) throws IOException, XmlException {
    sc.read();
    StringBuilder sb = new StringBuilder("(");
    boolean first = true;
    while (true) {
      spaces();
      String token = notation ? sc.scanName() : sc.scanNmtoken();
      if (token == null) {
        throw p.error(notation
            ? "The notation name is required in the notation type list for the \"" + attr + "\" attribute declaration."
            : "The name token is required in the enumerated type list for the \"" + attr + "\" attribute declaration.");
      }
      if (!first) {
        sb.append('|');
      }
      first = false;
      sb.append(token);
      spaces();
      int c = sc.peek();
      if (c == '|') {
        sc.read();
        continue;
      }
      if (c == ')') {
        sc.read();
        sb.append(')');
        return sb.toString();
      }
      throw p.error(notation
          ? "The notation type list must end with ')' in the \"" + attr + "\" attribute declaration."
          : "The enumerated type list must end with ')' in the \"" + attr + "\" attribute declaration.");
    }
  }

  private void entityDecl() throws IOException, XmlException {
    sc.skipString("<!ENTITY");
    if (!spaces()) {
      throw p.error("White space is required after \"<!ENTITY\" in the entity declaration.");
    }
    boolean parameter = false;
    if (sc.peek() == '%') {
      sc.read();
      parameter = true;
      if (!spaces()) {
        throw p.error("White space is required between the '%' and the entity name in the parameter entity declaration.");
      }
    }
    String name = sc.scanName();
    if (name == null) {
      throw p.error("The name of the entity is required in the entity declaration.");
    }
    if (!spaces()) {
      throw p.error("White space is required between the entity name \"" + name
          + "\" and the definition in the entity declaration.");
    }
    XmlDtd.EntityDecl decl = new XmlDtd.EntityDecl();
    decl.name = name;
    decl.parameter = parameter;
    decl.declaredExternally = external;
    decl.baseUri = sc.baseUri();
    int q = sc.peek();
    if (q == '"' || q == '\'') {
      decl.value = entityValue(q);
    } else {
      String[] ids = externalId(false, "<!ENTITY");
      decl.publicId = ids[0];
      decl.systemId = ids[1];
      decl.external = true;
      decl.expandedSystemId = XmlInput.expandDecl(ids[1], sc.baseUri());
      boolean space = spaces();
      if (!parameter && sc.skipString("NDATA")) {
        if (!space) {
          throw p.error("White space is required before \"NDATA\" in the declaration for the entity \"" + name + "\".");
        }
        if (!spaces()) {
          throw p.error("White space is required between \"NDATA\" and the notation name in the declaration for the entity \""
              + name + "\".");
        }
        decl.notation = sc.scanName();
        if (decl.notation == null) {
          throw p.error("The notation name is required after \"NDATA\" in the declaration for the entity \"" + name + "\".");
        }
      }
    }
    spaces();
    if (sc.peek() != '>') {
      throw p.error("The declaration for the entity \"" + name + "\" must end with '>'.");
    }
    sc.read();
    java.util.Map<String, XmlDtd.EntityDecl> table = parameter ? dtd.parameterEntities : dtd.generalEntities;
    if (table.containsKey(name)) {
      return;
    }
    table.put(name, decl);
    String reported = parameter ? "%" + name : name;
    if (decl.notation != null) {
      XmlPullParser.Ev e = ev(XmlPullParser.kUnparsedEntityDecl);
      e.a = reported;
      e.b = decl.publicId;
      e.c = decl.expandedSystemId;
      e.d = decl.notation;
      emit(e);
    } else if (decl.external) {
      XmlPullParser.Ev e = ev(XmlPullParser.kExternalEntityDecl);
      e.a = reported;
      e.b = decl.publicId;
      e.c = decl.expandedSystemId;
      emit(e);
    } else {
      XmlPullParser.Ev e = ev(XmlPullParser.kInternalEntityDecl);
      e.a = reported;
      e.b = decl.value;
      emit(e);
    }
  }

  private String entityValue(int quote) throws IOException, XmlException {
    sc.read();
    StringBuilder sb = new StringBuilder();
    int startDepth = sc.depth();
    while (true) {
      int c = sc.peek();
      if (c == XmlScanner.kEof) {
        throw p.error(XmlMessages.kPrematureEof);
      }
      if (c == XmlScanner.kEndOfEntity) {
        if (sc.depth() > startDepth) {
          sc.pop();
          continue;
        }
        throw p.error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == quote && sc.depth() == startDepth) {
        sc.read();
        return sb.toString();
      }
      if (c == '%') {
        if (!external && sc.depth() == 0) {
          sc.read();
          String refName = peekName();
          if (sc.peek() == ';') {
            sc.read();
          }
          throw p.error("The parameter entity reference \"%" + refName + ";\" cannot occur within markup in the internal subset of the DTD.");
        }
        paramEntityRef(false);
        continue;
      }
      if (c == '&') {
        if (sc.peek(1) == '#') {
          sc.read();
          sc.read();
          int cp = p.charRef();
          if (cp >= 0x10000) {
            sb.append(Character.highSurrogate(cp)).append(Character.lowSurrogate(cp));
          } else {
            sb.append((char) cp);
          }
          continue;
        }
        sc.read();
        String ref = sc.scanName();
        if (ref == null) {
          throw p.error(XmlMessages.kNameRequiredInReference);
        }
        if (sc.peek() != ';') {
          throw p.error(XmlMessages.semicolonRequiredInReference(ref));
        }
        sc.read();
        sb.append('&').append(ref).append(';');
        continue;
      }
      if (!XmlChars.isValid(c)) {
        if (Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1))) {
          sb.append((char) sc.read());
          sb.append((char) sc.read());
          continue;
        }
        throw p.error(XmlMessages.invalidChar("the literal entity value", c));
      }
      sb.append((char) sc.read());
    }
  }

  private String peekName() throws IOException {
    String n = sc.scanName();
    return n == null ? "" : n;
  }

  private void notationDecl() throws IOException, XmlException {
    sc.skipString("<!NOTATION");
    if (!spaces()) {
      throw p.error("White space is required after \"<!NOTATION\" in the notation declaration.");
    }
    String name = sc.scanName();
    if (name == null) {
      throw p.error("The name of the notation is required in the notation declaration.");
    }
    if (!spaces()) {
      throw p.error("White space is required after the notation name \"" + name + "\" in the notation declaration.");
    }
    String[] ids = externalId(true, "<!NOTATION");
    spaces();
    if (sc.peek() != '>') {
      throw p.error("The declaration for the notation \"" + name + "\" must end with '>'.");
    }
    sc.read();
    if (dtd.notations.containsKey(name)) {
      return;
    }
    XmlDtd.NotationDecl decl = new XmlDtd.NotationDecl();
    decl.name = name;
    decl.publicId = ids[0];
    decl.systemId = ids[1];
    decl.expandedSystemId = ids[1] == null ? null : XmlInput.expandDecl(ids[1], sc.baseUri());
    dtd.notations.put(name, decl);
    XmlPullParser.Ev e = ev(XmlPullParser.kNotationDecl);
    e.a = name;
    e.b = ids[0];
    e.c = decl.expandedSystemId;
    emit(e);
  }
}
