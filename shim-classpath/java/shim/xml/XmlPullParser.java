/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class XmlPullParser {
  public static final int kStartDocument = 1;
  public static final int kEndDocument = 2;
  public static final int kDtdStart = 3;
  public static final int kDtdEnd = 4;
  public static final int kElementDecl = 5;
  public static final int kAttributeDecl = 6;
  public static final int kInternalEntityDecl = 7;
  public static final int kExternalEntityDecl = 8;
  public static final int kUnparsedEntityDecl = 9;
  public static final int kNotationDecl = 10;
  public static final int kStartElement = 11;
  public static final int kEndElement = 12;
  public static final int kCharacters = 13;
  public static final int kIgnorableWhitespace = 14;
  public static final int kCdataStart = 15;
  public static final int kCdataEnd = 16;
  public static final int kComment = 17;
  public static final int kProcessingInstruction = 18;
  public static final int kEntityStart = 19;
  public static final int kEntityEnd = 20;
  public static final int kSkippedEntity = 21;

  public interface Resolver {
    XmlInput resolve(String name, String publicId, String systemId, String baseUri) throws IOException, XmlException;
  }

  public static final class Attr {
    public String name;
    public String value;
    public String type = "CDATA";
    public boolean specified = true;
    public boolean declared;
    public String uri = "";
    public String local;
    public String prefix = "";
    public boolean nsDecl;
  }

  public static final class Ev {
    public int kind;
    public String a;
    public String b;
    public String c;
    public String d;
    public String e;
    public List<Attr> attrs;
    public int line;
    public int col;
    public String systemId;
    public boolean flag;
    public String uri = "";
    public String local;
    public String prefix = "";
    public String[] nsDecls;
    public String literal;
    public int offset;
    public int lookahead;
    public int nextChar;
    public boolean charRef;
    public int staxPad;
  }

  private static final int kXmlDecl = 5;
  private static final int kDtdState = 6;
  private static final int kInit = 0;
  private static final int kProlog = 1;
  private static final int kContent = 2;
  private static final int kEpilog = 3;
  private static final int kDone = 4;

  final XmlScanner sc = new XmlScanner();
  private final XmlInput input;
  private final Resolver resolver;
  private final ArrayDeque<Ev> queue = new ArrayDeque<Ev>();
  private final ArrayList<String> elements = new ArrayList<String>();
  private final ArrayList<String[]> entityFrames = new ArrayList<String[]>();
  private int state = kInit;
  private Ev cur;
  private XmlDtdScanner dtdScanner;
  private XmlException deferredXml;
  private IOException deferredIo;

  public boolean namespaceAware;
  public boolean hasDeclaration;
  public boolean splitCharRefs;
  public boolean reportEntityRefs;
  private final ArrayList<String> nsPrefixes = new ArrayList<String>();
  private final ArrayList<String> nsUris = new ArrayList<String>();
  private final ArrayList<int[]> nsFrames = new ArrayList<int[]>();
  private final ArrayList<String[]> declStack = new ArrayList<String[]>();
  public boolean loadExternalDtd = true;
  public boolean externalGeneralEntities = true;
  public boolean externalParameterEntities = true;
  public String version = "1.0";
  public String declaredEncoding;
  public boolean standalone;
  public boolean standaloneDeclared;
  public XmlDtd dtd;
  private boolean seenDoctype;
  private boolean afterPop;

  public XmlPullParser(XmlInput input, Resolver resolver) {
    this.input = input;
    this.resolver = resolver;
  }

  public Ev current() {
    return cur;
  }

  public void closeInput() {
    sc.closeAll();
  }

  public int beginDocument() throws XmlException, IOException {
    int kind = next();
    if (state == kXmlDecl) {
      scanXmlDecl();
      state = kProlog;
      if (hasDeclaration) {
        cur.line = sc.line();
        cur.col = sc.col();
        cur.offset = sc.offset();
      }
    }
    return kind;
  }

  public int line() {
    if (cur != null && cur.line != 0) {
      return cur.line;
    }
    return sc.line();
  }

  public int column() {
    if (cur != null && cur.line != 0) {
      return cur.col;
    }
    return sc.col();
  }

  public String systemId() {
    return cur != null && cur.systemId != null ? cur.systemId : input.systemId;
  }

  public String publicId() {
    return input.publicId;
  }

  XmlException error(String message) {
    if (afterPop) {
      return new XmlException(message, -1, -1, input.systemId, input.publicId);
    }
    return new XmlException(message, sc.line(), sc.col(), sc.systemId() != null ? sc.systemId() : input.systemId,
        sc.publicId() != null ? sc.publicId() : input.publicId);
  }

  XmlException errorAfterPopIfDocEnd(String message) throws IOException {
    if (sc.peek() < 0) {
      return errorAfterPop(XmlMessages.kPrematureEof);
    }
    return error(message);
  }

  XmlException errorAfterPop(String message) {
    return new XmlException(message, -1, -1, input.systemId, input.publicId);
  }

  private Ev ev(int kind) {
    Ev e = new Ev();
    e.kind = kind;
    e.line = sc.line();
    e.col = sc.col();
    e.offset = sc.offset();
    e.systemId = sc.systemId() != null ? sc.systemId() : input.systemId;
    return e;
  }

  Ev newEvent(int kind) {
    return ev(kind);
  }

  void enqueue(Ev e) {
    queue.add(e);
  }

  public int next() throws XmlException, IOException {
    if (!queue.isEmpty()) {
      cur = queue.poll();
      return cur.kind;
    }
    if (deferredXml != null || deferredIo != null) {
      XmlException x = deferredXml;
      IOException io = deferredIo;
      deferredXml = null;
      deferredIo = null;
      if (x != null) {
        throw x;
      }
      throw io;
    }
    switch (state) {
    case kInit: {
      sc.pushReader(null, input.reader, input.systemId, input.publicId, input.baseUri);
      state = kXmlDecl;
      cur = ev(kStartDocument);
      cur.line = 1;
      cur.col = 1;
      return kStartDocument;
    }
    case kXmlDecl:
      scanXmlDecl();
      state = kProlog;
      return next();
    case kDtdState: {
      while (queue.isEmpty()) {
        if (!dtdScanner.step()) {
          state = kProlog;
          break;
        }
      }
      if (!queue.isEmpty()) {
        cur = queue.poll();
        return cur.kind;
      }
      return next();
    }
    case kProlog:
      return prolog();
    case kContent:
      return content();
    case kEpilog:
      return epilog();
    default:
      cur = null;
      return kEndDocument;
    }
  }

  private void scanXmlDecl() throws IOException, XmlException {
    if (!(sc.peek(0) == '<' && sc.peek(1) == '?' && sc.peek(2) == 'x' && sc.peek(3) == 'm' && sc.peek(4) == 'l'
        && sc.peek(5) >= 0 && XmlChars.isSpace(sc.peek(5)))) {
      return;
    }
    sc.skipString("<?xml");
    hasDeclaration = true;
    sc.skipSpaces();
    String first = pseudoName();
    String firstValue = pseudoValue(first == null ? "version" : first);
    if (!"version".equals(first)) {
      throw error(XmlMessages.kVersionInfoRequired);
    }
    version = firstValue;
    if (!version.equals("1.0") && !version.equals("1.1")) {
      throw error(XmlMessages.versionNotSupported(version));
    }
    boolean space = sc.skipSpaces();
    if (sc.lookingAt("encoding")) {
      if (!space) {
        throw error("White space is required before the encoding pseudo attribute in the XML declaration.");
      }
      sc.skipString("encoding");
      declaredEncoding = pseudoValue("encoding");
      space = sc.skipSpaces();
    }
    if (sc.lookingAt("standalone")) {
      if (!space) {
        throw error("White space is required before the encoding pseudo attribute in the XML declaration.");
      }
      sc.skipString("standalone");
      String v = pseudoValue("standalone");
      if (v.equals("yes")) {
        standalone = true;
      } else if (!v.equals("no")) {
        throw error(XmlMessages.sdDeclInvalid(v));
      }
      standaloneDeclared = true;
      sc.skipSpaces();
    }
    if (!sc.skipString("?>")) {
      if (sc.peek() == '?') {
        sc.read();
      }
      if (sc.peek() < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      throw error(XmlMessages.kXmlDeclUnterminated);
    }
    if (input.encodingProblem != null) {
      if (input.encodingInvalidName) {
        throw error(XmlMessages.encodingDeclInvalid(input.encodingProblem));
      }
      throw new java.io.UnsupportedEncodingException(input.encodingProblem);
    }
  }

  private String pseudoName() throws IOException {
    String name = sc.scanName();
    return name;
  }

  private String pseudoValue(String name) throws IOException, XmlException {
    sc.skipSpaces();
    if (sc.peek() != '=') {
      throw error(XmlMessages.eqRequiredInXmlDecl(name));
    }
    sc.read();
    sc.skipSpaces();
    int q = sc.peek();
    if (q != '"' && q != '\'') {
      throw error(XmlMessages.quoteRequiredInXmlDecl(name));
    }
    sc.read();
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == q) {
        sc.read();
        break;
      }
      sb.append((char) sc.read());
    }
    return sb.toString();
  }

  private int prolog() throws IOException, XmlException {
    while (true) {
      sc.skipSpaces();
      int c = sc.peek();
      if (c == XmlScanner.kEof || c == XmlScanner.kEndOfEntity) {
        throw error(XmlMessages.kPrematureEof);
      }
      if (c == '<') {
        int d = sc.peek(1);
        if (d == '?') {
          return processingInstruction();
        }
        if (d == '!') {
          if (sc.lookingAt("<!--")) {
            return comment();
          }
          if (sc.lookingAt("<!DOCTYPE")) {
            if (seenDoctype) {
              sc.skipString("<!DOCTYPE");
              throw error(XmlMessages.kAlreadySeenDoctype);
            }
            seenDoctype = true;
            dtdScanner = new XmlDtdScanner(this, sc);
            state = kDtdState;
            dtdScanner.start();
            cur = queue.poll();
            return cur.kind;
          }
          sc.read();
          sc.read();
          throw error(XmlMessages.kMarkupNotRecognizedInProlog);
        }
        if (d >= 0 && XmlChars.isNameStart(d) || d >= 0xD800 && d <= 0xDBFF) {
          state = kContent;
          return startElement();
        }
        sc.read();
        throw error(XmlMessages.kMarkupNotRecognizedInProlog);
      }
      throw error(XmlMessages.kContentIllegalInProlog);
    }
  }

  private int epilog() throws IOException, XmlException {
    while (true) {
      sc.skipSpaces();
      int c = sc.peek();
      if (c == XmlScanner.kEof || c == XmlScanner.kEndOfEntity) {
        sc.pop();
        afterPop = true;
        state = kDone;
        cur = null;
        return kEndDocument;
      }
      if (c == '<') {
        int d = sc.peek(1);
        if (d == '?') {
          return processingInstruction();
        }
        if (d == '!' && sc.lookingAt("<!--")) {
          return comment();
        }
        sc.read();
        if (sc.peek() < 0) {
          throw error(XmlMessages.kMarkupEntityMismatch);
        }
        if (sc.peek() == '!') {
          sc.read();
          throw error("Comment must start with \"<!--\".");
        }
        if (sc.peek() == '/') {
          sc.read();
        }
        throw error(XmlMessages.kMarkupNotRecognizedInMisc);
      }
      throw error(XmlMessages.kContentIllegalInTrailingMisc);
    }
  }

  private int comment() throws IOException, XmlException {
    sc.skipString("<!--");
    StringBuilder sb = new StringBuilder();
    int startDepth = sc.depth();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == '-' && sc.peek(1) == '-') {
        sc.read();
        sc.read();
        if (sc.peek() != '>') {
          throw error(XmlMessages.kDashDashInComment);
        }
        sc.read();
        break;
      }
      if (!XmlChars.isValid(c) && !(Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1)))) {
        throw error(XmlMessages.invalidChar("the comment", c));
      }
      sb.append((char) sc.read());
    }
    cur = ev(kComment);
    cur.a = sb.toString();
    return kComment;
  }

  private int processingInstruction() throws IOException, XmlException {
    sc.skipString("<?");
    String target = sc.scanName();
    if (target == null) {
      if (sc.peek() < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      throw error(XmlMessages.kPiTargetRequired);
    }
    if (target.equalsIgnoreCase("xml")) {
      throw error(XmlMessages.kReservedPiTarget);
    }
    StringBuilder data = new StringBuilder();
    if (sc.skipString("?>")) {
      // empty data
    } else {
      if (!sc.skipSpaces()) {
        if (sc.peek() < 0) {
          throw error(XmlMessages.kMarkupEntityMismatch);
        }
        throw error(XmlMessages.kSpaceRequiredInPi);
      }
      while (true) {
        int c = sc.peek();
        if (c < 0) {
          throw error(XmlMessages.kMarkupEntityMismatch);
        }
        if (c == '?' && sc.peek(1) == '>') {
          sc.read();
          sc.read();
          break;
        }
        if (!XmlChars.isValid(c) && !(Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1)))) {
          throw error(XmlMessages.invalidChar("the processing instruction", c));
        }
        data.append((char) sc.read());
      }
    }
    cur = ev(kProcessingInstruction);
    cur.a = target;
    cur.b = data.toString();
    return kProcessingInstruction;
  }

  private int content() throws IOException, XmlException {
    int c = sc.peek();
    if (c == XmlScanner.kEof) {
      throw error(XmlMessages.kMarkupEntityMismatch);
    }
    if (c == XmlScanner.kEndOfEntity) {
      return endOfEntity();
    }
    if (c == '<') {
      int d = sc.peek(1);
      if (d == '/') {
        return endElement();
      }
      if (d == '!') {
        if (sc.lookingAt("<!--")) {
          return comment();
        }
        if (sc.lookingAt("<![CDATA[")) {
          return cdata();
        }
        if (sc.lookingAt("<!DOCTYPE")) {
          throw new XmlException("Scanner State 24 not Recognized ", -1, -1, null, null, false, true);
        }
        if (sc.lookingAt("<!-")) {
          sc.skipString("<!-");
          throw error("Comment must start with \"<!--\".");
        }
        sc.read();
        sc.read();
        throw error(XmlMessages.kMarkupNotRecognizedInContent);
      }
      if (d == '?') {
        return processingInstruction();
      }
      if (d >= 0 && XmlChars.isNameStart(d) || d >= 0xD800 && d <= 0xDBFF) {
        return startElement();
      }
      sc.read();
      if (sc.peek() < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      throw error(XmlMessages.kMarkupNotRecognizedInContent);
    }
    if (c == '&' && sc.peek(1) != '#') {
      return entityReference();
    }
    return text();
  }

  private int endOfEntity() throws XmlException {
    String[] frame = entityFrames.isEmpty() ? null : entityFrames.get(entityFrames.size() - 1);
    if (frame == null) {
      throw error(XmlMessages.kMarkupEntityMismatch);
    }
    if (Integer.parseInt(frame[1]) != elements.size()) {
      throw error(XmlMessages.kMarkupEntityMismatch);
    }
    entityFrames.remove(entityFrames.size() - 1);
    sc.pop();
    cur = ev(kEntityEnd);
    cur.a = frame[0];
    return kEntityEnd;
  }

  private StringBuilder textBuf = new StringBuilder();
  private boolean textAscii;

  private int text() throws IOException, XmlException {
    boolean wasCharRef = false;
    textBuf.setLength(0);
    textAscii = true;
    int brackets = 0;
    boolean allSpace = true;
    while (true) {
      int c = sc.peek();
      if (c < 0 || c == '<') {
        break;
      }
      if (c == '&') {
        if (sc.peek(1) == '#') {
          if (splitCharRefs && textBuf.length() > 0) {
            break;
          }
          sc.read();
          sc.read();
          int cp = charRef();
          appendCodePoint(textBuf, cp);
          allSpace = false;
          brackets = 0;
          if (splitCharRefs) {
            wasCharRef = true;
            break;
          }
          continue;
        }
        break;
      }
      if (c == '>' && brackets >= 2) {
        sc.read();
        throw error(XmlMessages.kCdEndInContent);
      }
      if (c == ']') {
        brackets++;
      } else {
        brackets = 0;
      }
      if (!XmlChars.isValid(c)) {
        if (Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1))) {
          textBuf.append((char) sc.read());
          textBuf.append((char) sc.read());
          allSpace = false;
          continue;
        }
        throw error(XmlMessages.invalidChar("the element content of the document", c));
      }
      if (!XmlChars.isSpace(c)) {
        allSpace = false;
      }
      if (c >= 0x80) {
        textAscii = false;
      }
      textBuf.append((char) sc.read());
    }
    if (sc.peek() == XmlScanner.kEndOfEntity && !entityFrames.isEmpty()
        && Integer.parseInt(entityFrames.get(entityFrames.size() - 1)[1]) != elements.size()) {
      throw error(XmlMessages.kMarkupEntityMismatch);
    }
    if (textBuf.length() == 0) {
      return content();
    }
    String t = textBuf.toString();
    boolean ignorable = false;
    if (allSpace && dtd != null && !elements.isEmpty()) {
      XmlDtd.ElementDecl decl = dtd.elements.get(elements.get(elements.size() - 1));
      ignorable = decl != null && decl.kind == XmlDtd.kChildren;
    }
    cur = ev(ignorable ? kIgnorableWhitespace : kCharacters);
    cur.a = t;
    cur.charRef = wasCharRef;
    if (!wasCharRef && textAscii) {
      int next = sc.peek();
      cur.staxPad = next == '<' ? (sc.peek(1) == '/' ? 2 : 1) : next == '&' ? 1 : 0;
    }
    if (sc.peek() == '<' && textAscii) {
      int d = sc.peek(1);
      int delta = (d == '/' || d == '!' || d == '?') ? 2 : 1;
      cur.col += delta;
      cur.offset += delta;
      cur.lookahead = delta;
      cur.nextChar = d;
    }
    return cur.kind;
  }

  private static void appendCodePoint(StringBuilder sb, int cp) {
    if (cp >= 0x10000) {
      sb.append(Character.highSurrogate(cp)).append(Character.lowSurrogate(cp));
    } else {
      sb.append((char) cp);
    }
  }

  int charRef() throws IOException, XmlException {
    boolean hex = false;
    if (sc.peek() == 'x') {
      sc.read();
      hex = true;
    }
    StringBuilder digits = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c >= 0 && (c >= '0' && c <= '9' || hex && (c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F'))) {
        digits.append((char) sc.read());
      } else {
        break;
      }
    }
    if (digits.length() == 0) {
      throw error(hex ? XmlMessages.kHexdigitRequiredInCharRef : XmlMessages.kDigitRequiredInCharRef);
    }
    if (sc.peek() != ';') {
      throw error(XmlMessages.kSemicolonRequiredInCharRef);
    }
    sc.read();
    int cp;
    try {
      cp = Integer.parseInt(digits.toString(), hex ? 16 : 10);
    } catch (NumberFormatException e) {
      throw error(XmlMessages.invalidCharRef((hex ? "x" : "") + digits));
    }
    if (!XmlChars.isValid(cp)) {
      throw error(XmlMessages.invalidCharRef((hex ? "x" : "") + digits));
    }
    return cp;
  }

  private static String predefined(String name) {
    switch (name) {
    case "lt": return "<";
    case "gt": return ">";
    case "amp": return "&";
    case "quot": return "\"";
    case "apos": return "'";
    default: return null;
    }
  }

  private int entityReference() throws IOException, XmlException {
    sc.read();
    String name = sc.scanName();
    if (name == null) {
      throw error(XmlMessages.kNameRequiredInReference);
    }
    if (sc.peek() != ';') {
      throw error(XmlMessages.semicolonRequiredInReference(name));
    }
    sc.read();
    String pre = predefined(name);
    if (pre != null) {
      Ev start = ev(kEntityStart);
      start.a = name;
      Ev chars = ev(kCharacters);
      chars.a = pre;
      Ev end = ev(kEntityEnd);
      end.a = name;
      queue.add(chars);
      queue.add(end);
      cur = start;
      return kEntityStart;
    }
    XmlDtd.EntityDecl decl = dtd == null ? null : dtd.generalEntities.get(name);
    if (reportEntityRefs) {
      cur = ev(kSkippedEntity);
      cur.a = name;
      cur.b = decl != null && !decl.external ? decl.value : null;
      return kSkippedEntity;
    }
    if (decl == null) {
      if (dtd != null && !standalone && (dtd.systemId != null || dtd.hasExternalParameterEntity)) {
        cur = ev(kSkippedEntity);
        cur.a = name;
        return kSkippedEntity;
      }
      throw error(XmlMessages.entityNotDeclared(name));
    }
    if (decl.notation != null) {
      throw error(XmlMessages.referenceToUnparsedEntity(name));
    }
    if (sc.isActiveEntity(name)) {
      throw error(XmlMessages.recursiveReference(name, recursionPath(name)));
    }
    int markLine = sc.line();
    int markCol = sc.col();
    int markOffset = sc.offset();
    if (!decl.external) {
      sc.pushInternal(name, decl.value, decl.baseUri);
    } else {
      if (!externalGeneralEntities || resolver == null) {
        cur = ev(kSkippedEntity);
        cur.a = name;
        return kSkippedEntity;
      }
      XmlInput in = resolver.resolve(name, decl.publicId, decl.systemId, decl.baseUri);
      if (in == null) {
        cur = ev(kSkippedEntity);
        cur.a = name;
        return kSkippedEntity;
      }
      sc.pushReader(name, in.reader, in.systemId, in.publicId, in.baseUri);
      skipTextDecl();
    }
    entityFrames.add(new String[] {name, String.valueOf(elements.size())});
    cur = ev(kEntityStart);
    cur.a = name;
    cur.line = markLine;
    cur.col = markCol;
    cur.offset = markOffset;
    return kEntityStart;
  }

  private String recursionPath(String name) {
    String path = sc.entityPath(name);
    if (path.equals(name + " -> " + name)) {
      return name + " -> " + name + " -> " + name;
    }
    return path;
  }

  private void skipTextDecl() throws IOException, XmlException {
    if (sc.lookingAt("<?xml") && sc.peek(5) >= 0 && XmlChars.isSpace(sc.peek(5))) {
      scanTextDecl();
    }
  }

  void scanTextDecl() throws IOException, XmlException {
    sc.skipString("<?xml");
    sc.skipSpaces();
    boolean haveVersion = false;
    if (sc.lookingAt("version")) {
      sc.skipString("version");
      pseudoValue("version");
      haveVersion = true;
      boolean sp = sc.skipSpaces();
      if (!sc.lookingAt("encoding")) {
        throw error("More pseudo attributes are expected.");
      }
      if (!sp) {
        throw error("White space is required before the encoding pseudo attribute in the text declaration.");
      }
    }
    if (!sc.lookingAt("encoding")) {
      throw error("More pseudo attributes are expected.");
    }
    sc.skipString("encoding");
    pseudoValue("encoding");
    sc.skipSpaces();
    if (!sc.skipString("?>")) {
      throw error("The text declaration must end with \"?>\".");
    }
  }

  private int cdata() throws IOException, XmlException {
    sc.skipString("<![CDATA[");
    StringBuilder sb = new StringBuilder();
    while (true) {
      int c = sc.peek();
      if (c < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == ']' && sc.peek(1) == ']' && sc.peek(2) == '>') {
        sc.read();
        sc.read();
        sc.read();
        break;
      }
      if (!XmlChars.isValid(c)) {
        if (Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1))) {
          sb.append((char) sc.read());
          sb.append((char) sc.read());
          continue;
        }
        throw error(XmlMessages.invalidChar("the CDATA section", c));
      }
      sb.append((char) sc.read());
    }
    Ev start = ev(kCdataStart);
    if (sb.length() > 0) {
      Ev chars = ev(kCharacters);
      chars.a = sb.toString();
      queue.add(chars);
    }
    Ev end = ev(kCdataEnd);
    queue.add(end);
    cur = start;
    return kCdataStart;
  }

  private String scanElementName() throws IOException {
    if (!namespaceAware) {
      return sc.scanName();
    }
    StringBuilder sb = new StringBuilder();
    int c = sc.peek();
    if (c < 0 || !(XmlChars.isNameStart(c) || (c >= 0xD800 && c <= 0xDBFF))) {
      return null;
    }
    boolean colonSeen = false;
    while (true) {
      c = sc.peek();
      if (c < 0) {
        break;
      }
      if (c == ':') {
        if (colonSeen && sb.length() > 0 && sb.charAt(0) != ':') {
          break;
        }
        if (sb.length() > 0) {
          colonSeen = true;
        }
        sb.append((char) sc.read());
        continue;
      }
      if (c >= 0xD800 && c <= 0xDBFF) {
        int d = sc.peek(1);
        if (d >= 0xDC00 && d <= 0xDFFF) {
          sb.append((char) sc.read());
          sb.append((char) sc.read());
          continue;
        }
        break;
      }
      if (!XmlChars.isName(c)) {
        break;
      }
      sb.append((char) sc.read());
    }
    return sb.toString();
  }

  private void checkQName(String name) throws XmlException {
    if (!namespaceAware) {
      return;
    }
    int colon = name.indexOf(':');
    if (colon <= 0) {
      return;
    }
    String local = name.substring(colon + 1);
    if (local.isEmpty() || local.indexOf(':') >= 0 || !XmlChars.isNameStart(local.charAt(0))) {
      throw error("Element or attribute \"" + name + "\" do not match QName production: QName::=(NCName:)?NCName.");
    }
  }

  private String lookupPrefix(String prefix) {
    if (prefix.equals("xml")) {
      return "http://www.w3.org/XML/1998/namespace";
    }
    if (prefix.equals("xmlns")) {
      return "http://www.w3.org/2000/xmlns/";
    }
    for (int i = nsPrefixes.size() - 1; i >= 0; i--) {
      if (nsPrefixes.get(i).equals(prefix)) {
        return nsUris.get(i);
      }
    }
    return null;
  }

  private void declareNamespace(String attrName, String value, List<String> decls) throws XmlException {
    String prefix = attrName.equals("xmlns") ? "" : attrName.substring(6);
    if (prefix.equals("xml")) {
      if (!value.equals("http://www.w3.org/XML/1998/namespace")) {
        throw error("The prefix \"xml\" cannot be bound to any namespace other than its usual namespace; neither can the "
            + "namespace for \"xml\" be bound to any prefix other than \"xml\".");
      }
      return;
    }
    if (prefix.equals("xmlns")) {
      throw error("The prefix \"xmlns\" cannot be bound to any namespace explicitly; neither can the namespace for "
          + "\"xmlns\" be bound to any prefix explicitly.");
    }
    if (value.equals("http://www.w3.org/XML/1998/namespace")) {
      throw error("The prefix \"xml\" cannot be bound to any namespace other than its usual namespace; neither can the "
          + "namespace for \"xml\" be bound to any prefix other than \"xml\".");
    }
    if (value.equals("http://www.w3.org/2000/xmlns/")) {
      throw error("The prefix \"xmlns\" cannot be bound to any namespace explicitly; neither can the namespace for "
          + "\"xmlns\" be bound to any prefix explicitly.");
    }
    if (!prefix.isEmpty() && value.isEmpty()) {
      throw error("The value of the attribute \"prefix=\"xmlns\",localpart=\"" + prefix + "\",rawname=\"" + attrName
          + "\"\" is invalid. Prefixed namespace bindings may not be empty.");
    }
    nsPrefixes.add(prefix);
    nsUris.add(value);
    decls.add(prefix);
    decls.add(value);
  }

  private int startElement() throws IOException, XmlException {
    sc.read();
    String name = scanElementName();
    checkQName(name);
    int nsMark = nsPrefixes.size();
    List<String> decls = new ArrayList<String>();
    List<Attr> attrs = new ArrayList<Attr>();
    Set<String> seen = new HashSet<String>();
    boolean empty = false;
    while (true) {
      boolean space = sc.skipSpaces();
      int c = sc.peek();
      if (c == '>') {
        sc.read();
        break;
      }
      if (c == '/') {
        sc.read();
        if (sc.peek() != '>') {
          if (sc.peek() < 0) {
            throw error(XmlMessages.kMarkupEntityMismatch);
          }
          throw error(XmlMessages.elementUnterminated(name));
        }
        sc.read();
        empty = true;
        break;
      }
      if (c < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (space && (XmlChars.isNameStart(c) || (c >= 0xD800 && c <= 0xDBFF))) {
        String attrName = scanElementName();
        checkQName(attrName);
        sc.skipSpaces();
        if (sc.peek() != '=') {
          if (sc.peek() < 0) {
            throw error(XmlMessages.kMarkupEntityMismatch);
          }
          throw error(XmlMessages.eqRequiredInAttribute(name, attrName));
        }
        sc.read();
        sc.skipSpaces();
        int q = sc.peek();
        if (q != '"' && q != '\'') {
          if (q < 0) {
            throw error(XmlMessages.kMarkupEntityMismatch);
          }
          throw error(XmlMessages.openQuoteExpected(name, attrName));
        }
        sc.read();
        Attr a = new Attr();
        a.name = attrName;
        a.value = attributeValue(name, attrName, q);
        boolean decl = namespaceAware && (attrName.equals("xmlns") || attrName.startsWith("xmlns:"));
        if ((!namespaceAware || decl) && !seen.add(attrName)) {
          throw error(XmlMessages.attributeNotUnique(name, attrName));
        }
        if (decl) {
          a.nsDecl = true;
          declareNamespace(attrName, a.value, decls);
        }
        attrs.add(a);
        continue;
      }
      throw error(XmlMessages.elementUnterminated(name));
    }
    applyAttributeDecls(name, attrs);
    String[] declArray = decls.toArray(new String[decls.size()]);
    Ev start = ev(kStartElement);
    start.a = name;
    start.attrs = attrs;
    start.nsDecls = declArray;
    if (namespaceAware) {
      resolveNames(name, attrs, start);
    }
    elements.add(name);
    declStack.add(declArray);
    nsFrames.add(new int[] {nsMark});
    if (empty) {
      Ev end = ev(kEndElement);
      end.a = name;
      end.uri = start.uri;
      end.local = start.local;
      end.prefix = start.prefix;
      end.nsDecls = declArray;
      popElementState();
      queue.add(end);
      if (elements.isEmpty()) {
        state = kEpilog;
      }
    }
    cur = start;
    return kStartElement;
  }

  private void popElementState() {
    elements.remove(elements.size() - 1);
    declStack.remove(declStack.size() - 1);
    int mark = nsFrames.remove(nsFrames.size() - 1)[0];
    while (nsPrefixes.size() > mark) {
      nsPrefixes.remove(nsPrefixes.size() - 1);
      nsUris.remove(nsUris.size() - 1);
    }
  }

  private void resolveNames(String name, List<Attr> attrs, Ev start) throws XmlException {
    int colon = name.indexOf(':');
    if (colon > 0) {
      String prefix = name.substring(0, colon);
      String uri = lookupPrefix(prefix);
      if (uri == null) {
        throw error("The prefix \"" + prefix + "\" for element \"" + name + "\" is not bound.");
      }
      start.prefix = prefix;
      start.uri = uri;
      start.local = name.substring(colon + 1);
    } else {
      String uri = lookupPrefix("");
      start.uri = uri == null ? "" : uri;
      start.local = name;
    }
    Set<String> keys = new HashSet<String>();
    for (Attr a : attrs) {
      if (a.nsDecl) {
        a.uri = "";
        a.local = a.name.equals("xmlns") ? "xmlns" : a.name.substring(6);
        a.prefix = a.name.equals("xmlns") ? "" : "xmlns";
        continue;
      }
      int ac = a.name.indexOf(':');
      if (ac > 0) {
        String prefix = a.name.substring(0, ac);
        String uri = lookupPrefix(prefix);
        if (uri == null) {
          throw error("The prefix \"" + prefix + "\" for attribute \"" + a.name + "\" associated with an element type \""
              + name + "\" is not bound.");
        }
        a.prefix = prefix;
        a.uri = uri;
        a.local = a.name.substring(ac + 1);
        if (!keys.add(uri + "\u0000" + a.local)) {
          throw error("Attribute \"" + a.local + "\" bound to namespace \"" + uri + "\" was already specified for element \""
              + name + "\".");
        }
      } else {
        a.uri = "";
        a.local = a.name;
        if (!keys.add("\u0000" + a.local)) {
          throw error(XmlMessages.attributeNotUnique(name, a.name));
        }
      }
    }
  }

  private void applyAttributeDecls(String element, List<Attr> attrs) {
    if (dtd == null) {
      return;
    }
    List<XmlDtd.AttDef> defs = dtd.attributes.get(element);
    if (defs == null) {
      return;
    }
    for (XmlDtd.AttDef def : defs) {
      Attr found = null;
      for (Attr a : attrs) {
        if (a.name.equals(def.name)) {
          found = a;
          break;
        }
      }
      String saxType = saxType(def.type);
      if (found != null) {
        found.type = saxType;
        found.declared = true;
        if (!def.type.equals("CDATA")) {
          found.value = collapse(found.value);
        }
      } else if (def.value != null && !"#IMPLIED".equals(def.mode) && !"#REQUIRED".equals(def.mode)) {
        Attr a = new Attr();
        a.name = def.name;
        a.value = def.value;
        a.type = saxType;
        a.specified = false;
        a.declared = true;
        attrs.add(a);
      }
    }
  }

  private static String collapse(String v) {
    StringBuilder sb = new StringBuilder();
    boolean pendingSpace = false;
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (c == ' ') {
        pendingSpace = sb.length() > 0;
      } else {
        if (pendingSpace) {
          sb.append(' ');
          pendingSpace = false;
        }
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String saxType(String declared) {
    if (declared.startsWith("(")) {
      return "NMTOKEN";
    }
    if (declared.startsWith("NOTATION")) {
      return "NOTATION";
    }
    return declared;
  }

  String attributeValue(String element, String attr, int quote) throws IOException, XmlException {
    StringBuilder sb = new StringBuilder();
    attributeValue(sb, element, attr, quote, sc.depth());
    return sb.toString();
  }

  private void attributeValue(StringBuilder sb, String element, String attr, int quote, int startDepth)
      throws IOException, XmlException {
    while (true) {
      int c = sc.peek();
      if (c == XmlScanner.kEof) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == XmlScanner.kEndOfEntity) {
        if (sc.depth() > startDepth) {
          sc.pop();
          return;
        }
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      if (c == quote && sc.depth() == startDepth) {
        sc.read();
        return;
      }
      if (c == '<') {
        throw error(XmlMessages.lessThanInAttValue(element, attr));
      }
      if (c == '&') {
        sc.read();
        if (sc.peek() == '#') {
          sc.read();
          appendCodePoint(sb, charRef());
          continue;
        }
        String name = sc.scanName();
        if (name == null) {
          throw error(XmlMessages.kNameRequiredInReference);
        }
        if (sc.peek() != ';') {
          throw error(XmlMessages.semicolonRequiredInReference(name));
        }
        sc.read();
        String pre = predefined(name);
        if (pre != null) {
          sb.append(pre);
          continue;
        }
        XmlDtd.EntityDecl decl = dtd == null ? null : dtd.generalEntities.get(name);
        if (decl == null) {
          throw error(XmlMessages.entityNotDeclared(name));
        }
        if (decl.external) {
          throw error(XmlMessages.referenceToExternalEntity(name));
        }
        if (decl.notation != null) {
          throw error(XmlMessages.referenceToUnparsedEntity(name));
        }
        if (sc.isActiveEntity(name)) {
          throw error(XmlMessages.recursiveReference(name, recursionPath(name)));
        }
        sc.pushInternal(name, decl.value, decl.baseUri);
        attributeValue(sb, element, attr, quote, startDepth);
        continue;
      }
      if (c == '\t' || c == '\n') {
        sc.read();
        sb.append(' ');
        continue;
      }
      if (!XmlChars.isValid(c)) {
        if (Character.isHighSurrogate((char) c) && Character.isLowSurrogate((char) sc.peek(1))) {
          sb.append((char) sc.read());
          sb.append((char) sc.read());
          continue;
        }
        throw error(XmlMessages.invalidChar("the value of attribute \"" + attr + "\" and element is \"" + element + "\"", c));
      }
      sb.append((char) sc.read());
    }
  }

  private int endElement() throws IOException, XmlException {
    sc.read();
    sc.read();
    String open = elements.get(elements.size() - 1);
    if (!sc.skipString(open)) {
      if (sc.peek() < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      throw error(XmlMessages.eTagRequired(open));
    }
    int after = sc.peek();
    if (after >= 0 && XmlChars.isName(after)) {
      throw error(XmlMessages.eTagRequired(open));
    }
    sc.skipSpaces();
    if (sc.peek() != '>') {
      if (sc.peek() < 0) {
        throw error(XmlMessages.kMarkupEntityMismatch);
      }
      throw error(XmlMessages.eTagUnterminated(open));
    }
    sc.read();
    Ev end = ev(kEndElement);
    end.a = open;
    int colon = open.indexOf(':');
    if (namespaceAware) {
      if (colon > 0) {
        end.prefix = open.substring(0, colon);
        end.local = open.substring(colon + 1);
        end.uri = lookupPrefix(end.prefix);
      } else {
        end.local = open;
        String uri = lookupPrefix("");
        end.uri = uri == null ? "" : uri;
      }
    }
    end.nsDecls = declStack.get(declStack.size() - 1);
    popElementState();
    if (elements.isEmpty()) {
      state = kEpilog;
    }
    cur = end;
    return kEndElement;
  }

  void setDtd(XmlDtd d) {
    this.dtd = d;
  }

  public XmlInput input() {
    return input;
  }

  Resolver resolver() {
    return resolver;
  }
}
