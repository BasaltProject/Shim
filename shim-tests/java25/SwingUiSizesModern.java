import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SwingUiSizesModern {
  static void show(String name, JComponent c) {
    System.out.println(name + " " + c.getPreferredSize() + c.getMinimumSize() + c.getMaximumSize() + c.getInsets());
  }

  public static void main(String[] args) {
    System.setProperty("java.awt.headless", "true");
    show("button", new JButton("OK"));
    show("label", new JLabel("Hello"));
    show("textfield", new JTextField(10));
    show("textarea", new JTextArea(5, 20));
    show("checkbox", new JCheckBox("Check"));
    show("list", new JList<String>(new String[] {"a", "bb", "ccc"}));
    show("table", new JTable(3, 4));
    show("header", new JTable(2, 2).getTableHeader());
    show("tree", new JTree());
    show("combo", new JComboBox<String>(new String[] {"one", "two"}));
    JComboBox<String> editable = new JComboBox<String>(new String[] {"one", "two"});
    editable.setEditable(true);
    show("comboEditable", editable);
    show("progress", new JProgressBar());
    JProgressBar vertical = new JProgressBar(JProgressBar.VERTICAL);
    vertical.setStringPainted(true);
    show("progressVertical", vertical);
    JSlider slider = new JSlider();
    show("slider", slider);
    slider.setPaintTicks(true);
    slider.setMajorTickSpacing(25);
    slider.setPaintLabels(true);
    show("sliderLabels", slider);
    JSlider verticalSlider = new JSlider(JSlider.VERTICAL);
    verticalSlider.setPaintTicks(true);
    verticalSlider.setMajorTickSpacing(25);
    verticalSlider.setPaintLabels(true);
    show("sliderVertical", verticalSlider);
    show("spinner", new JSpinner());
    show("separator", new JSeparator());
    JToolTip tip = new JToolTip();
    tip.setTipText("Hello");
    show("tooltip", tip);
    for (String text : new String[] {"A", "Open", "File", "Quit now"}) {
      show("menuitem " + text, new JMenuItem(text));
      show("menu " + text, new JMenu(text));
      show("checkitem " + text, new JCheckBoxMenuItem(text));
    }
    JMenuItem accel = new JMenuItem("Open");
    accel.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
    show("accelerator", accel);
    JMenuBar bar = new JMenuBar();
    bar.add(new JMenu("File"));
    bar.add(new JMenu("Edit"));
    show("menubar", bar);
    JPopupMenu popup = new JPopupMenu();
    popup.add(new JMenuItem("One"));
    popup.add(new JMenuItem("Two"));
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("Chk"));
    show("popup", popup);
    JToolBar toolbar = new JToolBar();
    toolbar.add(new JButton("A"));
    toolbar.add(new JButton("BB"));
    show("toolbar", toolbar);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel("top"), new JLabel("bottom"));
    show("split", split);
    split.setSize(300, 200);
    split.doLayout();
    System.out.println(split.getLeftComponent().getBounds() + " " + split.getRightComponent().getBounds() + " "
        + split.getUI().getDividerLocation(split) + " " + split.getMinimumDividerLocation() + " "
        + split.getMaximumDividerLocation());
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    String[] families = {"Dialog", "Serif", "Monospaced"};
    for (int f = 0; f < families.length; f++) {
      for (int style = 0; style < 4; style++) {
        for (int size = 9; size <= 20; size += 3) {
          FontMetrics fm = g.getFontMetrics(new Font(families[f], style, size));
          StringBuilder sb = new StringBuilder(families[f] + " " + style + " " + size + " " + fm.getAscent() + " "
              + fm.getDescent() + " " + fm.getHeight() + " " + fm.getMaxAdvance() + ":");
          for (char ch = 32; ch < 127; ch++) {
            sb.append(' ').append(fm.charWidth(ch));
          }
          System.out.println(sb);
        }
      }
    }
  }
}
