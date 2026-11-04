import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.rtf.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class SwingFormsRtfJustifyModern {
  static void walkForm(View v, String ind) {
    if (v instanceof ComponentView) {
      Component c = ((ComponentView) v).getComponent();
      StringBuilder sb = new StringBuilder(ind + v.getClass().getSimpleName() + " " + v.getStartOffset() + "-" + v.getEndOffset());
      if (c != null) { sb.append(" ").append(c.getClass().getName()).append(" pref=").append(c.getPreferredSize()).append(" min=").append(c.getMinimumSize()).append(" max=").append(c.getMaximumSize()).append(" alignY=").append(c.getAlignmentY()).append(" enabled=").append(c.isEnabled()).append(" ").append(info(c)); } else sb.append(" comp=null");
      System.out.println(sb);
    }
    for (int i = 0; i < v.getViewCount(); i++) walkForm(v.getView(i), ind + " ");
  }
  static String info(Component c) {
    StringBuilder sb = new StringBuilder();
    if (c instanceof AbstractButton) { AbstractButton b = (AbstractButton) c; sb.append("text='").append(b.getText()).append("' selected=").append(b.isSelected()).append(" model=").append(b.getModel().getClass().getSimpleName()).append(" icon=").append(b.getIcon() != null).append(" ").append(b.getMargin()).append(" actions=").append(b.getActionCommand()); }
    if (c instanceof JPasswordField) { JPasswordField p = (JPasswordField) c; sb.append(" cols=").append(p.getColumns()).append(" echo=").append((int) p.getEchoChar()).append(" text='").append(new String(p.getPassword())).append("'"); }
    else if (c instanceof JTextField) { JTextField t = (JTextField) c; sb.append(" cols=").append(t.getColumns()).append(" text='").append(t.getText()).append("' editable=").append(t.isEditable()); }
    if (c instanceof JComboBox) { JComboBox<?> cb = (JComboBox<?>) c; sb.append(" items=").append(cb.getItemCount()).append(" sel=").append(cb.getSelectedIndex()).append(" model=").append(cb.getModel().getClass().getSimpleName()); for (int i = 0; i < cb.getItemCount(); i++) sb.append(" [").append(cb.getItemAt(i)).append("]").append(cb.getItemAt(i).getClass().getSimpleName()); }
    if (c instanceof JScrollPane) { Component v = ((JScrollPane) c).getViewport().getView(); sb.append(" view=").append(v.getClass().getName()).append(" ").append(info(v)); }
    if (c instanceof JList) { JList<?> l = (JList<?>) c; sb.append(" items=").append(l.getModel().getSize()).append(" visible=").append(l.getVisibleRowCount()).append(" mode=").append(l.getSelectionMode()).append(" sel=").append(Arrays.toString(l.getSelectedIndices())).append(" model=").append(l.getModel().getClass().getSimpleName()); }
    if (c instanceof JTextArea) { JTextArea t = (JTextArea) c; sb.append(" rows=").append(t.getRows()).append(" cols=").append(t.getColumns()).append(" text='").append(t.getText().replace("\n", "\\n")).append("' wrap=").append(t.getLineWrap()); }
    return sb.toString();
  }

  static void forms() throws Exception {
    String[] hs = {
      "<form><input type=text value='hello' size=10><input type=text><input type=password value=secret size=8><input type=checkbox checked><input type=checkbox><input type=radio name=r value=1 checked><input type=radio name=r value=2></form>",
      "<form><input type=submit value=Send><input type=submit><input type=reset><input type=reset value=Clear><input type=button value=Btn><input type=hidden name=h value=v><input type=image src=none.png alt=Im><input type=file></form>",
      "<form><select name=s><option>One<option selected>Two<option value=3>Three</select><select multiple size=3><option>A<option>B<option selected>C<option>D</select><select size=2><option>x<option>y</select></form>",
      "<form><textarea name=t rows=3 cols=20>some\ntext</textarea><textarea>d</textarea><textarea rows=2 cols=5></textarea></form>",
      "<form><input type=text size=0><input type=text size=30><input type=text maxlength=3 value=abcdef><input type=text readonly value=ro><input disabled type=checkbox><input type=submit disabled value=No></form>",
    };
    for (String h : hs) {
      System.out.println("== " + h.substring(0, Math.min(h.length(), 70)));
      JEditorPane p = new JEditorPane("text/html", h); p.setEditable(false); p.setSize(600, 300); p.getPreferredSize(); p.setSize(600, 300);
      SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
      System.out.println("pref " + p.getPreferredSize());
      walkForm(p.getUI().getRootView(p), "");
    }
  }
  static void find(View v, java.util.List<ComponentView> out) { if (v instanceof FormView) out.add((ComponentView) v); for (int i = 0; i < v.getViewCount(); i++) find(v.getView(i), out); }
  static void runSubmit(String html) throws Exception {
    final JEditorPane p = new JEditorPane("text/html", html); p.setEditable(false);
    ((HTMLEditorKit) p.getEditorKit()).setAutoFormSubmission(false);
    final StringBuilder log = new StringBuilder();
    p.addHyperlinkListener(new HyperlinkListener() { public void hyperlinkUpdate(HyperlinkEvent e) { if (e instanceof FormSubmitEvent) { FormSubmitEvent f = (FormSubmitEvent) e; log.append("submit method=" + f.getMethod() + " data=" + f.getData() + " url=" + f.getURL() + " target=" + f.getTarget() + " desc=" + f.getDescription() + " type=" + f.getEventType() + " elem=" + (f.getSourceElement() == null ? null : f.getSourceElement().getName()) + "\n"); } else log.append("link " + e.getURL() + "\n"); } });
    p.setSize(600, 300); p.getPreferredSize(); p.setSize(600, 300);
    SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    java.util.List<ComponentView> views = new ArrayList<>(); find(p.getUI().getRootView(p), views);
    for (ComponentView v : views) {
      Component c = v.getComponent();
      if (c instanceof JTextField && !(c instanceof JPasswordField)) ((JTextField) c).setText("new val&x");
      if (c instanceof JPasswordField) ((JPasswordField) c).setText("pw");
    }
    for (ComponentView v : views) { Component c = v.getComponent(); if (c instanceof JButton && ((JButton) c).getText().equals("Go")) ((JButton) c).doClick(); }
    SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    SwingUtilities.invokeAndWait(new Runnable() { public void run() { } });
    System.out.println(log);
    for (ComponentView v : views) { Component c = v.getComponent(); if (c instanceof JButton && ((JButton) c).getText().equals("Reset")) ((JButton) c).doClick(); }
    StringBuilder sb = new StringBuilder("after reset: ");
    for (ComponentView v : views) { Component c = v.getComponent(); if (c instanceof JTextField) sb.append(((JTextField) c).getText()).append("|"); }
    System.out.println(sb);
  }

  static void submits() throws Exception {
    runSubmit("<form action='http://example.org/s' method=get><input name=a value=1><input type=password name=pw value=x><input type=checkbox name=c value=yes checked><input type=checkbox name=d><input type=radio name=r value=1><input type=radio name=r value=2 checked><select name=s><option value=o1>One<option selected value=o2>Two</select><select name=m multiple><option selected>A<option selected>B<option>C</select><textarea name=t>line1\nline2</textarea><input type=hidden name=h value='h h'><input type=submit name=go value=Go><input type=reset value=Reset></form>");
    runSubmit("<form action='/rel' method=POST><input name=q value=x><input type=submit value=Go></form>");
  }
  static void rtfDump(StyledDocument d, String title) throws Exception {
    ByteArrayOutputStream w = new ByteArrayOutputStream(); new RTFEditorKit().write(w, d, 0, d.getLength());
    System.out.println("== " + title); System.out.println(new String(w.toByteArray(), "ISO-8859-1").replace("\r", "\\r"));
  }

  static void rtf() throws Exception {
    DefaultStyledDocument d = new DefaultStyledDocument();
    d.insertString(0, "one\ntwo\n", null);
    rtfDump(d, "plain trailing empty");
    SimpleAttributeSet sa = new SimpleAttributeSet(); StyleConstants.setSpaceAbove(sa, 6); StyleConstants.setSpaceBelow(sa, 3); StyleConstants.setLeftIndent(sa, 12); StyleConstants.setRightIndent(sa, 4); StyleConstants.setFirstLineIndent(sa, 8); StyleConstants.setAlignment(sa, StyleConstants.ALIGN_CENTER); StyleConstants.setLineSpacing(sa, 0.5f);
    d.setParagraphAttributes(0, 4, sa, false);
    rtfDump(d, "para attrs");
    SimpleAttributeSet ca = new SimpleAttributeSet(); StyleConstants.setBold(ca, true); StyleConstants.setForeground(ca, Color.RED); StyleConstants.setFontSize(ca, 18); StyleConstants.setFontFamily(ca, "Serif");
    d.setCharacterAttributes(0, 3, ca, false);
    rtfDump(d, "char attrs");
    DefaultStyledDocument f = new DefaultStyledDocument(); f.insertString(0, "\n\n", null); StyleConstants.setSpaceAbove(sa, 2); f.setParagraphAttributes(1, 1, sa, false);
    rtfDump(f, "empties");
    DefaultStyledDocument g = new DefaultStyledDocument(); g.insertString(0, "abc\ndef", null); StyleConstants.setUnderline(ca, true); StyleConstants.setItalic(ca, true); g.setCharacterAttributes(2, 3, ca, true);
    rtfDump(g, "mixed");
    JTextPane p = new JTextPane(); p.setText("hello");
    ByteArrayOutputStream w = new ByteArrayOutputStream(); new RTFEditorKit().write(w, p.getDocument(), 0, 3); System.out.println("== partial\n" + new String(w.toByteArray(), "ISO-8859-1").replace("\r", "\\r"));
  }
  static void positions(JTextComponent c, String title) throws Exception {
    c.setSize(c.getWidth(), 400); c.getPreferredSize();
    StringBuilder sb = new StringBuilder(title + " pref=" + c.getPreferredSize() + "\n");
    int len = c.getDocument().getLength();
    for (int i = 0; i <= len; i++) { Rectangle r = c.modelToView(i); sb.append(i).append(':').append(r.x).append(',').append(r.y).append(',').append(r.height).append(' '); if (i % 12 == 11) sb.append('\n'); }
    System.out.println(sb);
  }

  static void layout() throws Exception {
    String txt = "The quick brown fox jumps over the lazy dog and keeps running far away";
    int[] aligns = {StyleConstants.ALIGN_LEFT, StyleConstants.ALIGN_JUSTIFIED, StyleConstants.ALIGN_CENTER, StyleConstants.ALIGN_RIGHT};
    for (int al : aligns) {
      JTextPane p = new JTextPane(); p.setSize(150, 400); p.setText(txt);
      SimpleAttributeSet as = new SimpleAttributeSet(); StyleConstants.setAlignment(as, al); ((StyledDocument) p.getDocument()).setParagraphAttributes(0, 5, as, false);
      positions(p, "align" + al);
    }
    for (float ls : new float[]{0f, 0.5f, 1.0f}) {
      JTextPane p = new JTextPane(); p.setSize(150, 400); p.setText(txt);
      SimpleAttributeSet as = new SimpleAttributeSet(); StyleConstants.setLineSpacing(as, ls); StyleConstants.setSpaceAbove(as, 4); StyleConstants.setSpaceBelow(as, 6); StyleConstants.setFirstLineIndent(as, 10); StyleConstants.setLeftIndent(as, 5); StyleConstants.setRightIndent(as, 7);
      ((StyledDocument) p.getDocument()).setParagraphAttributes(0, 5, as, false);
      positions(p, "ls" + ls);
    }
    JTextPane j = new JTextPane(); j.setSize(150, 400); j.setText(txt); SimpleAttributeSet as = new SimpleAttributeSet(); StyleConstants.setAlignment(as, StyleConstants.ALIGN_JUSTIFIED); StyleConstants.setBold(as, true);
    ((StyledDocument) j.getDocument()).setParagraphAttributes(0, 5, as, false); ((StyledDocument) j.getDocument()).setCharacterAttributes(10, 15, as, false); positions(j, "justbold");
    JEditorPane h = new JEditorPane("text/html", "<p style='text-align:justify'>" + txt + "</p><p align=justify>" + txt + "</p>"); h.setSize(150, 400); positions(h, "html");
  }
  static void shot(JComponent c, String title) {
    c.setSize(c.getWidth() > 0 ? c.getWidth() : 200, c.getHeight() > 0 ? c.getHeight() : 80); 
    BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, c.getWidth(), c.getHeight()); c.print(g); g.dispose();
    int minx = 9999, miny = 9999, maxx = -1, maxy = -1; long h = 0; java.util.Map<Integer,Integer> hist = new java.util.TreeMap<>();
    for (int y = 0; y < img.getHeight(); y++) for (int x = 0; x < img.getWidth(); x++) { int p = img.getRGB(x, y) & 0xffffff; h = h * 31 + p; if (p != 0xffffff) { minx = Math.min(minx, x); miny = Math.min(miny, y); maxx = Math.max(maxx, x); maxy = Math.max(maxy, y); hist.merge(p, 1, Integer::sum); } }
    System.out.println(title + " bbox=" + minx + "," + miny + " " + maxx + "," + maxy + " hash=" + h + " " + hist);
  }

  static void selection() {
    String sp = "          \n      \n          ";
    for (boolean vis : new boolean[]{false, true}) {
      JTextArea ta = new JTextArea(sp); ta.setSize(200, 80); ta.select(2, 14); ta.getCaret().setSelectionVisible(vis); shot(ta, "area vis=" + vis);
      JTextField tf = new JTextField("             "); tf.setSize(200, 24); tf.select(2, 8); tf.getCaret().setSelectionVisible(vis); shot(tf, "field vis=" + vis);
      JTextPane tp = new JTextPane(); tp.setText(sp); tp.setSize(200, 80); tp.select(3, 15); tp.getCaret().setSelectionVisible(vis); shot(tp, "pane vis=" + vis);
    }
    JTextArea tw = new JTextArea(sp); tw.setSize(200, 80); tw.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7)); tw.select(2, 14); tw.getCaret().setSelectionVisible(true);
    try { tw.getHighlighter().addHighlight(0, 3, new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN)); } catch (BadLocationException e) { throw new RuntimeException(e); }
    shot(tw, "extra highlight");
  }

  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty("java.awt.headless", "true");
    SwingUtilities.invokeAndWait(new Runnable() { public void run() { try { selection(); layout(); rtf(); } catch (Exception e) { throw new RuntimeException(e); } } });
    forms();
    submits();
    System.exit(0);
  }
}
