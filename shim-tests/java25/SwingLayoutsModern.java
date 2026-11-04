import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

public class SwingLayoutsModern {
static class T15 {
  static class C extends JComponent { Dimension mn, pf, mx; float ax, ay;
    C(Random r) { int a = r.nextInt(50), b = a + r.nextInt(80), c = b + r.nextInt(200); int d = r.nextInt(50), e = d + r.nextInt(80), f = e + r.nextInt(200);
      mn = new Dimension(a, d); pf = new Dimension(b, e); mx = new Dimension(c, f); ax = r.nextInt(5) / 4f; ay = r.nextInt(5) / 4f; }
    public Dimension getMinimumSize() { return mn; } public Dimension getPreferredSize() { return pf; } public Dimension getMaximumSize() { return mx; }
    public float getAlignmentX() { return ax; } public float getAlignmentY() { return ay; } }
  public static void main(String[] a) {
    Random r = new Random(42);
    for (int t = 0; t < 60; t++) {
      JPanel p = new JPanel(); int n = 1 + r.nextInt(5);
      int kind = t % 4;
      LayoutManager lm = kind == 0 ? new BoxLayout(p, BoxLayout.X_AXIS) : kind == 1 ? new BoxLayout(p, BoxLayout.Y_AXIS) : kind == 2 ? new OverlayLayout(p) : new BoxLayout(p, BoxLayout.LINE_AXIS);
      p.setLayout(lm);
      for (int i = 0; i < n; i++) { C c = new C(r); if (r.nextInt(6) == 0) c.setVisible(false); p.add(c); }
      p.setBorder(BorderFactory.createEmptyBorder(r.nextInt(5), r.nextInt(5), r.nextInt(5), r.nextInt(5)));
      if (t % 7 == 3) p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      p.setSize(r.nextInt(500), r.nextInt(400));
      p.doLayout();
      StringBuilder sb = new StringBuilder(); LayoutManager2 l2 = (LayoutManager2) p.getLayout();
      sb.append(l2.preferredLayoutSize(p)).append(l2.minimumLayoutSize(p)).append(l2.maximumLayoutSize(p)).append(l2.getLayoutAlignmentX(p)).append(l2.getLayoutAlignmentY(p));
      for (Component c : p.getComponents()) sb.append(c.getBounds());
      System.out.println(sb);
    }
    SizeRequirements[] ch = { new SizeRequirements(5, 10, 40, 0.5f), new SizeRequirements(1, 20, 25, 0.2f) };
    System.out.println(Arrays.toString(SizeRequirements.adjustSizes(5, ch)));
    SizeSequence ss = new SizeSequence(5, 10); ss.setSize(2, 30); ss.insertEntries(1, 2, 7); ss.removeEntries(0, 1);
    System.out.println(Arrays.toString(ss.getSizes()) + ss.getPosition(3) + " " + ss.getIndex(25) + " " + ss.getIndex(1000) + " " + ss.getIndex(-3) + ss.getPosition(99));
  }
}

static class T18 {
  static class C extends JComponent { Dimension mn, pf, mx;
    C(Random r) { int a = r.nextInt(40), b = a + r.nextInt(60), c = b + r.nextInt(100); int d = r.nextInt(30), e = d + r.nextInt(50), f = e + r.nextInt(80); mn = new Dimension(a, d); pf = new Dimension(b, e); mx = new Dimension(c, f); }
    public Dimension getMinimumSize() { return mn; } public Dimension getPreferredSize() { return pf; } public Dimension getMaximumSize() { return mx; } }
  public static void main(String[] a) {
    Random r = new Random(5);
    String[] hs = {SpringLayout.WEST, SpringLayout.EAST}, vs = {SpringLayout.NORTH, SpringLayout.SOUTH};
    for (int t = 0; t < 80; t++) {
      JPanel p = new JPanel(); SpringLayout sl = new SpringLayout(); p.setLayout(sl);
      int n = 2 + r.nextInt(4); C[] cs = new C[n];
      for (int i = 0; i < n; i++) { cs[i] = new C(r); p.add(cs[i]); }
      for (int i = 0; i < n; i++) {
        if (i == 0) { sl.putConstraint("West", cs[0], r.nextInt(10), "West", p); sl.putConstraint("North", cs[0], r.nextInt(10), "North", p); }
        else {
          sl.putConstraint(r.nextBoolean() ? "West" : "North", cs[i], r.nextInt(8), r.nextBoolean() ? "East" : "South", cs[i-1]);
          if (r.nextBoolean()) sl.putConstraint("West", cs[i], r.nextInt(8), "West", p);
          if (r.nextBoolean()) sl.putConstraint("North", cs[i], r.nextInt(8), "North", p);
        }
      }
      sl.putConstraint("East", p, r.nextInt(10), "East", cs[n-1]); sl.putConstraint("South", p, r.nextInt(10), "South", cs[r.nextInt(n)]);
      StringBuilder sb = new StringBuilder();
      Dimension pf = p.getPreferredSize(); sb.append(pf).append(p.getMinimumSize()).append(p.getMaximumSize().width > 100000);
      for (int k = 0; k < 3; k++) {
        p.setSize(k == 0 ? pf.width : pf.width + r.nextInt(100) - 20 * k, k == 0 ? pf.height : pf.height + r.nextInt(100) - 20 * k);
        p.doLayout();
        for (Component c : p.getComponents()) sb.append(c.getBounds().x).append(',').append(c.getBounds().y).append(',').append(c.getBounds().width).append(',').append(c.getBounds().height).append(';');
        sb.append('|');
      }
      System.out.println(sb);
    }
  }
}

static class T17 {
  static class C extends JComponent { Dimension mn, pf;
    C(Random r) { int a = r.nextInt(40), b = a + r.nextInt(60); int d = r.nextInt(30), e = d + r.nextInt(50); mn = new Dimension(a, d); pf = new Dimension(b, e); }
    public Dimension getMinimumSize() { return mn; } public Dimension getPreferredSize() { return pf; } }
  public static void main(String[] a) {
    Random r = new Random(7);
    int[] anchors = {10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26};
    int[] gw = {1,1,1,2,3,0}; 
    for (int t = 0; t < 40; t++) {
      Container p = new JPanel(); GridBagLayout gbl = new GridBagLayout(); p.setLayout(gbl);
      if (r.nextInt(5) == 0) { gbl.columnWidths = new int[]{r.nextInt(30), r.nextInt(30)}; }
      if (r.nextInt(5) == 0) { gbl.rowWeights = new double[]{r.nextInt(3), r.nextInt(3), 1}; }
      int n = 1 + r.nextInt(7); boolean abs = r.nextBoolean();
      for (int i = 0; i < n; i++) {
        GridBagConstraints g = new GridBagConstraints();
        if (abs) { g.gridx = r.nextInt(4); g.gridy = r.nextInt(4); }
        
        g.gridwidth = gw[r.nextInt(gw.length)]; g.gridheight = gw[r.nextInt(gw.length)];
        if (abs && (g.gridwidth <= 0)) g.gridwidth = 1; if (g.gridwidth == 0 && g.gridheight == 0) g.gridheight = 1;
        g.weightx = r.nextInt(3) == 0 ? r.nextInt(4) / 2.0 : 0; g.weighty = r.nextInt(3) == 0 ? r.nextInt(4) / 2.0 : 0;
        g.fill = r.nextInt(4); g.anchor = anchors[r.nextInt(anchors.length)];
        g.insets = new Insets(r.nextInt(4), r.nextInt(4), r.nextInt(4), r.nextInt(4)); g.ipadx = r.nextInt(5); g.ipady = r.nextInt(5);
        C c = new C(r); if (r.nextInt(8) == 0) c.setVisible(false);
        p.add(c, g);
      }
      if (t % 5 == 4) p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      Dimension pf = p.getPreferredSize(); Dimension mn = p.getMinimumSize();
      StringBuilder sb = new StringBuilder(pf + " " + mn + " ");
      for (int k = 0; k < 3; k++) {
        p.setSize(k == 0 ? pf.width : k == 1 ? pf.width + r.nextInt(120) : Math.max(0, pf.width - r.nextInt(40)), k == 0 ? pf.height : k == 1 ? pf.height + r.nextInt(120) : Math.max(0, pf.height - r.nextInt(30)));
        p.doLayout();
        for (Component c : p.getComponents()) sb.append(c.getX()).append(',').append(c.getY()).append(',').append(c.getWidth()).append(',').append(c.getHeight()).append(';');
        if (false) System.out.println("W " + java.util.Arrays.toString(gbl.getLayoutWeights()[0]) + java.util.Arrays.toString(gbl.getLayoutDimensions()[0]) + " k=" + k); sb.append(gbl.getLayoutOrigin()).append(java.util.Arrays.toString(gbl.getLayoutDimensions()[0])).append(java.util.Arrays.toString(gbl.getLayoutWeights()[1])).append(gbl.location(5, 7)).append("|");
      }
      System.out.println(sb);
    }
  }
}

  public static void main(String[] a) {
    T15.main(a);
    T18.main(a);
    T17.main(a);
  }
}
