/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text.html;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class HTMLEditorKit extends javax.swing.text.StyledEditorKit implements javax.accessibility.Accessible {
  private static StyleSheet defaultStyles;
  private static Parser defaultParser;
  private static ViewFactory defaultFactory;
  private boolean bp_AutoFormSubmission = true;
  private java.awt.Cursor bp_DefaultCursor = java.awt.Cursor.getDefaultCursor();
  private java.awt.Cursor bp_LinkCursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR);
  private javax.swing.text.MutableAttributeSet bp_InputAttributes;
  private java.lang.String bp_ContentType;
  private javax.swing.Action[] bp_Actions;
  private javax.accessibility.AccessibleContext bp_AccessibleContext;
  public static final java.lang.String DEFAULT_CSS = "default.css";
  static final String DEFAULT_CSS_TEXT = ""
      + "a { color:blue; text-decoration:underline }\n"
      + "address { color:blue; font-style:italic }\n"
      + "b { font-weight:bold }\n"
      + "big { font-size:x-large }\n"
      + "blockquote { margin-bottom:5; margin-left:35; margin-right:35; margin-top:5 }\n"
      + "body { color:black; font-family:Serif; font-size:14pt; font-weight:normal; margin-left:0; margin-right:0 }\n"
      + "caption { caption-side:top; text-align:center }\n"
      + "center { text-align:center }\n"
      + "cite { font-style:italic }\n"
      + "code { font-family:Monospaced; font-size:small }\n"
      + "dd { margin-bottom:0; margin-left-ltr:40; margin-right-rtl:40; margin-top:0 }\n"
      + "dd p { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "dfn { font-style:italic }\n"
      + "dir { margin-bottom:10; margin-left-ltr:40; margin-right-rtl:40; margin-top:10 }\n"
      + "dir li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "dir li p { margin-bottom:0; margin-top:0 }\n"
      + "dl { margin-bottom:10; margin-left:0; margin-top:10 }\n"
      + "dt { margin-bottom:0; margin-top:0 }\n"
      + "em { font-style:italic }\n"
      + "h1 { font-size:x-large; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "h2 { font-size:large; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "h3 { font-size:medium; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "h4 { font-size:small; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "h5 { font-size:x-small; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "h6 { font-size:xx-small; font-weight:bold; margin-bottom:10; margin-top:10 }\n"
      + "i { font-style:italic }\n"
      + "kbd { font-family:Monospaced; font-size:small }\n"
      + "li p { margin-bottom:0; margin-top:0 }\n"
      + "menu { margin-bottom:10; margin-left-ltr:40; margin-right-rtl:40; margin-top:10 }\n"
      + "menu li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "menu li p { margin-bottom:0; margin-top:0 }\n"
      + "nobr { white-space:nowrap }\n"
      + "ol { list-style-type:decimal; margin-bottom:10; margin-left-ltr:50; margin-right-rtl:50; margin-top:10 }\n"
      + "ol li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "ol li p { margin-bottom:0; margin-top:0 }\n"
      + "p { margin-top:15 }\n"
      + "pre { font-family:Monospaced; margin-bottom:5; margin-top:5 }\n"
      + "pre p { margin-top:0 }\n"
      + "s { text-decoration:line-through }\n"
      + "samp { font-family:Monospaced; font-size:small }\n"
      + "small { font-size:x-small }\n"
      + "strike { text-decoration:line-through }\n"
      + "strong { font-weight:bold }\n"
      + "sub { vertical-align:sub }\n"
      + "sup { vertical-align:sup }\n"
      + "table { border-bottom-style:outset; border-bottom-width:0; border-left-style:outset; border-left-width:0; border-right-style:outset; border-right-width:0; border-top-style:outset; border-top-width:0 }\n"
      + "td { border-bottom-style:inset; border-bottom-width:0; border-left-style:inset; border-left-width:0; border-right-style:inset; border-right-width:0; border-top-style:inset; border-top-width:0; padding-bottom:3; padding-left:3; padding-right:3; padding-top:3 }\n"
      + "td p { margin-top:0 }\n"
      + "th { border-bottom-style:inset; border-bottom-width:0; border-left-style:inset; border-left-width:0; border-right-style:inset; border-right-width:0; border-top-style:inset; border-top-width:0; font-weight:bold; padding-bottom:3; padding-left:3; padding-right:3; padding-top:3; text-align:center }\n"
      + "tr { text-align:left }\n"
      + "tt { font-family:Monospaced }\n"
      + "u { text-decoration:underline }\n"
      + "ul { list-style-type:disc; margin-bottom:10; margin-left-ltr:50; margin-right-rtl:50; margin-top:10 }\n"
      + "ul li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "ul li menu { list-style-type:circle; margin-left-ltr:25; margin-right-rtl:25 }\n"
      + "ul li p { margin-bottom:0; margin-top:0 }\n"
      + "ul li ul { list-style-type:circle; margin-left-ltr:25; margin-right-rtl:25 }\n"
      + "ul li ul li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "ul li ul li ul { list-style-type:square; margin-left-ltr:25; margin-right-rtl:25 }\n"
      + "ul li ul li ul li { margin-bottom:0; margin-left:0; margin-right:0; margin-top:0 }\n"
      + "var { font-style:italic; font-weight:bold }\n";

  public static final java.lang.String BOLD_ACTION = "html-bold-action";
  public static final java.lang.String ITALIC_ACTION = "html-italic-action";
  public static final java.lang.String PARA_INDENT_LEFT = "html-para-indent-left";
  public static final java.lang.String PARA_INDENT_RIGHT = "html-para-indent-right";
  public static final java.lang.String FONT_CHANGE_BIGGER = "html-font-bigger";
  public static final java.lang.String FONT_CHANGE_SMALLER = "html-font-smaller";
  public static final java.lang.String COLOR_ACTION = "html-color-action";
  public static final java.lang.String LOGICAL_STYLE_ACTION = "html-logical-style-action";
  public static final java.lang.String IMG_ALIGN_TOP = "html-image-align-top";
  public static final java.lang.String IMG_ALIGN_MIDDLE = "html-image-align-middle";
  public static final java.lang.String IMG_ALIGN_BOTTOM = "html-image-align-bottom";
  public static final java.lang.String IMG_BORDER = "html-image-border";
  public HTMLEditorKit() {
  }
  public java.lang.Object clone() { return super.clone(); }
  public void write(java.io.Writer out, Document doc, int pos, int len) throws java.io.IOException, BadLocationException {
    if (doc instanceof HTMLDocument) {
      HTMLWriter w = new HTMLWriter(out, (HTMLDocument) doc, pos, len);
      w.write();
    } else if (doc instanceof javax.swing.text.StyledDocument) {
      MinimalHTMLWriter w = new MinimalHTMLWriter(out, (javax.swing.text.StyledDocument) doc, pos, len);
      w.write();
    } else {
      super.write(out, doc, pos, len);
    }
  }

  public void read(java.io.Reader in, Document doc, int pos) throws java.io.IOException, BadLocationException {
    if (doc instanceof HTMLDocument) {
      HTMLDocument hdoc = (HTMLDocument) doc;
      if (pos > doc.getLength()) {
        throw new BadLocationException("Invalid location", pos);
      }
      Parser p = getParser();
      if (p == null) {
        throw new java.io.IOException("Can't load parser");
      }
      ParserCallback receiver = hdoc.getReader(pos);
      Boolean ignoreCharset = (Boolean) doc.getProperty("IgnoreCharsetDirective");
      p.parse(in, receiver, ignoreCharset == null ? false : ignoreCharset.booleanValue());
      receiver.flush();
    } else {
      super.read(in, doc, pos);
    }
  }
  public StyleSheet getStyleSheet() {
    if (defaultStyles == null) {
      StyleSheet ss = new StyleSheet();
      ss.addRule(DEFAULT_CSS_TEXT);
      defaultStyles = ss;
    }
    return defaultStyles;
  }
  protected void createInputAttributes(javax.swing.text.Element element, javax.swing.text.MutableAttributeSet set) {
    set.removeAttributes(set);
    set.addAttributes(element.getAttributes());
    set.removeAttribute(StyleConstants.ComposedTextAttribute);
    Object o = set.getAttribute(StyleConstants.NameAttribute);
    if (o instanceof HTML.Tag) {
      HTML.Tag tag = (HTML.Tag) o;
      if (tag == HTML.Tag.IMG) {
        set.removeAttribute(HTML.Attribute.SRC);
        set.removeAttribute(HTML.Attribute.HEIGHT);
        set.removeAttribute(HTML.Attribute.WIDTH);
        set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
      } else if (tag instanceof HTML.UnknownTag || tag == HTML.Tag.BR) {
        set.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
      }
    }
  }
  private static final LinkController linkHandler = new LinkController();
  private javax.swing.JEditorPane theEditor;

  public void deinstall(javax.swing.JEditorPane c) {
    c.removeMouseListener(linkHandler);
    c.removeMouseMotionListener(linkHandler);
    super.deinstall(c);
    theEditor = null;
  }

  public void install(javax.swing.JEditorPane c) {
    c.addMouseListener(linkHandler);
    c.addMouseMotionListener(linkHandler);
    c.addCaretListener(navigateLink);
    super.install(c);
    theEditor = c;
  }

  private final NavigateLinkAction navigateLink = new NavigateLinkAction("next-link-action");
  private static final javax.swing.Action[] defaultActions = {
      new InsertHTMLTextAction("InsertTable", "<table border=1><tr><td></td></tr></table>", HTML.Tag.BODY,
          HTML.Tag.TABLE),
      new InsertHTMLTextAction("InsertTableRow", "<table border=1><tr><td></td></tr></table>", HTML.Tag.TABLE,
          HTML.Tag.TR, HTML.Tag.BODY, HTML.Tag.TABLE),
      new InsertHTMLTextAction("InsertTableDataCell", "<table border=1><tr><td></td></tr></table>", HTML.Tag.TR,
          HTML.Tag.TD, HTML.Tag.BODY, HTML.Tag.TABLE),
      new InsertHTMLTextAction("InsertUnorderedList", "<ul><li></li></ul>", HTML.Tag.BODY, HTML.Tag.UL),
      new InsertHTMLTextAction("InsertUnorderedListItem", "<ul><li></li></ul>", HTML.Tag.UL, HTML.Tag.LI,
          HTML.Tag.BODY, HTML.Tag.UL),
      new InsertHTMLTextAction("InsertOrderedList", "<ol><li></li></ol>", HTML.Tag.BODY, HTML.Tag.OL),
      new InsertHTMLTextAction("InsertOrderedListItem", "<ol><li></li></ol>", HTML.Tag.OL, HTML.Tag.LI,
          HTML.Tag.BODY, HTML.Tag.OL),
      new InsertHRAction(),
      new InsertHTMLTextAction("InsertPre", "<pre></pre>", HTML.Tag.BODY, HTML.Tag.PRE),
      new BeginAction(javax.swing.text.DefaultEditorKit.beginAction, false),
      new BeginAction(javax.swing.text.DefaultEditorKit.selectionBeginAction, true),
      new ActivateLinkAction("activate-link-action"),
      new NavigateLinkAction("next-link-action"),
      new NavigateLinkAction("previous-link-action")};
  public Document createDefaultDocument() {
    StyleSheet styles = getStyleSheet();
    StyleSheet ss = new StyleSheet();
    ss.addStyleSheet(styles);
    HTMLDocument doc = new HTMLDocument(ss);
    doc.setParser(getParser());
    doc.setAsynchronousLoadPriority(4);
    doc.setTokenThreshold(100);
    return doc;
  }
  public boolean isAutoFormSubmission() { return bp_AutoFormSubmission; }
  protected Parser getParser() {
    if (defaultParser == null) {
      defaultParser = new javax.swing.text.html.parser.ParserDelegator();
    }
    return defaultParser;
  }
  public void insertHTML(HTMLDocument doc, int offset, String html, int popDepth, int pushDepth, HTML.Tag insertTag)
      throws BadLocationException, java.io.IOException {
    if (offset > doc.getLength()) {
      throw new BadLocationException("Invalid offset", offset);
    }
    ParserCallback receiver = doc.getReader(offset, popDepth, pushDepth, insertTag);
    Boolean ignoreCharset = (Boolean) doc.getProperty("IgnoreCharsetDirective");
    getParser().parse(new java.io.StringReader(html), receiver, ignoreCharset != null && ignoreCharset.booleanValue());
    receiver.flush();
  }
  public void setStyleSheet(StyleSheet s) { defaultStyles = s; }
  public void setDefaultCursor(java.awt.Cursor p0) { bp_DefaultCursor = p0; }
  public void setLinkCursor(java.awt.Cursor p0) { bp_LinkCursor = p0; }
  public java.awt.Cursor getLinkCursor() { return bp_LinkCursor; }
  public void setAutoFormSubmission(boolean p0) { bp_AutoFormSubmission = p0; }
  public javax.swing.text.MutableAttributeSet getInputAttributes() {
    if (bp_InputAttributes == null) {
      bp_InputAttributes = new javax.swing.text.SimpleAttributeSet();
    }
    return bp_InputAttributes;
  }
  public ViewFactory getViewFactory() {
    if (defaultFactory == null) {
      defaultFactory = new HTMLFactory();
    }
    return defaultFactory;
  }
  public java.lang.String getContentType() { return "text/html"; }
  public javax.swing.Action[] getActions() {
    return javax.swing.text.TextAction.augmentList(super.getActions(), defaultActions);
  }
  public javax.accessibility.AccessibleContext getAccessibleContext() {
    if (theEditor == null) {
      return null;
    }
    if (bp_AccessibleContext == null) {
      AccessibleHTML a = new AccessibleHTML(theEditor);
      bp_AccessibleContext = a.getAccessibleContext();
    }
    return bp_AccessibleContext;
  }
  public java.awt.Cursor getDefaultCursor() { return bp_DefaultCursor; }

  public static class LinkController extends java.awt.event.MouseAdapter
      implements java.awt.event.MouseMotionListener, java.io.Serializable {
    private Element curElem;
    private boolean curElemImage;
    private String href;
    private int curOffset;

    public LinkController() {
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
      JEditorPane editor = (JEditorPane) e.getSource();
      if (!editor.isEditable() && editor.isEnabled() && javax.swing.SwingUtilities.isLeftMouseButton(e)) {
        java.awt.Point pt = new java.awt.Point(e.getX(), e.getY());
        int pos = editor.viewToModel(pt);
        if (pos >= 0) {
          activateLink(pos, editor, e);
        }
      }
    }

    public void mouseDragged(java.awt.event.MouseEvent e) { }

    public void mouseMoved(java.awt.event.MouseEvent e) {
      JEditorPane editor = (JEditorPane) e.getSource();
      if (!editor.isEnabled()) {
        return;
      }
      HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
      java.awt.Cursor newCursor = kit.getDefaultCursor();
      if (!editor.isEditable()) {
        java.awt.Point pt = new java.awt.Point(e.getX(), e.getY());
        javax.swing.text.Position.Bias[] bias = new javax.swing.text.Position.Bias[1];
        int pos = editor.getUI().viewToModel(editor, pt, bias);
        if (bias[0] == javax.swing.text.Position.Bias.Backward && pos > 0) {
          pos--;
        }
        if (pos >= 0 && editor.getDocument() instanceof HTMLDocument) {
          HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
          Element elem = hdoc.getCharacterElement(pos);
          if (curElem != elem || curOffset != pos) {
            curElem = elem;
            curOffset = pos;
            AttributeSet anchor = (AttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A);
            String newHref = anchor == null ? null : (String) anchor.getAttribute(HTML.Attribute.HREF);
            if (newHref != null) {
              newCursor = kit.getLinkCursor();
              if (!newHref.equals(href)) {
                fireEvent(editor, hdoc, HyperlinkEvent.EventType.EXITED, href, null, null);
                href = newHref;
                fireEvent(editor, hdoc, HyperlinkEvent.EventType.ENTERED, newHref, anchor, elem);
              }
            } else if (href != null) {
              fireEvent(editor, hdoc, HyperlinkEvent.EventType.EXITED, href, null, null);
              href = null;
            }
          } else if (href != null) {
            newCursor = kit.getLinkCursor();
          }
        }
      }
      if (editor.getCursor() != newCursor) {
        editor.setCursor(newCursor);
      }
    }

    private void fireEvent(JEditorPane editor, HTMLDocument hdoc, HyperlinkEvent.EventType type, String ref,
        AttributeSet anchor, Element elem) {
      if (ref == null) {
        return;
      }
      HyperlinkEvent ev = createHyperlinkEvent(editor, hdoc, ref, anchor, elem, null, type);
      if (ev != null) {
        editor.fireHyperlinkUpdate(ev);
      }
    }

    protected void activateLink(int pos, JEditorPane editor) { activateLink(pos, editor, null); }

    void activateLink(int pos, JEditorPane html, java.awt.event.MouseEvent mouseEvent) {
      Document doc = html.getDocument();
      if (doc instanceof HTMLDocument) {
        HTMLDocument hdoc = (HTMLDocument) doc;
        Element e = hdoc.getCharacterElement(pos);
        AttributeSet a = e.getAttributes();
        AttributeSet anchor = (AttributeSet) a.getAttribute(HTML.Tag.A);
        String ref = anchor == null ? null : (String) anchor.getAttribute(HTML.Attribute.HREF);
        if (ref != null) {
          HyperlinkEvent ev = createHyperlinkEvent(html, hdoc, ref, anchor, e, mouseEvent,
              HyperlinkEvent.EventType.ACTIVATED);
          if (ev != null) {
            html.fireHyperlinkUpdate(ev);
          }
        }
      }
    }

    static HyperlinkEvent createHyperlinkEvent(JEditorPane html, HTMLDocument hdoc, String href, AttributeSet anchor,
        Element element, java.awt.event.InputEvent inputEvent, HyperlinkEvent.EventType type) {
      URL u;
      try {
        URL base = hdoc.getBase();
        u = new URL(base, href);
        if ("file".equals(u.getProtocol()) && href.startsWith("#") && base != null) {
          String baseFile = base.getFile();
          String newFile = u.getFile();
          if (baseFile != null && newFile != null && !newFile.startsWith(baseFile)) {
            u = new URL(base, baseFile + href);
          }
        }
      } catch (MalformedURLException m) {
        u = null;
      }
      if (!hdoc.isFrameDocument()) {
        return new HyperlinkEvent(html, type, u, href, element, inputEvent);
      }
      String target = anchor == null ? null : (String) anchor.getAttribute(HTML.Attribute.TARGET);
      if (target == null || target.equals("")) {
        target = hdoc.getBaseTarget();
      }
      if (target == null) {
        target = "_self";
      }
      return new HTMLFrameHyperlinkEvent(html, type, u, href, element, inputEvent, target);
    }
  }

  public static abstract class Parser {
    protected Parser() {
    }
    public abstract void parse(java.io.Reader p0, javax.swing.text.html.HTMLEditorKit.ParserCallback p1, boolean p2) throws java.io.IOException;
  }

  public static class ParserCallback {
    public static final java.lang.Object IMPLIED = "_implied_";
    public ParserCallback() {
    }
    public void flush() throws BadLocationException { }
    public void handleText(char[] data, int pos) { }
    public void handleComment(char[] data, int pos) { }
    public void handleStartTag(HTML.Tag t, javax.swing.text.MutableAttributeSet a, int pos) { }
    public void handleEndTag(HTML.Tag t, int pos) { }
    public void handleSimpleTag(HTML.Tag t, javax.swing.text.MutableAttributeSet a, int pos) { }
    public void handleEndOfLineString(String eol) { }
    public void handleError(String errorMsg, int pos) { }
  }

  public static class HTMLFactory implements javax.swing.text.ViewFactory {
    public HTMLFactory() {
    }
    public View create(Element elem) {
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag) o;
        if (kind == HTML.Tag.CONTENT) {
          return new InlineView(elem);
        } else if (kind == HTML.Tag.IMPLIED) {
          Object ws = elem.getAttributes().getAttribute(CSS.Attribute.WHITE_SPACE);
          if (ws != null && ws.toString().equals("pre")) {
            return new LineView(elem);
          }
          return new ParagraphView(elem);
        } else if (kind == HTML.Tag.P || kind == HTML.Tag.H1 || kind == HTML.Tag.H2 || kind == HTML.Tag.H3
            || kind == HTML.Tag.H4 || kind == HTML.Tag.H5 || kind == HTML.Tag.H6 || kind == HTML.Tag.DT) {
          return new ParagraphView(elem);
        } else if (kind == HTML.Tag.MENU || kind == HTML.Tag.DIR || kind == HTML.Tag.UL || kind == HTML.Tag.OL) {
          return new ListView(elem);
        } else if (kind == HTML.Tag.BODY) {
          return new BodyBlockView(elem);
        } else if (kind == HTML.Tag.HTML) {
          return new BlockView(elem, View.Y_AXIS);
        } else if (kind == HTML.Tag.LI || kind == HTML.Tag.CENTER || kind == HTML.Tag.DL || kind == HTML.Tag.DD
            || kind == HTML.Tag.DIV || kind == HTML.Tag.BLOCKQUOTE || kind == HTML.Tag.PRE
            || kind == HTML.Tag.FORM || kind == HTML.Tag.TD || kind == HTML.Tag.TH || kind == HTML.Tag.CAPTION) {
          return new BlockView(elem, View.Y_AXIS);
        } else if (kind == HTML.Tag.NOFRAMES) {
          return new NoFramesView(elem, View.Y_AXIS);
        } else if (kind == HTML.Tag.IMG) {
          return new ImageView(elem);
        } else if (kind == HTML.Tag.HR) {
          return new HRuleView(elem);
        } else if (kind == HTML.Tag.BR) {
          return new BRView(elem);
        } else if (kind == HTML.Tag.TABLE) {
          return new TableView(elem);
        } else if (kind == HTML.Tag.INPUT || kind == HTML.Tag.SELECT || kind == HTML.Tag.TEXTAREA) {
          return new FormView(elem);
        } else if (kind == HTML.Tag.OBJECT) {
          return new ObjectView(elem);
        } else if (kind instanceof HTML.UnknownTag) {
          return new HiddenTagView(elem);
        } else if (kind == HTML.Tag.COMMENT) {
          return new CommentView(elem);
        } else if (kind == HTML.Tag.HEAD) {
          return new BlockView(elem, View.X_AXIS) {
            public float getPreferredSpan(int axis) { return 0; }

            public float getMinimumSpan(int axis) { return 0; }

            public float getMaximumSpan(int axis) { return 0; }

            protected void loadChildren(ViewFactory f) { }

            public java.awt.Shape modelToView(int pos, java.awt.Shape a, javax.swing.text.Position.Bias b) {
              return a;
            }

            public int getNextVisualPositionFrom(int pos, javax.swing.text.Position.Bias b, java.awt.Shape a,
                int direction, javax.swing.text.Position.Bias[] biasRet) {
              return getElement().getEndOffset();
            }
          };
        } else if (kind == HTML.Tag.TITLE || kind == HTML.Tag.META || kind == HTML.Tag.LINK
            || kind == HTML.Tag.STYLE || kind == HTML.Tag.SCRIPT || kind == HTML.Tag.AREA
            || kind == HTML.Tag.MAP || kind == HTML.Tag.PARAM || kind == HTML.Tag.APPLET) {
          return new HiddenTagView(elem);
        }
      }
      String nm = elem.getName();
      if (nm != null) {
        if (nm.equals(javax.swing.text.AbstractDocument.ContentElementName)) {
          return new javax.swing.text.LabelView(elem);
        } else if (nm.equals(javax.swing.text.AbstractDocument.ParagraphElementName)) {
          return new javax.swing.text.ParagraphView(elem);
        } else if (nm.equals(javax.swing.text.AbstractDocument.SectionElementName)) {
          return new javax.swing.text.BoxView(elem, View.Y_AXIS);
        } else if (nm.equals(StyleConstants.ComponentElementName)) {
          return new javax.swing.text.ComponentView(elem);
        } else if (nm.equals(StyleConstants.IconElementName)) {
          return new javax.swing.text.IconView(elem);
        }
      }
      return new javax.swing.text.LabelView(elem);
    }
  }

  public static class InsertHTMLTextAction extends HTMLTextAction {
    protected String html;
    protected HTML.Tag parentTag;
    protected HTML.Tag addTag;
    protected HTML.Tag alternateParentTag;
    protected HTML.Tag alternateAddTag;
    boolean adjustSelection;

    public InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag) {
      this(name, html, parentTag, addTag, null, null);
    }

    public InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag,
        HTML.Tag alternateParentTag, HTML.Tag alternateAddTag) {
      this(name, html, parentTag, addTag, alternateParentTag, alternateAddTag, true);
    }

    InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag, HTML.Tag alternateParentTag,
        HTML.Tag alternateAddTag, boolean adjustSelection) {
      super(name);
      this.html = html;
      this.parentTag = parentTag;
      this.addTag = addTag;
      this.alternateParentTag = alternateParentTag;
      this.alternateAddTag = alternateAddTag;
      this.adjustSelection = adjustSelection;
    }

    protected void insertHTML(JEditorPane editor, HTMLDocument doc, int offset, String html, int popDepth,
        int pushDepth, HTML.Tag addTag) {
      try {
        getHTMLEditorKit(editor).insertHTML(doc, offset, html, popDepth, pushDepth, addTag);
      } catch (java.io.IOException ioe) {
        throw new RuntimeException("Unable to insert: " + ioe);
      } catch (BadLocationException ble) {
        throw new RuntimeException("Unable to insert: " + ble);
      }
    }

    protected void insertAtBoundary(JEditorPane editor, HTMLDocument doc, int offset, Element insertElement,
        String html, HTML.Tag parentTag, HTML.Tag addTag) {
      insertAtBoundry(editor, doc, offset, insertElement, html, parentTag, addTag);
    }

    protected void insertAtBoundry(JEditorPane editor, HTMLDocument doc, int offset, Element insertElement,
        String html, HTML.Tag parentTag, HTML.Tag addTag) {
      boolean isFirst = offset == 0;
      Element e;
      Element commonParent;
      if (offset > 0 || insertElement == null) {
        e = doc.getDefaultRootElement();
        while (e != null && e.getStartOffset() != offset && !e.isLeaf()) {
          e = e.getElement(e.getElementIndex(offset));
        }
        commonParent = e != null ? e.getParentElement() : null;
      } else {
        commonParent = insertElement;
      }
      if (commonParent != null) {
        int pops = 0;
        int pushes = 0;
        if (isFirst && insertElement != null) {
          e = commonParent;
          while (e != null && !e.isLeaf()) {
            e = e.getElement(e.getElementIndex(offset));
            pops++;
          }
        } else {
          e = commonParent;
          offset--;
          while (e != null && !e.isLeaf()) {
            e = e.getElement(e.getElementIndex(offset));
            pops++;
          }
          e = commonParent;
          offset++;
          while (e != null && e != insertElement) {
            e = e.getElement(e.getElementIndex(offset));
            pushes++;
          }
        }
        pops = Math.max(0, pops - 1);
        insertHTML(editor, doc, offset, html, pops, pushes, addTag);
      }
    }

    boolean insertIntoTag(JEditorPane editor, HTMLDocument doc, int offset, HTML.Tag tag, HTML.Tag addTag) {
      Element e = findElementMatchingTag(doc, offset, tag);
      if (e != null && e.getStartOffset() == offset) {
        insertAtBoundary(editor, doc, offset, e, html, tag, addTag);
        return true;
      } else if (offset > 0) {
        int depth = elementCountToTag(doc, offset - 1, tag);
        if (depth != -1) {
          insertHTML(editor, doc, offset, html, depth, 0, addTag);
          return true;
        }
      }
      return false;
    }

    void adjustSelection(JEditorPane pane, HTMLDocument doc, int startOffset, int oldLength) {
      int newLength = doc.getLength();
      if (newLength != oldLength && startOffset < newLength) {
        if (startOffset > 0) {
          String text;
          try {
            text = doc.getText(startOffset - 1, 1);
          } catch (BadLocationException ble) {
            text = null;
          }
          if (text != null && text.length() > 0 && text.charAt(0) == '\n') {
            pane.select(startOffset, startOffset);
          } else {
            pane.select(startOffset + 1, startOffset + 1);
          }
        } else {
          pane.select(1, 1);
        }
      }
    }

    public void actionPerformed(ActionEvent ae) {
      JEditorPane editor = getEditor(ae);
      if (editor != null) {
        HTMLDocument doc = getHTMLDocument(editor);
        int offset = editor.getSelectionStart();
        int length = doc.getLength();
        boolean inserted;
        if (!insertIntoTag(editor, doc, offset, parentTag, addTag) && alternateParentTag != null) {
          inserted = insertIntoTag(editor, doc, offset, alternateParentTag, alternateAddTag);
        } else {
          inserted = true;
        }
        if (adjustSelection && inserted) {
          adjustSelection(editor, doc, offset, length);
        }
      }
    }
  }

  public static abstract class HTMLTextAction extends javax.swing.text.StyledEditorKit.StyledTextAction {
    public HTMLTextAction(String name) {
      super(name);
    }

    protected HTMLDocument getHTMLDocument(JEditorPane e) {
      Document d = e.getDocument();
      if (d instanceof HTMLDocument) {
        return (HTMLDocument) d;
      }
      throw new IllegalArgumentException("document must be HTMLDocument");
    }

    protected HTMLEditorKit getHTMLEditorKit(JEditorPane e) {
      javax.swing.text.EditorKit k = e.getEditorKit();
      if (k instanceof HTMLEditorKit) {
        return (HTMLEditorKit) k;
      }
      throw new IllegalArgumentException("EditorKit must be HTMLEditorKit");
    }

    protected Element[] getElementsAt(HTMLDocument doc, int offset) {
      return getElementsAt(doc.getDefaultRootElement(), offset, 0);
    }

    private Element[] getElementsAt(Element parent, int offset, int depth) {
      if (parent.isLeaf()) {
        Element[] retValue = new Element[depth + 1];
        retValue[depth] = parent;
        return retValue;
      }
      Element[] retValue = getElementsAt(parent.getElement(parent.getElementIndex(offset)), offset, depth + 1);
      retValue[depth] = parent;
      return retValue;
    }

    protected int elementCountToTag(HTMLDocument doc, int offset, HTML.Tag tag) {
      int depth = -1;
      Element e = doc.getCharacterElement(offset);
      while (e != null && e.getAttributes().getAttribute(StyleConstants.NameAttribute) != tag) {
        e = e.getParentElement();
        depth++;
      }
      if (e == null) {
        return -1;
      }
      return depth;
    }

    protected Element findElementMatchingTag(HTMLDocument doc, int offset, HTML.Tag tag) {
      Element e = doc.getDefaultRootElement();
      Element lastMatch = null;
      while (e != null) {
        if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag) {
          lastMatch = e;
        }
        e = e.getElement(e.getElementIndex(offset));
      }
      return lastMatch;
    }
  }

  static class InsertHRAction extends InsertHTMLTextAction {
    InsertHRAction() {
      super("InsertHR", "<hr>", null, HTML.Tag.IMPLIED, null, null, false);
    }

    public void actionPerformed(ActionEvent ae) {
      JEditorPane editor = getEditor(ae);
      if (editor != null) {
        HTMLDocument doc = getHTMLDocument(editor);
        int offset = editor.getSelectionStart();
        Element paragraph = doc.getParagraphElement(offset);
        if (paragraph.getParentElement() != null) {
          Element parent = paragraph.getParentElement();
          int pops = 0;
          int pushes = 0;
          if (offset > 0 && offset == paragraph.getStartOffset()) {
            insertAtBoundary(editor, doc, offset, parent, html, null, HTML.Tag.IMPLIED);
            return;
          }
          Element e = paragraph;
          while (e != null && e != parent) {
            e = e.getParentElement();
            pops++;
          }
          pops = Math.max(0, pops - 1) + (offset > paragraph.getStartOffset() ? 0 : 0);
          insertHTML(editor, doc, offset, html, pops, pushes, HTML.Tag.HR);
        }
      }
    }
  }

  static class BeginAction extends javax.swing.text.TextAction {
    private final boolean select;

    BeginAction(String nm, boolean select) {
      super(nm);
      this.select = select;
    }

    public void actionPerformed(ActionEvent e) {
      javax.swing.text.JTextComponent target = getTextComponent(e);
      if (target != null) {
        int pos = 0;
        if (target.getDocument() instanceof HTMLDocument) {
          Element root = target.getDocument().getDefaultRootElement();
          for (int i = 0; i < root.getElementCount(); i++) {
            if (root.getElement(i).getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY) {
              pos = root.getElement(i).getStartOffset();
              break;
            }
          }
        }
        if (select) {
          target.moveCaretPosition(pos);
        } else {
          target.setCaretPosition(pos);
        }
      }
    }
  }

  static class NavigateLinkAction extends javax.swing.text.TextAction implements javax.swing.event.CaretListener {
    private final boolean focusBack;
    private int focusedIndex = -1;

    NavigateLinkAction(String name) {
      super(name);
      focusBack = "previous-link-action".equals(name);
    }

    public void caretUpdate(javax.swing.event.CaretEvent e) {
      Object src = e.getSource();
      if (src instanceof javax.swing.text.JTextComponent) {
        Document d = ((javax.swing.text.JTextComponent) src).getDocument();
        if (d instanceof HTMLDocument && e.getDot() == e.getMark()) {
          focusedIndex = -1;
        }
      }
    }

    private java.util.List<Element> links(HTMLDocument doc) {
      java.util.List<Element> out = new java.util.ArrayList<Element>();
      collect(doc.getDefaultRootElement(), out);
      return out;
    }

    private void collect(Element e, java.util.List<Element> out) {
      if (e.isLeaf()) {
        if (e.getAttributes().getAttribute(HTML.Tag.A) != null) {
          if (out.isEmpty() || out.get(out.size() - 1).getAttributes().getAttribute(HTML.Tag.A)
              != e.getAttributes().getAttribute(HTML.Tag.A)) {
            out.add(e);
          }
        }
        return;
      }
      for (int i = 0; i < e.getElementCount(); i++) {
        collect(e.getElement(i), out);
      }
    }

    public void actionPerformed(ActionEvent e) {
      javax.swing.text.JTextComponent c = getTextComponent(e);
      if (c == null || c.isEditable() || !(c.getDocument() instanceof HTMLDocument)) {
        return;
      }
      java.util.List<Element> links = links((HTMLDocument) c.getDocument());
      if (links.isEmpty()) {
        return;
      }
      int caret = c.getSelectionStart();
      int target = -1;
      if (focusBack) {
        for (int i = links.size() - 1; i >= 0; i--) {
          if (links.get(i).getEndOffset() <= caret || (c.getSelectionStart() == c.getSelectionEnd()
              && links.get(i).getStartOffset() < caret)) {
            target = i;
            break;
          }
        }
        if (target < 0) {
          target = links.size() - 1;
        }
      } else {
        int from = c.getSelectionEnd();
        for (int i = 0; i < links.size(); i++) {
          if (links.get(i).getStartOffset() >= from && links.get(i).getStartOffset() != c.getSelectionStart()
              || links.get(i).getStartOffset() > c.getSelectionStart()) {
            target = i;
            break;
          }
        }
        if (target < 0) {
          target = 0;
        }
      }
      Element link = links.get(target);
      c.select(link.getStartOffset(), link.getEndOffset());
    }
  }

  static class ActivateLinkAction extends javax.swing.text.TextAction {
    ActivateLinkAction(String name) {
      super(name);
    }

    public void actionPerformed(ActionEvent e) {
      javax.swing.text.JTextComponent c = getTextComponent(e);
      if (c instanceof JEditorPane && !c.isEditable() && c.getDocument() instanceof HTMLDocument) {
        JEditorPane editor = (JEditorPane) c;
        int pos = editor.getSelectionStart();
        if (pos < editor.getDocument().getLength() || pos == 0) {
          new LinkController().activateLink(pos, editor, null);
        }
      }
    }
  }
}
