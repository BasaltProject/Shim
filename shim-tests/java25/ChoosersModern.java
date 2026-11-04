import java.awt.*;
import java.util.Locale;
import javax.swing.*;

public class ChoosersModern {
  static void values(JColorChooser cc) {
    JTabbedPane t = (JTabbedPane) cc.getComponent(0);
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < t.getTabCount(); i++) {
      sb.append(t.getTitleAt(i)).append(':');
      collect((Container) t.getComponentAt(i), sb);
      sb.append(" | ");
    }
    System.out.println(sb);
  }

  static void collect(Container c, StringBuilder sb) {
    for (Component x : c.getComponents()) {
      if (x instanceof JSpinner) {
        sb.append(((JSpinner) x).getValue()).append(',');
      } else if (x instanceof JFormattedTextField && !(x.getParent() instanceof JSpinner.DefaultEditor)) {
        sb.append("hex=").append(((JFormattedTextField) x).getText()).append(',');
      }
      if (x instanceof Container) {
        collect((Container) x, sb);
      }
    }
  }

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    Locale.setDefault(Locale.ENGLISH);
    JColorChooser cc = new JColorChooser(new Color(200, 100, 50));
    System.out.println(cc.getPreferredSize());
    values(cc);
    cc.setColor(new Color(10, 200, 90, 128));
    values(cc);
    cc.setColor(Color.black);
    values(cc);
    System.out.println(cc.getChooserPanels().length + " " + cc.getPreviewPanel().getClass().getSimpleName());
  }
}
