import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class SwingHtmlEditTablesModern {
  static void dumpTree(Document d, Element e, int ind, StringBuilder sb) throws Exception {
    for (int i = 0; i < ind; i++) sb.append("  ");
    sb.append(e.getName()).append(" [").append(e.getStartOffset()).append(",").append(e.getEndOffset()).append(")");
    AttributeSet a = e.getAttributes();
    java.util.List<String> ks = new ArrayList<>();
    for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) { Object k = en.nextElement(); if (k == StyleConstants.NameAttribute) continue; ks.add(k + "=" + a.getAttribute(k)); }
    Collections.sort(ks);
    sb.append(" ").append(ks);
    if (e.isLeaf()) sb.append(" '").append(d.getText(e.getStartOffset(), Math.min(e.getEndOffset(), d.getLength() + 1) - e.getStartOffset()).replace("\n", "\\n")).append("'");
    sb.append("\n");
    for (int i = 0; i < e.getElementCount(); i++) dumpTree(d, e.getElement(i), ind + 1, sb);
  }
  static HTMLDocument mk(String html) throws Exception {
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
    kit.read(new StringReader(html), doc, 0);
    return doc;
  }
  interface Op { void run(HTMLDocument d) throws Exception; }
  static void run(String name, String html, Op op) {
    System.out.println("=== " + name);
    try { HTMLDocument d = mk(html); op.run(d); StringBuilder sb = new StringBuilder(); dumpTree(d, d.getDefaultRootElement(), 0, sb); System.out.print(sb); System.out.println("len=" + d.getLength()); }
    catch (Throwable t) { System.out.println("EXC " + t); }
  }
  static Element byId(HTMLDocument d, String id) { return d.getElement(id); }

  static void insertCases() {
    String base = "<html><body><p id='p1'>one</p><div id='d1'><p id='p2'>two</p><p id='p3'>three</p></div><ul id='u1'><li id='l1'>a</li><li id='l2'>b</li></ul></body></html>";
    run("inner d1", base, d -> d.setInnerHTML(byId(d, "d1"), "<p>new</p>"));
    run("inner d1 text", base, d -> d.setInnerHTML(byId(d, "d1"), "hello <b>bold</b>"));
    run("inner u1", base, d -> d.setInnerHTML(byId(d, "u1"), "<li>x</li><li>y</li><li>z</li>"));
    run("inner l1", base, d -> d.setInnerHTML(byId(d, "l1"), "<b>q</b>"));
    run("outer p2", base, d -> d.setOuterHTML(byId(d, "p2"), "<p>replaced</p>"));
    run("outer d1", base, d -> d.setOuterHTML(byId(d, "d1"), "<div><p>zz</p></div>"));
    run("outer l2", base, d -> d.setOuterHTML(byId(d, "l2"), "<li>B2</li>"));
    run("afterStart d1", base, d -> d.insertAfterStart(byId(d, "d1"), "<p>first</p>"));
    run("afterStart u1", base, d -> d.insertAfterStart(byId(d, "u1"), "<li>zero</li>"));
    run("beforeEnd d1", base, d -> d.insertBeforeEnd(byId(d, "d1"), "<p>last</p>"));
    run("beforeEnd u1", base, d -> d.insertBeforeEnd(byId(d, "u1"), "<li>end</li>"));
    run("beforeStart p2", base, d -> d.insertBeforeStart(byId(d, "p2"), "<p>before</p>"));
    run("beforeStart d1", base, d -> d.insertBeforeStart(byId(d, "d1"), "<p>b4</p>"));
    run("afterEnd p2", base, d -> d.insertAfterEnd(byId(d, "p2"), "<p>after</p>"));
    run("afterEnd d1", base, d -> d.insertAfterEnd(byId(d, "d1"), "<p>aft</p>"));
    run("afterEnd p1", base, d -> d.insertAfterEnd(byId(d, "p1"), "<p>aft1</p>"));
    run("afterEnd u1", base, d -> d.insertAfterEnd(byId(d, "u1"), "<p>endofbody</p>"));
    run("beforeEnd body text", base, d -> d.insertBeforeEnd(d.getDefaultRootElement().getElement(0), "tail <i>it</i>"));
    run("afterEnd leaf last", base, d -> { Element e = d.getCharacterElement(d.getLength()-1); d.insertAfterEnd(e, "<b>zz</b>"); });
    run("afterEnd leaf mid", base, d -> { Element e = d.getCharacterElement(2); d.insertAfterEnd(e, "<b>zz</b>"); });
    run("beforeStart leaf", base, d -> { Element e = d.getCharacterElement(2); d.insertBeforeStart(e, "<b>zz</b>"); });
    run("outer leaf", base, d -> { Element e = d.getCharacterElement(2); d.setOuterHTML(e, "<i>QQ</i>"); });
    run("outer leaf nl", base, d -> { Element e = d.getCharacterElement(2); d.setOuterHTML(e, "<i>QQ</i>x<p>pp</p>"); });
    run("inner body", base, d -> d.setInnerHTML(d.getDefaultRootElement().getElement(1), "<p>only</p>"));
    run("inner body text", base, d -> d.setInnerHTML(d.getDefaultRootElement().getElement(1), "just text"));
    run("inner nested", base, d -> d.setInnerHTML(byId(d, "d1"), "<div><ul><li>a</li><li>b</li></ul></div>"));
    run("inner table", base, d -> d.setInnerHTML(byId(d, "d1"), "<table><tr><td>a</td><td>b</td></tr></table>"));
    run("outer body child last", base, d -> d.setOuterHTML(byId(d, "u1"), "<p>newlast</p>"));
    run("outer p1 first", base, d -> d.setOuterHTML(byId(d, "p1"), "<h1>H</h1><h2>I</h2>"));
    run("beforeEnd p text", base, d -> d.insertBeforeEnd(byId(d, "p1"), "<b>more</b>"));
    run("afterStart p text", base, d -> d.insertAfterStart(byId(d, "p1"), "<b>pre</b>"));
    run("beforeEnd li", base, d -> d.insertBeforeEnd(byId(d, "l1"), "<p>qq</p>"));
    run("afterStart body", base, d -> d.insertAfterStart(d.getDefaultRootElement().getElement(1), "<p>top</p>"));
    run("beforeEnd body", base, d -> d.insertBeforeEnd(d.getDefaultRootElement().getElement(1), "<p>bottom</p>"));
    run("afterEnd head", base, d -> d.insertAfterEnd(d.getDefaultRootElement().getElement(0), "<p>x</p>"));
    run("kit insertHTML mid", base, d -> new HTMLEditorKit().insertHTML(d, 2, "<b>X</b>", 0, 0, null));
    run("kit insertHTML end", base, d -> new HTMLEditorKit().insertHTML(d, d.getLength(), "<p>z</p>", 0, 0, null));
    run("kit insertHTML table", base, d -> new HTMLEditorKit().insertHTML(d, 4, "<table><tr><td>c</td></tr></table>", 0, 0, null));
  }

  static void dumpViews(View v, Shape a, String ind) {
    Rectangle r = a == null ? new Rectangle() : a.getBounds();
    System.out.println(ind + v.getClass().getSimpleName() + " " + v.getStartOffset() + "-" + v.getEndOffset() + " @" + r.x + "," + r.y + " " + r.width + "x" + r.height + " pref=" + v.getPreferredSpan(0) + "x" + v.getPreferredSpan(1) + " min=" + v.getMinimumSpan(0) + "x" + v.getMinimumSpan(1));
    if (v instanceof FlowView || v.getViewCount() == 0) return;
    for (int i = 0; i < v.getViewCount(); i++) { Shape c = v.getChildAllocation(i, a); dumpViews(v.getView(i), c, ind + "  "); }
  }
  static void tableCase(String h, int w, boolean editable) throws Exception { tableCase(h, w, editable, true); }
  static void tableCase(String h, int w, boolean editable, boolean views) throws Exception {
    System.out.println("== " + h + " w=" + w);
    JEditorPane p = new JEditorPane("text/html", h);
    p.setEditable(editable);
    p.setBorder(null);
    if (w > 0) p.setSize(w, 400);
    p.getPreferredSize();
    SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    if (views) {
      System.out.println("pref " + p.getPreferredSize());
      if (w > 0) { p.setSize(w, 400); System.out.println("pref2 " + p.getPreferredSize()); }
    }
    if (w > 0) { p.setSize(w + 1, 400); p.setSize(w, 400); }
    if (views) dumpViews(p.getUI().getRootView(p), new Rectangle(0, 0, p.getWidth(), p.getHeight()), "");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i <= p.getDocument().getLength(); i++) { Rectangle r = p.modelToView(i); sb.append(i).append(":").append(r == null ? "null" : r.x + "," + r.y + "," + r.width + "," + r.height).append(" "); }
    System.out.println(sb);
  }
  static void tables() throws Exception {
    String lt = "lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor";
    String[] hs = {
     "<table border=1><tr><td width=80>a</td><td>b</td></tr></table>",
     "<table border=1><tr><td width=80>a</td><td width=30>b</td></tr><tr><td>c</td><td>dddd</td></tr></table>",
     "<table border=1><tr><td>"+lt+"</td><td>short</td></tr></table>",
     "<table border=1 width=150><tr><td>"+lt+"</td><td>short</td></tr></table>",
     "<table border=1><tr><td rowspan=2>"+lt+"</td><td>b</td></tr><tr><td>d</td></tr></table>",
     "<table border=1><tr><td rowspan=2>x<br>x<br>x<br>x<br>x</td><td>b</td></tr><tr><td>d</td></tr><tr><td>e</td><td>f</td></tr></table>",
     "<table border=1><tr><td valign=top>a<br>b<br>c</td><td valign=top>x</td><td valign=bottom>y</td><td>z</td></tr></table>",
     "<table border=1><tr><td nowrap>"+lt+"</td></tr></table>",
     "<table border=1 align=center><tr><td>a</td></tr></table>",
     "<center><table border=1><tr><td>a</td></tr></table></center>",
     "<table><tr><td>no border</td><td>x</td></tr></table>",
     "<table border=0 cellpadding=10><tr><td>a</td><td>b</td></tr></table>",
     "<table border=3 cellspacing=6><tr><td>a</td><td>b</td></tr><tr><td>c</td></tr></table>",
     "<table border=1><tr><td></td><td>b</td></tr><tr></tr><tr><td>c</td></tr></table>",
     "<table border=1 bgcolor=red><tr bgcolor=blue><td bgcolor=green>a</td></tr></table>",
     "<table border=1><tr><td colspan=3>top</td></tr><tr><td>1</td><td>2</td><td>3</td></tr><tr><td colspan=2>12</td><td rowspan=2>3x</td></tr><tr><td>a</td><td>b</td></tr></table>",
     "<p>before</p><table border=1><tr><td>a</td></tr></table><p>after</p>",
     "<table border=1><tr><td><ul><li>one<li>two</ul></td><td><p>para</p><p>para2</p></td></tr></table>",
     "<table width=100% border=1><tr><td>a</td><td>b</td><td>c</td></tr></table>",
     "<table width=50% border=1><tr><td>a</td><td>b</td></tr></table>",
     "<table border=1><tr><td width=50%>a</td><td width=50%>b</td></tr></table>",
     "<table border=1 width=400><tr><td width=100>a</td><td>b</td></tr></table>",
     "<table border=1><thead><tr><th>h</th></tr></thead><tbody><tr><td>b</td></tr></tbody></table>",
    };
    for (String h : hs) { if (!h.contains("100%")) tableCase(h, 0, false); tableCase(h, 300, false); tableCase(h, 100, false); }
    String[] hidden = {"<p>a<!-- c -->b</p>", "<p>a<foo>x</foo>b</p>", "<p>a<script>var x;</script>b</p>", "<p>a<applet code=x></applet>b</p>"};
    for (String h : hidden) { tableCase(h, 2000, false); tableCase(h, 2000, true, false); }
  }

  static void keymaps() {
    for (String p : new String[] {"TextField", "TextArea", "EditorPane", "TextPane", "PasswordField", "FormattedTextField"}) {
      InputMap m = (InputMap) UIManager.get(p + ".focusInputMap");
      java.util.List<String> l = new ArrayList<>();
      for (KeyStroke k : m.allKeys()) l.add(k + " -> " + m.get(k));
      Collections.sort(l);
      System.out.println(p + " " + l);
    }
    for (JTextComponent c : new JTextComponent[] {new JTextField(), new JTextArea(), new JEditorPane(), new JTextPane(), new JPasswordField(), new JFormattedTextField()}) {
      java.util.List<String> n = new ArrayList<>();
      for (Object o : c.getActionMap().allKeys()) n.add(o instanceof String ? (String) o : "<default>");
      Collections.sort(n);
      System.out.println(c.getClass().getSimpleName() + " " + n.size() + " " + n + " keymap=" + c.getKeymap().getName());
    }
  }

  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty("java.awt.headless", "true");
    insertCases();
    tables();
    keymaps();
    System.exit(0);
  }
}
