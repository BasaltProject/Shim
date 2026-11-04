import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class NimbusModern {
  public static void main(String[] args) throws Exception {
    System.setProperty("java.awt.headless", "true");
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          NimbusLookAndFeel laf = new NimbusLookAndFeel();
          System.out.println(laf.getName() + "|" + laf.getID() + "|" + laf.getDescription() + "|"
              + laf.isNativeLookAndFeel() + "|" + laf.isSupportedLookAndFeel());
          UIManager.setLookAndFeel(laf);
          System.out.println(UIManager.getColor("nimbusBase") + " " + UIManager.getColor("control") + " "
              + UIManager.getFont("defaultFont").getSize() + " " + UIManager.getInsets("Button.contentMargins"));
          System.out.println(laf.getDerivedColor("nimbusBase", 0.01f, -0.5f, 0.2f, -10, true).getRGB());
          JButton b = new JButton("Nimbus");
          System.out.println(b.getUI().getClass().getName() + " " + b.getFont().getSize());
          JRootPane root = new JRootPane();
          root.setDefaultButton(b);
          root.getContentPane().add(b);
          System.out.println(b.isDefaultButton() + " " + b.getMargin() + " " + b.getFont().getFamily());
          System.out.println(UIManager.getColor("Button.foreground").getRGB() + " " + new JViewport().getBackground().getRGB());
          for (JComponent c : new JComponent[] {new JTextField(), new JPasswordField(), new JScrollPane()}) {
            c.setSize(100, 40);
            c.doLayout();
            java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(100, 40,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = im.createGraphics();
            c.paint(g);
            g.dispose();
            System.out.println(java.util.Arrays.hashCode(im.getRGB(0, 0, 100, 40, null, 0, 100)));
          }
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
}
