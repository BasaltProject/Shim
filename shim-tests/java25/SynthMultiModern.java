import java.awt.*;
import java.io.ByteArrayInputStream;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.*;

public class SynthMultiModern {
  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    final String xml = "<synth><style id=\"base\"><font name=\"Dialog\" size=\"14\" style=\"BOLD\"/>"
        + "<state><color value=\"#102030\" type=\"BACKGROUND\"/><color value=\"RED\" type=\"FOREGROUND\"/></state>"
        + "<state value=\"DISABLED\"><color value=\"#808080\" type=\"FOREGROUND\"/></state>"
        + "<insets top=\"1\" left=\"2\" bottom=\"3\" right=\"4\"/><opaque value=\"true\"/>"
        + "<property key=\"answer\" type=\"integer\" value=\"7\"/></style>"
        + "<bind style=\"base\" type=\"region\" key=\"Button\"/></synth>";
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          SynthLookAndFeel laf = new SynthLookAndFeel();
          laf.load(new ByteArrayInputStream(xml.getBytes("UTF-8")), SynthMultiModern.class);
          System.out.println(laf.getName() + "|" + laf.getID() + "|" + laf.isNativeLookAndFeel());
          UIManager.setLookAndFeel(laf);
          JButton b = new JButton("Hi");
          JLabel l = new JLabel("x");
          System.out.println(b.getUI().getClass().getName() + " " + l.getUI().getClass().getName());
          System.out.println(b.getFont() + " " + b.getBackground() + " " + b.getForeground() + " " + b.isOpaque());
          Region r = SynthLookAndFeel.getRegion(b);
          SynthStyle st = SynthLookAndFeel.getStyle(b, r);
          SynthContext ctx = new SynthContext(b, r, st, SynthConstants.ENABLED);
          SynthContext dis = new SynthContext(b, r, st, SynthConstants.DISABLED);
          System.out.println(r + " " + r.isSubregion() + " " + st.getInsets(ctx, null) + " "
              + st.getInt(ctx, "answer", 0) + " " + st.getBoolean(ctx, "none", true));
          System.out.println(st.getColor(dis, ColorType.FOREGROUND) + " " + st.getColor(ctx, ColorType.BACKGROUND));
          System.out.println(((SynthUI) b.getUI()).getContext(b).getRegion() + " " + Region.TREE_CELL.isSubregion() + " "
              + ColorType.FOCUS + " " + ColorType.MAX_COUNT);
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    MultiTest.run();
  }

  static class MultiTest {
    static void run() {
      javax.swing.plaf.multi.MultiLookAndFeel m = new javax.swing.plaf.multi.MultiLookAndFeel();
      UIDefaults d = m.getDefaults();
      System.out.println(m.getName() + "|" + m.getID() + "|" + d.size() + "|" + d.get("TreeUI"));
    }
  }
}
