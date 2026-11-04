import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.rtf.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.*;

public class SwingHtmlRtfTextModern {

  static void htmlTree(Element e, String ind, StringBuilder sb) {
    sb.append(ind).append(e.getName()).append("[").append(e.getStartOffset()).append(",").append(e.getEndOffset()).append("]");
    AttributeSet a = e.getAttributes(); List<String> at = new ArrayList<>();
    for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) { Object k = en.nextElement(); if (k == StyleConstants.NameAttribute) continue; at.add(k + "=" + a.getAttribute(k)); }
    Collections.sort(at); sb.append(" ").append(at).append("\n");
    for (int i = 0; i < e.getElementCount(); i++) htmlTree(e.getElement(i), ind + "  ", sb);
  }
  static void dump(String html) throws Exception {
    JEditorPane p = new JEditorPane("text/html", html);
    HTMLDocument d = (HTMLDocument) p.getDocument();
    StringBuilder sb = new StringBuilder("== " + html.replace("\n", "\\n") + "\n"); htmlTree(d.getDefaultRootElement(), "", sb);
    sb.append("text=[").append(d.getText(0, d.getLength()).replace("\n", "\\n")).append("]\n");
    for (int w : new int[]{0, 300, 120, 60}) { p.setSize(w, 400); Dimension pr = p.getPreferredSize(); sb.append("w" + w + " pref=" + pr.width + "x" + pr.height + " min=" + p.getMinimumSize().width + "x" + p.getMinimumSize().height + "\n"); }
    p.setSize(300, 400);
    StringBuilder r = new StringBuilder("rects:");
    for (int i = 0; i <= d.getLength() && i < 60; i += 3) { Rectangle rr = p.modelToView(i); r.append(" ").append(i).append("=").append(rr == null ? "null" : rr.x + "," + rr.y + "," + rr.height); }
    sb.append(r).append("\n");
    r = new StringBuilder("v2m:");
    for (int y = 0; y < 200; y += 19) for (int x = 0; x < 300; x += 41) r.append(" ").append(p.viewToModel(new Point(x, y)));
    sb.append(r).append("\n");
    System.out.print(sb);
  }
  
  static void labelSize(String n, JComponent c) { Dimension d = c.getPreferredSize(); System.out.println(n + " " + d.width + "x" + d.height + " min=" + c.getMinimumSize()); }
  
  static void rtfTree(Element e, String ind, StringBuilder sb) {
    sb.append(ind).append(e.getName()).append("[").append(e.getStartOffset()).append(",").append(e.getEndOffset()).append("]");
    AttributeSet a = e.getAttributes(); List<String> at = new ArrayList<>();
    for (Enumeration<?> en = a.getAttributeNames(); en.hasMoreElements();) { Object k = en.nextElement(); if (k == StyleConstants.NameAttribute || k == StyleConstants.ResolveAttribute) continue; at.add(k + "=" + a.getAttribute(k)); }
    Collections.sort(at); sb.append(" ").append(at).append("\n");
    for (int i = 0; i < e.getElementCount(); i++) rtfTree(e.getElement(i), ind + "  ", sb);
  }
  static void rtfDump(String rtf) throws Exception {
    RTFEditorKit kit = new RTFEditorKit();
    Document d = kit.createDefaultDocument();
    kit.read(new StringReader(rtf), d, 0);
    StringBuilder sb = new StringBuilder("== " + rtf.replace("\n", "\\n") + "\n"); rtfTree(d.getDefaultRootElement(), "", sb);
    sb.append("text=[").append(d.getText(0, d.getLength()).replace("\n", "\\n")).append("]\n");
        System.out.print(sb);
  }
  
  static void write(String html) throws Exception {
    JEditorPane p = new JEditorPane("text/html", html);
    StringWriter sw = new StringWriter();
    p.getEditorKit().write(sw, p.getDocument(), 0, p.getDocument().getLength());
    System.out.println("== " + html.replace("\n", "\\n") + "\n" + sw.toString().replace("\n", "\\n\n"));
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    dump("<html><body><h2>Heading two</h2><p>A paragraph with <i>italic</i>, <b>bold</b> and <u>underline</u> text that is long enough to wrap in narrow panes.</p><blockquote>quoted text</blockquote></body></html>");
    dump("<dl><dt>Term<dd>Definition text<dt>Term2<dd>Def2</dl>");
    dump("<ol><li>first<ul><li>nested a<li>nested b</ul><li>second</ol>");
    dump("<center>centered <font size=5 color=red>big red</font></center><p align=right>right</p>");
    dump("<div style=\"margin-left:20px;color:blue\">styled<span style=\"font-size:20px\"> large</span></div><p>x &amp; y &lt; z &copy; &#65;&nbsp;end");
    dump("<h1>One</h1><h3>Three</h3><h6>Six</h6><address>addr</address><pre>a\tb\n  c</pre>");
    dump("<html><head><title>T</title></head><body bgcolor=\"red\"><div class=x>div <a href=\"u\">link</a><br>after</div></body></html>");
    write("<html><body><p>Hello <b>bold</b> world</p></body></html>");
    write("<h1>Title</h1><ul><li>a<li>b</ul>");
    write("<div class=x>div <a href=\"u\">link</a><br>after</div><hr><img src=\"x.png\" width=3>");
    write("<table border=1><tr><td>a<td>b</table><pre>l1\n l2</pre>");
    labelSize("label", new JLabel("<html><b>bold</b> and <i>italic</i></html>"));
    labelSize("label2", new JLabel("<html>line1<br>line two<br>3</html>"));
    labelSize("label3", new JLabel("<html><h1>Head</h1><p>para</p></html>"));
    labelSize("label4", new JLabel("<html><ul><li>one<li>two</ul></html>"));
    labelSize("button", new JButton("<html><u>Under</u> <font color=red>red</font></html>"));
    JToolTip tip = new JToolTip();
    tip.setTipText("<html>Tip <b>bold</b><br>second line</html>");
    labelSize("tip", tip);
    labelSize("checkbox", new JCheckBox("<html>a<br>b</html>"));
    labelSize("wrapped", new JLabel("<html><body style='width:50px'>This is a long text that should wrap</body></html>"));
    rtfDump("{\\rtf1\\ansi{\\fonttbl{\\f0 Arial;}{\\f1 Times New Roman;}}{\\colortbl;\\red255\\green0\\blue0;}\\f0\\fs24 Hello \\b bold\\b0  \\i ital\\i0 \\par {\\cf1 red}\\f1 times\\par\\pard\\qc centered\\par}");
    rtfDump("{\\rtf1 Plain text with \\'e9 and \\u8364? unicode\\tab tab\\line next}");
  }
}
