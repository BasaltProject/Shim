/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class HTMLDocument extends DefaultStyledDocument {
  public static final String AdditionalComments = "AdditionalComments";
  static final String CR_KEY = "CR";
  private URL base;
  private boolean preservesUnknownTags = true;
  private int tokenThreshold = Integer.MAX_VALUE;
  private HTMLEditorKit.Parser parser;
  private boolean frameDocument;
  private String baseTarget;

  public HTMLDocument() {
    this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleSheet());
  }

  public HTMLDocument(StyleSheet styles) {
    this(new GapContent(BUFFER_SIZE_DEFAULT), styles);
  }

  public HTMLDocument(AbstractDocument.Content c, StyleSheet styles) {
    super(c, styles);
  }

  public HTMLEditorKit.ParserCallback getReader(int pos) {
    Object desc = getProperty(Document.StreamDescriptionProperty);
    if (desc instanceof URL) {
      setBase((URL) desc);
    }
    return new HTMLReader(pos);
  }

  public HTMLEditorKit.ParserCallback getReader(int pos, int popDepth, int pushDepth, HTML.Tag insertTag) {
    Object desc = getProperty(Document.StreamDescriptionProperty);
    if (desc instanceof URL) {
      setBase((URL) desc);
    }
    return new HTMLReader(pos, popDepth, pushDepth, insertTag);
  }

  private char charAt(int pos) {
    try {
      return getText(pos, 1).charAt(0);
    } catch (BadLocationException e) {
      return 0;
    }
  }

  void insertFragment(int offset, ElementSpec[] data) throws BadLocationException {
    super.insert(offset, data);
  }

  public StyleSheet getStyleSheet() { return (StyleSheet) getAttributeContext(); }

  public URL getBase() { return base; }

  public void setBase(URL u) {
    base = u;
    getStyleSheet().setBase(u);
  }

  public void setParser(HTMLEditorKit.Parser parser) {
    this.parser = parser;
    putProperty("__PARSER__", null);
  }

  public HTMLEditorKit.Parser getParser() { return parser; }

  public void setTokenThreshold(int n) { tokenThreshold = n; }

  public int getTokenThreshold() { return tokenThreshold; }

  public void setPreservesUnknownTags(boolean preservesTags) { preservesUnknownTags = preservesTags; }

  public boolean getPreservesUnknownTags() { return preservesUnknownTags; }

  boolean isFrameDocument() { return frameDocument; }

  void setFrameDocumentState(boolean frame) { frameDocument = frame; }

  String getBaseTarget() { return baseTarget; }

  void setBaseTarget(String target) { baseTarget = target; }

  public void processHTMLFrameHyperlinkEvent(HTMLFrameHyperlinkEvent e) { }

  protected AbstractElement createDefaultRoot() {
    writeLock();
    MutableAttributeSet a = new SimpleAttributeSet();
    a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.HTML);
    BlockElement html = new BlockElement(null, a.copyAttributes());
    a.removeAttributes(a);
    a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.BODY);
    BlockElement body = new BlockElement(html, a.copyAttributes());
    a.removeAttributes(a);
    a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.P);
    getStyleSheet().addCSSAttributeFromHTML(a, CSS.Attribute.MARGIN_TOP, "0");
    BlockElement paragraph = new BlockElement(body, a.copyAttributes());
    a.removeAttributes(a);
    a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
    RunElement brk = new RunElement(paragraph, a, 0, 1);
    Element[] buff = new Element[1];
    buff[0] = brk;
    paragraph.replace(0, 0, buff);
    buff[0] = paragraph;
    body.replace(0, 0, buff);
    buff[0] = body;
    html.replace(0, 0, buff);
    writeUnlock();
    return html;
  }

  protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
    return new RunElement(parent, a, p0, p1);
  }

  protected Element createBranchElement(Element parent, AttributeSet a) {
    return new BlockElement(parent, a);
  }

  protected void create(DefaultStyledDocument.ElementSpec[] data) {
    writeLock();
    try {
      StringBuilder text = new StringBuilder();
      for (ElementSpec spec : data) {
        if (spec.getType() == ElementSpec.ContentType && spec.getArray() != null) {
          text.append(spec.getArray(), spec.getOffset(), spec.getLength());
        }
      }
      int oldLen = getLength();
      if (oldLen > 0) {
        getContent().remove(0, oldLen);
      }
      if (text.length() > 0) {
        String body = text.toString();
        if (body.endsWith("\n")) {
          body = body.substring(0, body.length() - 1);
        }
        if (body.length() > 0) {
          getContent().insertString(0, body);
        }
      }
      BranchElement root = (BranchElement) getDefaultRootElement();
      int oldCount = root.getElementCount();
      Element[] removed = new Element[oldCount];
      for (int i = 0; i < oldCount; i++) {
        removed[i] = root.getElement(i);
      }
      List<BranchElement> stack = new ArrayList<BranchElement>();
      stack.add(root);
      int pos = 0;
      List<Element> top = new ArrayList<Element>();
      boolean first = true;
      for (ElementSpec spec : data) {
        BranchElement cur = stack.get(stack.size() - 1);
        switch (spec.getType()) {
          case ElementSpec.StartTagType: {
            if (first) {
              first = false;
              AttributeSet ra = spec.getAttributes();
              if (ra != null) {
                root.addAttributes(ra);
              }
              break;
            }
            BranchElement b = (BranchElement) createBranchElement(cur, spec.getAttributes());
            pendingAdd(cur, b, top, root);
            stack.add(b);
            break;
          }
          case ElementSpec.EndTagType:
            if (stack.size() > 1) {
              stack.remove(stack.size() - 1);
            }
            break;
          default: {
            int len = spec.getLength();
            Element leaf = createLeafElement(cur, spec.getAttributes(), pos, pos + len);
            pendingAdd(cur, leaf, top, root);
            pos += len;
            break;
          }
        }
      }
      Element[] added = top.toArray(new Element[0]);
      root.replace(0, oldCount, added);
      for (Element b : added) {
        finishBlock(b);
      }
      DefaultDocumentEvent evnt = new DefaultDocumentEvent(0, Math.max(0, text.length() - 1), DocumentEvent.EventType.INSERT);
      evnt.addEdit(new ElementEdit(root, 0, removed, added));
      evnt.end();
      writeUnlock();
      fireInsertUpdate(evnt);
      return;
    } catch (BadLocationException ble) {
      writeUnlock();
      throw new IllegalStateException(ble);
    }
  }

  private void pendingAdd(BranchElement cur, Element child, List<Element> top, BranchElement root) {
    if (cur == root) {
      top.add(child);
    } else {
      cur.replace(cur.getElementCount(), 0, new Element[] {child});
    }
  }

  private void finishBlock(Element e) { }

  protected void insert(int offset, DefaultStyledDocument.ElementSpec[] data) throws BadLocationException {
    if (offset == 0 && getLength() <= 1) {
      create(data);
      return;
    }
    super.insert(offset, data);
  }

  public Element getElement(String id) {
    if (id == null) {
      return null;
    }
    return findById(getDefaultRootElement(), id);
  }

  private Element findById(Element e, String id) {
    Object v = e.getAttributes().getAttribute(HTML.Attribute.ID);
    if (v != null && id.equals(v.toString())) {
      return e;
    }
    for (int i = 0; i < e.getElementCount(); i++) {
      Element r = findById(e.getElement(i), id);
      if (r != null) {
        return r;
      }
    }
    return null;
  }

  public Element getElement(Element e, Object attribute, Object value) {
    if (e == null) {
      return null;
    }
    Object v = e.getAttributes().getAttribute(attribute);
    if (v != null && v.equals(value)) {
      return e;
    }
    for (int i = 0; i < e.getElementCount(); i++) {
      Element r = getElement(e.getElement(i), attribute, value);
      if (r != null) {
        return r;
      }
    }
    return null;
  }

  public void setInnerHTML(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem != null && elem.isLeaf()) {
      throw new IllegalArgumentException("Can not set inner HTML of a leaf");
    }
    if (elem != null && htmlText != null) {
      int oldCount = elem.getElementCount();
      insertHTML(elem, elem.getStartOffset(), htmlText, true);
      if (elem.getElementCount() > oldCount) {
        removeElements(elem, elem.getElementCount() - oldCount, oldCount);
      }
    }
  }

  public void setOuterHTML(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem != null && elem.getParentElement() != null && htmlText != null) {
      int start = elem.getStartOffset();
      int end = elem.getEndOffset();
      int startLength = getLength();
      boolean wantsNewline = !elem.isLeaf();
      if (!wantsNewline && (end > startLength || getText(end - 1, 1).charAt(0) == '\n')) {
        wantsNewline = true;
      }
      Element parent = elem.getParentElement();
      int oldCount = parent.getElementCount();
      insertHTML(parent, start, htmlText, wantsNewline);
      int newLength = getLength();
      if (oldCount != parent.getElementCount()) {
        int removeIndex = parent.getElementIndex(start + (newLength - startLength));
        removeElements(parent, removeIndex, 1);
      }
    }
  }

  public void insertAfterStart(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem == null || htmlText == null) {
      return;
    }
    if (elem.isLeaf()) {
      throw new IllegalArgumentException("Can not insert HTML after start of a leaf");
    }
    insertHTML(elem, elem.getStartOffset(), htmlText, false);
  }

  public void insertBeforeStart(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem != null) {
      Element parent = elem.getParentElement();
      if (parent != null) {
        insertHTML(parent, elem.getStartOffset(), htmlText, false);
      }
    }
  }

  public void insertBeforeEnd(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem != null && elem.isLeaf()) {
      throw new IllegalArgumentException("Can not set inner HTML before end of leaf");
    }
    if (elem != null) {
      int offset = elem.getEndOffset();
      if (elem.getElement(elem.getElementIndex(offset - 1)).isLeaf() && getText(offset - 1, 1).charAt(0) == '\n') {
        offset--;
      }
      insertHTML(elem, offset, htmlText, false);
    }
  }

  public void insertAfterEnd(Element elem, String htmlText) throws BadLocationException, IOException {
    verifyParser();
    if (elem != null) {
      Element parent = elem.getParentElement();
      if (parent != null) {
        int offset = elem.getEndOffset();
        if (elem.isLeaf() && getText(offset - 1, 1).charAt(0) == '\n') {
          offset--;
        }
        insertHTML(parent, offset, htmlText, false);
      }
    }
  }

  private void insertHTML(Element parent, int offset, String html, boolean wantsTrailingNewline)
      throws BadLocationException, IOException {
    if (parent == null || html == null) {
      return;
    }
    HTMLEditorKit.Parser p = getParser();
    if (p == null) {
      return;
    }
    int lastOffset = Math.max(0, offset - 1);
    Element charElement = getCharacterElement(lastOffset);
    Element commonParent = parent;
    int pop = 0;
    int push = 0;
    if (parent.getStartOffset() > lastOffset) {
      while (commonParent != null && commonParent.getStartOffset() > lastOffset) {
        commonParent = commonParent.getParentElement();
        push++;
      }
      if (commonParent == null) {
        throw new BadLocationException("No common parent", offset);
      }
    }
    while (charElement != null && charElement != commonParent) {
      pop++;
      charElement = charElement.getParentElement();
    }
    if (charElement != null) {
      HTMLReader reader = new HTMLReader(offset, pop - 1, push, null, false, true, wantsTrailingNewline);
      p.parse(new StringReader(html), reader, true);
      reader.flush();
    }
  }

  private void removeElements(Element e, int index, int count) throws BadLocationException {
    writeLock();
    try {
      int start = e.getElement(index).getStartOffset();
      int end = e.getElement(index + count - 1).getEndOffset();
      if (end > getLength()) {
        removeElementsAtEnd(e, index, count, start, end);
      } else {
        removeElementsDirect(e, index, count, start, end);
      }
    } finally {
      writeUnlock();
    }
  }

  private void removeElementsDirect(Element e, int index, int count, int start, int end) throws BadLocationException {
    Element[] removed = new Element[count];
    Element[] added = new Element[0];
    for (int i = 0; i < count; i++) {
      removed[i] = e.getElement(i + index);
    }
    DefaultDocumentEvent dde = new DefaultDocumentEvent(start, end - start, DocumentEvent.EventType.REMOVE);
    ((AbstractDocument.BranchElement) e).replace(index, removed.length, added);
    dde.addEdit(new ElementEdit(e, index, removed, added));
    javax.swing.undo.UndoableEdit u = getContent().remove(start, end - start);
    if (u != null) {
      dde.addEdit(u);
    }
    postRemoveUpdate(dde);
    dde.end();
    fireRemoveUpdate(dde);
    if (u != null) {
      fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, dde));
    }
  }

  private void removeElementsAtEnd(Element e, int index, int count, int start, int end) throws BadLocationException {
    Element last = e.getElement(index - 1);
    while (!last.isLeaf()) {
      last = last.getElement(last.getElementCount() - 1);
    }
    Element[] removed = new Element[count];
    for (int i = 0; i < count; i++) {
      removed[i] = e.getElement(i + index);
    }
    Element lastParent = last.getParentElement();
    int lastIdx = lastParent.getElementIndex(last.getStartOffset());
    DefaultDocumentEvent dde = new DefaultDocumentEvent(start - 1, end - start, DocumentEvent.EventType.REMOVE);
    ((AbstractDocument.BranchElement) e).replace(index, count, new Element[0]);
    dde.addEdit(new ElementEdit(e, index, removed, new Element[0]));
    javax.swing.undo.UndoableEdit u = getContent().remove(start - 1, end - start);
    Element fresh = createLeafElement(lastParent, last.getAttributes(), last.getStartOffset(), getLength() + 1);
    ((AbstractDocument.BranchElement) lastParent).replace(lastIdx, 1, new Element[] {fresh});
    dde.addEdit(new ElementEdit(lastParent, lastIdx, new Element[] {last}, new Element[] {fresh}));
    if (u != null) {
      dde.addEdit(u);
    }
    postRemoveUpdate(dde);
    dde.end();
    fireRemoveUpdate(dde);
    if (u != null) {
      fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, dde));
    }
  }

  private void verifyParser() {
    if (getParser() == null) {
      throw new IllegalStateException("No HTMLEditorKit.Parser");
    }
  }

  public HTMLDocument.Iterator getIterator(HTML.Tag t) {
    if (t.isBlock()) {
      return null;
    }
    return new LeafIterator(t, this);
  }

  static class LeafIterator extends Iterator {
    private final HTML.Tag tag;
    private final List<Element> found = new ArrayList<Element>();
    private int idx;

    LeafIterator(HTML.Tag t, Document doc) {
      tag = t;
      collect(doc.getDefaultRootElement());
    }

    private void collect(Element e) {
      if (e.isLeaf()) {
        AttributeSet a = e.getAttributes();
        if (a.isDefined(tag) || a.getAttribute(StyleConstants.NameAttribute) == tag) {
          found.add(e);
        }
      }
      for (int i = 0; i < e.getElementCount(); i++) {
        collect(e.getElement(i));
      }
    }

    public AttributeSet getAttributes() {
      if (idx >= found.size()) {
        return null;
      }
      Element e = found.get(idx);
      Object o = e.getAttributes().getAttribute(tag);
      if (o instanceof AttributeSet) {
        return (AttributeSet) o;
      }
      return e.getAttributes();
    }

    public int getStartOffset() { return idx < found.size() ? found.get(idx).getStartOffset() : -1; }

    public int getEndOffset() { return idx < found.size() ? found.get(idx).getEndOffset() : -1; }

    public void next() { idx++; }

    public boolean isValid() { return idx < found.size(); }

    public HTML.Tag getTag() { return tag; }
  }

  public class HTMLReader extends HTMLEditorKit.ParserCallback {
    protected Vector<ElementSpec> parseBuffer = new Vector<ElementSpec>();
    protected MutableAttributeSet charAttr = new SimpleAttributeSet();
    private final Stack<AttributeSet> charAttrStack = new Stack<AttributeSet>();
    private final int offset;
    private final List<ElementSpec> headBuffer = new ArrayList<ElementSpec>();
    private boolean headDone;
    private boolean inHead;
    private boolean inTitle;
    private boolean inStyle;
    private boolean inScript;
    private boolean inPre;
    private boolean inParagraph;
    private boolean impliedParagraph;
    private boolean lastNewline;
    private boolean skipLeadingNewline;
    private boolean bodyOpen;
    private boolean lastStartWasBlock;
    private final Stack<HTML.Tag> blocks = new Stack<HTML.Tag>();
    private final StringBuilder styleText = new StringBuilder();
    private final StringBuilder titleText = new StringBuilder();
    private MutableAttributeSet bodyAttrs = new SimpleAttributeSet();
    private boolean flushed;
    private boolean inSelect;
    private boolean inTextArea;
    private MutableAttributeSet formAttrs;
    private final List<Option> selectOptions = new ArrayList<Option>();
    private Option currentOption;
    private final StringBuilder textAreaText = new StringBuilder();

    private boolean fragment;
    private boolean mid;
    private boolean noImplied;
    private boolean wantsTrailingNewline = true;
    private int popDepth;
    private int pushDepth;
    private HTML.Tag insertTag;
    private boolean foundInsertTag = true;

    public HTMLReader(int offset) {
      this.offset = offset;
      if (getLength() > 0) {
        startFragment(0, 0, null, false);
      }
    }

    public HTMLReader(int offset, int popDepth, int pushDepth, HTML.Tag insertTag) {
      this(offset, popDepth, pushDepth, insertTag, true, false, true);
    }

    HTMLReader(int offset, int popDepth, int pushDepth, HTML.Tag insertTag, boolean insertInsertTag,
        boolean insertAfterImplied, boolean wantsTrailingNewline) {
      this.offset = offset;
      this.wantsTrailingNewline = wantsTrailingNewline;
      startFragment(popDepth, pushDepth, insertTag, insertAfterImplied);
    }

    private void startFragment(int pop, int push, HTML.Tag tag, boolean afterImplied) {
      fragment = true;
      popDepth = pop;
      pushDepth = push;
      insertTag = tag;
      noImplied = afterImplied;
      mid = tag == null && !afterImplied;
      foundInsertTag = tag == null;
      bodyOpen = true;
      headDone = true;
    }

    private boolean skipFragment(HTML.Tag t, boolean start) {
      if (!fragment || foundInsertTag) {
        return false;
      }
      if (start && t == insertTag) {
        foundInsertTag = true;
        return false;
      }
      return true;
    }

    private MutableAttributeSet tagAttrs(HTML.Tag t, AttributeSet a) {
      SimpleAttributeSet s = new SimpleAttributeSet();
      if (a != null) {
        for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
          Object k = en.nextElement();
          if (k != IMPLIED) {
            s.addAttribute(k, a.getAttribute(k));
          }
        }
      }
      s.addAttribute(StyleConstants.NameAttribute, t);
      return s;
    }

    protected void registerTag(HTML.Tag t, TagAction a) { }

    protected void pushCharacterStyle() { charAttrStack.push(charAttr.copyAttributes()); }

    protected void popCharacterStyle() {
      if (!charAttrStack.isEmpty()) {
        charAttr = new SimpleAttributeSet(charAttrStack.pop());
      }
    }

    protected void blockOpen(HTML.Tag t, MutableAttributeSet attr) {
      if (impliedParagraph && t != HTML.Tag.IMPLIED) {
        blockClose(HTML.Tag.IMPLIED);
      }
      if (t == HTML.Tag.IMPLIED) {
        impliedParagraph = true;
      } else {
        inParagraph = false;
      }
      if (t == HTML.Tag.P || t.isParagraph() && t != HTML.Tag.IMPLIED) {
        inParagraph = true;
      }
      if (t == HTML.Tag.IMPLIED) {
        inParagraph = true;
      }
      add(new ElementSpec(attr, ElementSpec.StartTagType));
      blocks.push(t);
      lastStartWasBlock = true;
      lastNewline = false;
    }

    protected void blockClose(HTML.Tag t) {
      if (impliedParagraph && t != HTML.Tag.IMPLIED) {
        blockClose(HTML.Tag.IMPLIED);
      }
      if (blocks.isEmpty()) {
        return;
      }
      if (lastStartWasBlock && !t.isParagraph() && t != HTML.Tag.IMPLIED && t != HTML.Tag.HTML
          && t != HTML.Tag.BODY) {
        blockOpen(HTML.Tag.IMPLIED, tagAttrs(HTML.Tag.IMPLIED, inPre ? preAttrs() : null));
        addCR();
        blockClose(HTML.Tag.IMPLIED);
        lastStartWasBlock = false;
      } else if (lastStartWasBlock && (t.isParagraph() || t == HTML.Tag.IMPLIED || t != HTML.Tag.HTML)) {
        addCR();
      } else if ((t == HTML.Tag.IMPLIED || t.isParagraph()) && !lastNewline) {
        addCR();
      }
      add(new ElementSpec(null, ElementSpec.EndTagType));
      blocks.pop();
      if (t == HTML.Tag.IMPLIED) {
        impliedParagraph = false;
      }
      inParagraph = false;
      lastStartWasBlock = false;
    }

    private MutableAttributeSet preAttrs() {
      SimpleAttributeSet pa = new SimpleAttributeSet();
      pa.addAttribute(CSS.Attribute.WHITE_SPACE, new CSS.Value("pre"));
      return pa;
    }

    private void addCR() {
      SimpleAttributeSet a = new SimpleAttributeSet(charAttr);
      a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
      a.addAttribute(CR_KEY, Boolean.TRUE);
      a.removeAttribute(HTML.Tag.COMMENT);
      char[] nl = {'\n'};
      add(new ElementSpec(a, ElementSpec.ContentType, nl, 0, 1));
      lastNewline = true;
      lastStartWasBlock = false;
    }

    private void add(ElementSpec spec) {
      if (inHead && !headDone) {
        headBuffer.add(spec);
      } else {
        parseBuffer.addElement(spec);
      }
    }

    protected void addContent(char[] data, int offs, int length) { addContent(data, offs, length, true); }

    protected void addContent(char[] data, int offs, int length, boolean generateImpliedPIfNecessary) {
      if (length == 0) {
        return;
      }
      if (generateImpliedPIfNecessary && !inParagraph && !inHead && !(noImplied && blocks.isEmpty())) {
        MutableAttributeSet pa = new SimpleAttributeSet();
        if (inPre) {
          pa.addAttribute(CSS.Attribute.WHITE_SPACE, new CSS.Value("pre"));
        }
        blockOpen(HTML.Tag.IMPLIED, tagAttrs(HTML.Tag.IMPLIED, pa));
      }
      SimpleAttributeSet a = new SimpleAttributeSet(charAttr);
      a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
      a.removeAttribute(CR_KEY);
      add(new ElementSpec(a, ElementSpec.ContentType, data, offs, length));
      lastNewline = data[offs + length - 1] == '\n';
      lastStartWasBlock = false;
    }

    protected void addSpecialElement(HTML.Tag t, MutableAttributeSet a) {
      if (!inParagraph && !inHead && t != HTML.Tag.TITLE && !(noImplied && blocks.isEmpty())) {
        MutableAttributeSet pa = new SimpleAttributeSet();
        blockOpen(HTML.Tag.IMPLIED, tagAttrs(HTML.Tag.IMPLIED, pa));
      }
      SimpleAttributeSet ca = new SimpleAttributeSet(charAttr);
      ca.addAttributes(tagAttrs(t, a));
      char[] sp = {' '};
      add(new ElementSpec(ca, ElementSpec.ContentType, sp, 0, 1));
      lastNewline = false;
      lastStartWasBlock = false;
    }

    protected void textAreaContent(char[] data) { addContent(data, 0, data.length); }

    protected void preContent(char[] data) {
      int start = 0;
      for (int i = 0; i < data.length; i++) {
        if (data[i] == '\n') {
          if (i + 1 > start) {
            if (skipLeadingNewline && start == 0 && i == 0) {
              skipLeadingNewline = false;
              start = i + 1;
              continue;
            }
            addContent(data, start, i + 1 - start);
          }
          if (impliedParagraph) {
            blockClose(HTML.Tag.IMPLIED);
          }
          start = i + 1;
        }
      }
      skipLeadingNewline = false;
      if (start < data.length) {
        addContent(data, start, data.length - start);
      }
    }

    private void openBody() {
      if (bodyOpen) {
        return;
      }
      closeHead();
      bodyOpen = true;
      add(new ElementSpec(tagAttrs(HTML.Tag.BODY, bodyAttrs), ElementSpec.StartTagType));
      blocks.push(HTML.Tag.BODY);
      lastStartWasBlock = true;
    }

    private void closeHead() {
      if (headDone) {
        return;
      }
      headDone = true;
      inHead = false;
      List<ElementSpec> out = new ArrayList<ElementSpec>();
      out.add(new ElementSpec(tagAttrs(HTML.Tag.HEAD, null), ElementSpec.StartTagType));
      out.add(new ElementSpec(tagAttrs(HTML.Tag.IMPLIED, null), ElementSpec.StartTagType));
      out.addAll(headBuffer);
      SimpleAttributeSet a = new SimpleAttributeSet();
      a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
      a.addAttribute(CR_KEY, Boolean.TRUE);
      out.add(new ElementSpec(a, ElementSpec.ContentType, new char[] {'\n'}, 0, 1));
      out.add(new ElementSpec(null, ElementSpec.EndTagType));
      out.add(new ElementSpec(null, ElementSpec.EndTagType));
      parseBuffer.addAll(out);
      headBuffer.clear();
    }

    private void ensureStarted() {
      if (fragment) {
        return;
      }
      if (parseBuffer.isEmpty() && !flushed && headBuffer.isEmpty() && !headDone) {
        parseBuffer.addElement(new ElementSpec(tagAttrs(HTML.Tag.HTML, null), ElementSpec.StartTagType));
      }
    }

    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
      if (skipFragment(t, true)) {
        return;
      }
      ensureStarted();
      Object styleDecl = a.getAttribute(HTML.Attribute.STYLE);
      if (styleDecl != null) {
        a.removeAttribute(HTML.Attribute.STYLE);
        CSS.parseDeclarations(styleDecl.toString(), a);
      }
      if (inScript || inStyle && t != HTML.Tag.STYLE) {
        return;
      }
      if (t == HTML.Tag.HTML) {
        return;
      }
      if (t == HTML.Tag.HEAD) {
        inHead = !headDone;
        return;
      }
      if (t == HTML.Tag.BODY) {
        if (bodyOpen) {
          return;
        }
        bodyAttrs = new SimpleAttributeSet();
        for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
          Object k = en.nextElement();
          if (k != IMPLIED) {
            bodyAttrs.addAttribute(k, a.getAttribute(k));
          }
        }
        openBody();
        return;
      }
      if (t == HTML.Tag.TITLE) {
        if (!bodyOpen) {
          inHead = !headDone;
          inTitle = true;
          titleText.setLength(0);
          addSpecialElement(t, a);
        }
        return;
      }
      if (t == HTML.Tag.STYLE) {
        inStyle = true;
        styleText.setLength(0);
        return;
      }
      if (t == HTML.Tag.SCRIPT) {
        if (!bodyOpen) {
          openBody();
        }
        addSpecialElement(t, a);
        inScript = true;
        return;
      }
      if (t == HTML.Tag.META || t == HTML.Tag.LINK || t == HTML.Tag.BASE) {
        return;
      }
      if (t == HTML.Tag.SELECT || t == HTML.Tag.TEXTAREA) {
        if (!bodyOpen) {
          openBody();
        }
        formAttrs = new SimpleAttributeSet(a);
        if (t == HTML.Tag.SELECT) {
          inSelect = true;
          selectOptions.clear();
          currentOption = null;
        } else {
          inTextArea = true;
          textAreaText.setLength(0);
        }
        return;
      }
      if (t == HTML.Tag.OPTION) {
        if (inSelect) {
          currentOption = new Option(a);
          selectOptions.add(currentOption);
        }
        return;
      }
      if (t == HTML.Tag.APPLET || t instanceof HTML.UnknownTag) {
        if (t instanceof HTML.UnknownTag && (!preservesUnknownTags || !inParagraph)) {
          return;
        }
        if (!bodyOpen) {
          openBody();
        }
        addSpecialElement(t, a);
        return;
      }
      if (!bodyOpen) {
        openBody();
      }
      if (isBlockTag(t)) {
        if (t.isParagraph()) {
          blockOpen(t, tagAttrs(t, a));
        } else if (t == HTML.Tag.PRE) {
          if (impliedParagraph) {
            blockClose(HTML.Tag.IMPLIED);
          }
          blockOpen(t, tagAttrs(t, a));
          inPre = true;
          skipLeadingNewline = true;
        } else {
          blockOpen(t, tagAttrs(t, a));
        }
        return;
      }
      if (t == HTML.Tag.A || t == HTML.Tag.B || t == HTML.Tag.I || t == HTML.Tag.U || t == HTML.Tag.S
          || t == HTML.Tag.STRIKE || t == HTML.Tag.SUB || t == HTML.Tag.SUP || t == HTML.Tag.FONT
          || t == HTML.Tag.SPAN || t == HTML.Tag.EM || t == HTML.Tag.STRONG || t == HTML.Tag.TT
          || t == HTML.Tag.CODE || t == HTML.Tag.BIG || t == HTML.Tag.SMALL || t == HTML.Tag.CITE
          || t == HTML.Tag.DFN || t == HTML.Tag.KBD || t == HTML.Tag.SAMP || t == HTML.Tag.VAR
          || t == HTML.Tag.ADDRESS || t == HTML.Tag.NOBR || t == HTML.Tag.BASEFONT) {
        pushCharacterStyle();
        SimpleAttributeSet copy = new SimpleAttributeSet();
        for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
          Object k = en.nextElement();
          if (k != IMPLIED) {
            copy.addAttribute(k, a.getAttribute(k));
          }
        }
        charAttr.addAttribute(t, copy);
        applyCharCss(t, copy);
        return;
      }
      handleSimpleTag(t, a, pos);
    }

    private void applyCharCss(HTML.Tag t, AttributeSet a) {
      if (t == HTML.Tag.B) {
        charAttr.addAttribute(CSS.Attribute.FONT_WEIGHT, new CSS.Value("bold"));
      } else if (t == HTML.Tag.I) {
        charAttr.addAttribute(CSS.Attribute.FONT_STYLE, new CSS.Value("italic"));
      } else if (t == HTML.Tag.U) {
        charAttr.addAttribute(CSS.Attribute.TEXT_DECORATION, new CSS.Value("underline"));
      } else if (t == HTML.Tag.S || t == HTML.Tag.STRIKE) {
        charAttr.addAttribute(CSS.Attribute.TEXT_DECORATION, new CSS.Value("line-through"));
      } else if (t == HTML.Tag.SUB) {
        charAttr.addAttribute(CSS.Attribute.VERTICAL_ALIGN, new CSS.Value("sub"));
      } else if (t == HTML.Tag.SUP) {
        charAttr.addAttribute(CSS.Attribute.VERTICAL_ALIGN, new CSS.Value("sup"));
      } else if (t == HTML.Tag.FONT) {
        Object c = a.getAttribute(HTML.Attribute.COLOR);
        if (c != null) {
          charAttr.addAttribute(CSS.Attribute.COLOR, new CSS.Value(c.toString()));
        }
        Object f = a.getAttribute(HTML.Attribute.FACE);
        if (f != null) {
          charAttr.addAttribute(CSS.Attribute.FONT_FAMILY, new CSS.Value(f.toString()));
        }
        Object s = a.getAttribute(HTML.Attribute.SIZE);
        if (s != null) {
          String sz = s.toString().trim();
          try {
            if (sz.startsWith("+") || sz.startsWith("-")) {
              sz = String.valueOf(Math.max(1, Math.min(7, 3 + Integer.parseInt(sz.startsWith("+") ? sz.substring(1) : sz))));
            }
          } catch (NumberFormatException e) {
            sz = "3";
          }
          charAttr.addAttribute(CSS.Attribute.FONT_SIZE, new CSS.Value(sz));
        }
      }
      for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) {
        Object k = en.nextElement();
        if (k instanceof CSS.Attribute) {
          charAttr.addAttribute(k, a.getAttribute(k));
        }
      }
    }

    private boolean isBlockTag(HTML.Tag t) {
      return t == HTML.Tag.P || t == HTML.Tag.H1 || t == HTML.Tag.H2 || t == HTML.Tag.H3 || t == HTML.Tag.H4
          || t == HTML.Tag.H5 || t == HTML.Tag.H6 || t == HTML.Tag.DT || t == HTML.Tag.UL || t == HTML.Tag.OL
          || t == HTML.Tag.MENU || t == HTML.Tag.DIR || t == HTML.Tag.LI || t == HTML.Tag.DL || t == HTML.Tag.DD
          || t == HTML.Tag.DIV || t == HTML.Tag.BLOCKQUOTE || t == HTML.Tag.CENTER || t == HTML.Tag.PRE
          || t == HTML.Tag.FORM || t == HTML.Tag.TABLE || t == HTML.Tag.TR || t == HTML.Tag.TD || t == HTML.Tag.TH
          || t == HTML.Tag.CAPTION || t == HTML.Tag.NOFRAMES;
    }

    private void addEndElement(HTML.Tag t) {
      SimpleAttributeSet ea = new SimpleAttributeSet();
      ea.addAttribute(HTML.Attribute.ENDTAG, "true");
      addSpecialElement(t, ea);
    }

    public void handleEndTag(HTML.Tag t, int pos) {
      if (skipFragment(t, false)) {
        return;
      }
      if (t == HTML.Tag.SCRIPT) {
        inScript = false;
        addEndElement(t);
        return;
      }
      if (t == HTML.Tag.OPTION) {
        currentOption = null;
        return;
      }
      if (t == HTML.Tag.SELECT && inSelect) {
        inSelect = false;
        MutableAttributeSet sa = formAttrs;
        boolean multiple = sa.getAttribute(HTML.Attribute.MULTIPLE) != null;
        int size = 0;
        Object sz = sa.getAttribute(HTML.Attribute.SIZE);
        if (sz != null) {
          try {
            size = Integer.parseInt(sz.toString().trim());
          } catch (NumberFormatException e) {
            size = 0;
          }
        }
        if (multiple || size > 1) {
          OptionListModel<Option> m = new OptionListModel<Option>();
          int i = 0;
          for (Option o : selectOptions) {
            m.addElement(o);
          }
          m.setSelectionMode(multiple ? javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
              : javax.swing.ListSelectionModel.SINGLE_SELECTION);
          for (int k = 0; k < selectOptions.size(); k++) {
            if (selectOptions.get(k).isSelected()) {
              if (multiple) {
                m.addSelectionInterval(k, k);
              } else {
                m.setSelectionInterval(k, k);
              }
            }
          }
          sa.addAttribute(StyleConstants.ModelAttribute, m);
        } else {
          OptionComboBoxModel<Option> m = new OptionComboBoxModel<Option>();
          Option initial = null;
          for (Option o : selectOptions) {
            m.addElement(o);
            if (o.isSelected()) {
              initial = o;
            }
          }
          if (initial == null && !selectOptions.isEmpty()) {
            initial = selectOptions.get(0);
          }
          if (initial != null) {
            m.setSelectedItem(initial);
            m.setInitialSelection(initial);
          }
          sa.addAttribute(StyleConstants.ModelAttribute, m);
        }
        addSpecialElement(t, sa);
        return;
      }
      if (t == HTML.Tag.TEXTAREA && inTextArea) {
        inTextArea = false;
        MutableAttributeSet ta = formAttrs;
        javax.swing.text.PlainDocument pd = new javax.swing.text.PlainDocument();
        try {
          pd.insertString(0, textAreaText.toString(), null);
        } catch (BadLocationException e) {
          // empty model
        }
        ta.addAttribute(StyleConstants.ModelAttribute, pd);
        addSpecialElement(t, ta);
        return;
      }
      if (t == HTML.Tag.APPLET || t instanceof HTML.UnknownTag && preservesUnknownTags && inParagraph) {
        if (bodyOpen) {
          addEndElement(t);
        }
        return;
      }
      if (t == HTML.Tag.STYLE) {
        inStyle = false;
        getStyleSheet().addRule(styleText.toString());
        return;
      }
      if (inScript || inStyle) {
        return;
      }
      if (t == HTML.Tag.TITLE) {
        if (inTitle) {
          inTitle = false;
          putProperty(Document.TitleProperty, titleText.toString().trim());
          SimpleAttributeSet ca = new SimpleAttributeSet(charAttr);
          ca.addAttributes(tagAttrs(t, null));
          ca.addAttribute(HTML.Attribute.ENDTAG, "true");
          add(new ElementSpec(ca, ElementSpec.ContentType, new char[] {' '}, 0, 1));
        }
        return;
      }
      if (t == HTML.Tag.HEAD) {
        inHead = false;
        closeHead();
        return;
      }
      if (t == HTML.Tag.BODY || t == HTML.Tag.HTML) {
        return;
      }
      if (isBlockTag(t)) {
        if (!blocks.contains(t)) {
          return;
        }
        while (!blocks.isEmpty() && blocks.peek() != t) {
          HTML.Tag top = blocks.peek();
          if (top == HTML.Tag.BODY) {
            return;
          }
          blockClose(top);
        }
        blockClose(t);
        if (t == HTML.Tag.PRE) {
          inPre = false;
        }
        return;
      }
      if (t == HTML.Tag.A || t == HTML.Tag.B || t == HTML.Tag.I || t == HTML.Tag.U || t == HTML.Tag.S
          || t == HTML.Tag.STRIKE || t == HTML.Tag.SUB || t == HTML.Tag.SUP || t == HTML.Tag.FONT
          || t == HTML.Tag.SPAN || t == HTML.Tag.EM || t == HTML.Tag.STRONG || t == HTML.Tag.TT
          || t == HTML.Tag.CODE || t == HTML.Tag.BIG || t == HTML.Tag.SMALL || t == HTML.Tag.CITE
          || t == HTML.Tag.DFN || t == HTML.Tag.KBD || t == HTML.Tag.SAMP || t == HTML.Tag.VAR
          || t == HTML.Tag.ADDRESS || t == HTML.Tag.NOBR || t == HTML.Tag.BASEFONT) {
        popCharacterStyle();
      }
    }

    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
      if (skipFragment(t, true)) {
        return;
      }
      ensureStarted();
      if (inScript || inStyle) {
        return;
      }
      if (t == HTML.Tag.META || t == HTML.Tag.LINK || t == HTML.Tag.BASE || t == HTML.Tag.PARAM
          || t == HTML.Tag.ISINDEX) {
        return;
      }
      if (!bodyOpen) {
        openBody();
      }
      if (t == HTML.Tag.BR || t == HTML.Tag.HR || t == HTML.Tag.IMG || t == HTML.Tag.INPUT || t == HTML.Tag.SELECT
          || t == HTML.Tag.TEXTAREA || t == HTML.Tag.OBJECT || t == HTML.Tag.APPLET || t == HTML.Tag.AREA
          || t == HTML.Tag.MAP) {
        addSpecialElement(t, a);
      }
    }

    public void handleText(char[] data, int pos) {
      if (skipFragment(null, false)) {
        return;
      }
      ensureStarted();
      if (inSelect) {
        if (currentOption != null) {
          String prev = currentOption.getLabel();
          currentOption.setLabel((prev == null ? "" : prev) + new String(data));
        }
        return;
      }
      if (inTextArea) {
        textAreaText.append(data);
        return;
      }
      if (inScript) {
        SimpleAttributeSet ca = new SimpleAttributeSet();
        ca.addAttribute(HTML.Attribute.COMMENT, new String(data));
        addSpecialElement(HTML.Tag.COMMENT, ca);
        return;
      }
      if (inStyle) {
        styleText.append(data);
        return;
      }
      if (inTitle) {
        titleText.append(data);
        return;
      }
      if (inHead && !bodyOpen) {
        boolean ws = true;
        for (char c : data) {
          if (!Character.isWhitespace(c)) {
            ws = false;
          }
        }
        if (ws) {
          return;
        }
        openBody();
      }
      if (!bodyOpen) {
        openBody();
      }
      if (inPre) {
        preContent(data);
      } else {
        addContent(data, 0, data.length);
      }
    }

    public void handleComment(char[] data, int pos) {
      if (skipFragment(null, false)) {
        return;
      }
      ensureStarted();
      if (inScript || inStyle || !bodyOpen) {
        return;
      }
      SimpleAttributeSet a = new SimpleAttributeSet();
      a.addAttribute(HTML.Attribute.COMMENT, new String(data));
      addSpecialElement(HTML.Tag.COMMENT, a);
    }

    public void handleEndOfLineString(String eol) {
      if (eol != null) {
        putProperty(DefaultEditorKit_EndOfLineStringProperty, eol);
      }
    }

    private void flushFragment() throws BadLocationException {
      if (flushed) {
        return;
      }
      flushed = true;
      if (impliedParagraph) {
        blockClose(HTML.Tag.IMPLIED);
      }
      while (!blocks.isEmpty()) {
        blockClose(blocks.peek());
      }
      List<ElementSpec> out = new ArrayList<ElementSpec>();
      int depth = 0;
      if (mid) {
        Element e = getCharacterElement(Math.max(0, offset - 1)).getParentElement();
        while (e != null && !"body".equals(e.getName())) {
          depth++;
          e = e.getParentElement();
        }
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        out.add(new ElementSpec(a, ElementSpec.ContentType, new char[] {'\n'}, 0, 1));
        for (int i = 0; i < depth; i++) {
          out.add(new ElementSpec(null, ElementSpec.EndTagType));
        }
      }
      if (insertTag != null && popDepth > 0 && offset > 0 && offset < getLength() && charAt(offset) != '\n') {
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        ElementSpec nl = new ElementSpec(a, ElementSpec.ContentType, new char[] {'\n'}, 0, 1);
        nl.setDirection(ElementSpec.JoinPreviousDirection);
        out.add(nl);
      }
      for (int i = 0; i < popDepth; i++) {
        out.add(new ElementSpec(null, ElementSpec.EndTagType));
      }
      for (int i = 0; i < pushDepth; i++) {
        ElementSpec es = new ElementSpec(null, ElementSpec.StartTagType);
        es.setDirection(ElementSpec.JoinNextDirection);
        out.add(es);
      }
      out.addAll(parseBuffer);
      parseBuffer.removeAllElements();
      ElementSpec last = out.isEmpty() ? null : out.get(out.size() - 1);
      if (wantsTrailingNewline && last != null && last.getType() == ElementSpec.ContentType && !lastNewline) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        a.addAttribute(CR_KEY, Boolean.TRUE);
        out.add(new ElementSpec(a, ElementSpec.ContentType, new char[] {'\n'}, 0, 1));
      }
      for (int i = 0; i < depth; i++) {
        ElementSpec es = new ElementSpec(null, ElementSpec.StartTagType);
        es.setDirection(ElementSpec.JoinFractureDirection);
        out.add(es);
      }
      if (out.isEmpty()) {
        return;
      }
      insertFragment(offset, out.toArray(new ElementSpec[0]));
    }

    public void flush() throws BadLocationException {
      if (fragment) {
        flushFragment();
        return;
      }
      ensureStarted();
      if (!bodyOpen) {
        openBody();
      }
      if (impliedParagraph) {
        blockClose(HTML.Tag.IMPLIED);
      }
      while (!blocks.isEmpty()) {
        HTML.Tag top = blocks.peek();
        blockClose(top);
      }
      add(new ElementSpec(null, ElementSpec.EndTagType));
      if (flushed || parseBuffer.isEmpty()) {
        return;
      }
      flushed = true;
      ElementSpec[] spec = new ElementSpec[parseBuffer.size()];
      parseBuffer.copyInto(spec);
      parseBuffer.removeAllElements();
      if (offset == 0) {
        create(spec);
      } else {
        insert(offset, spec);
      }
    }

    public class TagAction {
      public void start(HTML.Tag t, MutableAttributeSet a) { }

      public void end(HTML.Tag t) { }
    }

    public class BlockAction extends TagAction {
      public void start(HTML.Tag t, MutableAttributeSet attr) { blockOpen(t, attr); }

      public void end(HTML.Tag t) { blockClose(t); }
    }

    public class ParagraphAction extends BlockAction {
      public void start(HTML.Tag t, MutableAttributeSet a) { super.start(t, a); }

      public void end(HTML.Tag t) { super.end(t); }
    }

    public class CharacterAction extends TagAction {
      public void start(HTML.Tag t, MutableAttributeSet attr) {
        pushCharacterStyle();
        charAttr.addAttribute(t, attr.copyAttributes());
      }

      public void end(HTML.Tag t) { popCharacterStyle(); }
    }

    public class SpecialAction extends TagAction {
      public void start(HTML.Tag t, MutableAttributeSet a) { addSpecialElement(t, a); }
    }

    public class FormAction extends SpecialAction {
      public void start(HTML.Tag t, MutableAttributeSet attr) { super.start(t, attr); }

      public void end(HTML.Tag t) { }
    }

    public class HiddenAction extends TagAction {
      public void start(HTML.Tag t, MutableAttributeSet a) { addSpecialElement(t, a); }

      public void end(HTML.Tag t) { }
    }

    public class IsindexAction extends TagAction {
      public void start(HTML.Tag t, MutableAttributeSet a) { }
    }

    public class PreAction extends BlockAction {
      public void start(HTML.Tag t, MutableAttributeSet attr) { super.start(t, attr); }

      public void end(HTML.Tag t) { super.end(t); }
    }
  }

  private static final String DefaultEditorKit_EndOfLineStringProperty = "__EndOfLine__";

  public class RunElement extends LeafElement {
    public RunElement(Element parent, AttributeSet a, int offs0, int offs1) {
      super(parent, a, offs0, offs1);
    }

    public String getName() {
      Object o = getAttribute(StyleConstants.NameAttribute);
      if (o != null) {
        return o.toString();
      }
      return super.getName();
    }

    public AttributeSet getResolveParent() { return null; }
  }

  public class BlockElement extends BranchElement {
    public BlockElement(Element parent, AttributeSet a) {
      super(parent, a);
    }

    public String getName() {
      Object o = getAttribute(StyleConstants.NameAttribute);
      if (o != null) {
        return o.toString();
      }
      return super.getName();
    }

    public AttributeSet getResolveParent() { return null; }
  }

  public static abstract class Iterator {
    protected Iterator() { }

    public abstract AttributeSet getAttributes();

    public abstract int getStartOffset();

    public abstract int getEndOffset();

    public abstract void next();

    public abstract boolean isValid();

    public abstract HTML.Tag getTag();
  }

  protected void fireChangedUpdate(DocumentEvent e) { super.fireChangedUpdate(e); }

  protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent e) { super.fireUndoableEditUpdate(e); }

  public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
    super.setParagraphAttributes(offset, length, s, replace);
  }

  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
    super.insertUpdate(chng, attr);
  }

  public java.awt.Font getFont(AttributeSet attr) { return getStyleSheet().getFont(attr); }

  public java.awt.Color getForeground(AttributeSet attr) { return getStyleSheet().getForeground(attr); }

  public java.awt.Color getBackground(AttributeSet attr) { return getStyleSheet().getBackground(attr); }
}
