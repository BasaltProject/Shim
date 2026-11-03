/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;

public class JEditorPane extends JTextComponent {
  public static final String W3C_LENGTH_UNITS = "JEditorPane.w3cLengthUnits";
  public static final String HONOR_DISPLAY_PROPERTIES = "JEditorPane.honorDisplayProperties";

  private static final Hashtable<String, String> kTypeRegistry = new Hashtable<String, String>(3);
  private static final Hashtable<String, ClassLoader> kLoaderRegistry = new Hashtable<String, ClassLoader>(3);
  private static final Hashtable<String, EditorKit> kKitRegistry = new Hashtable<String, EditorKit>(3);
  private static final Hashtable<String, String> kDefaultEditorKitMap = new Hashtable<String, String>(0);

  static {
    kDefaultEditorKitMap.put("text/plain", "javax.swing.JEditorPane$PlainEditorKit");
    kDefaultEditorKitMap.put("text/html", "javax.swing.text.html.HTMLEditorKit");
    kDefaultEditorKitMap.put("text/rtf", "javax.swing.text.rtf.RTFEditorKit");
    kDefaultEditorKitMap.put("application/rtf", "javax.swing.text.rtf.RTFEditorKit");
    kDefaultEditorKitMap.put("text/richtext", "javax.swing.text.rtf.RTFEditorKit");
  }

  private AccessibleContext jepAccessible;
  private EditorKit kit;
  private boolean isUserSetEditorKit;
  private URL pageUrl;
  private Hashtable<String, EditorKit> typeHandlers;

  public JEditorPane() {
    super();
    setFocusCycleRoot(true);
    setEditorKit(createDefaultEditorKit());
    isUserSetEditorKit = false;
  }

  public JEditorPane(URL initialPage) throws IOException {
    this();
    setPage(initialPage);
  }

  public JEditorPane(String url) throws IOException {
    this();
    setPage(url);
  }

  public JEditorPane(String type, String text) {
    this();
    setContentType(type);
    setText(text);
  }

  public synchronized void addHyperlinkListener(HyperlinkListener listener) {
    listenerList.add(HyperlinkListener.class, listener);
  }

  public synchronized void removeHyperlinkListener(HyperlinkListener listener) {
    listenerList.remove(HyperlinkListener.class, listener);
  }

  public synchronized HyperlinkListener[] getHyperlinkListeners() {
    return listenerList.getListeners(HyperlinkListener.class);
  }

  public void fireHyperlinkUpdate(HyperlinkEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == HyperlinkListener.class) {
        ((HyperlinkListener) listeners[i + 1]).hyperlinkUpdate(e);
      }
    }
  }

  public void setPage(URL page) throws IOException {
    if (page == null) {
      throw new IOException("invalid url");
    }
    URL old = pageUrl;
    InputStream in = getStream(page);
    EditorKit kit = getEditorKit();
    Document doc = kit.createDefaultDocument();
    doc.putProperty(Document.StreamDescriptionProperty, page);
    setDocument(doc);
    try {
      read(in, doc);
    } finally {
      in.close();
    }
    pageUrl = page;
    firePropertyChange("page", old, page);
  }

  private void read(InputStream in, Document doc) throws IOException {
    String charset = (String) getClientProperty("charset");
    java.io.Reader r = (charset != null) ? new InputStreamReader(in, charset) : new InputStreamReader(in);
    try {
      kit.read(r, doc, 0);
    } catch (BadLocationException e) {
      throw new IOException(e.getMessage());
    }
  }

  public void read(InputStream in, Object desc) throws IOException {
    EditorKit kit = getEditorKit();
    Document doc = kit.createDefaultDocument();
    doc.putProperty(Document.StreamDescriptionProperty, desc);
    setDocument(doc);
    read(in, doc);
  }

  protected InputStream getStream(URL page) throws IOException {
    return page.openStream();
  }

  public void scrollToReference(String reference) { }

  public URL getPage() { return pageUrl; }

  public void setPage(String url) throws IOException {
    if (url == null) {
      throw new IOException("invalid url");
    }
    URL page = new URL(url);
    setPage(page);
  }

  public String getUIClassID() { return "EditorPaneUI"; }

  protected EditorKit createDefaultEditorKit() { return new PlainEditorKit(); }

  public EditorKit getEditorKit() {
    if (kit == null) {
      kit = createDefaultEditorKit();
      isUserSetEditorKit = false;
    }
    return kit;
  }

  public final String getContentType() { return (kit != null) ? kit.getContentType() : null; }

  public final void setContentType(String type) {
    int parm = type.indexOf(';');
    if (parm > -1) {
      String paramString = type.substring(parm);
      type = type.substring(0, parm).trim();
      if (paramString.toLowerCase().indexOf("charset=") > -1) {
        int idx = paramString.toLowerCase().indexOf("charset=") + 8;
        String cs = paramString.substring(idx).trim();
        int end = cs.indexOf(';');
        putClientProperty("charset", end > -1 ? cs.substring(0, end).trim() : cs);
      }
    }
    if ((kit == null) || (!type.equals(kit.getContentType())) || !isUserSetEditorKit) {
      EditorKit k = getEditorKitForContentType(type);
      if (k != null && k != kit) {
        setEditorKit(k);
        isUserSetEditorKit = false;
      }
    }
  }

  public void setEditorKit(EditorKit kit) {
    EditorKit old = this.kit;
    isUserSetEditorKit = true;
    if (old != null) {
      old.deinstall(this);
    }
    this.kit = kit;
    if (this.kit != null) {
      this.kit.install(this);
      setDocument(this.kit.createDefaultDocument());
    }
    firePropertyChange("editorKit", old, kit);
  }

  public EditorKit getEditorKitForContentType(String type) {
    if (typeHandlers == null) {
      typeHandlers = new Hashtable<String, EditorKit>(3);
    }
    EditorKit k = typeHandlers.get(type);
    if (k == null) {
      k = createEditorKitForContentType(type);
      if (k != null) {
        setEditorKitForContentType(type, k);
      }
    }
    if (k == null) {
      k = createDefaultEditorKit();
    }
    return k;
  }

  public void setEditorKitForContentType(String type, EditorKit k) {
    if (typeHandlers == null) {
      typeHandlers = new Hashtable<String, EditorKit>(3);
    }
    typeHandlers.put(type, k);
  }

  public void replaceSelection(String content) {
    if (!isEditable()) {
      UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
      return;
    }
    EditorKit kit = getEditorKit();
    if (kit instanceof StyledEditorKit) {
      try {
        Document doc = getDocument();
        Caret caret = getCaret();
        int p0 = Math.min(caret.getDot(), caret.getMark());
        int p1 = Math.max(caret.getDot(), caret.getMark());
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).replace(p0, p1 - p0, content, ((StyledEditorKit) kit).getInputAttributes());
        } else {
          if (p0 != p1) {
            doc.remove(p0, p1 - p0);
          }
          if (content != null && content.length() > 0) {
            doc.insertString(p0, content, ((StyledEditorKit) kit).getInputAttributes());
          }
        }
      } catch (BadLocationException e) {
        UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
      }
    } else {
      super.replaceSelection(content);
    }
  }

  public static EditorKit createEditorKitForContentType(String type) {
    EditorKit k = kKitRegistry.get(type);
    if (k == null) {
      String classname = getEditorKitClassNameForContentType(type);
      ClassLoader loader = kLoaderRegistry.get(type);
      try {
        Class<?> c;
        if (loader != null) {
          c = loader.loadClass(classname);
        } else {
          c = Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
        }
        k = (EditorKit) c.getDeclaredConstructor().newInstance();
        kKitRegistry.put(type, k);
      } catch (Throwable e) {
        k = null;
      }
    }
    if (k != null) {
      return (EditorKit) k.clone();
    }
    return null;
  }

  public static void registerEditorKitForContentType(String type, String classname) {
    registerEditorKitForContentType(type, classname, Thread.currentThread().getContextClassLoader());
  }

  public static void registerEditorKitForContentType(String type, String classname, ClassLoader loader) {
    kTypeRegistry.put(type, classname);
    if (loader != null) {
      kLoaderRegistry.put(type, loader);
    } else {
      kLoaderRegistry.remove(type);
    }
    kKitRegistry.remove(type);
  }

  public static String getEditorKitClassNameForContentType(String type) {
    String name = kTypeRegistry.get(type);
    return name != null ? name : kDefaultEditorKitMap.get(type);
  }

  public Dimension getPreferredSize() { return super.getPreferredSize(); }

  public void setText(String t) {
    try {
      Document doc = getDocument();
      doc.remove(0, doc.getLength());
      if (t == null || t.equals("")) {
        return;
      }
      java.io.Reader r = new StringReader(t);
      EditorKit kit = getEditorKit();
      kit.read(r, doc, 0);
    } catch (IOException ioe) {
      UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
    } catch (BadLocationException ble) {
      UIManager.getLookAndFeel().provideErrorFeedback(JEditorPane.this);
    }
  }

  public String getText() {
    String txt;
    try {
      StringWriter buf = new StringWriter();
      Document doc = getDocument();
      getEditorKit().write(buf, doc, 0, doc.getLength());
      txt = buf.toString();
    } catch (IOException ioe) {
      txt = null;
    } catch (BadLocationException ble) {
      txt = null;
    }
    return txt;
  }

  public boolean getScrollableTracksViewportWidth() {
    if (getParent() instanceof JViewport) {
      JViewport port = (JViewport) getParent();
      int w = port.getWidth();
      Dimension min = getMinimumSize();
      Dimension max = getMaximumSize();
      if ((w >= min.width) && (w <= max.width)) {
        return true;
      }
    }
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    if (getParent() instanceof JViewport) {
      JViewport port = (JViewport) getParent();
      int h = port.getHeight();
      Dimension min = getMinimumSize();
      if (h >= min.height) {
        Dimension max = getMaximumSize();
        if (h <= max.height) {
          return true;
        }
      }
    }
    return false;
  }

  protected String paramString() {
    String kitString = (kit != null ? kit.toString() : "");
    String pageString = (pageUrl != null ? pageUrl.toString() : "");
    return super.paramString() + ",kit=" + kitString + ",page=" + pageString;
  }

  public AccessibleContext getAccessibleContext() {
    if (jepAccessible == null) {
      if (getEditorKit() instanceof javax.swing.text.html.HTMLEditorKit) {
        jepAccessible = new AccessibleJEditorPaneHTML();
      } else {
        jepAccessible = new AccessibleJEditorPane();
      }
    }
    return jepAccessible;
  }

  protected class AccessibleJEditorPaneHTML extends AccessibleJEditorPane {
    private final AccessibleContext accessibleContext;

    public javax.accessibility.AccessibleText getAccessibleText() { return new JEditorPaneAccessibleHypertextSupport(); }

    protected AccessibleJEditorPaneHTML() {
      javax.swing.text.html.HTMLEditorKit kit = (javax.swing.text.html.HTMLEditorKit) JEditorPane.this.getEditorKit();
      accessibleContext = kit.getAccessibleContext();
    }

    public int getAccessibleChildrenCount() {
      return accessibleContext != null ? accessibleContext.getAccessibleChildrenCount() : 0;
    }

    public javax.accessibility.Accessible getAccessibleChild(int i) {
      return accessibleContext != null ? accessibleContext.getAccessibleChild(i) : null;
    }

    public javax.accessibility.Accessible getAccessibleAt(java.awt.Point p) {
      if (accessibleContext != null && p != null) {
        try {
          javax.accessibility.AccessibleComponent acomp = accessibleContext.getAccessibleComponent();
          return acomp != null ? acomp.getAccessibleAt(p) : null;
        } catch (java.awt.IllegalComponentStateException e) {
          return null;
        }
      }
      return null;
    }
  }

  protected class JEditorPaneAccessibleHypertextSupport extends AccessibleJEditorPane
      implements javax.accessibility.AccessibleHypertext {
    private final java.util.List<HTMLLink> links = new java.util.ArrayList<HTMLLink>();

    public class HTMLLink extends javax.accessibility.AccessibleHyperlink {
      private final javax.swing.text.Element element;

      public HTMLLink(javax.swing.text.Element e) { element = e; }

      public boolean isValid() { return JEditorPaneAccessibleHypertextSupport.this.linksValid; }

      public int getAccessibleActionCount() { return 1; }

      public boolean doAccessibleAction(int i) {
        if (i == 0 && isValid()) {
          javax.swing.text.AttributeSet as = element.getAttributes();
          javax.swing.text.AttributeSet anchor = (javax.swing.text.AttributeSet) as.getAttribute(javax.swing.text.html.HTML.Tag.A);
          String href = anchor != null ? (String) anchor.getAttribute(javax.swing.text.html.HTML.Attribute.HREF) : null;
          if (href != null) {
            java.net.URL u;
            try {
              u = new java.net.URL(((javax.swing.text.html.HTMLDocument) getDocument()).getBase(), href);
            } catch (java.net.MalformedURLException m) {
              u = null;
            }
            if (u != null) {
              fireHyperlinkUpdate(new javax.swing.event.HyperlinkEvent(JEditorPane.this,
                  javax.swing.event.HyperlinkEvent.EventType.ACTIVATED, u, href, element));
              return true;
            }
          }
        }
        return false;
      }

      public String getAccessibleActionDescription(int i) {
        if (i == 0 && isValid()) {
          Document d = JEditorPane.this.getDocument();
          if (d != null) {
            try {
              return d.getText(getStartIndex(), getEndIndex() - getStartIndex());
            } catch (javax.swing.text.BadLocationException exception) {
              return null;
            }
          }
        }
        return null;
      }

      public Object getAccessibleActionObject(int i) {
        if (i == 0 && isValid()) {
          javax.swing.text.AttributeSet as = element.getAttributes();
          javax.swing.text.AttributeSet anchor = (javax.swing.text.AttributeSet) as.getAttribute(javax.swing.text.html.HTML.Tag.A);
          String href = anchor != null ? (String) anchor.getAttribute(javax.swing.text.html.HTML.Attribute.HREF) : null;
          if (href != null) {
            try {
              return new java.net.URL(((javax.swing.text.html.HTMLDocument) getDocument()).getBase(), href);
            } catch (java.net.MalformedURLException m) {
              return null;
            }
          }
        }
        return null;
      }

      public Object getAccessibleActionAnchor(int i) { return getAccessibleActionDescription(i); }

      public int getStartIndex() { return element.getStartOffset(); }

      public int getEndIndex() { return element.getEndOffset(); }
    }

    private boolean linksValid = true;

    private void loadLinks() {
      links.clear();
      Document d = JEditorPane.this.getDocument();
      if (d instanceof javax.swing.text.html.HTMLDocument) {
        javax.swing.text.html.HTMLDocument hd = (javax.swing.text.html.HTMLDocument) d;
        for (javax.swing.text.html.HTMLDocument.Iterator it = hd.getIterator(javax.swing.text.html.HTML.Tag.A);
            it != null && it.isValid(); it.next()) {
          javax.swing.text.Element e = hd.getCharacterElement(it.getStartOffset());
          links.add(new HTMLLink(e));
        }
      }
    }

    public int getLinkCount() {
      loadLinks();
      return links.size();
    }

    public int getLinkIndex(int charIndex) {
      loadLinks();
      for (int i = 0; i < links.size(); i++) {
        HTMLLink l = links.get(i);
        if (l.getStartIndex() <= charIndex && charIndex < l.getEndIndex()) {
          return i;
        }
      }
      return -1;
    }

    public javax.accessibility.AccessibleHyperlink getLink(int linkIndex) {
      loadLinks();
      return linkIndex >= 0 && linkIndex < links.size() ? links.get(linkIndex) : null;
    }

    public String getLinkText(int linkIndex) {
      loadLinks();
      if (linkIndex < 0 || linkIndex >= links.size()) {
        return null;
      }
      return links.get(linkIndex).getAccessibleActionDescription(0);
    }
  }

  protected class AccessibleJEditorPane extends AccessibleJTextComponent {
    public String getAccessibleDescription() {
      String description = accessibleDescription;
      if (description == null) {
        description = (String) getClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY);
      }
      if (description == null) {
        description = JEditorPane.this.getContentType();
      }
      return description;
    }

    public javax.accessibility.AccessibleStateSet getAccessibleStateSet() {
      javax.accessibility.AccessibleStateSet states = super.getAccessibleStateSet();
      states.add(javax.accessibility.AccessibleState.MULTI_LINE);
      return states;
    }
  }

  static class PlainEditorKit extends DefaultEditorKit implements javax.swing.text.ViewFactory {
    public PlainEditorKit() { }

    public javax.swing.text.ViewFactory getViewFactory() { return this; }

    public javax.swing.text.View create(javax.swing.text.Element elem) {
      return new javax.swing.text.WrappedPlainView(elem);
    }
  }
}
