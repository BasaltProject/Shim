import javax.swing.*;
import javax.swing.text.*;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;

public class SwingTextViewsModern {

  static void sizes(String n, JComponent c) {
    Dimension a = c.getPreferredSize(), b = c.getMinimumSize(), m = c.getMaximumSize();
    System.out.println(n + " pref=" + a.width + "x" + a.height + " min=" + b.width + "x" + b.height + " max=" + (m.width==Integer.MAX_VALUE?"M":m.width) + "x" + (m.height==Integer.MAX_VALUE?"M":m.height) + " ins=" + c.getInsets());
  }
  
  static void rects(String n, JTextComponent c) throws Exception {
    StringBuilder sb = new StringBuilder(n + ":");
    int len = c.getDocument().getLength();
    for (int i = 0; i <= len; i++) { Rectangle r = c.modelToView(i); sb.append(" ").append(i).append("=").append(r == null ? "null" : r.x + "," + r.y + "," + r.width + "," + r.height); }
    System.out.println(sb);
    sb = new StringBuilder(n + " v2m:");
    for (int y = 0; y < c.getHeight(); y += 7) for (int x = 0; x < c.getWidth(); x += 13) sb.append(" ").append(c.viewToModel(new Point(x, y)));
    System.out.println(sb);
  }
  static void sized(String n, JComponent c, int w, int h) { c.setSize(w, h); Dimension d = c.getPreferredSize(); System.out.println(n + " @" + w + "x" + h + " pref=" + d.width + "x" + d.height + " min=" + c.getMinimumSize().width + "x" + c.getMinimumSize().height); }
  static void tree(Element e, String ind, StringBuilder sb) {
    sb.append(ind).append(e.getName()).append("[").append(e.getStartOffset()).append(",").append(e.getEndOffset()).append("]\n");
    for (int i = 0; i < e.getElementCount(); i++) tree(e.getElement(i), ind + " ", sb);
  }
  
  static void sizesPart() throws Exception {
    sizes("TextField", new JTextField());
    sizes("TextField10", new JTextField(10));
    sizes("TextFieldHello", new JTextField("Hello world"));
    sizes("PasswordField", new JPasswordField("secret"));
    sizes("PasswordField8", new JPasswordField(8));
    sizes("Formatted", new JFormattedTextField(42));
    sizes("TextArea", new JTextArea());
    sizes("TextArea5x20", new JTextArea(5, 20));
    sizes("TextAreaTxt", new JTextArea("one\ntwo is longer\nthree"));
    JTextArea w = new JTextArea("one two three four five six seven", 3, 10); w.setLineWrap(true); sizes("TextAreaWrap", w);
    w = new JTextArea("one two three four five six seven"); w.setLineWrap(true); w.setWrapStyleWord(true); sizes("TextAreaWrap2", w);
    sizes("EditorPane", new JEditorPane());
    JEditorPane e = new JEditorPane(); e.setText("hello\nworld"); sizes("EditorPaneTxt", e);
    sizes("TextPane", new JTextPane());
    JTextPane tp = new JTextPane(); tp.setText("Hello pane\nsecond line"); sizes("TextPaneTxt", tp);
    JTextField f = new JTextField("abc"); f.setSize(100,19); 
    System.out.println(f.modelToView(0) + " " + f.modelToView(2) + " " + f.modelToView(3));
    System.out.println(f.viewToModel(new Point(5, 5)) + " " + f.viewToModel(new Point(100, 5)));
    System.out.println(f.getBaseline(100, 20) + " " + f.getBaselineResizeBehavior());
    JTextArea ta = new JTextArea("one\ntwo is longer\nthree"); ta.setSize(100,45);
    System.out.println(ta.modelToView(0) + " " + ta.modelToView(6) + " " + ta.modelToView(ta.getText().length()));
    System.out.println(ta.viewToModel(new Point(30, 20)));
    JEditorPane ep = new JEditorPane("text/html", "<html><body><p>Hello <b>bold</b></p><ul><li>a<li>b</ul></body></html>");
    sizes("Html", ep);
    ep.setSize(200, 100); sizes("Html200", ep);
    System.out.println(ep.getDocument().getClass()+" "+ep.getEditorKit().getClass());
  }

  static void viewsPart() throws Exception {
    String txt = "The quick brown fox jumps over the lazy dog\nand\ttabs here\n\nLast line is a pretty long line that wraps around";
    JTextArea ta = new JTextArea(txt);
    ta.setSize(300, 200); rects("plain", ta);
    ta.setLineWrap(true); ta.setSize(120, 300); rects("wrapchar", ta); sized("wrapchar", ta, 120, 300); sized("wrapchar", ta, 60, 300);
    ta.setWrapStyleWord(true); ta.setSize(120, 300); rects("wrapword", ta); sized("wrapword", ta, 120, 300);
    ta.setTabSize(4); ta.setSize(120, 300); rects("tab4", ta);
    JEditorPane ep = new JEditorPane(); ep.setText(txt); ep.setSize(150, 300); rects("editor", ep); sized("editor", ep, 150, 300);
    JTextPane tp = new JTextPane();
    StyledDocument d = tp.getStyledDocument();
    SimpleAttributeSet bold = new SimpleAttributeSet(); StyleConstants.setBold(bold, true);
    SimpleAttributeSet big = new SimpleAttributeSet(); StyleConstants.setFontSize(big, 20);
    SimpleAttributeSet ital = new SimpleAttributeSet(); StyleConstants.setItalic(ital, true); StyleConstants.setFontFamily(ital, "Serif");
    d.insertString(0, "Hello ", null); d.insertString(6, "bold words", bold); d.insertString(16, " BIG one", big); d.insertString(24, " and italic serif text that goes on and on\nSecond paragraph", ital);
    SimpleAttributeSet cen = new SimpleAttributeSet(); StyleConstants.setAlignment(cen, StyleConstants.ALIGN_CENTER);
    d.setParagraphAttributes(70, 1, cen, false);
    tp.setSize(200, 300); rects("pane", tp); sized("pane", tp, 200, 300); sized("pane", tp, 80, 300); sized("pane0", tp, 0, 0);
    StringBuilder sb = new StringBuilder(); tree(d.getDefaultRootElement(), "", sb); System.out.print(sb);
    JTextField f = new JTextField("hello world, a text field"); f.setSize(80, 20); rects("field", f);
    f.setHorizontalAlignment(JTextField.RIGHT); rects("fieldR", f);
    f.setHorizontalAlignment(JTextField.CENTER); f.setSize(300,20); rects("fieldC", f);
    JPasswordField pf = new JPasswordField("secret"); pf.setSize(100, 20); rects("pw", pf);
    JTextComponent[] cs = {ta, ep};
    String w = "  foo-bar baz_qux  hello, world!\nnext line here";
    JTextArea wa = new JTextArea(w);
    StringBuilder s2 = new StringBuilder("words:");
    for (int i = 0; i <= w.length(); i++) {
      s2.append(" ").append(i).append(":");
      try { s2.append(Utilities.getWordStart(wa, i)); } catch (Exception e) { s2.append("E"); }
      try { s2.append("/").append(Utilities.getWordEnd(wa, i)); } catch (Exception e) { s2.append("/E"); }
      try { s2.append("/").append(Utilities.getNextWord(wa, i)); } catch (Exception e) { s2.append("/E"); }
      try { s2.append("/").append(Utilities.getPreviousWord(wa, i)); } catch (Exception e) { s2.append("/E"); }
    }
    System.out.println(s2);
    wa.setSize(200, 100);
    s2 = new StringBuilder("rows:");
    for (int i = 0; i <= w.length(); i++) { s2.append(" ").append(Utilities.getRowStart(wa, i)).append("/").append(Utilities.getRowEnd(wa, i)).append("/").append(Utilities.getPositionAbove(wa, i, 30)).append("/").append(Utilities.getPositionBelow(wa, i, 30)); }
    System.out.println(s2);
    Segment sg = new Segment(); sg.array = "ab\tcd efgh ij".toCharArray(); sg.offset = 0; sg.count = sg.array.length;
    FontMetrics fm = wa.getFontMetrics(wa.getFont());
    System.out.println(Utilities.getTabbedTextWidth(sg, fm, 0, null, 0) + " " + Utilities.getTabbedTextOffset(sg, fm, 0, 30, null, 0) + " " + Utilities.getBreakLocation(sg, fm, 0, 45, null, 0));
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    sizesPart();
    viewsPart();
  }
}
